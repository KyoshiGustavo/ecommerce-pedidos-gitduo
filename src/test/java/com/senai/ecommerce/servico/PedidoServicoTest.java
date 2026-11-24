package com.senai.ecommerce.servico;

import com.senai.ecommerce.excecao.PedidoInvalidoException;
import com.senai.ecommerce.modelo.Cliente;
import com.senai.ecommerce.modelo.Pedido;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PedidoServicoTest {
    private PedidoServico pedidos;
    private Cliente cliente;
    private Pedido primeiro;

    @BeforeEach
    void prepararCenario() {
        pedidos = new PedidoServico();
        cliente = new Cliente("Ana Silva", "52998224725", "ana@email.com");
        primeiro = pedidos.criar(cliente);
    }

    @Test
    @DisplayName("Deve encontrar pedido já criado pelo seu número")
    void deveEncontrarPedidoQuandoNumeroExiste() throws PedidoInvalidoException {
        Pedido encontrado = pedidos.buscar(primeiro.getNumero());

        assertSame(primeiro, encontrado);
    }

    @Test
    @DisplayName("Deve atribuir o próximo número quando cria outro pedido")
    void deveAtribuirNumeroSeguinteQuandoCriaOutroPedido() {
        Pedido segundo = pedidos.criar(cliente);

        assertEquals(primeiro.getNumero() + 1, segundo.getNumero());
    }

    @Test
    @DisplayName("Deve listar todos os pedidos criados")
    void deveListarPedidosQuandoCadastrados() {
        Pedido segundo = pedidos.criar(cliente);

        assertEquals(List.of(primeiro, segundo), pedidos.listar());
    }

    @Test
    @DisplayName("Deve informar o número consultado quando o pedido não existe")
    void deveRecusarBuscaQuandoPedidoInexistente() {
        PedidoInvalidoException erro = assertThrows(PedidoInvalidoException.class,
            () -> pedidos.buscar(999));

        assertAll(
            () -> assertEquals(999, erro.getNumeroPedido()),
            () -> assertEquals(List.of(primeiro), pedidos.listar())
        );
    }
}
