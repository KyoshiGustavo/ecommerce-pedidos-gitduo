package com.senai.ecommerce.modelo.pagamento;

import java.math.BigDecimal;
import java.util.UUID;

public class Boleto extends FormaPagamento {
    public Boleto() {
        super("Boleto Bancário");
    }

    @Override
    public boolean processar(BigDecimal valor) {
        setComprovante("BOL-" + UUID.randomUUID().toString().substring(0, 8));
        return true;
    }
}