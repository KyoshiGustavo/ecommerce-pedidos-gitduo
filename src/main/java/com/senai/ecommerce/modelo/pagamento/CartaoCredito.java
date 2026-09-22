package com.senai.ecommerce.modelo.pagamento;

import java.math.BigDecimal;

public class CartaoCredito extends FormaPagamento implements ProcessadorPagamento {
    private final String numero;
    private final int parcelas;

    public CartaoCredito(BigDecimal valor, String numero, int parcelas) {
        super(valor);
        if (parcelas <= 0) {
            throw new IllegalArgumentException("Número de parcelas deve ser positivo");
        }
        this.numero = numero;
        this.parcelas = parcelas;
    }

    @Override
    public boolean processar(BigDecimal valor) {
        System.out.println("Autorizando cartão em " + parcelas + "x");
        return true;
    }

    @Override
    public String getComprovante() {
        return "CARD-" + System.currentTimeMillis();
    }

    @Override
    public String getDescricao() {
        return "Cartão de Crédito (" + parcelas + "x)";
    }
}