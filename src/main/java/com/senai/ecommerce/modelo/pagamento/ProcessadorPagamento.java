package com.senai.ecommerce.modelo.pagamento;

import com.senai.ecommerce.util.Mensagens;
import java.math.BigDecimal;

public interface ProcessadorPagamento {
    boolean processar(BigDecimal valor);
    String getComprovante();
    String getDescricao();

    /**
     * Motivo da última recusa, usado na PagamentoRecusadoException.
     * Método default: as formas que nunca recusam (Pix, Boleto, Dinheiro) não precisam implementá-lo.
     */
    default String getMotivoRecusa() {
        return Mensagens.TRANSACAO_NAO_AUTORIZADA;
    }
}
