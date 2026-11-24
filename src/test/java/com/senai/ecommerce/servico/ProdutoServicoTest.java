package com.senai.ecommerce.servico;

import com.senai.ecommerce.excecao.CadastroDuplicadoException;
import com.senai.ecommerce.modelo.Produto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProdutoServicoTest {
    private ProdutoServico produtos;
    private Produto notebook;

    @BeforeEach
    void prepararCenario() throws CadastroDuplicadoException {
        produtos = new ProdutoServico();
        notebook = produtos.cadastrar("Notebook", new BigDecimal("3500.00"), 3);
    }

    @Test
    @DisplayName("Deve disponibilizar produto após cadastro válido")
    void deveDisponibilizarProdutoQuandoCadastroValido() throws CadastroDuplicadoException {
        Produto teclado = produtos.cadastrar("Teclado", new BigDecimal("150.00"), 4);

        assertTrue(produtos.listar().contains(teclado));
    }

    @Test
    @DisplayName("Deve localizar produto pelo nome com caixa e espaços diferentes")
    void deveEncontrarProdutoQuandoNomeEquivalente() {
        var encontrado = produtos.buscarPorNome("  NOTEBOOK  ");

        assertSame(notebook, encontrado.orElseThrow());
    }

    @Test
    @DisplayName("Deve sugerir alternativa com estoque suficiente, incluindo a quantidade exata")
    void deveSugerirAlternativaQuandoEstoqueSuficiente() throws CadastroDuplicadoException {
        produtos.cadastrar("Mouse", new BigDecimal("10.00"), 1);
        Produto teclado = produtos.cadastrar("Teclado", new BigDecimal("30.00"), 3);

        List<Produto> alternativas = produtos.comEstoqueMinimo(3, notebook);

        assertEquals(List.of(teclado), alternativas);
    }

    @Test
    @DisplayName("Deve recusar produto duplicado e preservar o cadastro original")
    void deveRecusarProdutoDuplicado() {
        CadastroDuplicadoException erro = assertThrows(CadastroDuplicadoException.class,
            () -> produtos.cadastrar("  notebook ", BigDecimal.TEN, 1));

        assertAll(
            () -> assertEquals("Produto", erro.getTipo()),
            () -> assertEquals("notebook", erro.getIdentificador()),
            () -> assertEquals(List.of(notebook), produtos.listar())
        );
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"Fantasma"})
    @DisplayName("Deve retornar busca vazia quando o nome é nulo ou desconhecido")
    void deveDevolverOptionalVazio(String nome) {
        var encontrado = produtos.buscarPorNome(nome);

        assertTrue(encontrado.isEmpty());
    }
}
