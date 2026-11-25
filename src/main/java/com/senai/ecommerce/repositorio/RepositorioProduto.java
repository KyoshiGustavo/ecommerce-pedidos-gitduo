package com.senai.ecommerce.repositorio;

import com.senai.ecommerce.modelo.Produto;
import java.util.Optional;

public interface RepositorioProduto extends Repositorio<Produto> {
    Optional<Produto> buscarPorNome(String nome);
    void atualizarEstoque(Long id, int quantidade);
    void inativar(Long id);
    void reativar(Long id);
}
