package com.senai.ecommerce.modelo.pagamento;

import com.senai.ecommerce.util.Mensagens;
import com.senai.ecommerce.util.Validador;
import java.math.BigDecimal;
import java.util.UUID;

public class CartaoCredito extends FormaPagamento {
    private final BigDecimal limiteDisponivel; // null = sem limite simulado
    private String motivoRecusa = Mensagens.TRANSACAO_NAO_AUTORIZADA;

    /** Cartão sem limite simulado: aprova qualquer valor positivo. */
    public CartaoCredito() {
        super("Cartão de Crédito");
        this.limiteDisponivel = null;
    }

    /** Cartão com limite disponível simulado: recusa compras acima dele. */
    public CartaoCredito(BigDecimal limiteDisponivel) {
        super("Cartão de Crédito");
        this.limiteDisponivel = Validador.valorPositivo(limiteDisponivel, "Limite do cartão");
    }

    @Override
    public boolean processar(BigDecimal valor) {
        Validador.valorPositivo(valor, "Valor da cobrança");
        if (limiteDisponivel != null && valor.compareTo(limiteDisponivel) > 0) {
            motivoRecusa = String.format(Mensagens.CARTAO_LIMITE_INSUFICIENTE,
                    limiteDisponivel.toPlainString(), valor.toPlainString());
            return false;
        }
        setComprovante("CARD-" + UUID.randomUUID().toString().substring(0, 8));
        return true;
    }

    @Override
    public String getMotivoRecusa() {
        return motivoRecusa;
    }
}
