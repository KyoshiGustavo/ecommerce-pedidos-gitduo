package com.senai.ecommerce.modelo;

import java.math.BigDecimal;

public class ItemPedido {
    private Produto produto;
    private int quantidade;
    private BigDecimal precoPraticado;

    public ItemPedido(Produto produto, int quantidade) {
        if (produto == null) {
            throw new IllegalArgumentException("Produto é obrigatório.");
        }
        this.produto = produto;
        setQuantidade(quantidade);
        this.precoPraticado = produto.getPreco();
    }

    public Produto getProduto() {
        return produto;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade do item deve ser maior que zero.");
        }
        this.quantidade = quantidade;
    }

    public BigDecimal getPrecoPraticado() {
        return precoPraticado;
    }

    public BigDecimal getSubtotal() {
        return precoPraticado.multiply(BigDecimal.valueOf(quantidade));
    }

    @Override
    public String toString() {
        return produto.getNome() + " x " + quantidade + " = R$ " + getSubtotal();
    }
}