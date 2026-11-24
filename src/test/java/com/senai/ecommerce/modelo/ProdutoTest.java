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
        Produto produto = new Produto("  Notebook  ", new BigDecimal("3000.00"), 5);

        assertAll(
            () -> assertEquals("Notebook", produto.getNome()),
            () -> assertEquals(0, new BigDecimal("3000.00").compareTo(produto.getPreco())),
            () -> assertEquals(5, produto.getQuantidadeEmEstoque())
        );
    }

    @Test
    @DisplayName("Deve baixar o estoque quando há quantidade suficiente")
    void deveBaixarEstoqueQuandoHaQuantidadeSuficiente() throws EstoqueInsuficienteException {
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
    @DisplayName("Deve lançar exceção com dados de negócio e manter estado inalterado quando estoque for insuficiente")
    void deveLancarExcecaoQuandoEstoqueInsuficiente() {
        EstoqueInsuficienteException erro = assertThrows(
            EstoqueInsuficienteException.class,
            () -> notebook.baixarEstoque(50)
        );

        assertAll("Verificação da exceção e estado inalterado",
            () -> assertTrue(erro.getMessage().contains("Notebook")),
            () -> assertSame(notebook, erro.getProduto()),
            () -> assertEquals(50, erro.getQuantidadeSolicitada()),
            () -> assertEquals(5, erro.getQuantidadeDisponivel()),
            () -> assertEquals(5, notebook.getQuantidadeEmEstoque())
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"-0.01", "-5.00", "-500.00"})
    @DisplayName("Deve recusar criação de produto com preço negativo")
    void deveRecusarPrecoNegativo(String precoInvalido) {
        assertThrows(
            IllegalArgumentException.class,
            () -> new Produto("Teclado", new BigDecimal(precoInvalido), 5)
        );
    }

    @Test
    @DisplayName("Deve recusar criação de produto com preço zero")
    void deveRecusarPrecoZero() {
        assertThrows(IllegalArgumentException.class, () -> new Produto("Teclado", BigDecimal.ZERO, 5));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    @DisplayName("Deve recusar produto com nome vazio")
    void deveRecusarNomeVazio(String nomeInvalido) {
        assertThrows(IllegalArgumentException.class,
            () -> new Produto(nomeInvalido, new BigDecimal("10.00"), 5));
    }

    @Test
    @DisplayName("Deve recusar estoque negativo na criação")
    void deveRecusarEstoqueNegativo() {
        assertThrows(IllegalArgumentException.class,
            () -> new Produto("Teclado", new BigDecimal("10.00"), -1));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -50})
    @DisplayName("Deve recusar baixa de estoque com quantidade zero ou negativa (defeito de quem chamou)")
    void deveRecusarBaixaComQuantidadeInvalida(int quantidade) {
        assertThrows(IllegalArgumentException.class, () -> notebook.baixarEstoque(quantidade));
        assertEquals(5, notebook.getQuantidadeEmEstoque());
    }
    @Test
    @DisplayName("Deve zerar o estoque quando a baixa consome todas as unidades")
    void deveZerarEstoqueQuandoBaixaConsomeTudo() throws EstoqueInsuficienteException {
        notebook.baixarEstoque(5);

        assertEquals(0, notebook.getQuantidadeEmEstoque());
    }

    @Test
    @DisplayName("Deve aceitar reposição exatamente até o limite do estoque")
    void deveAceitarReposicaoQuandoAtingeLimite() {
        Produto produto = new Produto("Mouse", new BigDecimal("10.00"), Integer.MAX_VALUE - 1);

        produto.reporEstoque(1);

        assertEquals(Integer.MAX_VALUE, produto.getQuantidadeEmEstoque());
    }

    @Test
    @DisplayName("Deve recusar reposição acima do limite sem alterar o estoque")
    void devePreservarEstoqueQuandoReposicaoExcedeLimite() {
        Produto produtoLotado = new Produto("Mouse", new BigDecimal("10.00"), Integer.MAX_VALUE);

        assertThrows(IllegalArgumentException.class, () -> produtoLotado.reporEstoque(1));

        assertEquals(Integer.MAX_VALUE, produtoLotado.getQuantidadeEmEstoque());
    }
}
