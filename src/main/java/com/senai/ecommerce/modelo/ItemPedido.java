package com.senai.ecommerce.modelo;

import java.math.BigDecimal;

public class ItemPedido {
    private final Long id;
    private final Produto produto;
    private int quantidade;
    private final BigDecimal precoPraticado;

    public ItemPedido(Produto produto, int quantidade) {
        this(null, produto, quantidade, produto == null ? null : produto.getPreco());
    }

    public ItemPedido(Long id, Produto produto, int quantidade, BigDecimal precoPraticado) {
        if (id != null && id <= 0) {
            throw new IllegalArgumentException("Id do item deve ser positivo.");
        }
        if (produto == null) {
            throw new IllegalArgumentException("Produto é obrigatório.");
        }
        if (precoPraticado == null || precoPraticado.signum() <= 0) {
            throw new IllegalArgumentException("Preço praticado deve ser maior que zero.");
        }
        this.id = id;
        this.produto = produto;
        setQuantidade(quantidade);
        this.precoPraticado = precoPraticado;
    }

    public Long getId() {
        return id;
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