package com.senai.ecommerce.modelo.pagamento;

import java.math.BigDecimal;
import java.util.UUID;

public class Pix extends FormaPagamento {
    public Pix() {
        super("PIX");
    }

    @Override
    public boolean processar(BigDecimal valor) {
        setComprovante("PIX-" + UUID.randomUUID().toString().substring(0, 8));
        return true;
    }
}