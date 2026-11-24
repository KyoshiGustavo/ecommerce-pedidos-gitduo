package com.senai.ecommerce.modelo;

import com.senai.ecommerce.excecao.EstoqueInsuficienteException;
import com.senai.ecommerce.util.Mensagens;
import com.senai.ecommerce.util.Validador;
import java.math.BigDecimal;

public class Produto {
    private String nome;
    private BigDecimal preco;
    private int quantidadeEmEstoque;

    public Produto(String nome, BigDecimal preco, int quantidadeEmEstoque) {
        this.nome = Validador.textoObrigatorio(nome, "Nome do produto");
        this.preco = Validador.valorPositivo(preco, "Preço");
        this.quantidadeEmEstoque = Validador.estoqueNaoNegativo(quantidadeEmEstoque, "Estoque");
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

    /**
     * Detecta e lança; quem decide o que fazer é a camada de cima.
     * Quantidade inválida é defeito de quem chamou (unchecked);
     * estoque insuficiente é situação de negócio prevista (checked).
     */
    public void baixarEstoque(int quantidade) throws EstoqueInsuficienteException {
        Validador.quantidadePositiva(quantidade, "Quantidade");
        if (quantidade > this.quantidadeEmEstoque) {
            throw new EstoqueInsuficienteException(this, quantidade);
        }
        this.quantidadeEmEstoque -= quantidade;
    }

    public void reporEstoque(int quantidade) {
        Validador.quantidadePositiva(quantidade, "Quantidade a repor");
        int capacidadeRestante = Integer.MAX_VALUE - this.quantidadeEmEstoque;
        if (quantidade > capacidadeRestante) {
            throw new IllegalArgumentException(
                    String.format(Mensagens.REPOSICAO_EXCEDE_LIMITE, quantidade, capacidadeRestante));
        }
        this.quantidadeEmEstoque += quantidade;
    }

    @Override
    public String toString() {
        return nome + " — R$ " + preco.toPlainString() + " — estoque: " + quantidadeEmEstoque;
    }
}
