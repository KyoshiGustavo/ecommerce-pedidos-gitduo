package com.senai.ecommerce.persistencia;

import com.senai.ecommerce.excecao.EstoqueInsuficienteException;
import com.senai.ecommerce.modelo.ItemPedido;
import com.senai.ecommerce.modelo.Pedido;
import com.senai.ecommerce.repositorio.FinalizadorPedido;
import com.senai.ecommerce.repositorio.RepositorioException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class FinalizadorPedidoJdbc implements FinalizadorPedido {
    private final Jdbc jdbc;
    private final ProdutoDAO produtos;
    private final PedidoDAO pedidos;
    private final ItemPedidoDAO itens;

    public FinalizadorPedidoJdbc(ConexaoFactory conexoes) {
        jdbc = new Jdbc(conexoes);
        produtos = new ProdutoDAO(conexoes);
        pedidos = new PedidoDAO(conexoes);
        itens = new ItemPedidoDAO(conexoes);
    }

    @Override
    public Pedido finalizar(Pedido pedido) throws EstoqueInsuficienteException {
        Objects.requireNonNull(pedido, "Pedido é obrigatório.");
        return jdbc.transacao("Não foi possível finalizar o pedido.", conexao -> {
            long id = pedido.getId() == null
                ? gravarPedidoNovo(pedido, conexao)
                : finalizarRascunho(pedido.getId(), conexao);
            return pedidos.buscarPorId(id, conexao).orElseThrow(
                () -> new RepositorioException("O pedido finalizado não foi encontrado."));
        });
    }

    private long gravarPedidoNovo(Pedido pedido, Connection conexao)
            throws SQLException, EstoqueInsuficienteException {
        List<ItemPedido> itensDoPedido = pedido.getItens();
        exigirItensValidos(itensDoPedido);
        String situacao = "PAGO".equals(pedido.getStatus()) ? "PAGO" : "FINALIZADO";
        long id = pedidos.inserir(pedido, situacao, conexao);
        // Bloqueia os produtos antes de os INSERTs adquirirem bloqueios de chave estrangeira.
        for (long produtoId : itensDoPedido.stream().map(item -> item.getProduto().getId()).distinct().sorted().toList()) {
            ProdutoDAO.buscarAtivoBloqueado(produtoId, conexao);
        }
        for (ItemPedido item : itensDoPedido) {
            itens.inserir(id, item, conexao);
        }
        baixarEstoque(itensDoPedido, conexao);
        return id;
    }

    private long finalizarRascunho(long id, Connection conexao)
            throws SQLException, EstoqueInsuficienteException {
        PedidoDAO.exigirAberto(id, conexao);
        List<ItemPedido> itensDoPedido = ItemPedidoDAO.listarPorPedido(id, conexao);
        exigirItensValidos(itensDoPedido);
        baixarEstoque(itensDoPedido, conexao);
        pedidos.marcarFinalizado(id, conexao);
        return id;
    }

    private void baixarEstoque(List<ItemPedido> itensDoPedido, Connection conexao)
            throws SQLException, EstoqueInsuficienteException {
        // Ordem fixa de bloqueio para dois pedidos que usam os mesmos produtos.
        List<ItemPedido> ordenados = itensDoPedido.stream()
            .sorted(Comparator.comparing(item -> item.getProduto().getId())).toList();
        for (ItemPedido item : ordenados) {
            produtos.baixarEstoque(item.getProduto(), item.getQuantidade(), conexao);
        }
    }

    private static void exigirItensValidos(List<ItemPedido> itensDoPedido) {
        if (itensDoPedido.isEmpty()) {
            throw new IllegalArgumentException("Adicione pelo menos um item antes de finalizar o pedido.");
        }
        for (ItemPedido item : itensDoPedido) {
            Jdbc.idObrigatorio(item.getProduto().getId(), "O produto do item");
        }
    }
}
