package com.senai.ecommerce.persistencia;

import com.senai.ecommerce.modelo.Cliente;
import com.senai.ecommerce.modelo.Pedido;
import com.senai.ecommerce.repositorio.RepositorioException;
import com.senai.ecommerce.repositorio.RepositorioPedido;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class PedidoDAO implements RepositorioPedido {
    private static final String ABERTO = "ABERTO";
    private static final String FINALIZADO = "FINALIZADO";
    private static final String PAGO = "PAGO";
    private static final String CONSULTA = """
        SELECT p.id, p.numero, p.situacao, p.comprovante,
               c.id AS cliente_id, c.nome, c.documento, c.email
        FROM pedido p JOIN cliente c ON c.id = p.cliente_id
        """;
    private static final String NUMERO_DUPLICADO = "Já existe um pedido com esse número.";
    private static final String CLIENTE_INEXISTENTE = "O cliente do pedido não está cadastrado.";

    private final Jdbc jdbc;

    public PedidoDAO(ConexaoFactory conexoes) {
        jdbc = new Jdbc(conexoes);
    }

    @Override
    public Pedido salvar(Pedido pedido) {
        Objects.requireNonNull(pedido, "Pedido é obrigatório.");
        if (pedido.getId() != null) {
            return jdbc.transacao("Não foi possível salvar o pedido.", conexao -> {
                atualizar(pedido, pedido.getId(), conexao);
                return buscarPorId(pedido.getId(), conexao).orElseThrow(() -> inexistente(pedido.getId()));
            });
        }
        if (!ABERTO.equals(pedido.getStatus()) || !pedido.getItens().isEmpty()) {
            throw new IllegalArgumentException("Use a finalização para gravar um pedido novo com itens.");
        }
        return jdbc.usar("Não foi possível criar o rascunho.", conexao -> {
            long id = inserir(pedido, ABERTO, conexao);
            return Pedido.reconstituir(id, pedido.getNumero(), pedido.getCliente(), ABERTO, null, List.of());
        });
    }

    @Override
    public void atualizar(Pedido pedido) {
        Objects.requireNonNull(pedido, "Pedido é obrigatório.");
        long id = Jdbc.idObrigatorio(pedido.getId(), "O pedido");
        jdbc.transacao("Não foi possível atualizar o pedido.", conexao -> {
            atualizar(pedido, id, conexao);
            return null;
        });
    }

    private static void atualizar(Pedido pedido, long id, Connection conexao) throws SQLException {
        long clienteId = Jdbc.idObrigatorio(pedido.getCliente().getId(), "O cliente");
        String atual = situacaoBloqueada(id, conexao);
        try {
            int linhas;
            if (ABERTO.equals(atual) && ABERTO.equals(pedido.getStatus())) {
                linhas = Jdbc.alterar(conexao,
                    "UPDATE pedido SET numero = ?, cliente_id = ? WHERE id = ? AND situacao = 'ABERTO'",
                    pedido.getNumero(), clienteId, id);
            } else if (FINALIZADO.equals(atual) && PAGO.equals(pedido.getStatus())) {
                linhas = Jdbc.alterar(conexao,
                    "UPDATE pedido SET situacao = 'PAGO', comprovante = ? WHERE id = ? AND situacao = 'FINALIZADO'",
                    comprovanteObrigatorio(pedido), id);
            } else {
                throw new RepositorioException("O pedido não permite esta alteração na situação atual.");
            }
            Jdbc.exigirUmaLinha(linhas, "O pedido " + id + " não foi alterado.");
        } catch (SQLException e) {
            throw Jdbc.traduzirIntegridade(e, NUMERO_DUPLICADO, CLIENTE_INEXISTENTE);
        }
    }

    @Override
    public void remover(Long id) {
        long chave = Jdbc.idObrigatorio(id, "O pedido");
        jdbc.transacao("Não foi possível excluir o pedido.", conexao -> {
            exigirAberto(chave, conexao);
            Jdbc.exigirUmaLinha(Jdbc.alterar(conexao,
                "DELETE FROM pedido WHERE id = ? AND situacao = 'ABERTO'", chave),
                "O pedido " + chave + " não foi excluído.");
            return null;
        });
    }

    @Override
    public Optional<Pedido> buscarPorId(Long id) {
        long chave = Jdbc.idObrigatorio(id, "O pedido");
        return jdbc.usar("Não foi possível consultar o pedido.", conexao -> buscarPorId(chave, conexao));
    }

    @Override
    public List<Pedido> listarTodos() {
        return listar("");
    }

    @Override
    public List<Pedido> listarPorCliente(Long clienteId) {
        long chave = Jdbc.idObrigatorio(clienteId, "O cliente");
        return listar(" WHERE p.cliente_id = ?", chave);
    }

    @Override
    public List<Pedido> listarPorSituacao(String situacao) {
        if (!ABERTO.equals(situacao) && !FINALIZADO.equals(situacao) && !PAGO.equals(situacao)) {
            throw new IllegalArgumentException("Situação deve ser ABERTO, FINALIZADO ou PAGO.");
        }
        return listar(" WHERE p.situacao = ?", situacao);
    }

    private List<Pedido> listar(String filtro, Object... parametros) {
        return jdbc.usar("Não foi possível listar os pedidos.", conexao -> {
            List<Cabecalho> cabecalhos = Jdbc.listar(conexao, CONSULTA + filtro + " ORDER BY p.id",
                PedidoDAO::mapearCabecalho, parametros);
            List<Pedido> pedidos = new ArrayList<>();
            for (Cabecalho cabecalho : cabecalhos) {
                pedidos.add(reconstituir(cabecalho, conexao));
            }
            return pedidos;
        });
    }

    Optional<Pedido> buscarPorId(long id, Connection conexao) throws SQLException {
        Optional<Cabecalho> cabecalho = Jdbc.buscar(conexao, CONSULTA + " WHERE p.id = ?",
            PedidoDAO::mapearCabecalho, id);
        if (cabecalho.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(reconstituir(cabecalho.get(), conexao));
    }

    long inserir(Pedido pedido, String situacao, Connection conexao) throws SQLException {
        long cliente = Jdbc.idObrigatorio(pedido.getCliente().getId(), "O cliente");
        try {
            return Jdbc.inserir(conexao,
                "INSERT INTO pedido (numero, cliente_id, situacao, comprovante) VALUES (?, ?, ?, ?)",
                pedido.getNumero(), cliente, situacao, PAGO.equals(situacao) ? comprovanteObrigatorio(pedido) : null);
        } catch (SQLException e) {
            throw Jdbc.traduzirIntegridade(e, NUMERO_DUPLICADO, CLIENTE_INEXISTENTE);
        }
    }

    void marcarFinalizado(long id, Connection conexao) throws SQLException {
        int linhas = Jdbc.alterar(conexao,
            "UPDATE pedido SET situacao = 'FINALIZADO' WHERE id = ? AND situacao = 'ABERTO'", id);
        Jdbc.exigirUmaLinha(linhas, "O pedido " + id + " não pôde ser finalizado.");
    }

    static void exigirAberto(long id, Connection conexao) throws SQLException {
        if (!ABERTO.equals(situacaoBloqueada(id, conexao))) {
            throw new RepositorioException("O pedido " + id + " já está finalizado ou pago e está protegido.");
        }
    }

    private static String situacaoBloqueada(long id, Connection conexao) throws SQLException {
        return Jdbc.buscar(conexao, "SELECT situacao FROM pedido WHERE id = ? FOR UPDATE",
            linha -> linha.getString("situacao"), id).orElseThrow(() -> inexistente(id));
    }

    private static RepositorioException inexistente(long id) {
        return new RepositorioException("Pedido " + id + " não encontrado.");
    }

    private static String comprovanteObrigatorio(Pedido pedido) {
        String comprovante = pedido.getComprovante();
        if (comprovante == null || comprovante.isBlank()) {
            throw new IllegalArgumentException("Pedido pago precisa de comprovante.");
        }
        return comprovante;
    }

    private static Pedido reconstituir(Cabecalho cabecalho, Connection conexao) throws SQLException {
        return Pedido.reconstituir(cabecalho.id(), cabecalho.numero(), cabecalho.cliente(), cabecalho.situacao(),
            cabecalho.comprovante(), ItemPedidoDAO.listarPorPedido(cabecalho.id(), conexao));
    }

    private static Cabecalho mapearCabecalho(ResultSet linha) throws SQLException {
        Cliente cliente = new Cliente(linha.getLong("cliente_id"), linha.getString("nome"),
            linha.getString("documento"), linha.getString("email"));
        return new Cabecalho(linha.getLong("id"), linha.getInt("numero"), cliente,
            linha.getString("situacao"), linha.getString("comprovante"));
    }

    private record Cabecalho(long id, int numero, Cliente cliente, String situacao, String comprovante) {}
}
