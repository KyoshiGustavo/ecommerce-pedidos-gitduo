package com.senai.ecommerce.modelo.pagamento;

import java.math.BigDecimal;
import java.util.UUID;

public class Dinheiro extends FormaPagamento {
    public Dinheiro() {
        super("Dinheiro");
    }

    @Override
    public boolean processar(BigDecimal valor) {
        setComprovante("CASH-" + UUID.randomUUID().toString().substring(0, 8));
        return true;
    }
}