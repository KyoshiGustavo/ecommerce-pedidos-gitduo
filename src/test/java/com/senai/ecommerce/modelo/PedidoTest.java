package com.senai.ecommerce.modelo;

import com.senai.ecommerce.excecao.EstoqueInsuficienteException;
import com.senai.ecommerce.excecao.PagamentoRecusadoException;
import com.senai.ecommerce.excecao.PedidoInvalidoException;
import com.senai.ecommerce.modelo.pagamento.CartaoCredito;
import com.senai.ecommerce.modelo.pagamento.Pix;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PedidoTest {

    private Cliente cliente;
    private Produto notebook;
    private Produto mouse;

    @BeforeEach
    void prepararCenario() {
        // Ordem dos parâmetros: nome, documento (CPF válido), email
        cliente = new Cliente("Ana Silva", "52998224725", "ana.silva@email.com");
        notebook = new Produto("Notebook", new BigDecimal("3000.00"), 10);
        mouse = new Produto("Mouse", new BigDecimal("100.00"), 20);
    }

    private Pedido criarPedidoComDoisItens() throws Exception {
        Pedido p = new Pedido(1, cliente);
        p.adicionarItem(notebook, 1);
        p.adicionarItem(mouse, 2);
        return p;
    }

    @Nested
    @DisplayName("Contexto: Pedido em Aberto")
    class PedidoEmAberto {

        @Test
        @DisplayName("Deve calcular o valor total corretamente ao adicionar itens")
        void deveSomarTotalComDoisItens() throws Exception {
            Pedido pedido = criarPedidoComDoisItens();

            BigDecimal total = pedido.getTotal();

            assertEquals(0, new BigDecimal("3200.00").compareTo(total));
        }

        @Test
        @DisplayName("Deve usar o preço unitário quando o pedido tem um item incluído sem quantidade")
        void deveCalcularTotalComUmItem() throws Exception {
            Pedido pedido = new Pedido(1, cliente);

            pedido.adicionarItem(mouse);

            assertEquals(0, mouse.getPreco().compareTo(pedido.getTotal()));
        }

        @Test
        @DisplayName("Deve ter total zero quando o pedido está vazio")
        void deveCalcularTotalZeroQuandoPedidoVazio() {
            Pedido pedido = new Pedido(1, cliente);

            BigDecimal total = pedido.getTotal();

            assertEquals(0, BigDecimal.ZERO.compareTo(total));
        }

        @Test
        @DisplayName("Deve recusar pagamento se o pedido estiver sem itens e manter o pedido ABERTO")
        void deveRecusarPagamentoDePedidoSemItens() {
            Pedido pedidoVazio = new Pedido(2, cliente);
            PedidoInvalidoException erro = assertThrows(PedidoInvalidoException.class,
                () -> pedidoVazio.pagar(new Pix()));

            assertAll(
                () -> assertEquals(pedidoVazio.getNumero(), erro.getNumeroPedido()),
                () -> assertEquals("ABERTO", pedidoVazio.getStatus()),
                () -> assertNull(pedidoVazio.getComprovante())
            );
        }

        @Test
        @DisplayName("Deve lançar EstoqueInsuficienteException sem alterar estoque nem itens")
        void deveManterEstadoQuandoEstoqueInsuficiente() {
            Pedido pedido = new Pedido(3, cliente);
            Produto produtoComTresUnidades = new Produto("Notebook", new BigDecimal("3000.00"), 3);

            EstoqueInsuficienteException erro = assertThrows(EstoqueInsuficienteException.class,
                () -> pedido.adicionarItem(produtoComTresUnidades, 50));

            assertAll(
                () -> assertEquals(50, erro.getQuantidadeSolicitada()),
                () -> assertEquals(3, erro.getQuantidadeDisponivel()),
                () -> assertEquals(3, produtoComTresUnidades.getQuantidadeEmEstoque()),
                () -> assertEquals(0, BigDecimal.ZERO.compareTo(pedido.getTotal()))
            );
        }

        @Test
        @DisplayName("Deve somar a quantidade quando o mesmo produto é adicionado duas vezes")
        void deveSomarQuantidadeDeProdutoRepetido() throws Exception {
            Pedido pedido = new Pedido(5, cliente);
            pedido.adicionarItem(mouse, 2);

            pedido.adicionarItem(mouse, 3);

            assertAll(
                () -> assertEquals(1, pedido.getItens().size(), "O pedido reúne o produto em um item"),
                () -> assertEquals(5, pedido.getItens().get(0).getQuantidade()),
                () -> assertEquals(15, mouse.getQuantidadeEmEstoque()),
                () -> assertEquals(0, new BigDecimal("500.00").compareTo(pedido.getTotal()))
            );
        }

        @Test
        @DisplayName("Produto repetido sem estoque suficiente não altera a quantidade já pedida")
        void deveManterItemQuandoRepeticaoExcedeEstoque() throws Exception {
            Pedido pedido = new Pedido(6, cliente);
            pedido.adicionarItem(mouse, 15);

            assertThrows(EstoqueInsuficienteException.class, () -> pedido.adicionarItem(mouse, 10));

            assertAll(
                () -> assertEquals(0, new BigDecimal("1500.00").compareTo(pedido.getTotal())),
                () -> assertEquals(5, mouse.getQuantidadeEmEstoque())
            );
        }

        @Test
        @DisplayName("Deve recusar produto nulo e preservar o total")
        void deveRecusarProdutoNulo() {
            Pedido pedido = new Pedido(4, cliente);

            assertThrows(IllegalArgumentException.class, () -> pedido.adicionarItem(null, 1));

            assertEquals(0, BigDecimal.ZERO.compareTo(pedido.getTotal()));
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -3})
        @DisplayName("Deve recusar quantidade zero ou negativa sem alterar pedido e estoque")
        void deveRecusarQuantidadeInvalida(int quantidade) {
            Pedido pedido = new Pedido(4, cliente);

            assertThrows(IllegalArgumentException.class, () -> pedido.adicionarItem(mouse, quantidade));

            assertAll(
                () -> assertEquals(0, BigDecimal.ZERO.compareTo(pedido.getTotal())),
                () -> assertEquals(20, mouse.getQuantidadeEmEstoque())
            );
        }

        @ParameterizedTest
        @NullSource
        @ValueSource(ints = {0, -1})
        @DisplayName("Deve recusar construção com número de pedido inválido")
        void deveRecusarNumeroInvalido(Integer numero) {
            assertThrows(IllegalArgumentException.class, () -> new Pedido(numero, cliente));
        }

        @Test
        @DisplayName("Deve recusar construção com cliente nulo")
        void deveRecusarClienteNulo() {
            assertThrows(IllegalArgumentException.class, () -> new Pedido(1, null));
        }
    }

    @Nested
    @DisplayName("Pedido em pagamento")
    class Pagamento {

        @Test
        @DisplayName("Deve alterar status para PAGO quando o pagamento for aprovado")
        void devePassarParaPagoQuandoAprovado() throws Exception {
            Pedido pedido = criarPedidoComDoisItens();
            pedido.pagar(new Pix());

            assertAll(
                () -> assertEquals("PAGO", pedido.getStatus()),
                () -> assertNotNull(pedido.getComprovante())
            );
        }

        @Test
        @DisplayName("Deve recusar processador nulo sem alterar a situação do pedido")
        void deveRecusarProcessadorNulo() throws Exception {
            Pedido pedido = criarPedidoComDoisItens();

            assertThrows(IllegalArgumentException.class, () -> pedido.pagar(null));

            assertAll(
                () -> assertEquals("ABERTO", pedido.getStatus()),
                () -> assertNull(pedido.getComprovante())
            );
        }

        @Test
        @DisplayName("Deve manter o pedido ABERTO quando o pagamento for recusado")
        void deveManterPedidoAbertoQuandoPagamentoRecusado() throws Exception {
            Pedido pedido = new Pedido(1, cliente);
            pedido.adicionarItem(mouse, 2);
            CartaoCredito cartao = new CartaoCredito(new BigDecimal("50.00"));

            PagamentoRecusadoException erro = assertThrows(PagamentoRecusadoException.class,
                () -> pedido.pagar(cartao));

            assertAll(
                () -> assertEquals("ABERTO", pedido.getStatus()),
                () -> assertNull(pedido.getComprovante()),
                () -> assertEquals("Cartão de Crédito", erro.getFormaPagamento()),
                () -> assertTrue(erro.getMotivo().contains("limite"))
            );
        }

        @Test
        @DisplayName("Deve recusar valor informado divergente sem processar o pagamento")
        void deveRecusarValorDivergente() throws Exception {
            Pedido pedido = new Pedido(1, cliente);
            pedido.adicionarItem(mouse, 2);
            Pix pix = new Pix();

            PagamentoRecusadoException erro = assertThrows(PagamentoRecusadoException.class,
                () -> pedido.pagar(pix, new BigDecimal("150.00")));

            assertAll(
                () -> assertEquals("ABERTO", pedido.getStatus()),
                () -> assertNull(pedido.getComprovante()),
                () -> assertNull(pix.getComprovante()),
                () -> assertTrue(erro.getMotivo().contains("150.00"))
            );
        }

        @Test
        @DisplayName("Deve aceitar pagamento quando o valor informado confere com o total")
        void deveAceitarValorCorreto() throws Exception {
            Pedido pedido = criarPedidoComDoisItens();

            boolean aprovado = pedido.pagar(new Pix(), new BigDecimal("3200.000"));

            assertAll(
                () -> assertTrue(aprovado),
                () -> assertEquals("PAGO", pedido.getStatus())
            );
        }
    }

    @Nested
    @DisplayName("Pedido já pago")
    class PedidoPago {
        private Pedido pedido;

        @BeforeEach
        void prepararPedidoPago() throws Exception {
            pedido = criarPedidoComDoisItens();
            pedido.pagar(new Pix());
        }

        @Test
        @DisplayName("Deve impedir a segunda cobrança de um pedido já pago")
        void deveImpedirPagamentoDuplicado() throws Exception {
            String comprovante = pedido.getComprovante();
            Pix segundaForma = new Pix();

            assertThrows(PedidoInvalidoException.class, () -> pedido.pagar(segundaForma));

            assertAll(
                () -> assertEquals("PAGO", pedido.getStatus()),
                () -> assertEquals(comprovante, pedido.getComprovante()),
                () -> assertNull(segundaForma.getComprovante())
            );
        }

        @Test
        @DisplayName("Deve recusar adicionar item a pedido já pago sem baixar estoque")
        void deveRecusarItemEmPedidoPago() throws Exception {
            int estoqueAntes = mouse.getQuantidadeEmEstoque();
            BigDecimal totalAntes = pedido.getTotal();

            PedidoInvalidoException erro = assertThrows(PedidoInvalidoException.class,
                () -> pedido.adicionarItem(mouse, 1));
            assertAll(
                () -> assertEquals(pedido.getNumero(), erro.getNumeroPedido()),
                () -> assertEquals(estoqueAntes, mouse.getQuantidadeEmEstoque()),
                () -> assertEquals(0, totalAntes.compareTo(pedido.getTotal()))
            );
        }
    }
}
