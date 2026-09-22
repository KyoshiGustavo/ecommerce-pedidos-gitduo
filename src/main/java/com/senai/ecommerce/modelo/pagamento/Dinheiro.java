package com.senai.ecommerce.modelo.pagamento;

import java.math.BigDecimal;

public class Dinheiro extends FormaPagamento implements ProcessadorPagamento {
    private final BigDecimal valorRecebido;

    public Dinheiro(BigDecimal valor, BigDecimal valorRecebido) {
        super(valor);
        if (valorRecebido == null || valorRecebido.compareTo(valor) < 0) {
            throw new IllegalArgumentException("Valor recebido é insuficiente.");
        }
        this.valorRecebido = valorRecebido;
    }

    @Override
    public boolean processar(BigDecimal valor) {
        System.out.println("Pagamento em dinheiro recebido. Troco: R$ " + valorRecebido.subtract(valor));
        return true;
    }

    @Override
    public String getComprovante() {
        return "RECIBO-DINHEIRO-" + System.currentTimeMillis();
    }

    @Override
    public String getDescricao() {
        return "Dinheiro em Espécie";
    }
}