package com.senai.ecommerce.excecao;

/**
 * Operação incompatível com a situação atual do pedido (checked): pagar pedido sem itens,
 * pagar duas vezes, adicionar item a pedido já pago, consultar pedido inexistente.
 * Quem chamou consegue reagir (informar o usuário, abrir novo pedido), por isso é checked.
 */
public class PedidoInvalidoException extends ECommerceException {
    private final int numeroPedido;

    public PedidoInvalidoException(int numero, String motivo) {
        super("Pedido " + numero + ": " + motivo);
        this.numeroPedido = numero;
    }

    public int getNumeroPedido() {
        return numeroPedido;
    }
}
