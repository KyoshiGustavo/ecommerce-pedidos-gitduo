package com.senai.ecommerce.servico;

import com.senai.ecommerce.excecao.CadastroDuplicadoException;
import com.senai.ecommerce.excecao.ClienteNaoEncontradoException;
import com.senai.ecommerce.excecao.PedidoInvalidoException;
import com.senai.ecommerce.modelo.Cliente;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ServicosTest {

    @Test
    @DisplayName("Produto duplicado (ignorando maiúsculas e espaços) deve ser recusado e não entrar no cadastro")
    void deveRecusarProdutoDuplicado() throws Exception {
        ProdutoServico produtos = new ProdutoServico();
        produtos.cadastrar("Notebook", new BigDecimal("3500.00"), 3);

        CadastroDuplicadoException erro = assertThrows(CadastroDuplicadoException.class,
            () -> produtos.cadastrar("  notebook ", new BigDecimal("10.00"), 1));
        assertEquals("Produto", erro.getTipo());
        assertEquals(1, produtos.listar().size());
    }

    @Test
    @DisplayName("Cliente duplicado deve ser detectado pelos dígitos do documento, com ou sem máscara")
    void deveRecusarClienteDuplicado() throws Exception {
        ClienteServico clientes = new ClienteServico();
        clientes.cadastrar("Ana Silva", "529.982.247-25", "ana@email.com");

        assertThrows(CadastroDuplicadoException.class,
            () -> clientes.cadastrar("Outra Ana", "52998224725", "outra@email.com"));
        assertEquals(1, clientes.listar().size());
    }

    @Test
    @DisplayName("Busca por documento inexistente deve lançar ClienteNaoEncontradoException com o identificador")
    void deveLancarClienteNaoEncontrado() {
        ClienteServico clientes = new ClienteServico();
        ClienteNaoEncontradoException erro = assertThrows(ClienteNaoEncontradoException.class,
            () -> clientes.buscarPorDocumento("111.444.777-35"));
        assertEquals("111.444.777-35", erro.getIdentificador());
    }

    @Test
    @DisplayName("Busca de cliente cadastrado deve funcionar com ou sem máscara (caminho feliz)")
    void deveEncontrarClienteCadastrado() throws Exception {
        ClienteServico clientes = new ClienteServico();
        Cliente ana = clientes.cadastrar("Ana Silva", "529.982.247-25", "ana@email.com");
        assertSame(ana, clientes.buscarPorDocumento("52998224725"));
    }

    @Test
    @DisplayName("Pedido inexistente deve lançar PedidoInvalidoException; pedido criado deve ser encontrado")
    void devePedirPedidoExistente() throws Exception {
        PedidoServico pedidos = new PedidoServico();
        Cliente ana = new Cliente("Ana Silva", "52998224725", "ana@email.com");
        var criado = pedidos.criar(ana);

        assertSame(criado, pedidos.buscar(criado.getNumero()));
        assertThrows(PedidoInvalidoException.class, () -> pedidos.buscar(999));
    }

    @Test
    @DisplayName("Produto buscado e não encontrado devolve Optional vazio, não exceção")
    void deveDevolverOptionalVazio() {
        assertTrue(new ProdutoServico().buscarPorNome("Fantasma").isEmpty());
        assertTrue(new ProdutoServico().buscarPorNome(null).isEmpty());
    }
}
