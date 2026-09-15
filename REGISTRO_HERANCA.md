# Registro de Decisão de Herança - Aula 06

## Decisões de RECUSA

### Proposta A — CarrinhoDeCompras extends ArrayList<ItemPedido>
- **Decisão:** Recusar.
- **Justificativa:** O Carrinho "TEM UMA" lista de itens e não "É UMA" lista. Herdar de ArrayList exporia métodos indesejados como `clear()` e `add(null)`, violando o encapsulamento e as regras de negócio.

### Proposta B — PedidoCancelado extends Pedido
- **Decisão:** Recusar.
- **Justificativa:** Cancelado é um ESTADO do pedido, não um TIPO permanente. Usaremos um Enum `SituacaoDoPedido` para controlar o estado do objeto ao longo da sua vida.