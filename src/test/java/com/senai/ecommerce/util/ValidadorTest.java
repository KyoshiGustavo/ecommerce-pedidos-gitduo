package com.senai.ecommerce.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class ValidadorTest {

    @ParameterizedTest
    @ValueSource(strings = {"529.982.247-25", "52998224725", "111.444.777-35", "11.222.333/0001-81", "11222333000181"})
    @DisplayName("Deve aceitar CPF e CNPJ válidos, com ou sem máscara")
    void deveAceitarDocumentosValidos(String documento) {
        assertEquals(documento, Validador.documentoValido(documento));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"123", "12345678900", "111.111.111-11", "529.982.247-24", "11.222.333/0001-80", "abcdefghijk"})
    @DisplayName("Deve recusar documento nulo, vazio, curto, com dígito verificador errado ou repetido")
    void deveRecusarDocumentosInvalidos(String documento) {
        assertThrows(IllegalArgumentException.class, () -> Validador.documentoValido(documento));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ana@email.com", "ana.silva+loja@mail.com.br", "a_b-c@dominio.org"})
    @DisplayName("Deve aceitar e-mails bem formados")
    void deveAceitarEmailsValidos(String email) {
        assertEquals(email, Validador.emailValido(email));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"ana.email.com", "ana@", "@email.com", "ana@email", "ana@@email.com", "ana silva@email.com"})
    @DisplayName("Deve recusar e-mails sem formato válido")
    void deveRecusarEmailsInvalidos(String email) {
        assertThrows(IllegalArgumentException.class, () -> Validador.emailValido(email));
    }

    @Test
    @DisplayName("Mensagem de erro deve informar o valor recebido e o esperado")
    void deveInformarRecebidoEEsperadoQuandoEmailInvalido() {
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
            () -> Validador.emailValido("ana.email.com"));

        assertAll(
            () -> assertTrue(erro.getMessage().contains("ana.email.com")),
            () -> assertTrue(erro.getMessage().contains("nome@dominio.com"))
        );
    }

    @Test
    @DisplayName("Texto obrigatório deve devolver o texto sem espaços nas pontas")
    void deveAparar() {
        assertEquals("Ana", Validador.textoObrigatorio("  Ana  ", "Nome"));
    }
}
