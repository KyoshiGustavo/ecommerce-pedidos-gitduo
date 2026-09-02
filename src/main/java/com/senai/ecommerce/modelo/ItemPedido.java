package com.senai.ecommerce.modelo;

public class ItemPedido {
    private Produto produto;
    private int quantidade;
    private double precoPraticado; // Regra de negócio: grava o preço no momento da compra

    public ItemPedido(Produto produto, int quantidade) {
        this.produto = produto;
        this.quantidade = quantidade;
        this.precoPraticado = produto.getPreco(); // Pega o preço atual do produto
    }

    public Produto getProduto() {
        return produto;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    public double getPrecoPraticado() {
        return precoPraticado;
    }

    // Método de negócio
    public double calcularSubtotal() {
        return precoPraticado * quantidade;
    }

    @Override
    public String toString() {
        return String.format("%s x %d = R$ %.2f", produto.getNome(), quantidade, calcularSubtotal());
    }
}