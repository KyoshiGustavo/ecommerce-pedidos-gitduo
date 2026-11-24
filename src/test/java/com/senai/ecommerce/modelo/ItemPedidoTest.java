package com.senai.ecommerce.modelo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ItemPedidoTest {
    private Produto notebook;

    @BeforeEach
    void prepararCenario() {
        notebook = new Produto("Notebook", new BigDecimal("3000.00"), 5);
    }

    @Test
    @DisplayName("Deve calcular subtotal igual ao preço quando há uma unidade")
    void deveCalcularSubtotalComUmaUnidade() {
        ItemPedido item = new ItemPedido(notebook, 1);

        BigDecimal subtotal = item.getSubtotal();

        assertEquals(0, new BigDecimal("3000.00").compareTo(subtotal));
    }

    @Test
    @DisplayName("Deve multiplicar o preço quando há mais de uma unidade")
    void deveCalcularSubtotalComVariasUnidades() {
        ItemPedido item = new ItemPedido(notebook, 2);

        BigDecimal subtotal = item.getSubtotal();

        assertEquals(0, new BigDecimal("6000.00").compareTo(subtotal));
    }

    @Test
    @DisplayName("Deve recalcular o subtotal quando a quantidade muda")
    void deveRecalcularSubtotalQuandoQuantidadeMuda() {
        ItemPedido item = new ItemPedido(notebook, 1);

        item.setQuantidade(3);

        assertEquals(0, new BigDecimal("9000.00").compareTo(item.getSubtotal()));
    }

    @Test
    @DisplayName("Deve recusar a criação de um item com produto nulo")
    void deveRecusarProdutoNulo() {
        assertThrows(IllegalArgumentException.class, () -> new ItemPedido(null, 1));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    @DisplayName("Deve preservar o subtotal quando a nova quantidade é inválida")
    void devePreservarSubtotalQuandoQuantidadeInvalida(int quantidade) {
        ItemPedido item = new ItemPedido(notebook, 2);

        assertThrows(IllegalArgumentException.class, () -> item.setQuantidade(quantidade));

        assertEquals(0, new BigDecimal("6000.00").compareTo(item.getSubtotal()));
    }
}
