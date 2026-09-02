package com.senai.ecommerce.util;

import java.util.Random;

/**
 * Cálculos e formatações de apoio ao módulo de pedidos.
 * Classe utilitária: todos os métodos são estáticos e não pode ser instanciada.
 */
public class PedidoUtils {

    // ----------------------------------------------------- Regras de Negócio (Constantes)
    private static final double VALOR_POR_QUILO    = 7.50;
    private static final double FRETE_MINIMO       = 15.00;
    private static final double VALOR_FRETE_GRATIS = 300.00;
    private static final double TAXA_DESCONTO      = 0.10;
    private static final double DESCONTO_MAXIMO    = 50.00;

    // Construtor privado para evitar instanciação
    private PedidoUtils() {
    }

    // ----------------------------------------------------- Identificação
    /**
     * Gera o identificador visível do pedido no formato PED-2026-NNNNN.
     */
    public static String gerarNumeroDoPedido() {
        Random sorteio = new Random();
        int sequencial = sorteio.nextInt(100000); // Sorteia de 0 a 99999
        return String.format("PED-2026-%05d", sequencial);
    }

    // ----------------------------------------------------- Cálculos
    /**
     * Soma preço * quantidade de todos os itens do pedido.
     */
    public static double calcularSubtotal(double[] precos, int[] quantidades) {
        if (precos == null || quantidades == null || precos.length == 0) {
            return 0.0;
        }
        
        double subtotal = 0.0;
        for (int i = 0; i < precos.length; i++) {
            subtotal += precos[i] * quantidades[i];
        }
        return subtotal;
    }

    /**
     * Cobra por quilo iniciado (Math.ceil), respeita o frete mínimo (Math.max)
     * e zera acima do limite de frete grátis.
     */
    public static double calcularFrete(double pesoEmQuilos, double subtotal) {
        if (subtotal >= VALOR_FRETE_GRATIS || pesoEmQuilos <= 0) {
            return 0.0;
        }
        
        double quilosCobrados = Math.ceil(pesoEmQuilos);
        double freteCalculado = quilosCobrados * VALOR_POR_QUILO;
        
        return Math.max(freteCalculado, FRETE_MINIMO);
    }

    /**
     * Aplica a taxa de desconto sobre o subtotal, respeitando o teto máximo (Math.min).
     */
    public static double calcularDesconto(double subtotal) {
        if (subtotal <= 0) {
            return 0.0;
        }
        
        double descontoCalculado = subtotal * TAXA_DESCONTO;
        return Math.min(descontoCalculado, DESCONTO_MAXIMO);
    }

    // ----------------------------------------------------- Formatação
    /**
     * Devolve a linha do recibo alinhada em colunas.
     */
    public static String formatarLinhaDoRecibo(String nome, double preco, int quantidade) {
        return String.format("%-20s R$ %8.2f  x%3d", nome, preco, quantidade);
    }

    /**
     * Monta o recibo completo do pedido usando StringBuilder.
     */
    public static String montarRecibo(String[] produtos, double[] precos, int[] quantidades) {
        if (produtos == null || precos == null || quantidades == null) {
            return "";
        }

        StringBuilder recibo = new StringBuilder();
        recibo.append("=== RECIBO DO PEDIDO ===").append(System.lineSeparator());
        
        for (int i = 0; i < produtos.length; i++) {
            recibo.append(formatarLinhaDoRecibo(produtos[i], precos[i], quantidades[i]))
                  .append(System.lineSeparator());
        }
        
        return recibo.toString();
    }
}