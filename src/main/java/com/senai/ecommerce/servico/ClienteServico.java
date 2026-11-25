package com.senai.ecommerce.servico;

import com.senai.ecommerce.modelo.Cliente;
import com.senai.ecommerce.repositorio.RepositorioCliente;
import com.senai.ecommerce.repositorio.RepositorioException;

public final class ClienteServico {
    private final RepositorioCliente clientes;

    public ClienteServico(RepositorioCliente clientes) {
        this.clientes = clientes;
    }

    public Cliente cadastrar(Cliente cliente) {
        if (cliente.getId() != null) {
            throw new IllegalArgumentException("O cliente já tem id. Use a atualização.");
        }
        if (clientes.buscarPorEmail(cliente.getEmail()).isPresent()) {
            throw new RepositorioException("Já existe um cliente com esse e-mail.");
        }
        return clientes.salvar(cliente);
    }
}
