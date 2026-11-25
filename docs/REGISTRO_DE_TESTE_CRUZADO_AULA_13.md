# Políticas de exclusão e registro de teste cruzado

## Identificação

| Campo | Valor |
|---|---|
| Equipe | Gustavo Kyoshi e João Miranda |
| Branch prevista | `feature/crud-completo` |
| Nº do Pull Request | Pendente |
| Equipe que testou o nosso sistema | Pendente da sessão entre equipes |
| Equipe cujo sistema testamos | Pendente da sessão entre equipes |

## Parte 1 — Políticas de exclusão

| Relacionamento | Tipo | Política | Justificativa |
|---|---|---|---|
| Pedido → ItemPedido | Composição | CASCADE em pedido aberto | O item não existe sem o pedido a que pertence. |
| Cliente → Pedido | Associação | RESTRICT | O histórico precisa identificar o comprador. |
| Produto → ItemPedido | Associação | RESTRICT no banco e exclusão lógica no catálogo | O pedido preserva seu vínculo e o preço praticado. |

| Entidade ou caso | Decisão |
|---|---|
| Produto vendido ou nunca vendido | Inativar sempre. Nome fica reservado e o cadastro pode ser reativado com o mesmo id. |
| Cliente | Excluir fisicamente quando não tem pedidos. |
| Pedido aberto | Excluir fisicamente, com os itens. O rascunho ainda não movimentou estoque. |
| Pedido finalizado ou pago | Preservar. A venda movimentou estoque e o pagamento, quando presente, precisa de histórico. |
| Item | Excluir fisicamente enquanto o pedido estiver aberto. |

As justificativas completas estão em [Políticas de exclusão](POLITICAS_EXCLUSAO_AULA_13.md).

## Parte 2 — CRUD por entidade

| Entidade | Create | Read | Update | Delete |
|---|---|---|---|---|
| Produto | `salvar` sem id | `buscarPorId`, `buscarPorNome`, `listarTodos` | `salvar`, `atualizar`, `atualizarEstoque` e `reativar` | `remover` e `inativar` |
| Cliente | `salvar` sem id | `buscarPorId`, `buscarPorEmail`, `listarTodos` | `salvar`, `atualizar` e `atualizarEmail` | `remover` |
| Pedido | `salvar` para rascunho e `finalizar` para pedido com itens | `buscarPorId`, `listarTodos`, `listarPorCliente`, `listarPorSituacao` | `atualizar` e `salvar` com id | `remover` para rascunho |
| ItemPedido | `inserir` com id do pedido | `buscarPorId`, `listarTodos`, `listarPorPedido` | `atualizar` a quantidade do rascunho | `remover` para rascunho |

Os quatro DAOs implementam as interfaces de repositório. A apresentação e o serviço usam as interfaces. `java.sql` fica no pacote de persistência. Para trocar de SGBD, é preciso ajustar driver, conexão e eventuais particularidades do schema e do SQL nos DAOs, preservando os contratos usados pelos chamadores.

## Parte 3 — Finalização transacional

| Passo | Operação | Se falhar |
|---|---|---|
| 1 | Gravar pedido novo ou bloquear rascunho existente | Nada novo fica gravado. Um rascunho existente fica como estava. |
| 2 | Gravar itens novos e identificar os produtos | Pedido novo e itens inseridos na operação são desfeitos. |
| 3 | Baixar estoque com checagem do saldo atual | Baixas anteriores da mesma tentativa também são desfeitas. |
| 4 | Confirmar a transação após gravar a situação final | Uma falha é propagada e a conexão tenta rollback. |

| Falha provocada | Esperado | Obtido na validação local |
|---|---|---|
| Restrição no segundo INSERT de item | Sem pedido, itens ou baixa de estoque | Nenhum pedido ou item persistiu. Estoques permaneceram em 5 e 3. |
| Restrição na segunda baixa de estoque | Primeira baixa também revertida | Nenhum pedido ou item persistiu. Estoques permaneceram em 5 e 3. |
| Estoque insuficiente no segundo produto | Rollback e mensagem de negócio | Foi lançada `EstoqueInsuficienteException` para o segundo produto. O primeiro estoque voltou a 5. |
| Nenhuma falha | Pedido finalizado com itens e estoque abatido | Dois itens persistiram. Estoques passaram de 5 e 3 para 4 e 1. |
| Rascunho existente, estoque insuficiente | Preservar rascunho e itens existentes | O pedido permaneceu aberto com os dois itens. A tentativa posterior com estoque suficiente foi confirmada. |

Os DAOs da finalização compartilham a mesma conexão. `Jdbc.transacao` restaura o autocommit no `finally` depois de encerrar a transação e fecha a conexão com try-with-resources. Quando o próprio rollback falha, a conexão é fechada sem reativar o autocommit, para evitar confirmação de dados parciais.

### Resultado da execução

| Execução | Resultado |
|---|---|
| Projeto recebido | 3 testes unitários preexistentes passaram. O ZIP não continha uma suíte separada da Aula 11. |
| Maven com H2 em memória no modo MySQL | 20 executados, 0 falhas, 0 erros, 0 ignorados. |
| Maven com MariaDB 11.4.5 e MySQL Connector/J | 20 executados, 0 falhas, 0 erros, 0 ignorados. |
| Fluxo via terminal em MariaDB | Schema aplicado duas vezes, cadastro de cliente e produto, rascunho, inclusão de item, finalização e Pix simulado. Consulta SQL confirmou situação PAGO, duas unidades vendidas e saldo de três unidades. |

O servidor MariaDB foi usado para verificar o comportamento com um banco real da família MySQL. A execução automatizada padrão usa H2 e descarta o banco de cada teste.

## Parte 4 — Validação local do roteiro que será usado no teste cruzado

| Cenário | Resultado local | Evidência |
|---|---|---|
| Atualizar registro inexistente | Erro informa o id, sem falso sucesso | `deveRecusarIdNuloETratarRetornoZero` |
| Excluir cliente com pedidos | Operação recusada e registros preservados | `deveTraduzirExclusaoDeClienteComPedidos` |
| Excluir pedido aberto com itens | Pedido e itens foram excluídos. Estoque permaneceu igual. | `deveAtualizarRascunhoEExcluirSeusItensEmCascata` |
| Alterar preço do produto e reler pedido antigo | Total histórico conservado, mesmo ao inativar o produto | `devePreservarPrecoHistoricoECadastroReferenciado` |
| Falhar na finalização | Pedido, itens e baixas da operação foram desfeitos | Três cenários descritos na Parte 3 |
| Inserir, excluir e inserir de novo | Cliente recadastrado recebeu novo id. Produto inativo preservou id ao reativar. | `deveAtualizarExcluirERecadastrarCliente` e `deveInativarSemApagarERestaurarProduto` |
| Atualizar com id nulo | Operação recusada antes de chamar o banco | `deveRecusarIdNuloETratarRetornoZero` |
| Duas vendas disputam a última unidade | Uma conclui; a outra é recusada por falta de estoque | `deveRecusarVendaConcorrenteAcimaDoEstoque` |

## Parte 5 — Devolutiva entre equipes

| Campo | Registro |
|---|---|
| O que encontramos no sistema da outra equipe | Pendente de receber e testar a outra entrega. |
| O que encontraram no nosso sistema | Pendente do teste de uma equipe visitante. |
| Falha mais reveladora relatada por pares | Pendente. |
| Política alterada por causa da devolutiva | Pendente. |

Os resultados locais acima são evidência reproduzível para a sessão. A devolutiva externa será registrada após a troca entre equipes.

## Parte 6 — Contagem da camada de persistência

A contagem usa linhas físicas dos arquivos `.java` sob `persistencia`, incluindo comentários e espaços. A classificação abaixo usa arquivos inteiros, pois os DAOs misturam SQL, mensagens e mapeamento. Contratos, testes, schema e console ficam fora do total.

| Bloco | Linhas |
|---|---:|
| Quatro DAOs, com SQL, mapeamento e regras de escrita | 571 |
| Conexão, execução JDBC e instalação do schema | 229 |
| Finalizador do pedido, com regras de orquestração | 85 |
| **Total** | **885** |

A separação por arquivo contabiliza 85 linhas no componente dedicado à regra de finalização. Validações de id, situação e integridade também aparecem nos DAOs. Há quatro implementações de repositório com a mesma organização de escrita e leitura. Preparação de comandos, checagem da quantidade de linhas e fechamento de recursos são reutilizados.

A regra transacional fica concentrada no finalizador. Os DAOs contêm regras de escrita e traduzem falhas do banco. O recurso comum de abrir a conexão, preparar comandos, executar e fechar ficou em `Jdbc`, para ser reaproveitado pelos quatro DAOs.

## Parte 7 — Fica para a Aula 14

- Rever injeção de dependências e como ela permite testar um serviço com um repositório em memória.
- Comparar o controle explícito de transações com `@Transactional`.
- Comparar os contratos JDBC com repositórios Spring Data JPA e rever o mapeamento das quatro entidades.
