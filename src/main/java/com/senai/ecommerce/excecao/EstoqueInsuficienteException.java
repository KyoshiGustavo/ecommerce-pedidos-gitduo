package com.senai.ecommerce.excecao;

import com.senai.ecommerce.modelo.Produto;

/**
 * Quantidade pedida maior que a disponível (checked).
 * Carrega o produto e as quantidades para que quem trata possa sugerir alternativas.
 */
public class EstoqueInsuficienteException extends ECommerceException {
    private final Produto produto;
    private final int quantidadeSolicitada;
    private final int quantidadeDisponivel;

    public EstoqueInsuficienteException(Produto produto, int quantidadeSolicitada) {
        super("Estoque insuficiente de " + produto.getNome()
            + ": disponível " + produto.getQuantidadeEmEstoque()
            + ", solicitado " + quantidadeSolicitada + ". "
            + (produto.getQuantidadeEmEstoque() > 0
                ? "Peça no máximo " + produto.getQuantidadeEmEstoque() + " unidade(s) ou escolha outro produto."
                : "Produto esgotado; escolha outro produto."));
        this.produto = produto;
        this.quantidadeSolicitada = quantidadeSolicitada;
        this.quantidadeDisponivel = produto.getQuantidadeEmEstoque();
    }

    public Produto getProduto() {
        return produto;
    }

    public int getQuantidadeSolicitada() {
        return quantidadeSolicitada;
    }

    public int getQuantidadeDisponivel() {
        return quantidadeDisponivel;
    }
}
