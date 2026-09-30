package com.senai.ecommerce.modelo.pagamento;

import java.math.BigDecimal;

public abstract class FormaPagamento implements ProcessadorPagamento {
    private String descricao;
    private String comprovante;

    public FormaPagamento(String descricao) {
        this.descricao = descricao;
    }

    @Override
    public String getDescricao() {
        return descricao;
    }

    @Override
    public String getComprovante() {
        return comprovante;
    }

    protected void setComprovante(String comprovante) {
        this.comprovante = comprovante;
    }

    @Override
    public abstract boolean processar(BigDecimal valor);
}