package com.senai.ecommerce.persistencia;

import com.senai.ecommerce.excecao.EstoqueInsuficienteException;
import com.senai.ecommerce.modelo.Cliente;
import com.senai.ecommerce.modelo.ItemPedido;
import com.senai.ecommerce.modelo.Pedido;
import com.senai.ecommerce.modelo.Produto;
import com.senai.ecommerce.modelo.pagamento.Pix;
import com.senai.ecommerce.repositorio.RepositorioException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class CrudIntegracaoTest {
    private ConexaoFactory conexoes;
    private Connection ancoraH2;
    private ConexaoFactory servidor;
    private String bancoExterno;
    private ProdutoDAO produtos;
    private ClienteDAO clientes;
    private PedidoDAO pedidos;
    private ItemPedidoDAO itens;
    private FinalizadorPedidoJdbc finalizador;
    private Cliente ana;
    private Produto notebook;
    private Produto mouse;

    @BeforeEach
    void prepararBanco() throws SQLException {
        String nome = "ecommerce_a13_" + UUID.randomUUID().toString().replace("-", "");
        String urlServidor = System.getProperty("aula13.jdbcServidor");
        if (urlServidor == null) {
            conexoes = new ConexaoFactory("jdbc:h2:mem:" + nome + ";MODE=MySQL", "sa", "");
            ancoraH2 = conexoes.abrir();
        } else {
            String usuario = System.getenv().getOrDefault("ECOMMERCE_TEST_DB_USER", "root");
            String senha = System.getenv().getOrDefault("ECOMMERCE_TEST_DB_PASSWORD", "");
            int parametros = urlServidor.indexOf('?');
            String base = parametros < 0 ? urlServidor : urlServidor.substring(0, parametros);
            String opcoes = parametros < 0 ? "" : urlServidor.substring(parametros);
            if (!base.endsWith("/")) {
                throw new IllegalArgumentException("aula13.jdbcServidor deve terminar em / e apontar para o servidor.");
            }
            servidor = new ConexaoFactory(urlServidor, usuario, senha);
            executarSql(servidor, "CREATE DATABASE " + nome);
            bancoExterno = nome;
            conexoes = new ConexaoFactory(base + nome + opcoes, usuario, senha);
        }
        EsquemaBanco.preparar(conexoes);
        produtos = new ProdutoDAO(conexoes);
        clientes = new ClienteDAO(conexoes);
        pedidos = new PedidoDAO(conexoes);
        itens = new ItemPedidoDAO(conexoes);
        finalizador = new FinalizadorPedidoJdbc(conexoes);
        ana = clientes.salvar(new Cliente("Ana", "529.982.247-25", "ana@exemplo.com"));
        notebook = produtos.salvar(new Produto("Notebook", new BigDecimal("3000.00"), 5));
        mouse = produtos.salvar(new Produto("Mouse", new BigDecimal("100.00"), 3));
    }

    @AfterEach
    void limparBanco() throws SQLException {
        if (ancoraH2 != null) {
            ancoraH2.close();
        }
        if (bancoExterno != null) {
            executarSql(servidor, "DROP DATABASE IF EXISTS " + bancoExterno);
        }
    }

    @Test
    @DisplayName("Produto persiste alteração de preço e permite corrigir apenas o estoque")
    void deveAtualizarProdutoERelerDoBanco() {
        Produto atualizado = produtos.salvar(new Produto(notebook.getId(), "Notebook Pro", new BigDecimal("3500.00"), 100));
        assertEquals(5, atualizado.getQuantidadeEmEstoque(), "Editar nome e preço preserva o estoque");
        produtos.atualizarEstoque(notebook.getId(), 7);

        Produto relido = produtos.buscarPorNome("Notebook Pro").orElseThrow();
        assertEquals(notebook.getId(), relido.getId());
        assertEquals(7, relido.getQuantidadeEmEstoque());
        assertEquals(0, new BigDecimal("3500.00").compareTo(relido.getPreco()));
    }

    @Test
    @DisplayName("Preço com centésimos fracionários é recusado antes de arredondar a venda")
    void deveRecusarPrecoComPrecisaoInvalida() {
        Produto invalido = new Produto(notebook.getId(), "Notebook", new BigDecimal("10.001"), 5);

        assertThrows(IllegalArgumentException.class, () -> produtos.atualizar(invalido));

        assertEquals(0, notebook.getPreco().compareTo(produtos.buscarPorId(notebook.getId()).orElseThrow().getPreco()));
    }

    @Test
    @DisplayName("Produto inativo some do catálogo, mantém nome reservado e pode ser reativado")
    void deveInativarSemApagarERestaurarProduto() {
        produtos.remover(notebook.getId());

        assertTrue(produtos.buscarPorId(notebook.getId()).isEmpty());
        assertTrue(produtos.buscarPorNome("Notebook").isEmpty());
        assertEquals(1, produtos.listarTodos().size());
        assertThrows(RepositorioException.class, () -> produtos.salvar(new Produto("Notebook", BigDecimal.TEN, 1)));
        assertThrows(RepositorioException.class, () -> produtos.inativar(notebook.getId()));

        produtos.reativar(notebook.getId());
        assertEquals(notebook.getId(), produtos.buscarPorNome("Notebook").orElseThrow().getId());
        assertEquals(5, produtos.buscarPorId(notebook.getId()).orElseThrow().getQuantidadeEmEstoque());
    }

    @Test
    @DisplayName("Cliente atualizado permanece consultável e cadastro sem pedidos pode ser excluído")
    void deveAtualizarExcluirERecadastrarCliente() {
        clientes.salvar(new Cliente(ana.getId(), "Ana Silva", ana.getDocumento(), "ana@novo.com"));
        clientes.atualizarEmail(ana.getId(), "contato@novo.com");

        Cliente relida = clientes.buscarPorEmail("contato@novo.com").orElseThrow();
        assertEquals("Ana Silva", relida.getNome());
        assertEquals(ana.getId(), clientes.buscarPorId(relida.getId()).orElseThrow().getId());

        clientes.remover(relida.getId());
        assertTrue(clientes.buscarPorId(relida.getId()).isEmpty());
        assertTrue(clientes.listarTodos().isEmpty());
        Cliente recadastrada = clientes.salvar(new Cliente("Ana", ana.getDocumento(), "contato@novo.com"));
        assertNotEquals(relida.getId(), recadastrada.getId());
    }

    @Test
    @DisplayName("Cliente com pedidos é protegido pela chave estrangeira e recebe mensagem clara")
    void deveTraduzirExclusaoDeClienteComPedidos() {
        Pedido rascunho = pedidos.salvar(new Pedido(101, ana));

        RepositorioException erro = assertThrows(RepositorioException.class, () -> clientes.remover(ana.getId()));
        assertTrue(erro.getMessage().contains("tem pedidos"));
        assertInstanceOf(SQLException.class, erro.getCause());
        assertTrue(clientes.buscarPorId(ana.getId()).isPresent());
        assertTrue(pedidos.buscarPorId(rascunho.getId()).isPresent());
    }

    @Test
    @DisplayName("Pedido aberto permite atualizar o cabeçalho, filtrar e excluir em cascata")
    void deveAtualizarRascunhoEExcluirSeusItensEmCascata() {
        Pedido rascunho = pedidos.salvar(new Pedido(101, ana));
        ItemPedido item = itens.inserir(rascunho.getId(), new ItemPedido(notebook, 2));

        pedidos.atualizar(new Pedido(rascunho.getId(), 102, ana));
        assertEquals(102, pedidos.buscarPorId(rascunho.getId()).orElseThrow().getNumero());
        assertEquals(rascunho.getId(), pedidos.listarPorCliente(ana.getId()).get(0).getId());
        assertEquals(1, pedidos.listarPorSituacao("ABERTO").size());

        pedidos.remover(rascunho.getId());
        assertTrue(pedidos.buscarPorId(rascunho.getId()).isEmpty());
        assertTrue(itens.buscarPorId(item.getId()).isEmpty());
        assertTrue(itens.listarPorPedido(rascunho.getId()).isEmpty());
        assertEquals(5, quantidade(notebook));
    }

    @Test
    @DisplayName("Item de um pedido aberto permite inserir, ler, ajustar quantidade e excluir")
    void deveCompletarCrudDoItem() {
        Pedido rascunho = pedidos.salvar(new Pedido(101, ana));
        ItemPedido item = itens.inserir(rascunho.getId(), new ItemPedido(mouse, 1));

        item.setQuantidade(2);
        itens.atualizar(item);
        ItemPedido relido = itens.buscarPorId(item.getId()).orElseThrow();
        assertEquals(2, relido.getQuantidade());
        assertEquals(0, new BigDecimal("200.00").compareTo(relido.getSubtotal()));
        assertEquals(1, itens.listarPorPedido(rascunho.getId()).size());

        itens.remover(item.getId());
        assertTrue(itens.buscarPorId(item.getId()).isEmpty());
        assertEquals(3, quantidade(mouse));
    }

    @Test
    @DisplayName("Atualizações e exclusões sem identificador ou sem registro devolvem erro")
    void deveRecusarIdNuloETratarRetornoZero() {
        assertAll(
            () -> assertThrows(IllegalArgumentException.class,
                () -> produtos.atualizar(new Produto("Avulso", BigDecimal.TEN, 1))),
            () -> assertThrows(IllegalArgumentException.class, () -> clientes.atualizarEmail(null, "x@exemplo.com")),
            () -> assertThrows(IllegalArgumentException.class, () -> pedidos.atualizar(new Pedido(1, ana))),
            () -> assertThrows(IllegalArgumentException.class, () -> itens.atualizar(new ItemPedido(mouse, 1))),
            () -> assertThrows(IllegalArgumentException.class, () -> produtos.remover(null)),
            () -> assertThrows(IllegalArgumentException.class, () -> clientes.remover(null)),
            () -> assertThrows(IllegalArgumentException.class, () -> pedidos.remover(null)),
            () -> assertThrows(IllegalArgumentException.class, () -> itens.remover(null)),
            () -> assertTrue(assertThrows(RepositorioException.class,
                () -> produtos.atualizarEstoque(999L, 10)).getMessage().contains("999")),
            () -> assertTrue(assertThrows(RepositorioException.class,
                () -> clientes.atualizarEmail(999L, "x@exemplo.com")).getMessage().contains("999")),
            () -> assertThrows(RepositorioException.class, () -> clientes.remover(999L)),
            () -> assertThrows(RepositorioException.class, () -> pedidos.remover(999L)),
            () -> assertThrows(RepositorioException.class, () -> itens.remover(999L))
        );
    }

    @Test
    @DisplayName("Pedido histórico preserva preço e vínculo mesmo após alteração e inativação do produto")
    void devePreservarPrecoHistoricoECadastroReferenciado() throws Exception {
        Pedido pago = pedidoNovo(101);
        pago.pagar(new Pix());
        Pedido gravado = finalizador.finalizar(pago);

        produtos.atualizar(new Produto(notebook.getId(), "Notebook Pro", BigDecimal.TEN, 5));
        produtos.inativar(notebook.getId());
        Pedido relido = pedidos.buscarPorId(gravado.getId()).orElseThrow();
        assertEquals("PAGO", relido.getStatus());
        assertEquals(0, new BigDecimal("3200.00").compareTo(relido.getTotal()));
        assertThrows(SQLException.class, () -> executarSql(conexoes, "DELETE FROM produto WHERE id = " + notebook.getId()));
        assertThrows(RepositorioException.class, () -> pedidos.remover(relido.getId()));
        assertThrows(RepositorioException.class, () -> itens.remover(relido.getItens().get(0).getId()));
    }

    @Test
    @DisplayName("Falha no segundo INSERT desfaz o pedido e o primeiro item")
    void deveDesfazerQuandoSegundoItemFalha() throws Exception {
        executarSql(conexoes, "ALTER TABLE item_pedido ADD CONSTRAINT ck_teste_item CHECK (quantidade <> 2)");
        Pedido novo = pedidoNovo(101);

        RepositorioException erro = assertThrows(RepositorioException.class, () -> finalizador.finalizar(novo));

        assertInstanceOf(SQLException.class, erro.getCause());
        assertBancoSemPedidoNovo();
    }

    @Test
    @DisplayName("Falha SQL na segunda baixa reverte também a primeira baixa de estoque")
    void deveDesfazerQuandoBaixaDeEstoqueFalha() throws Exception {
        executarSql(conexoes, "ALTER TABLE produto ADD CONSTRAINT ck_teste_estoque CHECK (quantidade <> 1)");
        Pedido novo = pedidoNovo(101);

        RepositorioException erro = assertThrows(RepositorioException.class, () -> finalizador.finalizar(novo));

        assertInstanceOf(SQLException.class, erro.getCause());
        assertBancoSemPedidoNovo();
    }

    @Test
    @DisplayName("Estoque insuficiente no segundo item desfaz toda a transação")
    void deveDesfazerQuandoFaltaEstoqueNoMeioDoLaco() throws Exception {
        Pedido novo = pedidoNovo(101);
        produtos.atualizarEstoque(mouse.getId(), 1);

        EstoqueInsuficienteException erro = assertThrows(EstoqueInsuficienteException.class,
            () -> finalizador.finalizar(novo));

        assertEquals(mouse.getId(), erro.getProduto().getId());
        assertTrue(pedidos.listarTodos().isEmpty());
        assertTrue(itens.listarTodos().isEmpty());
        assertEquals(5, quantidade(notebook));
        assertEquals(1, quantidade(mouse));
        assertNull(novo.getId());
    }

    @Test
    @DisplayName("Finalizar pedido novo grava itens e baixa o estoque uma vez")
    void deveConfirmarPedidoNovoUmaUnicaVez() throws Exception {
        Pedido novo = pedidoNovo(101);

        Pedido gravado = finalizador.finalizar(novo);

        assertEquals("FINALIZADO", gravado.getStatus());
        assertEquals(2, gravado.getItens().size());
        assertEquals(4, quantidade(notebook));
        assertEquals(1, quantidade(mouse));
        assertThrows(RepositorioException.class, () -> finalizador.finalizar(gravado));
        assertThrows(RepositorioException.class, () -> finalizador.finalizar(novo));
        assertEquals(1, pedidos.listarTodos().size());
        assertEquals(4, quantidade(notebook));
    }

    @Test
    @DisplayName("Falha mantém rascunho existente e sucesso posterior permite pagamento")
    void devePreservarRascunhoNoRollbackEPermitirPagarDepois() throws Exception {
        Pedido rascunho = pedidos.salvar(new Pedido(101, ana));
        itens.inserir(rascunho.getId(), new ItemPedido(notebook, 1));
        itens.inserir(rascunho.getId(), new ItemPedido(mouse, 4));

        assertThrows(EstoqueInsuficienteException.class, () -> finalizador.finalizar(rascunho));
        assertEquals("ABERTO", pedidos.buscarPorId(rascunho.getId()).orElseThrow().getStatus());
        assertEquals(2, itens.listarPorPedido(rascunho.getId()).size());
        assertEquals(5, quantidade(notebook));

        produtos.atualizarEstoque(mouse.getId(), 4);
        Pedido finalizado = finalizador.finalizar(rascunho);
        assertEquals(4, quantidade(notebook));
        assertEquals(0, quantidade(mouse));
        assertThrows(RepositorioException.class, () -> pedidos.remover(finalizado.getId()));
        finalizado.pagar(new Pix());
        Pedido pago = pedidos.salvar(finalizado);
        assertEquals("PAGO", pago.getStatus());
        assertNotNull(pago.getComprovante());
    }

    @Test
    @DisplayName("Produto inativado depois de incluído impede a finalização do rascunho")
    void deveRecusarProdutoInativoNaFinalizacao() {
        Pedido rascunho = pedidos.salvar(new Pedido(101, ana));
        itens.inserir(rascunho.getId(), new ItemPedido(notebook, 1));
        itens.inserir(rascunho.getId(), new ItemPedido(mouse, 1));
        produtos.inativar(mouse.getId());

        RepositorioException erro = assertThrows(RepositorioException.class, () -> finalizador.finalizar(rascunho));

        assertTrue(erro.getMessage().contains("inativo"));
        assertEquals("ABERTO", pedidos.buscarPorId(rascunho.getId()).orElseThrow().getStatus());
        assertEquals(5, quantidade(notebook));
    }

    @Test
    @DisplayName("Dois pedidos concorrentes não vendem a mesma unidade do estoque")
    void deveRecusarVendaConcorrenteAcimaDoEstoque() throws Exception {
        produtos.atualizarEstoque(mouse.getId(), 1);
        var banco = produtos.buscarPorId(mouse.getId()).orElseThrow();
        Pedido primeiro = new Pedido(101, ana);
        primeiro.adicionarItem(banco);
        Pedido segundo = new Pedido(102, ana);
        segundo.adicionarItem(produtos.buscarPorId(mouse.getId()).orElseThrow());
        CountDownLatch partida = new CountDownLatch(1);
        var executor = Executors.newFixedThreadPool(2);
        try {
            var a = executor.submit(tentarFinalizar(primeiro, partida));
            var b = executor.submit(tentarFinalizar(segundo, partida));
            partida.countDown();

            assertNotEquals(a.get(10, TimeUnit.SECONDS), b.get(10, TimeUnit.SECONDS));
            assertEquals(1, pedidos.listarTodos().size());
            assertEquals(1, itens.listarTodos().size());
            assertEquals(0, quantidade(mouse));
        } finally {
            executor.shutdownNow();
        }
    }

    private Callable<Boolean> tentarFinalizar(Pedido pedido, CountDownLatch partida) {
        return () -> {
            partida.await();
            try {
                finalizador.finalizar(pedido);
                return true;
            } catch (EstoqueInsuficienteException estoqueInsuficiente) {
                return false;
            }
        };
    }

    private Pedido pedidoNovo(int numero) throws EstoqueInsuficienteException {
        Pedido novo = new Pedido(numero, ana);
        novo.adicionarItem(new Produto(notebook.getId(), notebook.getNome(), notebook.getPreco(), 5), 1);
        novo.adicionarItem(new Produto(mouse.getId(), mouse.getNome(), mouse.getPreco(), 3), 2);
        return novo;
    }

    private void assertBancoSemPedidoNovo() {
        assertTrue(pedidos.listarTodos().isEmpty());
        assertTrue(itens.listarTodos().isEmpty());
        assertEquals(5, quantidade(notebook));
        assertEquals(3, quantidade(mouse));
    }

    private int quantidade(Produto produto) {
        return produtos.buscarPorId(produto.getId()).orElseThrow().getQuantidadeEmEstoque();
    }

    private static void executarSql(ConexaoFactory fabrica, String sql) throws SQLException {
        try (var conexao = fabrica.abrir(); var comando = conexao.createStatement()) {
            comando.execute(sql);
        }
    }
}
