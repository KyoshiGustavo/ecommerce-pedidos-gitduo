package com.senai.ecommerce.modelo.pagamento;

import java.math.BigDecimal;
import java.util.UUID;

public class CartaoCredito extends FormaPagamento {
    public CartaoCredito() {
        super("Cartão de Crédito");
    }

    @Override
    public boolean processar(BigDecimal valor) {
        setComprovante("CARD-" + UUID.randomUUID().toString().substring(0, 8));
        return true;
    }
}