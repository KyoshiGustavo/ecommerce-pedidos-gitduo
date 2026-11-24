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
            assertEquals(0, new BigDecimal("3200.00").compareTo(pedido.getTotal()));
        }

        @Test
        @DisplayName("Deve recusar pagamento se o pedido estiver sem itens e manter o pedido ABERTO")
        void deveRecusarPagamentoDePedidoSemItens() {
            Pedido pedidoVazio = new Pedido(2, cliente);
            assertThrows(PedidoInvalidoException.class, () -> pedidoVazio.pagar(new Pix()));
            assertEquals("ABERTO", pedidoVazio.getStatus());
        }

        @Test
        @DisplayName("Deve lançar EstoqueInsuficienteException sem alterar estoque nem itens")
        void deveManterEstadoQuandoEstoqueInsuficiente() {
            Pedido pedido = new Pedido(3, cliente);
            assertThrows(EstoqueInsuficienteException.class, () -> pedido.adicionarItem(notebook, 50));
            assertEquals(10, notebook.getQuantidadeEmEstoque());
            assertTrue(pedido.getItens().isEmpty());
        }

        @Test
        @DisplayName("Deve somar a quantidade quando o mesmo produto é adicionado duas vezes")
        void deveSomarQuantidadeDeProdutoRepetido() throws Exception {
            Pedido pedido = new Pedido(5, cliente);
            pedido.adicionarItem(mouse, 2);
            pedido.adicionarItem(mouse, 3);

            assertEquals(1, pedido.getItens().size());
            assertEquals(5, pedido.getItens().get(0).getQuantidade());
            assertEquals(15, mouse.getQuantidadeEmEstoque());
            assertEquals(0, new BigDecimal("500.00").compareTo(pedido.getTotal()));
        }

        @Test
        @DisplayName("Produto repetido sem estoque suficiente não altera a quantidade já pedida")
        void deveManterItemQuandoRepeticaoExcedeEstoque() throws Exception {
            Pedido pedido = new Pedido(6, cliente);
            pedido.adicionarItem(mouse, 15);
            assertThrows(EstoqueInsuficienteException.class, () -> pedido.adicionarItem(mouse, 10));

            assertEquals(15, pedido.getItens().get(0).getQuantidade());
            assertEquals(5, mouse.getQuantidadeEmEstoque());
        }

        @Test
        @DisplayName("Deve recusar produto nulo e quantidade zero ou negativa")
        void deveRecusarArgumentosInvalidos() {
            Pedido pedido = new Pedido(4, cliente);
            assertThrows(IllegalArgumentException.class, () -> pedido.adicionarItem(null, 1));
            assertThrows(IllegalArgumentException.class, () -> pedido.adicionarItem(mouse, 0));
            assertThrows(IllegalArgumentException.class, () -> pedido.adicionarItem(mouse, -2));
            assertEquals(20, mouse.getQuantidadeEmEstoque());
        }

        @Test
        @DisplayName("Deve recusar número de pedido inválido e cliente nulo")
        void deveRecusarConstrucaoInvalida() {
            assertThrows(IllegalArgumentException.class, () -> new Pedido(0, cliente));
            assertThrows(IllegalArgumentException.class, () -> new Pedido(null, cliente));
            assertThrows(IllegalArgumentException.class, () -> new Pedido(1, null));
        }
    }

    @Nested
    @DisplayName("Contexto: Processamento de Pagamento")
    class ProcessamentoPagamento {

        @Test
        @DisplayName("Deve alterar status para PAGO quando o pagamento for aprovado")
        void devePassarParaPagoQuandoAprovado() throws Exception {
            Pedido pedido = criarPedidoComDoisItens();
            pedido.pagar(new Pix());

            assertEquals("PAGO", pedido.getStatus());
        }

        @Test
        @DisplayName("Deve recusar processador nulo sem alterar a situação do pedido")
        void deveRecusarProcessadorNulo() throws Exception {
            Pedido pedido = criarPedidoComDoisItens();
            assertThrows(IllegalArgumentException.class, () -> pedido.pagar(null));
            assertEquals("ABERTO", pedido.getStatus());
        }

        @Test
        @DisplayName("Deve manter o pedido ABERTO quando o pagamento for recusado")
        void deveManterPedidoAbertoQuandoPagamentoRecusado() throws Exception {
            Pedido pedido = criarPedidoComDoisItens(); // total R$ 3200,00
            PagamentoRecusadoException erro = assertThrows(PagamentoRecusadoException.class,
                () -> pedido.pagar(new CartaoCredito(new BigDecimal("100.00"))));

            assertAll(
                () -> assertEquals("ABERTO", pedido.getStatus()),
                () -> assertNull(pedido.getComprovante()),
                () -> assertEquals("Cartão de Crédito", erro.getFormaPagamento()),
                () -> assertTrue(erro.getMotivo().contains("limite"))
            );
        }

        @Test
        @DisplayName("Deve impedir a segunda cobrança de um pedido já pago")
        void deveImpedirPagamentoDuplicado() throws Exception {
            Pedido pedido = criarPedidoComDoisItens();
            pedido.pagar(new Pix());
            String comprovante = pedido.getComprovante();

            assertThrows(PedidoInvalidoException.class, () -> pedido.pagar(new Pix()));
            assertEquals("PAGO", pedido.getStatus());
            assertEquals(comprovante, pedido.getComprovante());
        }

        @Test
        @DisplayName("Deve recusar adicionar item a pedido já pago sem baixar estoque")
        void deveRecusarItemEmPedidoPago() throws Exception {
            Pedido pedido = criarPedidoComDoisItens();
            pedido.pagar(new Pix());
            int estoqueAntes = mouse.getQuantidadeEmEstoque();

            PedidoInvalidoException erro = assertThrows(PedidoInvalidoException.class,
                () -> pedido.adicionarItem(mouse, 1));
            assertEquals(1, erro.getNumeroPedido());
            assertEquals(estoqueAntes, mouse.getQuantidadeEmEstoque());
        }

        @Test
        @DisplayName("Deve recusar pagamento cujo valor informado diverge do total")
        void deveRecusarValorDivergente() throws Exception {
            Pedido pedido = criarPedidoComDoisItens();
            assertThrows(PagamentoRecusadoException.class,
                () -> pedido.pagar(new Pix(), new BigDecimal("10.00")));
            assertEquals("ABERTO", pedido.getStatus());
        }

        @Test
        @DisplayName("Deve aceitar pagamento quando o valor informado confere com o total (caminho feliz)")
        void deveAceitarValorCorreto() throws Exception {
            Pedido pedido = criarPedidoComDoisItens();
            assertTrue(pedido.pagar(new Pix(), new BigDecimal("3200.00")));
            assertEquals("PAGO", pedido.getStatus());
        }
    }
}
