package com.senai.ecommerce.persistencia;

import com.senai.ecommerce.repositorio.RepositorioException;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

final class Jdbc {
    private static final BigDecimal PRECO_MAXIMO = new BigDecimal("9999999999999.99");

    @FunctionalInterface
    interface Operacao<T, E extends Exception> {
        T executar(Connection conexao) throws SQLException, E;
    }

    @FunctionalInterface
    interface Mapeador<T> {
        T mapear(ResultSet linha) throws SQLException;
    }

    private final ConexaoFactory conexoes;

    Jdbc(ConexaoFactory conexoes) {
        this.conexoes = conexoes;
    }

    <T> T usar(String acao, Operacao<T, RuntimeException> operacao) {
        try (Connection conexao = conexoes.abrir()) {
            return operacao.executar(conexao);
        } catch (SQLException e) {
            throw new RepositorioException(acao, e);
        }
    }

    <T, E extends Exception> T transacao(String acao, Operacao<T, E> operacao) throws E {
        try (Connection conexao = conexoes.abrir()) {
            boolean autoCommitAnterior = conexao.getAutoCommit();
            conexao.setAutoCommit(false);
            Throwable falha = null;
            boolean transacaoEncerrada = false;
            try {
                T resultado = operacao.executar(conexao);
                conexao.commit();
                transacaoEncerrada = true;
                return resultado;
            } catch (Exception | Error e) {
                falha = e;
                try {
                    conexao.rollback();
                    transacaoEncerrada = true;
                } catch (SQLException erroRollback) {
                    e.addSuppressed(erroRollback);
                }
                throw e;
            } finally {
                // Ligar o autocommit após um rollback malsucedido poderia confirmar dados parciais.
                // Nesse caso o try-with-resources fecha a conexão sem restaurá-lo.
                if (transacaoEncerrada) {
                    try {
                        conexao.setAutoCommit(autoCommitAnterior);
                    } catch (SQLException erroRestauracao) {
                        if (falha == null) {
                            throw erroRestauracao;
                        }
                        falha.addSuppressed(erroRestauracao);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RepositorioException(acao, e);
        }
    }

    static long idObrigatorio(Long id, String entidade) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(entidade + " precisa de um id positivo.");
        }
        return id;
    }

    static BigDecimal precoMonetario(BigDecimal preco) {
        BigDecimal semZeros = preco.stripTrailingZeros();
        if (semZeros.scale() > 2 || preco.compareTo(PRECO_MAXIMO) > 0) {
            throw new IllegalArgumentException("Informe um preço de até 13 dígitos inteiros e 2 casas decimais.");
        }
        return preco.setScale(2);
    }

    static int alterar(Connection conexao, String sql, Object... parametros) throws SQLException {
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            preencher(ps, parametros);
            return ps.executeUpdate();
        }
    }

    static void exigirUmaLinha(int linhas, String semRegistro) {
        if (linhas == 0) {
            throw new RepositorioException(semRegistro);
        }
        if (linhas != 1) {
            throw new RepositorioException("A operação deveria afetar um único registro.");
        }
    }

    static long inserir(Connection conexao, String sql, Object... parametros) throws SQLException {
        try (PreparedStatement ps = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preencher(ps, parametros);
            exigirUmaLinha(ps.executeUpdate(), "A inclusão não gravou nenhum registro.");
            try (ResultSet chaves = ps.getGeneratedKeys()) {
                if (!chaves.next()) {
                    throw new SQLException("Identificador gerado indisponível.");
                }
                return chaves.getLong(1);
            }
        }
    }

    static <T> Optional<T> buscar(Connection conexao, String sql, Mapeador<T> mapear, Object... parametros)
            throws SQLException {
        return listar(conexao, sql, mapear, parametros).stream().findFirst();
    }

    static <T> List<T> listar(Connection conexao, String sql, Mapeador<T> mapear, Object... parametros)
            throws SQLException {
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            preencher(ps, parametros);
            try (ResultSet resultado = ps.executeQuery()) {
                List<T> registros = new ArrayList<>();
                while (resultado.next()) {
                    registros.add(mapear.mapear(resultado));
                }
                return registros;
            }
        }
    }

    static RepositorioException traduzirIntegridade(SQLException e, String duplicado, String vinculo)
            throws SQLException {
        String estado = e.getSQLState();
        int codigo = e.getErrorCode();
        if (duplicado != null && (codigo == 1062 || "23505".equals(estado))) {
            return new RepositorioException(duplicado, e);
        }
        if (vinculo != null && (codigo == 1451 || codigo == 1452
                || "23503".equals(estado) || "23506".equals(estado))) {
            return new RepositorioException(vinculo, e);
        }
        throw e;
    }

    private static void preencher(PreparedStatement ps, Object[] parametros) throws SQLException {
        for (int i = 0; i < parametros.length; i++) {
            ps.setObject(i + 1, parametros[i]);
        }
    }
}
