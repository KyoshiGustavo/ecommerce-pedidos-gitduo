package com.senai.ecommerce.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PedidoUtilsTest {
    private double[] precos;
    private int[] quantidades;

    @BeforeEach
    void prepararCenario() {
        precos = new double[]{50.0, 25.5};
        quantidades = new int[]{2, 3};
    }

    @Test
    @DisplayName("Deve somar o preço multiplicado pela quantidade de cada item")
    void deveCalcularSubtotalQuandoHaItens() {
        double subtotal = PedidoUtils.calcularSubtotal(precos, quantidades);

        assertEquals(176.5, subtotal, 0.00001);
    }

    @Test
    @DisplayName("Deve retornar subtotal zero quando não há itens")
    void deveCalcularSubtotalZeroQuandoNaoHaItens() {
        double subtotal = PedidoUtils.calcularSubtotal(new double[0], new int[0]);

        assertEquals(0.0, subtotal);
    }

    @ParameterizedTest
    @CsvSource({
        "0.5, 100.00, 15.00",
        "2.1, 299.99, 22.50",
        "2.1, 300.00, 0.00",
        "0.0, 100.00, 0.00"
    })
    @DisplayName("Deve aplicar frete mínimo, arredondamento por quilo e frete grátis")
    void deveAplicarRegrasQuandoCalculaFrete(double peso, double subtotal, double esperado) {
        double frete = PedidoUtils.calcularFrete(peso, subtotal);

        assertEquals(esperado, frete, 0.00001);
    }

    @ParameterizedTest
    @CsvSource({"-1.00, 0.00", "200.00, 20.00", "500.00, 50.00", "900.00, 50.00"})
    @DisplayName("Deve aplicar desconto de 10% respeitando o teto de R$ 50")
    void deveAplicarTetoQuandoCalculaDesconto(double subtotal, double esperado) {
        double desconto = PedidoUtils.calcularDesconto(subtotal);

        assertEquals(esperado, desconto, 0.00001);
    }
}
