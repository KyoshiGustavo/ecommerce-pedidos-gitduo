package com.senai.ecommerce.modelo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class ClienteTest {
    private String cpf;
    private Cliente cliente;

    @BeforeEach
    void prepararCenario() {
        cpf = "529.982.247-25";
        cliente = new Cliente("Ana Silva", cpf, "ana@email.com");
    }

    @Test
    @DisplayName("Deve aceitar CPF válido e normalizar o nome no cadastro")
    void deveCriarClienteComCpfValido() {
        Cliente cadastrado = new Cliente("  Ana Silva  ", cpf, "ana@email.com");

        assertAll(
            () -> assertEquals("Ana Silva", cadastrado.getNome()),
            () -> assertEquals(cpf, cadastrado.getDocumento())
        );
    }

    @Test
    @DisplayName("Deve aceitar CNPJ válido quando o cadastro é de uma empresa")
    void deveCriarClienteComCnpjValido() {
        Cliente empresa = new Cliente("Empresa", "11.222.333/0001-81", "contato@empresa.com");

        assertEquals("11.222.333/0001-81", empresa.getDocumento());
    }

    @Test
    @DisplayName("Deve atualizar o e-mail quando o novo endereço é válido")
    void deveAlterarEmailQuandoValido() {
        cliente.setEmail("  compras@email.com  ");

        assertEquals("compras@email.com", cliente.getEmail());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    @DisplayName("Deve recusar criação com nome nulo ou vazio")
    void deveRecusarNomeInvalido(String nome) {
        assertThrows(IllegalArgumentException.class, () -> new Cliente(nome, cpf, "ana@email.com"));
    }

    @Test
    @DisplayName("Deve recusar criação com e-mail sem arroba")
    void deveRecusarEmailSemArroba() {
        assertThrows(IllegalArgumentException.class, () -> new Cliente("Ana Silva", cpf, "ana.silva.email.com"));
    }

    @Test
    @DisplayName("Deve recusar criação com documento de dígitos verificadores inválidos")
    void deveRecusarDocumentoInvalido() {
        assertThrows(IllegalArgumentException.class,
            () -> new Cliente("Ana Silva", "123.456.789-00", "ana@email.com"));
    }

    @Test
    @DisplayName("Deve manter o endereço anterior quando o novo e-mail é inválido")
    void deveManterEmailQuandoAlteracaoInvalida() {
        assertThrows(IllegalArgumentException.class, () -> cliente.setEmail(null));

        assertEquals("ana@email.com", cliente.getEmail());
    }
}
