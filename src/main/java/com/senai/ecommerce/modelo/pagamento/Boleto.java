package com.senai.ecommerce.modelo.pagamento;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Boleto extends FormaPagamento implements ProcessadorPagamento {
    private final LocalDate vencimento;

    public Boleto(BigDecimal valor, LocalDate vencimento) {
        super(valor);
        if (vencimento == null || vencimento.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Data de vencimento inválida");
        }
        this.vencimento = vencimento;
    }

    @Override
    public boolean processar(BigDecimal valor) {
        System.out.println("Gerando boleto com vencimento em " + vencimento);
        return true;
    }

    @Override
    public String getComprovante() {
        return "BOL-" + System.currentTimeMillis();
    }

    @Override
    public String getDescricao() {
        return "Boleto - vencimento: " + vencimento;
    }
}