package com.senai.ecommerce.apresentacao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CenariosDeErroTest {

    @Test
    @DisplayName("Todos os cenários do Mapa de Exceções devem produzir exatamente o resultado previsto")
    void todosOsCenariosDevemBaterComOPrevisto() {
        var resultados = new CenariosDeErro().executar();

        assertFalse(resultados.isEmpty());
        resultados.forEach(r ->
            assertTrue(r.ok(), r.id() + " (" + r.descricao() + ") esperado " + r.esperado() + " mas obtido " + r.obtido()));
    }
}
