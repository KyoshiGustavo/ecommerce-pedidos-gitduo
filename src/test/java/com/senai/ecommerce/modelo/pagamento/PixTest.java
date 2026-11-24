package com.senai.ecommerce.modelo.pagamento;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PixTest {
    private Pix pagamento;

    @BeforeEach
    void prepararCenario() {
        pagamento = new Pix();
    }

    @ParameterizedTest(name = "valor R$ {0}")
    @ValueSource(strings = {"0.01", "125.50", "10000.00"})
    @DisplayName("Deve aprovar pagamento via Pix e emitir comprovante para valores positivos")
    void deveAprovarEEmitirComprovanteQuandoValorPositivo(String valor) {
        boolean aprovado = pagamento.processar(new BigDecimal(valor));

        assertAll(
            () -> assertTrue(aprovado),
            () -> assertTrue(pagamento.getComprovante().matches("PIX-[0-9a-f]{8}"))
        );
    }
}
