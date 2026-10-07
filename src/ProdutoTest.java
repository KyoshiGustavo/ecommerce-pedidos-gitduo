package com.senai.ecommerce.modelo;

import com.senai.ecommerce.excecao.EstoqueInsuficienteException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ProdutoTest {

    private Produto notebook;

    @BeforeEach
    void prepararCenario() {
        notebook = new Produto("Notebook", new BigDecimal("3000.00"), 5);
    }

    @Test
    @DisplayName("Deve criar produto válido com todos os atributos corretos")
    void deveCriarProdutoValido() {
        assertAll("Verificação dos atributos do produto",
            () -> assertEquals("Notebook", notebook.getNome()),
            () -> assertEquals(0, new BigDecimal("3000.00").compareTo(notebook.getPreco())),
            () -> assertEquals(5, notebook.getQuantidadeEmEstoque())
        );
    }

    @Test
    @DisplayName("Deve baixar o estoque quando há quantidade suficiente")
    void deveBaixarEstoqueQuandoHaQuantidadeSuficiente() {
        notebook.baixarEstoque(2);
        assertEquals(3, notebook.getQuantidadeEmEstoque());
    }

    @Test
    @DisplayName("Deve repor o estoque corretamente")
    void deveReporEstoque() {
        notebook.reporEstoque(5);
        assertEquals(10, notebook.getQuantidadeEmEstoque());
    }

    @Test
    @DisplayName("Deve lançar exceção e manter estado inalterado quando estoque for insuficiente")
    void deveLancarExcecaoQuandoEstoqueInsuficiente() {
        EstoqueInsuficienteException erro = assertThrows(
            EstoqueInsuficienteException.class,
            () -> notebook.baixarEstoque(50)
        );

        assertAll("Verificação da exceção e estado inalterado",
            () -> assertTrue(erro.getMessage().contains("Notebook")),
            () -> assertEquals(5, notebook.getQuantidadeEmEstoque())
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"-0.01", "-10.00", "-500.00"})
    @DisplayName("Deve recusar criação de produto com preços negativos")
    void deveRecusarPrecoNegativo(String precoInvalido) {
        assertThrows(
            IllegalArgumentException.class,
            () -> new Produto("Teclado", new BigDecimal(precoInvalido), 5)
        );
    }
}