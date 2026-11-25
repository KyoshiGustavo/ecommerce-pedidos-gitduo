package com.senai.ecommerce.repositorio;

import java.util.List;
import java.util.Optional;

public interface Repositorio<T> {
    T salvar(T entidade);
    void atualizar(T entidade);
    void remover(Long id);
    Optional<T> buscarPorId(Long id);
    List<T> listarTodos();
}
