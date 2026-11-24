package com.senai.ecommerce.modelo.pagamento;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProcessadorPagamentoTest {
    private List<ProcessadorPagamento> processadores;

    @BeforeEach
    void prepararCenario() {
        processadores = List.of(new Pix(), new Boleto(), new CartaoCredito(), new Dinheiro());
    }

    @Test
    @DisplayName("Deve fornecer motivo padrão quando o processador não detalha recusas")
    void deveFornecerMotivoPadraoQuandoSemDetalhamento() {
        ProcessadorPagamento pix = new Pix();

        String motivo = pix.getMotivoRecusa();

        assertEquals("transação não autorizada. Tente outra forma de pagamento.", motivo);
    }

    @Test
    @DisplayName("Deve processar todas as formas pela interface comum")
    void deveProcessarTodasAsFormasPolimorficamente() {
        BigDecimal valor = new BigDecimal("125.50");

        assertAll(processadores.stream().map(processador -> () -> {
            boolean aprovado = processador.processar(valor);

            assertAll(processador.getDescricao(),
                () -> assertTrue(aprovado),
                () -> assertNotNull(processador.getComprovante())
            );
        }));
    }
}
