package com.senai.ecommerce.excecao;

/**
 * A forma de pagamento não aprovou a transação (checked).
 * Quem trata pode oferecer outra forma de pagamento; o pedido NÃO muda de situação.
 */
public class PagamentoRecusadoException extends ECommerceException {
    private final String formaPagamento;
    private final String motivo;

    public PagamentoRecusadoException(String formaPagamento, String motivo) {
        super("Pagamento por " + formaPagamento + " recusado: " + motivo);
        this.formaPagamento = formaPagamento;
        this.motivo = motivo;
    }

    public String getFormaPagamento() {
        return formaPagamento;
    }

    public String getMotivo() {
        return motivo;
    }
}
