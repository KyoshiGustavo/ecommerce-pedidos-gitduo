package com.senai.ecommerce.modelo;

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
        // Ordem dos parâmetros: nome, documento, email
        cliente = new Cliente("Ana Silva", "12345678900", "ana.silva@email.com");
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
        @DisplayName("Deve recusar pagamento se o pedido estiver sem itens")
        void deveRecusarPagamentoDePedidoSemItens() {
            Pedido pedidoVazio = new Pedido(2, cliente);
            assertThrows(
                IllegalStateException.class,
                () -> pedidoVazio.pagar(new Pix())
            );
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
    }
}