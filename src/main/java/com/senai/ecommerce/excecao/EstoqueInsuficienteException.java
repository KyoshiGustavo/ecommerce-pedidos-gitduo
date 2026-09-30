package com.senai.ecommerce.excecao;

import com.senai.ecommerce.modelo.Produto;

public class EstoqueInsuficienteException extends ECommerceException {
    private final Produto produto;
    private final int quantidadeSolicitada;

    public EstoqueInsuficienteException(Produto produto, int quantidadeSolicitada) {
        super("Estoque insuficiente de " + produto.getNome() 
            + ": disponível " + produto.getQuantidadeEmEstoque() 
            + ", solicitado " + quantidadeSolicitada);
        this.produto = produto;
        this.quantidadeSolicitada = quantidadeSolicitada;
    }

    public Produto getProduto() {
        return produto;
    }

    public int getQuantidadeSolicitada() {
        return quantidadeSolicitada;
    }
}