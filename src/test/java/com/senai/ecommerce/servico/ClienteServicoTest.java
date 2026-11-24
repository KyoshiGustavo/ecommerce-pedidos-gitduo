package com.senai.ecommerce.servico;

import com.senai.ecommerce.excecao.CadastroDuplicadoException;
import com.senai.ecommerce.excecao.ClienteNaoEncontradoException;
import com.senai.ecommerce.modelo.Cliente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ClienteServicoTest {
    private ClienteServico clientes;
    private Cliente ana;

    @BeforeEach
    void prepararCenario() throws CadastroDuplicadoException {
        clientes = new ClienteServico();
        ana = clientes.cadastrar("Ana Silva", "529.982.247-25", "ana@email.com");
    }

    @ParameterizedTest
    @ValueSource(strings = {"529.982.247-25", "52998224725"})
    @DisplayName("Deve encontrar o mesmo cliente ao comparar CPF com ou sem máscara")
    void deveEncontrarClienteCadastrado(String documento) throws ClienteNaoEncontradoException {
        Cliente encontrado = clientes.buscarPorDocumento(documento);

        assertSame(ana, encontrado);
    }

    @Test
    @DisplayName("Deve cadastrar outro cliente quando o documento é diferente")
    void deveCadastrarClienteQuandoDocumentoDiferente() throws CadastroDuplicadoException {
        Cliente bia = clientes.cadastrar("Bia Lima", "111.444.777-35", "bia@email.com");

        assertEquals(List.of(ana, bia), clientes.listar());
    }

    @Test
    @DisplayName("Deve recusar CPF duplicado e manter o cadastro anterior")
    void deveRecusarClienteDuplicado() {
        CadastroDuplicadoException erro = assertThrows(CadastroDuplicadoException.class,
            () -> clientes.cadastrar("Outra Ana", "52998224725", "outra@email.com"));

        assertAll(
            () -> assertEquals("Cliente", erro.getTipo()),
            () -> assertEquals("52998224725", erro.getIdentificador()),
            () -> assertEquals(List.of(ana), clientes.listar())
        );
    }

    @Test
    @DisplayName("Deve informar o documento buscado quando o cliente não é encontrado")
    void deveLancarClienteNaoEncontrado() {
        ClienteNaoEncontradoException erro = assertThrows(ClienteNaoEncontradoException.class,
            () -> clientes.buscarPorDocumento("111.444.777-35"));

        assertAll(
            () -> assertEquals("111.444.777-35", erro.getIdentificador()),
            () -> assertEquals(List.of(ana), clientes.listar())
        );
    }
}
