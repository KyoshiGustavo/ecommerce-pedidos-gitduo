package com.senai.ecommerce.repositorio;

import com.senai.ecommerce.excecao.EstoqueInsuficienteException;
import com.senai.ecommerce.modelo.Pedido;

public interface FinalizadorPedido {
    Pedido finalizar(Pedido pedido) throws EstoqueInsuficienteException;
}
