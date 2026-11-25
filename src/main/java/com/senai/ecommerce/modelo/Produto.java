package com.senai.ecommerce.modelo;

import com.senai.ecommerce.excecao.EstoqueInsuficienteException;
import java.math.BigDecimal;

public class Produto {
    private final Long id;
    private String nome;
    private BigDecimal preco;
    private int quantidadeEmEstoque;

    public Produto(String nome, BigDecimal preco, int quantidadeEmEstoque) {
        this(null, nome, preco, quantidadeEmEstoque);
    }

    public Produto(Long id, String nome, BigDecimal preco, int quantidadeEmEstoque) {
        if (id != null && id <= 0) {
            throw new IllegalArgumentException("Id do produto deve ser positivo.");
        }
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome do produto é obrigatório.");
        }
        if (preco == null || preco.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Preço deve ser maior que zero.");
        }
        if (quantidadeEmEstoque < 0) {
            throw new IllegalArgumentException("Estoque não pode ser negativo.");
        }
        this.id = id;
        this.nome = nome;
        this.preco = preco;
        this.quantidadeEmEstoque = quantidadeEmEstoque;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public int getQuantidadeEmEstoque() {
        return quantidadeEmEstoque;
    }

    public void baixarEstoque(int quantidade) throws EstoqueInsuficienteException {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero.");
        }
        if (quantidade > this.quantidadeEmEstoque) {
            throw new EstoqueInsuficienteException(this, quantidade);
        }
        this.quantidadeEmEstoque -= quantidade;
    }
    public void reporEstoque(int quantidade) {
    if (quantidade <= 0) {
        throw new IllegalArgumentException("A quantidade a repor deve ser maior que zero.");
    }
    this.quantidadeEmEstoque += quantidade;
}
}