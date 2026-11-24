package com.senai.ecommerce.modelo;

import com.senai.ecommerce.util.Validador;
import java.math.BigDecimal;

public class ItemPedido {
    private Produto produto;
    private int quantidade;
    private BigDecimal precoPraticado;

    public ItemPedido(Produto produto, int quantidade) {
        this.produto = Validador.naoNulo(produto, "Produto");
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
        this.quantidade = Validador.quantidadePositiva(quantidade, "Quantidade do item");
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
