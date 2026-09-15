package com.senai.ecommerce.modelo.pagamento;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public abstract class FormaPagamento {
    private BigDecimal valor;
    private LocalDateTime dataDoPagamento;

    protected FormaPagamento(BigDecimal valor) {
        setValor(valor);
        this.dataDoPagamento = LocalDateTime.now();
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor do pagamento deve ser positivo");
        }
        this.valor = valor;
    }

    public LocalDateTime getDataDoPagamento() {
        return dataDoPagamento;
    }

    public abstract boolean processar();

    public String getResumo() {
        return String.format("%s no valor de R$ %s", getClass().getSimpleName(), valor);
    }
}