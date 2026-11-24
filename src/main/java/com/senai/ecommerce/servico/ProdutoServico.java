package com.senai.ecommerce.servico;

import com.senai.ecommerce.excecao.CadastroDuplicadoException;
import com.senai.ecommerce.modelo.Produto;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Cadastro de produtos em memória (a persistência em banco entra nas Aulas 12 e 13).
 * Camada de serviço: detecta duplicidade e lança; quem decide o que dizer ao usuário é a apresentação.
 */
public class ProdutoServico {
    private final Map<String, Produto> produtos = new LinkedHashMap<>();

    /** Cria o produto (o construtor valida tudo) e recusa nome repetido. */
    public Produto cadastrar(String nome, BigDecimal preco, int estoque) throws CadastroDuplicadoException {
        Produto novo = new Produto(nome, preco, estoque);
        String chave = chave(novo.getNome());
        if (produtos.containsKey(chave)) {
            throw new CadastroDuplicadoException("Produto", novo.getNome());
        }
        produtos.put(chave, novo);
        return novo;
    }

    /** Busca que pode legitimamente não achar nada devolve Optional, não exceção. */
    public Optional<Produto> buscarPorNome(String nome) {
        if (nome == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(produtos.get(chave(nome)));
    }

    public List<Produto> listar() {
        return Collections.unmodifiableList(new ArrayList<>(produtos.values()));
    }

    /** Produtos (exceto o informado) com estoque suficiente para a quantidade: base da sugestão de similar. */
    public List<Produto> comEstoqueMinimo(int quantidade, Produto excluir) {
        List<Produto> disponiveis = new ArrayList<>();
        for (Produto produto : produtos.values()) {
            if (produto != excluir && produto.getQuantidadeEmEstoque() >= quantidade) {
                disponiveis.add(produto);
            }
        }
        return disponiveis;
    }

    private String chave(String nome) {
        return nome.trim().toLowerCase(Locale.ROOT);
    }
}
