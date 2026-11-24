package com.senai.ecommerce.apresentacao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CenariosDeErroTest {
    private CenariosDeErro cenarios;

    @BeforeEach
    void prepararCenario() {
        cenarios = new CenariosDeErro();
    }


    @Test
    @DisplayName("Todos os cenários do Mapa de Exceções devem produzir exatamente o resultado previsto")
    void deveValidarTodosOsCenariosQuandoExecutados() {
        var resultados = cenarios.executar();

        assertEquals(24, resultados.size());
        resultados.forEach(r ->
            assertTrue(r.ok(), r.id() + " (" + r.descricao() + ") esperado " + r.esperado() + " mas obtido " + r.obtido()));
    }
}
