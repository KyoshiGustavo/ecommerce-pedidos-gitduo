package com.senai.ecommerce.repositorio;

import com.senai.ecommerce.modelo.Pedido;
import java.util.List;

public interface RepositorioPedido extends Repositorio<Pedido> {
    List<Pedido> listarPorCliente(Long clienteId);
    List<Pedido> listarPorSituacao(String situacao);
}
