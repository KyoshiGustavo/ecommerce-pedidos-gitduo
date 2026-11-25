# Exercícios de CRUD e transações da Aula 13

## E1 — Atualização e quantidade de linhas

| Comando do slide | Linhas |
|---|---:|
| Alterar o Mouse, id 2 | 1 |
| Atualizar estoque usando id 42 | 0 |
| Alterar produtos com quantidade 0 | 2 |
| Aumentar todos os preços em 10% | 5 |

O quarto comando afeta a tabela inteira. Escrever o filtro primeiro e executar o SELECT correspondente permite conferir o alcance antes de alterar. Retorno zero significa que a execução SQL foi bem-sucedida e nenhuma linha atendeu à condição. No projeto, `ClienteDAO.atualizarEmail` valida o id, valida o e-mail e verifica se uma linha foi afetada.

## E2 — Exclusão de categoria referenciada

O banco impede produtos órfãos, que apontariam para uma categoria inexistente. A mensagem **C** orienta como resolver o vínculo. A apresenta detalhes internos e B omite o motivo e a ação. Em um DAO da categoria, o fluxo seria:

```java
long chave = Jdbc.idObrigatorio(id, "A categoria");
jdbc.usar("Não foi possível excluir a categoria.", conexao -> {
    try {
        Jdbc.exigirUmaLinha(Jdbc.alterar(conexao, "DELETE FROM categoria WHERE id = ?", chave),
            "Categoria " + chave + " não encontrada.");
        return null;
    } catch (SQLException e) {
        throw Jdbc.traduzirIntegridade(e, null,
            "A categoria tem produtos. Mova-os para outra categoria antes de excluir.");
    }
});
```

A causa JDBC é preservada para diagnóstico. Se a categoria não existe, o retorno zero vira uma mensagem de registro não encontrado. O equivalente executável no e-commerce é `ClienteDAO.remover`, protegido por pedidos associados.

## E3 — Políticas para os relacionamentos hipotéticos

| Relacionamento | Tipo | Política e motivo |
|---|---|---|
| Pedido → Histórico de situação | Composição | CASCADE se o pedido for um rascunho cuja remoção é permitida. Para registros financeiros preservados, o pedido permanece e leva seu histórico. |
| Cliente → Endereço cadastral | Composição | CASCADE para endereços reutilizáveis da conta. Um pedido antigo guarda seu próprio endereço de entrega. |
| Categoria → Produto | Associação | RESTRICT. É preciso transferir os produtos antes de apagar a categoria. |
| Pedido → Pagamento | Associação com valor financeiro próprio | RESTRICT. O registro de cobrança precisa continuar auditável. |
| Cliente → Avaliação | Associação | SET NULL é defensável se a avaliação puder permanecer anônima. A coluna `cliente_id` deve aceitar NULL. |

Exemplo em uma tabela hipotética sem a chave já definida:

```sql
ALTER TABLE endereco
    ADD CONSTRAINT fk_endereco_cliente
    FOREIGN KEY (cliente_id) REFERENCES cliente(id) ON DELETE CASCADE;
```

## E4 — Inativação hipotética de cliente

```sql
ALTER TABLE cliente ADD COLUMN ativo BOOLEAN NOT NULL DEFAULT TRUE;
UPDATE cliente SET ativo = FALSE WHERE id = ? AND ativo = TRUE;
```

O método validaria o id e chamaria `Jdbc.exigirUmaLinha` sobre a atualização. `listarTodos`, `buscarPorEmail` usado no login e `contarClientes` para o relatório filtrariam `ativo = TRUE`. A consulta de um cliente associado a pedido antigo teria um método explícito de consulta histórica. Bruno conseguiu entrar porque o login deixou passar um cadastro inativo. Para Carla, o e-mail continua reservado por unicidade. A ação é recuperar a conta e reativá-la com a validação apropriada. No projeto entregue, o soft delete foi aplicado a Produto.

## E5 — Cancelamento hipotético com estoque

Se cada DAO confirma separadamente, o pedido fica cancelado e apenas o primeiro item tem estoque reposto antes da falha no segundo. O segundo e o terceiro continuam sem reposição. Um rollback de outra conexão não alcança as operações já confirmadas. Isso viola **atomicidade**. Em um fluxo de cancelamento, as operações usariam a mesma conexão:

```java
jdbc.transacao("Não foi possível cancelar o pedido.", conexao -> {
    pedidoDAO.marcarCanceladoComBloqueio(pedidoId, conexao);
    for (ItemPedido item : ItemPedidoDAO.listarPorPedido(pedidoId, conexao)) {
        produtoDAO.reporEstoque(item, conexao);
    }
    return null;
});
```

O bloqueio e a validação da situação impediriam reposição em dobro. O auxiliar inicia, confirma, desfaz e fecha a conexão. Esse exemplo é conceitual. O projeto implementa e testa o fluxo transacional obrigatório de **finalização**.

## E6 — Interface de cliente e teste sem banco

`RepositorioCliente` declara os métodos. `ClienteDAO.salvar` usa a presença de id para decidir entre cadastro e atualização. `ClienteServico` recebe a interface no construtor. `ClienteServicoTest` injeta um `RepositorioClienteEmMemoria` e verifica que o segundo cadastro do mesmo e-mail é recusado. A restrição no banco mantém a unicidade quando dois cadastros concorrem.
