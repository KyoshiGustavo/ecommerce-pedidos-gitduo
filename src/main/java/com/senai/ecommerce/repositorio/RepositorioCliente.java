package com.senai.ecommerce.repositorio;

import com.senai.ecommerce.modelo.Cliente;
import java.util.Optional;

public interface RepositorioCliente extends Repositorio<Cliente> {
    Optional<Cliente> buscarPorEmail(String email);
    void atualizarEmail(Long id, String email);
}
