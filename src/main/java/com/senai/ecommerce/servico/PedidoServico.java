package com.senai.ecommerce.servico;

import com.senai.ecommerce.excecao.PedidoInvalidoException;
import com.senai.ecommerce.modelo.Cliente;
import com.senai.ecommerce.modelo.Pedido;
import com.senai.ecommerce.util.Mensagens;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Criação e consulta de pedidos em memória, com numeração sequencial. */
public class PedidoServico {
    private final Map<Integer, Pedido> pedidos = new LinkedHashMap<>();
    private int proximoNumero = 1;

    public Pedido criar(Cliente cliente) {
        Pedido pedido = new Pedido(proximoNumero, cliente); // valida o cliente antes de consumir o número
        pedidos.put(proximoNumero, pedido);
        proximoNumero++;
        return pedido;
    }

    public Pedido buscar(int numero) throws PedidoInvalidoException {
        Pedido pedido = pedidos.get(numero);
        if (pedido == null) {
            throw new PedidoInvalidoException(numero, Mensagens.PEDIDO_INEXISTENTE);
        }
        return pedido;
    }

    public List<Pedido> listar() {
        return Collections.unmodifiableList(new ArrayList<>(pedidos.values()));
    }
}
