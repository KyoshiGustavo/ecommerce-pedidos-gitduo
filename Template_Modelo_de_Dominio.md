# Template — Modelo de Domínio

> Entrega da **Aula 07** da Atividade Desafiadora
> Este documento fecha o modelo de domínio do sistema.

## Identificação

| Campo | Valor |
|---|---|
| Equipe / Squad | Gustavo Kyoshi & João Miranda |
| Branch | `feature/relacionamentos` |
| Nº do Pull Request | |
| Diagrama | `docs/diagrama-de-classes.png` + `.drawio` |

---

## Parte 1 — Inventário das ligações

| Classe | Atributo | Tipo | Relacionamento | Multiplicidade |
|---|---|---|---|---|
| `Pedido` | `cliente` | `Cliente` | associação | 1 |
| `Pedido` | `itens` | `List<ItemPedido>` | **composição** | 1..* |
| `Pedido` | `formaPagamento` | `FormaPagamento` | associação | 0..1 |
| `ItemPedido` | `produto` | `Produto` | associação | 1 |
| `Cliente` | `endereco` | `Endereco` | associação | 0..1 |

---

## Parte 2 — A pergunta do ciclo de vida

| Pergunta | Resposta | Conclusão |
|---|---|---|
| Apaguei o pedido. O cliente some? | não | associação |
| Apaguei o pedido. Os itens somem? | sim | **composição** ◆ |
| Apaguei o item. O produto some? | não | associação |
| Apaguei a categoria. Os produtos somem? | não | agregação ◇ |

> **A regra em uma frase:** na composição, a parte não existe fora do todo. Em todos os outros casos, existe.

---

## Parte 3 — Multiplicidade vira validação

| Relacionamento | Multiplicidade | Validação implementada | Onde |
|---|---|---|---|
| Pedido → Cliente | 1 | recusa `cliente == null` | construtor |
| Pedido ◆ ItemPedido | 1..* | recusa pagar pedido vazio | `pagarCom()` / `calcularValorTotal()` |
| ItemPedido → Produto | 1 | recusa `produto == null` | `adicionarItem()` |
| Cliente → Endereco | 0..1 | aceita `null` sem quebrar | construtor / setter |

---

## Parte 4 — Decisão obrigatória: item repetido

| Alternativa | Vantagem | Desvantagem |
|---|---|---|
| Somar quantidades no item existente | recibo limpo, um item por produto | perde o histórico de entrada |
| Criar uma segunda linha | simples; preserva preços diferentes | recibo com linhas repetidas |

**Decisão da equipe:** Somar a quantidade no item existente se o produto já estiver no pedido.

**Por quê:** Mantém o recibo limpo, legível e evita ter linhas duplicadas para o mesmo produto no carrinho.

**Onde está implementada:** Método `adicionarItem` na classe `Pedido`.

---

## Parte 5 — Testes de integridade

| # | Cenário | Esperado | Passou? |
|---|---|---|---|
| 1 | `new Pedido("PED-1", null)` | `IllegalArgumentException` | Sim |
| 2 | `adicionarItem(null, 2)` | `IllegalArgumentException` | Sim |
| 3 | `adicionarItem(produto, 0)` | `IllegalArgumentException` | Sim |
| 4 | `adicionarItem(produto, 9999)` | `IllegalStateException` | Sim |
| 5 | `pagarCom(pix)` com pedido vazio | `IllegalStateException` | Sim |
| 6 | `getItens().clear()` | `UnsupportedOperationException` | Sim |
| 7 | total de 3 itens | conferido à mão | Sim |
| 8 | mesmo produto duas vezes | soma de quantidade | Sim |

---

## Parte 6 — Navegabilidade

| Ligação | Direção | Por quê |
|---|---|---|
| Pedido → Cliente | unidirecional | o pedido precisa do cliente; o cliente não precisa listar pedidos diretamente na memória |

**Alguma ligação ficou bidirecional?** (X) não ( ) sim

---

## Parte 7 — Feedback entre pares (Momento 4)

**Equipe que avaliou o nosso modelo:** Squad Alpha

| Item | Resposta recebida |
|---|---|
| **Ponto forte do nosso modelo** | Excelente encapsulamento na composição de `Pedido` com `ItemPedido`, protegendo a lista com `unmodifiableList`. |
| **Seta questionada, e a pergunta feita** | Questionaram a associação de `FormaPagamento` ser (0..1). Explicamos que o pedido nasce aberto antes de ser pago. |
| **Mudança de requisito que quebraria o modelo** | Permitir dividir o pagamento de um único pedido entre múltiplas formas de pagamento. |
| **O que vamos ajustar** | Mantivemos (0..1), mas validamos para que o pedido não finalize sem pagamento. |

**Modelo que avaliamos:** Squad Beta

| Item | O que devolvemos |
|---|---|
| **Ponto forte** | Boa estruturação da hierarquia de herança. |
| **Seta questionada** | Uso de Agregação em vez de Composição entre Pedido e ItemPedido. |
| **Mudança de requisito que quebraria** | O getter da lista de itens devolvia a coleção mutável diretamente. |