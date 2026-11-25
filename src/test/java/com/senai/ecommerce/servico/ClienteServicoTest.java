package com.senai.ecommerce.servico;

import com.senai.ecommerce.modelo.Cliente;
import com.senai.ecommerce.repositorio.RepositorioCliente;
import com.senai.ecommerce.repositorio.RepositorioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ClienteServicoTest {
    @Test
    @DisplayName("Serviço detecta e-mail duplicado sem abrir banco")
    void deveRecusarEmailDuplicadoSemBanco() {
        var repositorio = new RepositorioClienteEmMemoria();
        var servico = new ClienteServico(repositorio);
        Cliente original = servico.cadastrar(new Cliente("Ana", "123", "ana@exemplo.com"));

        RepositorioException erro = assertThrows(RepositorioException.class,
            () -> servico.cadastrar(new Cliente("Bia", "456", "ana@exemplo.com")));

        assertTrue(erro.getMessage().contains("e-mail"));
        assertEquals(List.of(original), repositorio.listarTodos());
    }

    private static final class RepositorioClienteEmMemoria implements RepositorioCliente {
        private final List<Cliente> clientes = new ArrayList<>();

        @Override
        public Cliente salvar(Cliente cliente) {
            clientes.add(cliente);
            return cliente;
        }

        @Override
        public Optional<Cliente> buscarPorEmail(String email) {
            return clientes.stream().filter(cliente -> cliente.getEmail().equals(email)).findFirst();
        }

        @Override
        public List<Cliente> listarTodos() {
            return List.copyOf(clientes);
        }

        @Override
        public Optional<Cliente> buscarPorId(Long id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void atualizar(Cliente cliente) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void atualizarEmail(Long id, String email) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void remover(Long id) {
            throw new UnsupportedOperationException();
        }
    }
}
