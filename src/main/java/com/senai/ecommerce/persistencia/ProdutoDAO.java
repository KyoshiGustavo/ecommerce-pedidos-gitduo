package com.senai.ecommerce.persistencia;

import com.senai.ecommerce.excecao.EstoqueInsuficienteException;
import com.senai.ecommerce.modelo.Produto;
import com.senai.ecommerce.repositorio.RepositorioException;
import com.senai.ecommerce.repositorio.RepositorioProduto;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class ProdutoDAO implements RepositorioProduto {
    private static final String CATALOGO =
        "SELECT id, nome, preco, quantidade FROM produto WHERE ativo = TRUE";
    private static final String DUPLICADO =
        "Já existe um produto com esse nome. Se estiver inativo, reative seu cadastro.";

    private final Jdbc jdbc;

    public ProdutoDAO(ConexaoFactory conexoes) {
        jdbc = new Jdbc(conexoes);
    }

    @Override
    public Produto salvar(Produto produto) {
        Objects.requireNonNull(produto, "Produto é obrigatório.");
        if (produto.getId() != null) {
            return jdbc.transacao("Não foi possível salvar o produto.", conexao -> {
                atualizar(produto.getId(), produto, conexao);
                return buscarAtivoBloqueado(produto.getId(), conexao);
            });
        }
        var preco = Jdbc.precoMonetario(produto.getPreco());
        return jdbc.usar("Não foi possível cadastrar o produto.", conexao -> {
            try {
                long id = Jdbc.inserir(conexao, "INSERT INTO produto (nome, preco, quantidade) VALUES (?, ?, ?)",
                    produto.getNome(), preco, produto.getQuantidadeEmEstoque());
                return new Produto(id, produto.getNome(), preco, produto.getQuantidadeEmEstoque());
            } catch (SQLException e) {
                throw Jdbc.traduzirIntegridade(e, DUPLICADO, null);
            }
        });
    }

    @Override
    public void atualizar(Produto produto) {
        Objects.requireNonNull(produto, "Produto é obrigatório.");
        long id = Jdbc.idObrigatorio(produto.getId(), "O produto");
        jdbc.usar("Não foi possível atualizar o produto.", conexao -> {
            atualizar(id, produto, conexao);
            return null;
        });
    }

    private static void atualizar(long id, Produto produto, Connection conexao) throws SQLException {
        try {
            int linhas = Jdbc.alterar(conexao,
                "UPDATE produto SET nome = ?, preco = ? WHERE id = ? AND ativo = TRUE",
                produto.getNome(), Jdbc.precoMonetario(produto.getPreco()), id);
            Jdbc.exigirUmaLinha(linhas, produtoIndisponivel(id));
        } catch (SQLException e) {
            throw Jdbc.traduzirIntegridade(e, DUPLICADO, null);
        }
    }

    @Override
    public void atualizarEstoque(Long id, int quantidade) {
        long chave = Jdbc.idObrigatorio(id, "O produto");
        if (quantidade < 0) {
            throw new IllegalArgumentException("Estoque não pode ser negativo.");
        }
        jdbc.usar("Não foi possível atualizar o estoque.", conexao -> {
            int linhas = Jdbc.alterar(conexao,
                "UPDATE produto SET quantidade = ? WHERE id = ? AND ativo = TRUE", quantidade, chave);
            Jdbc.exigirUmaLinha(linhas, produtoIndisponivel(chave));
            return null;
        });
    }

    @Override
    public Optional<Produto> buscarPorId(Long id) {
        long chave = Jdbc.idObrigatorio(id, "O produto");
        return jdbc.usar("Não foi possível consultar o produto.", conexao ->
            Jdbc.buscar(conexao, CATALOGO + " AND id = ?", ProdutoDAO::mapear, chave));
    }

    @Override
    public Optional<Produto> buscarPorNome(String nome) {
        return jdbc.usar("Não foi possível consultar o produto.", conexao ->
            Jdbc.buscar(conexao, CATALOGO + " AND nome = ?", ProdutoDAO::mapear, nome));
    }

    @Override
    public List<Produto> listarTodos() {
        return jdbc.usar("Não foi possível listar os produtos.", conexao ->
            Jdbc.listar(conexao, CATALOGO + " ORDER BY id", ProdutoDAO::mapear));
    }

    @Override
    public void remover(Long id) {
        inativar(id);
    }

    @Override
    public void inativar(Long id) {
        alterarAtivo(id, false);
    }

    @Override
    public void reativar(Long id) {
        alterarAtivo(id, true);
    }

    private void alterarAtivo(Long id, boolean ativo) {
        long chave = Jdbc.idObrigatorio(id, "O produto");
        String acao = ativo ? "reativar" : "inativar";
        jdbc.usar("Não foi possível " + acao + " o produto.", conexao -> {
            int linhas = Jdbc.alterar(conexao, "UPDATE produto SET ativo = ? WHERE id = ? AND ativo = ?",
                ativo, chave, !ativo);
            Jdbc.exigirUmaLinha(linhas, "Produto " + chave + " não encontrado ou já está no estado solicitado.");
            return null;
        });
    }

    void baixarEstoque(Produto referencia, int quantidade, Connection conexao)
            throws SQLException, EstoqueInsuficienteException {
        long id = Jdbc.idObrigatorio(referencia.getId(), "O produto");
        Produto atual = buscarAtivoBloqueado(id, conexao);
        atual.baixarEstoque(quantidade);
        int linhas = Jdbc.alterar(conexao,
            "UPDATE produto SET quantidade = quantidade - ? WHERE id = ? AND ativo = TRUE AND quantidade >= ?",
            quantidade, id, quantidade);
        Jdbc.exigirUmaLinha(linhas, "Não foi possível baixar o estoque do produto " + id + ".");
    }

    static Produto buscarAtivoBloqueado(long id, Connection conexao) throws SQLException {
        return Jdbc.buscar(conexao, CATALOGO + " AND id = ? FOR UPDATE", ProdutoDAO::mapear, id)
            .orElseThrow(() -> new RepositorioException(produtoIndisponivel(id)));
    }

    private static String produtoIndisponivel(long id) {
        return "Produto " + id + " não encontrado ou inativo.";
    }

    private static Produto mapear(ResultSet linha) throws SQLException {
        return new Produto(linha.getLong("id"), linha.getString("nome"), linha.getBigDecimal("preco"),
            linha.getInt("quantidade"));
    }
}
