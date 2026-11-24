# Mapa de Exceções do Sistema

> Entrega da **Aula 09** da Atividade Desafiadora.
> Este documento é o roteiro da suíte de testes da **Aula 10**: cada linha da Parte 3 vira um método com `assertThrows`.
>
> A coluna **Obtido** da Parte 3 foi preenchida executando o próprio sistema
> (`java -cp target/classes com.senai.ecommerce.Aplicacao --cenarios`, ou a opção 7 do menu) e é
> conferida automaticamente por `CenariosDeErroTest`. Cada cenário declara o esperado **antes** de executar.

## Identificação

| Campo | Valor |
|---|---|
| Equipe / Squad | Gustavo Kyoshi & João Miranda |
| Branch | `feature/tratamento-excecoes` |
| Nº do Pull Request | _(preencher ao abrir o PR)_ |
| Pacote de exceções | `com.senai.ecommerce.excecao` |

---

## Parte 1 — O critério da equipe

| Pergunta | Resposta da equipe |
|---|---|
| Quando uma exceção será **checked**? | Quando é uma **situação de negócio prevista** e quem chamou o método consegue fazer algo útil a respeito: oferecer outro produto, outra forma de pagamento, pedir para conferir o documento, informar que o pedido já está pago. |
| Quando será **unchecked**? | Quando é **defeito de quem chamou** o método: argumento nulo, vazio, negativo ou fora do formato. Só se resolve corrigindo o código/entrada que chamou. Usamos as da linguagem (`IllegalArgumentException`, `IllegalStateException`). |
| Qual é a exceção-raiz do domínio? | `ECommerceException` (checked). Todas as exceções de negócio descendem dela. |
| Quem trata: o domínio ou a camada de apresentação? | O **domínio detecta e lança** (`Produto`, `Pedido`); o **serviço** detecta duplicidade/inexistência e lança; a **apresentação** (`MenuConsole`) trata tudo o que sobrou e converte em mensagem. |
| O que fazemos quando não sabemos tratar? | **Deixamos subir.** Nenhum `catch` existe só "por reflexo". Quando traduzimos, preservamos a causa (`new IllegalArgumentException(msg, e)`). A única rede de segurança é a fronteira com o usuário (`MenuConsole.executarAcao`), que grava o rastro em `logs/ecommerce.log` e mostra mensagem útil. |

> O critério é **um só** para o projeto inteiro. Quem consome o domínio não precisa adivinhar a regra de cada método.

### Decisão checked × unchecked por situação

| Situação | Checked ou unchecked? | Exceção | Argumento |
|---|---|---|---|
| Estoque insuficiente para o item pedido | **checked** | `EstoqueInsuficienteException` | Quem chamou pode reduzir a quantidade, oferecer outro produto ou avisar compras. |
| Preço negativo no cadastro de produto | **unchecked** | `IllegalArgumentException` | Nenhuma reação em execução resolve; o código/entrada que passou o valor está errado. |
| Pagamento recusado pelo cartão | **checked** | `PagamentoRecusadoException` | Quem chamou pode oferecer outra forma de pagamento. O pedido não muda de situação. |
| Pedido pago pela segunda vez | **checked** | `PedidoInvalidoException` | Situação de negócio possível (duplo clique, reenvio). Quem chamou reage informando que já está pago, sem cobrar de novo. |
| Cliente não encontrado pelo identificador | **checked** | `ClienteNaoEncontradoException` | Quem chamou pode pedir para conferir o CPF/CNPJ ou oferecer o cadastro. |
| E-mail em formato inválido | **unchecked** | `IllegalArgumentException` | Defeito de entrada/validação; objeto não pode nascer inválido. |
| Produto ou cliente duplicado | **checked** | `CadastroDuplicadoException` | Quem chamou pode usar o cadastro existente (ou repor estoque, no caso do produto). |

> **Nota de desenho — pedido já pago:** o *Guia de Exceções Customizadas* cita `IllegalStateException` para este caso,
> mas o *Roteiro Prático* (Passo 3 e tabela de cenários) pede `PedidoInvalidoException`, e é o Roteiro que define os
> cenários a testar. Adotamos `PedidoInvalidoException`. `IllegalStateException` fica para estado inconsistente
> do **próprio objeto/recurso** (ex.: entrada de dados encerrada no menu).

---

## Parte 2 — Hierarquia de exceções do projeto

| Exceção | Herda de | Checked? | Dados que carrega | Quem lança |
|---|---|---|---|---|
| `ECommerceException` | `Exception` | sim | mensagem, causa | _(raiz — ninguém)_ |
| `EstoqueInsuficienteException` | `ECommerceException` | sim | produto, qtd solicitada, qtd disponível | `Produto.baixarEstoque` |
| `PagamentoRecusadoException` | `ECommerceException` | sim | forma de pagamento, motivo | `Pedido.pagar` |
| `ClienteNaoEncontradoException` | `ECommerceException` | sim | identificador buscado | `ClienteServico.buscarPorDocumento` |
| `PedidoInvalidoException` | `ECommerceException` | sim | número do pedido | `Pedido.adicionarItem`, `Pedido.pagar`, `PedidoServico.buscar` |
| `CadastroDuplicadoException` | `ECommerceException` | sim | tipo (Produto/Cliente), identificador | `ProdutoServico.cadastrar`, `ClienteServico.cadastrar` |

**Exceções da linguagem que decidimos reaproveitar (em vez de criar as nossas):**

| Exceção | Onde usamos | Por quê |
|---|---|---|
| `IllegalArgumentException` | `Validador` (nome, preço, estoque, quantidade, e-mail, documento), `Pedido`, `Produto`, `ItemPedido`, `CartaoCredito`, processador `null` | Todo programador Java reconhece; criar `ParametroInvalidoException` seria só um nome pior. |
| `IllegalStateException` | `MenuConsole.lerTexto` (entrada de dados encerrada) | Recurso em estado que não permite a operação. |

---

## Parte 3 — Mapa de validações por fluxo

Previsto declarado antes de executar; **obtido** é o resultado real da execução. Resultado: **24 de 24 cenários conforme o esperado**.

### Cadastro de produto

| # | Entrada / situação | Exceção esperada | Mensagem exibida | Obtido | OK |
|---|---|---|---|---|---|
| P1 | Nome vazio | `IllegalArgumentException` | Nome do produto é obrigatório. Recebido: vazio. Informe um texto não vazio. | IllegalArgumentException | ✅ |
| P2 | Preço negativo | `IllegalArgumentException` | Preço inválido. Recebido: R$ -5.00. Informe um valor maior que zero. | IllegalArgumentException | ✅ |
| P3 | Preço zero | `IllegalArgumentException` | Preço inválido. Recebido: R$ 0. Informe um valor maior que zero. | IllegalArgumentException | ✅ |
| P4 | Estoque negativo | `IllegalArgumentException` | Estoque inválido. Recebido: -1. Informe zero ou mais unidades. | IllegalArgumentException | ✅ |
| P5 | Produto duplicado (mesmo nome) | `CadastroDuplicadoException` | Produto já cadastrado: 'notebook'. Use o cadastro existente em vez de criar outro. | CadastroDuplicadoException; segundo cadastro não entra: segue 1 produto | ✅ |

### Cadastro de cliente

| # | Entrada / situação | Exceção esperada | Mensagem exibida | Obtido | OK |
|---|---|---|---|---|---|
| C1 | Nome vazio | `IllegalArgumentException` | Nome é obrigatório. Recebido: vazio. Informe um texto não vazio. | IllegalArgumentException | ✅ |
| C2 | E-mail sem @ | `IllegalArgumentException` | E-mail inválido. Recebido: 'ana.silva.email.com'. Use o formato nome@dominio.com. | IllegalArgumentException | ✅ |
| C3 | Documento inválido (dígitos verificadores) | `IllegalArgumentException` | Documento inválido. Recebido: '123.456.789-00'. Informe um CPF (11 dígitos) ou CNPJ (14 dígitos) válido. | IllegalArgumentException | ✅ |
| C4 | Cliente duplicado (mesmo CPF, com e sem máscara) | `CadastroDuplicadoException` | Cliente já cadastrado: '52998224725'. Use o cadastro existente em vez de criar outro. | CadastroDuplicadoException; segundo cadastro não entra: segue 1 cliente | ✅ |
| C5 | Busca de cliente por documento inexistente | `ClienteNaoEncontradoException` | Cliente não encontrado para o identificador '111.444.777-35'. Confira o CPF/CNPJ ou cadastre o cliente. | ClienteNaoEncontradoException | ✅ |

### Pedido

| # | Entrada / situação | Exceção esperada | Mensagem exibida | Obtido | OK |
|---|---|---|---|---|---|
| D1 | Quantidade zero | `IllegalArgumentException` | Quantidade inválida. Recebido: 0. Informe uma quantidade maior que zero. | IllegalArgumentException; pedido sem itens e estoque intacto | ✅ |
| D2 | Quantidade negativa | `IllegalArgumentException` | Quantidade inválida. Recebido: -3. Informe uma quantidade maior que zero. | IllegalArgumentException; pedido sem itens e estoque intacto | ✅ |
| D3 | Adicionar 50 de um produto com 3 em estoque | `EstoqueInsuficienteException` | Estoque insuficiente de Notebook: disponível 3, solicitado 50. Peça no máximo 3 unidade(s) ou escolha outro produto. | EstoqueInsuficienteException; estoque segue 3 e pedido sem itens | ✅ |
| D4 | Produto null | `IllegalArgumentException` | Produto é obrigatório. Recebido: nulo. Informe um valor. | IllegalArgumentException; pedido sem itens | ✅ |
| D5 | Adicionar item a pedido já pago | `PedidoInvalidoException` | Pedido 4: já está pago e não aceita novos itens. Abra um novo pedido para continuar comprando. | PedidoInvalidoException; estoque do teclado segue 9 (item recusado não baixou estoque) | ✅ |
| D6 | Número de pedido inválido (zero) | `IllegalArgumentException` | Número do pedido inválido. Recebido: '0'. Informe um número inteiro maior que zero. | IllegalArgumentException | ✅ |

### Pagamento

| # | Entrada / situação | Exceção esperada | Mensagem exibida | Obtido | OK |
|---|---|---|---|---|---|
| G1 | Processador de pagamento null | `IllegalArgumentException` | Forma de pagamento é obrigatória. Recebido: nulo. Escolha Pix, Boleto, Cartão de crédito ou Dinheiro. | IllegalArgumentException; pedido permanece ABERTO | ✅ |
| G2 | Pagar pedido sem itens | `PedidoInvalidoException` | Pedido 11: não possui itens e não pode ser pago. Adicione ao menos um item antes de pagar. | PedidoInvalidoException; pedido permanece ABERTO | ✅ |
| G3 | Pagamento recusado pelo cartão (limite menor que o total) | `PagamentoRecusadoException` | Pagamento por Cartão de Crédito recusado: limite disponível do cartão (R$ 50.00) é menor que o valor da compra (R$ 200.00). Use outro cartão ou outra forma de pagamento. | PagamentoRecusadoException; pedido permanece ABERTO e sem comprovante | ✅ |
| G4 | Pagar um pedido já pago | `PedidoInvalidoException` | Pedido 13: já está pago; a cobrança não será repetida. Nenhuma ação é necessária. | PedidoInvalidoException; segue PAGO e o comprovante da primeira cobrança não mudou (não cobrou duas vezes) | ✅ |
| G5 | Valor informado diverge do total | `PagamentoRecusadoException` | Pagamento por PIX recusado: valor informado R$ 150.00 diverge do total do pedido R$ 200.00. Confira o valor e tente novamente. | PagamentoRecusadoException; pedido permanece ABERTO | ✅ |

### Caminho feliz (não esqueçam!)

| # | Entrada / situação | Exceção esperada | Mensagem exibida | Obtido | OK |
|---|---|---|---|---|---|
| H1 | Cadastrar produto válido | `nenhuma (caminho feliz)` | — | nenhuma exceção; produto cadastrado | ✅ |
| H2 | Criar pedido com 2 itens | `nenhuma (caminho feliz)` | — | nenhuma exceção; total R$ 3200.00 | ✅ |
| H3 | Sequência completa: pedido criado, pago com Pix, situação PAGO | `nenhuma (caminho feliz)` | — | nenhuma exceção; situação PAGO com comprovante | ✅ |

> Validação escrita às pressas costuma barrar também o que é válido. Os três cenários acima garantem que não barramos.

---

## Parte 4 — Onde cada exceção é tratada

| Exceção | Lançada em | Tratada em | O que o tratamento faz |
|---|---|---|---|
| `EstoqueInsuficienteException` | `Produto.baixarEstoque` | `MenuConsole.executarAcao` | Informa o disponível e lista produtos com estoque suficiente (alternativa). Estoque e itens ficam inalterados. |
| `PagamentoRecusadoException` | `Pedido.pagar` | `MenuConsole.executarAcao` | Mostra o motivo e orienta a tentar outra forma de pagamento. O pedido permanece ABERTO. |
| `ClienteNaoEncontradoException` | `ClienteServico.buscarPorDocumento` | `MenuConsole.executarAcao` (multi-catch) | Pede para conferir o CPF/CNPJ ou cadastrar o cliente. |
| `PedidoInvalidoException` | `Pedido`, `PedidoServico.buscar` | `MenuConsole.executarAcao` (multi-catch) | Explica por que a operação é incompatível com a situação do pedido e o que fazer. |
| `CadastroDuplicadoException` | `ProdutoServico`, `ClienteServico` | `MenuConsole.executarAcao` | Informa que já existe e sugere usar o cadastro existente. |
| `ECommerceException` (qualquer outra) | domínio / serviços | `MenuConsole.executarAcao` | Rede de segurança do negócio: vira mensagem, nunca stack trace. |
| `IllegalArgumentException` / `IllegalStateException` | `Validador`, domínio, `MenuConsole.lerTexto` | `MenuConsole.executarAcao` (multi-catch) | "Dado inválido: …" com valor recebido e esperado. |
| `NumberFormatException` | `MenuConsole.lerInteiro` / `converterDecimal` | no próprio ponto de leitura | **Traduzida** para `IllegalArgumentException` com mensagem de usuário, **preservando a causa**. |
| Qualquer `RuntimeException` inesperada | qualquer ponto | `MenuConsole.executarAcao` (último recurso) | Grava rastro completo em `logs/ecommerce.log` e mostra mensagem útil. Nunca stack trace na tela. |

---

## Parte 5 — Caça aos antipadrões

Busca feita no código inteiro (`src/`) ao final do trabalho.

| Antipadrão | Quantas ocorrências? | Como foi corrigido |
|---|---|---|
| `catch` vazio | 0 | Nenhum `catch` sem ação. Todo `catch` informa, traduz, registra ou recusa. |
| `e.printStackTrace()` no código entregue | 0 | O rastro vai para `logs/ecommerce.log` via `RegistroErros` (`java.util.logging`), nunca para a tela. |
| `catch (Exception e)` genérico | 0 (havia 1 em `Aplicacao`) | Substituído por catches específicos, do mais específico para o mais genérico. Existe **1 último recurso deliberado**, `catch (RuntimeException e)`, apenas na fronteira com o usuário (`MenuConsole.executarAcao`), que registra a causa em log e mostra mensagem útil: é o que garante "nenhum stack trace para o usuário" sem engolir o erro. |
| Exceção usada para controlar fluxo normal | 0 | Busca de produto que pode não existir devolve `Optional` (`ProdutoServico.buscarPorNome`), não exceção. |
| Mensagem genérica (`"erro"`, `"falhou"`) | 0 | Todas as mensagens ficam em `util/Mensagens`, no formato: o que houve, valor recebido, o que fazer. |
| Recurso aberto e não fechado | 0 no fluxo do usuário | `Scanner` em try-with-resources (`Aplicacao`). O `FileHandler` do log vive durante todo o processo por design e é encerrado com a JVM. |

---

## Parte 6 — Revisão em pares (Momento 4)

Equipe visitante: ______________________________

| # | Falha que conseguimos provocar | A equipe tinha previsto? | Corrigido? |
|---|---|---|---|
| 1 | | | |
| 2 | | | |
| 3 | | | |
| 4 | | | |

**A falha mais reveladora que recebemos:** _(preencher no Momento 4, com o que ela mostrou sobre o desenho do sistema)_

> Esta parte só existe depois da aula: depende da outra equipe tentar quebrar o sistema. Dica para a revisão:
> o menu (`mvn exec:java`) já traz a opção 8 (dados de exemplo) e a opção 7 (bateria de cenários).

---

## Parte 7 — Fica para a Aula 10

Cada linha da Parte 3 vira um método de teste. Já existem na suíte (ver `src/test/java`):

- [x] `ProdutoTest.deveLancarExcecaoQuandoEstoqueInsuficiente()`
- [x] `ProdutoTest.deveRecusarPrecoInvalido()` (negativo e zero)
- [x] `PedidoTest.deveRecusarPagamentoDePedidoSemItens()`
- [x] `PedidoTest.deveManterPedidoAbertoQuandoPagamentoRecusado()`
- [x] `PedidoTest.deveSomarTotalComDoisItens()` _(caminho feliz)_
- [x] `PedidoTest.deveImpedirPagamentoDuplicado()`, `deveRecusarItemEmPedidoPago()`, `deveRecusarValorDivergente()`, `deveRecusarProcessadorNulo()`
- [x] `ValidadorTest` (e-mail e documento: válidos e inválidos)
- [x] `ServicosTest` (duplicados, cliente/pedido não encontrado)
- [x] `CenariosDeErroTest` (executa os 24 cenários deste mapa)
- [ ] _(continue a lista na Aula 10: `assertThrows` por linha da Parte 3, um método por cenário)_

> A Aula 10 começa exatamente aqui. Quanto mais completa esta lista, mais rápido a suíte de testes fica pronta.
