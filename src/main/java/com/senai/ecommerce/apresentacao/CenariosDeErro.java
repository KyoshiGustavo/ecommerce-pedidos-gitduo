package com.senai.ecommerce.apresentacao;

import com.senai.ecommerce.excecao.CadastroDuplicadoException;
import com.senai.ecommerce.excecao.ClienteNaoEncontradoException;
import com.senai.ecommerce.excecao.ECommerceException;
import com.senai.ecommerce.excecao.EstoqueInsuficienteException;
import com.senai.ecommerce.excecao.PagamentoRecusadoException;
import com.senai.ecommerce.excecao.PedidoInvalidoException;
import com.senai.ecommerce.modelo.Cliente;
import com.senai.ecommerce.modelo.Pedido;
import com.senai.ecommerce.modelo.Produto;
import com.senai.ecommerce.modelo.pagamento.CartaoCredito;
import com.senai.ecommerce.modelo.pagamento.Pix;
import com.senai.ecommerce.servico.ClienteServico;
import com.senai.ecommerce.servico.ProdutoServico;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Bateria de cenários de erro do Mapa de Exceções (Parte 3), executada contra o sistema real.
 *
 * Cada cenário declara ANTES a exceção esperada e a pós-condição esperada; só então executa e
 * compara com o obtido. Prever e depois conferir é o que separa teste de tentativa.
 */
public class CenariosDeErro {

    @FunctionalInterface
    interface Acao {
        void executar() throws ECommerceException;
    }

    @FunctionalInterface
    interface PosCondicao {
        boolean confere();
    }

    public record Resultado(String id, String fluxo, String descricao, String esperado,
                            String obtido, String posCondicao, boolean ok) {
    }

    private record Cenario(String id, String fluxo, String descricao, Class<? extends Throwable> esperada,
                           Acao acao, String descricaoPos, PosCondicao pos) {
    }

    private static final String CPF_VALIDO = "529.982.247-25";
    private static final String EMAIL_VALIDO = "ana.silva@email.com";

    private final List<Cenario> cenarios = new ArrayList<>();

    public CenariosDeErro() {
        montarCadastroDeProduto();
        montarCadastroDeCliente();
        montarPedido();
        montarPagamento();
        montarCaminhoFeliz();
    }

    // ===================================================== Montagem dos cenários

    private void montarCadastroDeProduto() {
        String f = "Cadastro de produto";
        add("P1", f, "Nome vazio", IllegalArgumentException.class,
                () -> new Produto("  ", new BigDecimal("10.00"), 1), null, null);
        add("P2", f, "Preço negativo", IllegalArgumentException.class,
                () -> new Produto("Teclado", new BigDecimal("-5.00"), 1), null, null);
        add("P3", f, "Preço zero", IllegalArgumentException.class,
                () -> new Produto("Teclado", BigDecimal.ZERO, 1), null, null);
        add("P4", f, "Estoque negativo", IllegalArgumentException.class,
                () -> new Produto("Teclado", new BigDecimal("10.00"), -1), null, null);

        ProdutoServico produtos = new ProdutoServico();
        add("P5", f, "Produto duplicado (mesmo nome)", CadastroDuplicadoException.class,
                () -> {
                    produtos.cadastrar("Notebook", new BigDecimal("3500.00"), 3);
                    produtos.cadastrar("notebook ", new BigDecimal("3000.00"), 1);
                },
                "segundo cadastro não entra: segue 1 produto", () -> produtos.listar().size() == 1);
    }

    private void montarCadastroDeCliente() {
        String f = "Cadastro de cliente";
        add("C1", f, "Nome vazio", IllegalArgumentException.class,
                () -> new Cliente("", CPF_VALIDO, EMAIL_VALIDO), null, null);
        add("C2", f, "E-mail sem @", IllegalArgumentException.class,
                () -> new Cliente("Ana Silva", CPF_VALIDO, "ana.silva.email.com"), null, null);
        add("C3", f, "Documento inválido (dígitos verificadores)", IllegalArgumentException.class,
                () -> new Cliente("Ana Silva", "123.456.789-00", EMAIL_VALIDO), null, null);

        ClienteServico clientes = new ClienteServico();
        add("C4", f, "Cliente duplicado (mesmo CPF, com e sem máscara)", CadastroDuplicadoException.class,
                () -> {
                    clientes.cadastrar("Ana Silva", CPF_VALIDO, EMAIL_VALIDO);
                    clientes.cadastrar("Ana S.", "52998224725", "outra@email.com");
                },
                "segundo cadastro não entra: segue 1 cliente", () -> clientes.listar().size() == 1);
        add("C5", f, "Busca de cliente por documento inexistente", ClienteNaoEncontradoException.class,
                () -> new ClienteServico().buscarPorDocumento("111.444.777-35"), null, null);
    }

    private void montarPedido() {
        String f = "Pedido";
        Produto mouse = new Produto("Mouse", new BigDecimal("100.00"), 5);
        Pedido pedidoQtd = new Pedido(1, clientePadrao());
        add("D1", f, "Quantidade zero", IllegalArgumentException.class,
                () -> pedidoQtd.adicionarItem(mouse, 0),
                "pedido sem itens e estoque intacto", () -> pedidoQtd.getItens().isEmpty() && mouse.getQuantidadeEmEstoque() == 5);
        add("D2", f, "Quantidade negativa", IllegalArgumentException.class,
                () -> pedidoQtd.adicionarItem(mouse, -3),
                "pedido sem itens e estoque intacto", () -> pedidoQtd.getItens().isEmpty() && mouse.getQuantidadeEmEstoque() == 5);

        Produto notebook = new Produto("Notebook", new BigDecimal("3500.00"), 3);
        Pedido pedidoEstoque = new Pedido(2, clientePadrao());
        add("D3", f, "Adicionar 50 de um produto com 3 em estoque", EstoqueInsuficienteException.class,
                () -> pedidoEstoque.adicionarItem(notebook, 50),
                "estoque segue 3 e pedido sem itens",
                () -> notebook.getQuantidadeEmEstoque() == 3 && pedidoEstoque.getItens().isEmpty());

        Pedido pedidoNulo = new Pedido(3, clientePadrao());
        add("D4", f, "Produto null", IllegalArgumentException.class,
                () -> pedidoNulo.adicionarItem(null, 1),
                "pedido sem itens", () -> pedidoNulo.getItens().isEmpty());

        Produto teclado = new Produto("Teclado", new BigDecimal("250.00"), 10);
        Pedido pedidoPago = new Pedido(4, clientePadrao());
        add("D5", f, "Adicionar item a pedido já pago", PedidoInvalidoException.class,
                () -> {
                    pedidoPago.adicionarItem(teclado, 1);
                    pedidoPago.pagar(new Pix());
                    pedidoPago.adicionarItem(teclado, 1);
                },
                "estoque do teclado segue 9 (item recusado não baixou estoque)",
                () -> teclado.getQuantidadeEmEstoque() == 9 && pedidoPago.getItens().size() == 1);

        add("D6", f, "Número de pedido inválido (zero)", IllegalArgumentException.class,
                () -> new Pedido(0, clientePadrao()), null, null);
    }

    private void montarPagamento() {
        String f = "Pagamento";
        Produto mouse = new Produto("Mouse", new BigDecimal("100.00"), 20);

        Pedido pedidoProcNulo = new Pedido(10, clientePadrao());
        add("G1", f, "Processador de pagamento null", IllegalArgumentException.class,
                () -> {
                    pedidoProcNulo.adicionarItem(mouse, 1);
                    pedidoProcNulo.pagar(null);
                },
                "pedido permanece ABERTO", () -> "ABERTO".equals(pedidoProcNulo.getStatus()));

        Pedido pedidoVazio = new Pedido(11, clientePadrao());
        add("G2", f, "Pagar pedido sem itens", PedidoInvalidoException.class,
                () -> pedidoVazio.pagar(new Pix()),
                "pedido permanece ABERTO", () -> "ABERTO".equals(pedidoVazio.getStatus()));

        Pedido pedidoRecusado = new Pedido(12, clientePadrao());
        add("G3", f, "Pagamento recusado pelo cartão (limite menor que o total)", PagamentoRecusadoException.class,
                () -> {
                    pedidoRecusado.adicionarItem(mouse, 2); // total R$ 200,00
                    pedidoRecusado.pagar(new CartaoCredito(new BigDecimal("50.00")));
                },
                "pedido permanece ABERTO e sem comprovante",
                () -> "ABERTO".equals(pedidoRecusado.getStatus()) && pedidoRecusado.getComprovante() == null);

        Pedido pedidoDuasVezes = new Pedido(13, clientePadrao());
        String[] primeiroComprovante = new String[1];
        add("G4", f, "Pagar um pedido já pago", PedidoInvalidoException.class,
                () -> {
                    pedidoDuasVezes.adicionarItem(mouse, 1);
                    pedidoDuasVezes.pagar(new Pix());
                    primeiroComprovante[0] = pedidoDuasVezes.getComprovante();
                    pedidoDuasVezes.pagar(new Pix());
                },
                "segue PAGO e o comprovante da primeira cobrança não mudou (não cobrou duas vezes)",
                () -> "PAGO".equals(pedidoDuasVezes.getStatus())
                        && primeiroComprovante[0] != null
                        && primeiroComprovante[0].equals(pedidoDuasVezes.getComprovante()));

        Pedido pedidoDivergente = new Pedido(14, clientePadrao());
        add("G5", f, "Valor informado diverge do total", PagamentoRecusadoException.class,
                () -> {
                    pedidoDivergente.adicionarItem(mouse, 2); // total R$ 200,00
                    pedidoDivergente.pagar(new Pix(), new BigDecimal("150.00"));
                },
                "pedido permanece ABERTO", () -> "ABERTO".equals(pedidoDivergente.getStatus()));
    }

    private void montarCaminhoFeliz() {
        String f = "Caminho feliz";
        ProdutoServico produtos = new ProdutoServico();
        add("H1", f, "Cadastrar produto válido", null,
                () -> produtos.cadastrar("Notebook", new BigDecimal("3000.00"), 10),
                "produto cadastrado", () -> produtos.listar().size() == 1);

        Produto notebook = new Produto("Notebook", new BigDecimal("3000.00"), 10);
        Produto mouse = new Produto("Mouse", new BigDecimal("100.00"), 20);
        Pedido pedidoDoisItens = new Pedido(20, clientePadrao());
        add("H2", f, "Criar pedido com 2 itens", null,
                () -> {
                    pedidoDoisItens.adicionarItem(notebook, 1);
                    pedidoDoisItens.adicionarItem(mouse, 2);
                },
                "total R$ 3200.00", () -> pedidoDoisItens.getTotal().compareTo(new BigDecimal("3200.00")) == 0);

        Pedido pedidoPix = new Pedido(21, clientePadrao());
        add("H3", f, "Sequência completa: pedido criado, pago com Pix, situação PAGO", null,
                () -> {
                    pedidoPix.adicionarItem(notebook, 1);
                    pedidoPix.pagar(new Pix());
                },
                "situação PAGO com comprovante",
                () -> "PAGO".equals(pedidoPix.getStatus()) && pedidoPix.getComprovante() != null);
    }

    private static Cliente clientePadrao() {
        return new Cliente("Ana Silva", CPF_VALIDO, EMAIL_VALIDO);
    }

    private void add(String id, String fluxo, String descricao, Class<? extends Throwable> esperada,
                     Acao acao, String descricaoPos, PosCondicao pos) {
        cenarios.add(new Cenario(id, fluxo, descricao, esperada, acao, descricaoPos, pos));
    }

    // ===================================================== Execução

    public List<Resultado> executar() {
        List<Resultado> resultados = new ArrayList<>();
        for (Cenario cenario : cenarios) {
            resultados.add(executar(cenario));
        }
        return resultados;
    }

    private Resultado executar(Cenario cenario) {
        Throwable lancada = null;
        try {
            cenario.acao().executar();
        } catch (ECommerceException | RuntimeException e) {
            lancada = e;
        }

        String esperado = cenario.esperada() == null ? "nenhuma exceção" : cenario.esperada().getSimpleName();
        String obtido = lancada == null
                ? "nenhuma exceção"
                : lancada.getClass().getSimpleName() + ": " + lancada.getMessage();

        boolean excecaoOk = cenario.esperada() == null
                ? lancada == null
                : lancada != null && lancada.getClass() == cenario.esperada();
        boolean posOk = cenario.pos() == null || cenario.pos().confere();
        String pos = cenario.descricaoPos() == null ? "—" : cenario.descricaoPos();

        return new Resultado(cenario.id(), cenario.fluxo(), cenario.descricao(), esperado, obtido, pos,
                excecaoOk && posOk);
    }

    /** Executa e imprime em formato legível. Devolve true se todos os cenários bateram com o esperado. */
    public boolean executarEImprimir(PrintStream saida) {
        List<Resultado> resultados = executar();
        saida.println();
        saida.println("=== BATERIA DE CENÁRIOS DE ERRO (previsto x obtido) ===");
        String fluxoAtual = "";
        int aprovados = 0;
        for (Resultado r : resultados) {
            if (!r.fluxo().equals(fluxoAtual)) {
                fluxoAtual = r.fluxo();
                saida.println();
                saida.println("--- " + fluxoAtual + " ---");
            }
            saida.println((r.ok() ? "[OK]  " : "[FALHOU] ") + r.id() + " · " + r.descricao());
            saida.println("      esperado: " + r.esperado());
            saida.println("      obtido:   " + r.obtido());
            saida.println("      depois:   " + r.posCondicao());
            if (r.ok()) {
                aprovados++;
            }
        }
        saida.println();
        saida.println("Resultado: " + aprovados + " de " + resultados.size() + " cenários conforme o esperado.");
        return aprovados == resultados.size();
    }

    /** Linhas de tabela Markdown, usadas para preencher o Mapa de Exceções com o resultado real. */
    public void imprimirMarkdown(PrintStream saida) {
        for (Resultado r : executar()) {
            saida.println("| " + r.id() + " | " + r.fluxo() + " | " + r.descricao() + " | `" + r.esperado()
                    + "` | " + r.obtido().replace("|", "\\|") + " | " + r.posCondicao() + " | "
                    + (r.ok() ? "✅" : "❌") + " |");
        }
    }
}
