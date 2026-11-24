package com.senai.ecommerce.modelo.pagamento;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class CartaoCreditoTest {
    private CartaoCredito cartao;

    @BeforeEach
    void prepararCenario() {
        cartao = new CartaoCredito(new BigDecimal("100.00"));
    }

    @ParameterizedTest(name = "valor R$ {0}")
    @ValueSource(strings = {"0.01", "50.00", "100.000"})
    @DisplayName("Deve aprovar e emitir comprovante quando a cobrança está dentro do limite")
    void deveAprovarQuandoDentroDoLimite(String valor) {
        boolean aprovado = cartao.processar(new BigDecimal(valor));

        assertAll(
            () -> assertTrue(aprovado),
            () -> assertTrue(cartao.getComprovante().matches("CARD-[0-9a-f]{8}"))
        );
    }

    @Test
    @DisplayName("Deve aprovar valor positivo com cartão sem limite simulado")
    void deveAprovarQuandoCartaoNaoTemLimiteSimulado() {
        CartaoCredito cartaoSemLimite = new CartaoCredito();

        boolean aprovado = cartaoSemLimite.processar(new BigDecimal("1000.00"));

        assertAll(
            () -> assertTrue(aprovado),
            () -> assertTrue(cartaoSemLimite.getComprovante().matches("CARD-[0-9a-f]{8}"))
        );
    }

    @Test
    @DisplayName("Deve recusar compra acima do limite sem emitir comprovante")
    void deveRecusarQuandoAcimaDoLimite() {
        boolean aprovado = cartao.processar(new BigDecimal("100.01"));

        assertAll(
            () -> assertFalse(aprovado),
            () -> assertNull(cartao.getComprovante()),
            () -> assertTrue(cartao.getMotivoRecusa().contains("100.00"))
        );
    }

    @Test
    @DisplayName("Deve recusar cobrança de valor zero sem emitir comprovante")
    void deveRecusarQuandoCobrancaZero() {
        assertThrows(IllegalArgumentException.class, () -> cartao.processar(BigDecimal.ZERO));

        assertNull(cartao.getComprovante());
    }
}
