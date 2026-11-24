package com.senai.ecommerce.servico;

import com.senai.ecommerce.excecao.CadastroDuplicadoException;
import com.senai.ecommerce.excecao.ClienteNaoEncontradoException;
import com.senai.ecommerce.modelo.Cliente;
import com.senai.ecommerce.util.Validador;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Cadastro de clientes em memória. O identificador é o documento (CPF/CNPJ),
 * comparado só pelos dígitos: "529.982.247-25" e "52998224725" são o mesmo cliente.
 */
public class ClienteServico {
    private final Map<String, Cliente> clientes = new LinkedHashMap<>();

    public Cliente cadastrar(String nome, String documento, String email) throws CadastroDuplicadoException {
        Cliente novo = new Cliente(nome, documento, email);
        String chave = Validador.somenteDigitos(novo.getDocumento());
        if (clientes.containsKey(chave)) {
            throw new CadastroDuplicadoException("Cliente", novo.getDocumento());
        }
        clientes.put(chave, novo);
        return novo;
    }

    public Cliente buscarPorDocumento(String documento) throws ClienteNaoEncontradoException {
        Cliente encontrado = clientes.get(Validador.somenteDigitos(documento));
        if (encontrado == null) {
            throw new ClienteNaoEncontradoException(String.valueOf(documento));
        }
        return encontrado;
    }

    public List<Cliente> listar() {
        return Collections.unmodifiableList(new ArrayList<>(clientes.values()));
    }
}
