# Políticas de exclusão — Aula 13

| Relacionamento ou caso | Política | Justificativa |
|---|---|---|
| Pedido → ItemPedido | CASCADE na exclusão de um pedido aberto | Um item pertence ao pedido e não tem existência própria. |
| Cliente → Pedido | RESTRICT | O cadastro permanece enquanto houver pedidos que precisem identificar o comprador. |
| Produto → ItemPedido | RESTRICT no banco, exclusão lógica no catálogo | Pedidos antigos precisam manter a referência do produto. O item também guarda o preço praticado na venda. |
| Produto nunca vendido | Exclusão lógica | A operação é reversível e mantém a mesma política em todo o catálogo. Um nome já cadastrado fica reservado para reativação. |
| Pedido finalizado ou pago | Exclusão recusada | A finalização já baixou estoque. O pedido pago também tem registro de cobrança. Qualquer cancelamento futuro deve ser um fluxo próprio, com compensação de estoque e pagamento quando aplicável. |
| Item de pedido aberto | Exclusão física | Ainda faz parte de um rascunho e não movimentou o estoque persistido. Itens finalizados e pagos são protegidos. |

O catálogo só consulta produtos ativos. A leitura histórica de um item resolve o produto associado mesmo que ele tenha sido inativado e usa o preço que consta no próprio item.

Pedidos abertos no banco são rascunhos. Produtos podem ficar sem estoque antes da finalização. A disponibilidade é conferida dentro da transação, com o estoque atual do banco.
