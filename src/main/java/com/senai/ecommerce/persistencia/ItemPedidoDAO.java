package com.senai.ecommerce.persistencia;

import com.senai.ecommerce.modelo.ItemPedido;
import com.senai.ecommerce.modelo.Produto;
import com.senai.ecommerce.repositorio.RepositorioException;
import com.senai.ecommerce.repositorio.RepositorioItemPedido;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class ItemPedidoDAO implements RepositorioItemPedido {
    // Este JOIN é uma leitura histórica. Um produto inativado continua pertencendo ao pedido.
    private static final String CONSULTA = """
        SELECT i.id, i.quantidade, i.preco_praticado,
               pr.id AS produto_id, pr.nome, pr.preco, pr.quantidade AS estoque
        FROM item_pedido i JOIN produto pr ON pr.id = i.produto_id
        """;
    private static final String INSERIR_DADOS = """
        INSERT INTO item_pedido (pedido_id, produto_id, quantidade, preco_praticado)
        VALUES (?, ?, ?, ?)
        """;

    private final Jdbc jdbc;

    public ItemPedidoDAO(ConexaoFactory conexoes) {
        jdbc = new Jdbc(conexoes);
    }

    @Override
    public ItemPedido inserir(Long pedidoId, ItemPedido item) {
        long pedido = Jdbc.idObrigatorio(pedidoId, "O pedido");
        Objects.requireNonNull(item, "Item é obrigatório.");
        if (item.getId() != null) {
            throw new IllegalArgumentException("O item já tem id. Use a atualização.");
        }
        long produto = Jdbc.idObrigatorio(item.getProduto().getId(), "O produto");
        return jdbc.transacao("Não foi possível incluir o item.", conexao -> {
            PedidoDAO.exigirAberto(pedido, conexao);
            Produto atual = ProdutoDAO.buscarAtivoBloqueado(produto, conexao);
            ItemPedido novo = new ItemPedido(atual, item.getQuantidade());
            long id = inserir(pedido, novo, conexao);
            return new ItemPedido(id, atual, novo.getQuantidade(), novo.getPrecoPraticado());
        });
    }

    long inserir(long pedidoId, ItemPedido item, Connection conexao) throws SQLException {
        long produto = Jdbc.idObrigatorio(item.getProduto().getId(), "O produto");
        try {
            return Jdbc.inserir(conexao, INSERIR_DADOS, pedidoId, produto, item.getQuantidade(),
                Jdbc.precoMonetario(item.getPrecoPraticado()));
        } catch (SQLException e) {
            throw Jdbc.traduzirIntegridade(e, null, "O pedido ou o produto do item não está cadastrado.");
        }
    }

    @Override
    public void atualizar(ItemPedido item) {
        Objects.requireNonNull(item, "Item é obrigatório.");
        long id = Jdbc.idObrigatorio(item.getId(), "O item");
        alterarItem(id, "UPDATE item_pedido SET quantidade = ? WHERE id = ?", item.getQuantidade(), id);
    }

    @Override
    public void remover(Long id) {
        long chave = Jdbc.idObrigatorio(id, "O item");
        alterarItem(chave, "DELETE FROM item_pedido WHERE id = ?", chave);
    }

    private void alterarItem(long id, String sql, Object... parametros) {
        jdbc.transacao("Não foi possível alterar o item do pedido.", conexao -> {
            long pedido = Jdbc.buscar(conexao, "SELECT pedido_id FROM item_pedido WHERE id = ?",
                linha -> linha.getLong("pedido_id"), id).orElseThrow(() -> new RepositorioException(mensagemInexistente(id)));
            PedidoDAO.exigirAberto(pedido, conexao);
            Jdbc.exigirUmaLinha(Jdbc.alterar(conexao, sql, parametros), mensagemInexistente(id));
            return null;
        });
    }

    @Override
    public Optional<ItemPedido> buscarPorId(Long id) {
        long chave = Jdbc.idObrigatorio(id, "O item");
        return jdbc.usar("Não foi possível consultar o item.", conexao ->
            Jdbc.buscar(conexao, CONSULTA + " WHERE i.id = ?", ItemPedidoDAO::mapear, chave));
    }

    @Override
    public List<ItemPedido> listarTodos() {
        return jdbc.usar("Não foi possível listar os itens.", conexao ->
            Jdbc.listar(conexao, CONSULTA + " ORDER BY i.id", ItemPedidoDAO::mapear));
    }

    @Override
    public List<ItemPedido> listarPorPedido(Long pedidoId) {
        long chave = Jdbc.idObrigatorio(pedidoId, "O pedido");
        return jdbc.usar("Não foi possível listar os itens do pedido.", conexao -> listarPorPedido(chave, conexao));
    }

    static List<ItemPedido> listarPorPedido(long pedidoId, Connection conexao) throws SQLException {
        return Jdbc.listar(conexao, CONSULTA + " WHERE i.pedido_id = ? ORDER BY i.id", ItemPedidoDAO::mapear, pedidoId);
    }

    private static String mensagemInexistente(long id) {
        return "Item " + id + " não encontrado.";
    }

    private static ItemPedido mapear(ResultSet linha) throws SQLException {
        Produto produto = new Produto(linha.getLong("produto_id"), linha.getString("nome"),
            linha.getBigDecimal("preco"), linha.getInt("estoque"));
        return new ItemPedido(linha.getLong("id"), produto, linha.getInt("quantidade"),
            linha.getBigDecimal("preco_praticado"));
    }
}
