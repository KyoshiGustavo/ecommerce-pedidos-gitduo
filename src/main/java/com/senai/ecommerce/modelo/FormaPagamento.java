package com.senai.ecommerce.modelo;

import java.math.BigDecimal;

public abstract class FormaPagamento {
    private BigDecimal valor;

    public FormaPagamento(BigDecimal valor) {
        setValor(valor);
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor do pagamento deve ser maior que zero.");
        }
        this.valor = valor;
    }

    public abstract boolean processar();
}