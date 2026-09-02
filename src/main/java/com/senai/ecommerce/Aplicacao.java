package com.senai.ecommerce;

import com.senai.ecommerce.util.PedidoUtils;

public class Aplicacao {

    public static void main(String[] args) {
        String[] produtos    = {"Teclado", "Monitor", "Mouse"};
        double[] precos      = {150.00, 899.90, 79.50};
        int[]    quantidades = {1, 2, 3};

        double subtotal = PedidoUtils.calcularSubtotal(precos, quantidades);

        System.out.println("==========================================");
        System.out.println("Pedido:   " + PedidoUtils.gerarNumeroDoPedido());
        System.out.println("Subtotal: R$ " + String.format("%.2f", subtotal));
        System.out.println("Frete:    R$ " + String.format("%.2f", PedidoUtils.calcularFrete(4.2, subtotal)));
        System.out.println("Desconto: R$ " + String.format("%.2f", PedidoUtils.calcularDesconto(subtotal)));
        System.out.println("==========================================");
        System.out.println(PedidoUtils.montarRecibo(produtos, precos, quantidades));
    }
}