package com.senai.ecommerce.persistencia;

import com.senai.ecommerce.modelo.Cliente;
import com.senai.ecommerce.repositorio.RepositorioCliente;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class ClienteDAO implements RepositorioCliente {
    private static final String CONSULTA = "SELECT id, nome, documento, email FROM cliente";
    private static final String DUPLICADO = "Já existe um cliente com esse documento ou e-mail.";
    private final Jdbc jdbc;

    public ClienteDAO(ConexaoFactory conexoes) {
        jdbc = new Jdbc(conexoes);
    }

    @Override
    public Cliente salvar(Cliente cliente) {
        Objects.requireNonNull(cliente, "Cliente é obrigatório.");
        if (cliente.getId() != null) {
            atualizar(cliente);
            return cliente;
        }
        return jdbc.usar("Não foi possível cadastrar o cliente.", conexao -> {
            try {
                long id = Jdbc.inserir(conexao, "INSERT INTO cliente (nome, documento, email) VALUES (?, ?, ?)",
                    cliente.getNome(), cliente.getDocumento(), cliente.getEmail());
                return new Cliente(id, cliente.getNome(), cliente.getDocumento(), cliente.getEmail());
            } catch (SQLException e) {
                throw Jdbc.traduzirIntegridade(e, DUPLICADO, null);
            }
        });
    }

    @Override
    public void atualizar(Cliente cliente) {
        Objects.requireNonNull(cliente, "Cliente é obrigatório.");
        long id = Jdbc.idObrigatorio(cliente.getId(), "O cliente");
        atualizar(id, "UPDATE cliente SET nome = ?, documento = ?, email = ? WHERE id = ?",
            cliente.getNome(), cliente.getDocumento(), cliente.getEmail(), id);
    }

    @Override
    public void atualizarEmail(Long id, String email) {
        long chave = Jdbc.idObrigatorio(id, "O cliente");
        String normalizado = Cliente.validarEmail(email);
        atualizar(chave, "UPDATE cliente SET email = ? WHERE id = ?", normalizado, chave);
    }

    private void atualizar(long id, String sql, Object... parametros) {
        jdbc.usar("Não foi possível atualizar o cliente.", conexao -> {
            try {
                Jdbc.exigirUmaLinha(Jdbc.alterar(conexao, sql, parametros), inexistente(id));
                return null;
            } catch (SQLException e) {
                throw Jdbc.traduzirIntegridade(e, DUPLICADO, null);
            }
        });
    }

    @Override
    public void remover(Long id) {
        long chave = Jdbc.idObrigatorio(id, "O cliente");
        jdbc.usar("Não foi possível excluir o cliente.", conexao -> {
            try {
                Jdbc.exigirUmaLinha(Jdbc.alterar(conexao, "DELETE FROM cliente WHERE id = ?", chave),
                    inexistente(chave));
                return null;
            } catch (SQLException e) {
                throw Jdbc.traduzirIntegridade(e, null,
                    "O cliente " + chave + " tem pedidos e não pode ser excluído.");
            }
        });
    }

    @Override
    public Optional<Cliente> buscarPorId(Long id) {
        long chave = Jdbc.idObrigatorio(id, "O cliente");
        return jdbc.usar("Não foi possível consultar o cliente.", conexao ->
            Jdbc.buscar(conexao, CONSULTA + " WHERE id = ?", ClienteDAO::mapear, chave));
    }

    @Override
    public Optional<Cliente> buscarPorEmail(String email) {
        String normalizado = Cliente.validarEmail(email);
        return jdbc.usar("Não foi possível consultar o cliente.", conexao ->
            Jdbc.buscar(conexao, CONSULTA + " WHERE email = ?", ClienteDAO::mapear, normalizado));
    }

    @Override
    public List<Cliente> listarTodos() {
        return jdbc.usar("Não foi possível listar os clientes.", conexao ->
            Jdbc.listar(conexao, CONSULTA + " ORDER BY id", ClienteDAO::mapear));
    }

    private static String inexistente(long id) {
        return "Cliente " + id + " não encontrado.";
    }

    private static Cliente mapear(ResultSet linha) throws SQLException {
        return new Cliente(linha.getLong("id"), linha.getString("nome"), linha.getString("documento"),
            linha.getString("email"));
    }
}
