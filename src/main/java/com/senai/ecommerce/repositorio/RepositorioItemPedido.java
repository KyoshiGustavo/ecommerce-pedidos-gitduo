package com.senai.ecommerce.repositorio;

import com.senai.ecommerce.modelo.ItemPedido;
import java.util.List;
import java.util.Optional;

public interface RepositorioItemPedido {
    ItemPedido inserir(Long pedidoId, ItemPedido item);
    Optional<ItemPedido> buscarPorId(Long id);
    List<ItemPedido> listarTodos();
    List<ItemPedido> listarPorPedido(Long pedidoId);
    void atualizar(ItemPedido item);
    void remover(Long id);
}
