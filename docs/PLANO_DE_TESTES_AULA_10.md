# Plano de Testes Unitários — Aula 10

| Campo | Valor |
|---|---|
| Equipe | Gustavo Kyoshi e João Miranda |
| Branch local | `feature/testes-unitarios`, a partir da entrega da Aula 09 presente no ZIP |
| Pull Request | Pendente de abertura, revisão e integração |
| Versões | Java 21 (compilação Java 17), Maven 3.9.9, JUnit 5.10.2, Surefire 3.2.5 |
| Resultado final | 127 testes executados, 0 falhas, 0 erros e 0 ignorados |
| Tempo medido | 2,143 s no `mvn clean test`, incluindo compilação |
| Data da execução | 24/11/2026 |

## Parte 0 — Combinados

| Decisão | Aplicação |
|---|---|
| Nomes e organização | Métodos começam com `deve` e descrevem resultado e condição. Os testes de Java espelham os pacotes de produção em `src/test/java`. |
| Cenários | `@BeforeEach` recria objetos mutáveis. Os métodos auxiliares ficam na classe de teste que os usa, como `criarPedidoComDoisItens` em `PedidoTest`. Chamadas puras a `Validador` recebem os argumentos de cada teste. |
| Asserções | `assertThrows` recebe somente a ação que deve falhar. Asserções adicionais conferem a exceção e o estado após a falha. Valores `BigDecimal` são comparados com `compareTo`. |
| Cobertura | Meta de 70% das linhas em cada classe do modelo de domínio. |

| Elemento | Testar? | Motivo |
|---|---|---|
| Cálculo do total do pedido | Sim | Define quanto o cliente deve pagar. |
| Getters e setters sem regra | Somente como observação | Servem para conferir o resultado de uma regra já exercitada. |
| Validações dos construtores | Sim | Impedem que objetos entrem no sistema com dados inválidos. |
| Método que só imprime no console | Fora da suíte unitária do domínio | A apresentação da mensagem pode mudar sem alterar a operação. |
| Mudança de situação do pedido | Sim | Garante que só um pagamento aprovado muda o pedido para `PAGO`. |
| `toString` das entidades | Fora da suíte do domínio | A formatação textual pode mudar sem alterar as regras. |

## Parte 1 — Caminho feliz

| Classe de teste | Método | Cenário e ação | Esperado | Resultado |
|---|---|---|---|---|
| `ProdutoTest` | `deveCriarProdutoValido` | Criar produto com espaços no nome, preço R$ 3.000 e estoque 5 | Nome normalizado, preço e estoque corretos | OK |
| `ProdutoTest` | `deveBaixarEstoqueQuandoHaQuantidadeSuficiente` | Baixar 2 unidades de um estoque de 5 | Estoque 3 | OK |
| `ProdutoTest` | `deveReporEstoque` | Repor 5 unidades em um estoque de 5 | Estoque 10 | OK |
| `ClienteTest` | `deveCriarClienteComCpfValido` | Construir cadastro pessoal com CPF válido e nome com espaços | CPF preservado e nome normalizado | OK |
| `ClienteTest` | `deveCriarClienteComCnpjValido` | Construir cadastro empresarial com CNPJ válido | CNPJ aceito e preservado | OK |
| `ClienteTest` | `deveAlterarEmailQuandoValido` | Atualizar e-mail com endereço válido e espaços externos | Novo e-mail normalizado | OK |
| `ClienteServicoTest` | `deveEncontrarClienteCadastrado` | Consultar o CPF com e sem máscara | As duas consultas encontram o mesmo cliente | OK |
| `ItemPedidoTest` | `deveCalcularSubtotalComUmaUnidade` | Calcular subtotal de uma unidade de R$ 3.000 | R$ 3.000,00 | OK |
| `ItemPedidoTest` | `deveCalcularSubtotalComVariasUnidades` | Calcular subtotal de duas unidades de R$ 3.000 | R$ 6.000,00 | OK |
| `ItemPedidoTest` | `deveRecalcularSubtotalQuandoQuantidadeMuda` | Alterar quantidade de 1 para 3 | R$ 9.000,00 | OK |
| `PedidoTest` | `deveCalcularTotalComUmItem` | Incluir um mouse de R$ 100 com quantidade padrão | Total igual ao preço de uma unidade | OK |
| `PedidoTest` | `deveSomarTotalComDoisItens` | Somar um notebook de R$ 3.000 e dois mouses de R$ 100 | R$ 3.200,00 | OK |
| `PedidoTest` | `devePassarParaPagoQuandoAprovado` | Pagar pedido com Pix | Situação `PAGO` e comprovante emitido | OK |
| `PedidoTest` | `deveAceitarValorCorreto` | Informar R$ 3.200,000 para pedido de R$ 3.200,00 | Pagamento aprovado mesmo com escalas decimais diferentes | OK |
| `FuncionarioTest` | `deveCriarFuncionarioQuandoDadosValidos`, `deveAlterarCargoQuandoValido`, `deveAlterarMatriculaQuandoValida` | Cadastrar e alterar dados válidos em cenários separados | Matrícula e cargo normalizados | OK |
| `PixTest` | `deveAprovarEEmitirComprovanteQuandoValorPositivo` | Processar R$ 0,01, R$ 125,50 e R$ 10.000,00 | Pagamento aprovado e comprovante Pix em cada execução | OK |
| `BoletoTest` | `deveAprovarEEmitirComprovanteQuandoValorPositivo` | Processar os mesmos três valores | Pagamento aprovado e comprovante de boleto em cada execução | OK |
| `DinheiroTest` | `deveAprovarEEmitirComprovanteQuandoValorPositivo` | Processar os mesmos três valores | Pagamento aprovado e comprovante de dinheiro em cada execução | OK |
| `CartaoCreditoTest` | `deveAprovarQuandoDentroDoLimite` | Processar R$ 0,01, R$ 50,00 e R$ 100,000 com limite de R$ 100,00 | Aprovação e comprovante em cada execução, inclusive no limite | OK |
| `ProcessadorPagamentoTest` | `deveProcessarTodasAsFormasPolimorficamente` | Processar R$ 125,50 por meio da interface comum | As quatro formas aprovam e emitem comprovante | OK |

## Parte 2 — Cenários do Mapa de Exceções da Aula 09

Os IDs são os da Parte 3 do [Mapa de Exceções](MAPA_DE_EXCECOES.md). Os 21 casos de erro usam `assertThrows` nos testes específicos. Os casos com a mesma regra e dados diferentes usam invocações parametrizadas. H1–H3 verificam operações válidas.

| ID | Classe e método | Ação | Esperado e estado após a ação | Resultado |
|---|---|---|---|---|
| P1 | `ProdutoTest.deveRecusarNomeVazio` | Construir produto com nome vazio | `IllegalArgumentException`. Objeto não criado. | OK |
| P2 | `ProdutoTest.deveRecusarPrecoNegativo` | Construir produto com preço negativo | `IllegalArgumentException`. Objeto não criado. | OK |
| P3 | `ProdutoTest.deveRecusarPrecoZero` | Construir produto com preço zero | `IllegalArgumentException`. Objeto não criado. | OK |
| P4 | `ProdutoTest.deveRecusarEstoqueNegativo` | Construir produto com estoque -1 | `IllegalArgumentException`. Objeto não criado. | OK |
| P5 | `ProdutoServicoTest.deveRecusarProdutoDuplicado` | Cadastrar o mesmo nome novamente | `CadastroDuplicadoException`. Cadastro original preservado. | OK |
| C1 | `ClienteTest.deveRecusarNomeInvalido` | Construir cliente com nome vazio | `IllegalArgumentException`. Objeto não criado. | OK |
| C2 | `ClienteTest.deveRecusarEmailSemArroba` | Construir cliente com e-mail sem @ | `IllegalArgumentException`. Objeto não criado. | OK |
| C3 | `ClienteTest.deveRecusarDocumentoInvalido` | Construir cliente com CPF de dígitos verificadores inválidos | `IllegalArgumentException`. Objeto não criado. | OK |
| C4 | `ClienteServicoTest.deveRecusarClienteDuplicado` | Cadastrar CPF já existente com outra máscara | `CadastroDuplicadoException`. Cadastro original preservado. | OK |
| C5 | `ClienteServicoTest.deveLancarClienteNaoEncontrado` | Consultar CPF ausente | `ClienteNaoEncontradoException` com o documento buscado. Cadastros preservados. | OK |
| D1 | `PedidoTest.deveRecusarQuantidadeInvalida` | Adicionar quantidade zero | `IllegalArgumentException`. Total zero e estoque intacto. | OK |
| D2 | `PedidoTest.deveRecusarQuantidadeInvalida` | Adicionar quantidade -3 | `IllegalArgumentException`. Total zero e estoque intacto. | OK |
| D3 | `PedidoTest.deveManterEstadoQuandoEstoqueInsuficiente` | Solicitar 50 unidades de um estoque de 3 | `EstoqueInsuficienteException` com quantidades solicitada e disponível. Total zero e estoque 3. | OK |
| D4 | `PedidoTest.deveRecusarProdutoNulo` | Adicionar produto nulo | `IllegalArgumentException`. Total permanece zero. | OK |
| D5 | `PedidoTest.deveRecusarItemEmPedidoPago` | Adicionar produto após pagamento | `PedidoInvalidoException` com número do pedido. Total e estoque preservados. | OK |
| D6 | `PedidoTest.deveRecusarNumeroInvalido` | Criar pedido de número zero | `IllegalArgumentException`. Objeto não criado. | OK |
| G1 | `PedidoTest.deveRecusarProcessadorNulo` | Pagar pedido com processador nulo | `IllegalArgumentException`. Pedido aberto e sem comprovante. | OK |
| G2 | `PedidoTest.deveRecusarPagamentoDePedidoSemItens` | Pagar pedido vazio via Pix | `PedidoInvalidoException` com número do pedido. Pedido aberto e sem comprovante. | OK |
| G3 | `PedidoTest.deveManterPedidoAbertoQuandoPagamentoRecusado` | Pagar R$ 200 com cartão de limite R$ 50 | `PagamentoRecusadoException` com forma e motivo. Pedido aberto e sem comprovante. | OK |
| G4 | `PedidoTest.deveImpedirPagamentoDuplicado` | Tentar pagar um pedido já pago | `PedidoInvalidoException`. Primeiro comprovante preservado e segunda cobrança não processada. | OK |
| G5 | `PedidoTest.deveRecusarValorDivergente` | Informar R$ 150 para pedido de R$ 200 | `PagamentoRecusadoException` com o valor informado. Pedido aberto e sem cobrança. | OK |
| H1 | `ProdutoServicoTest.deveDisponibilizarProdutoQuandoCadastroValido` | Cadastrar um produto válido | Produto aparece no cadastro. | OK |
| H2 | `PedidoTest.deveSomarTotalComDoisItens` | Criar pedido com dois itens | Total de R$ 3.200,00. | OK |
| H3 | `PedidoTest.devePassarParaPagoQuandoAprovado` | Pagar um pedido válido via Pix | Situação `PAGO` e comprovante emitido. | OK |

O teste `ProdutoTest.deveLancarExcecaoQuandoEstoqueInsuficiente` verifica o tipo da exceção, o produto, as quantidades e o estado do estoque diretamente. A verificação agregada dos 24 cenários de console da Aula 09 também roda com a suíte em `CenariosDeErroTest`.

## Parte 3 — Casos de borda

| Classe | Borda | Esperado | Resultado |
|---|---|---|---|
| `Produto` | Baixa igual ao estoque | Aceita e zera o estoque | OK |
| `Produto` | Baixa zero ou negativa | Recusa e mantém o estoque | OK |
| `Produto` | Reposição até ou acima de `Integer.MAX_VALUE` | Aceita exatamente o máximo e rejeita excedente com estado intacto | OK |
| `Cliente` | Nome vazio ou nulo, e-mail de atualização nulo | Construção inválida recusada e e-mail anterior preservado na alteração | OK |
| `Pedido` | Vazio, um item e produto nulo | Total zero no vazio, um preço unitário no item único e exceção para produto nulo | OK |
| `ItemPedido` | Quantidade igual a 1, zero e negativa | Preço unitário para 1 e subtotal preservado ao rejeitar quantidade inválida | OK |
| `CartaoCredito` | Valor igual ou um centavo acima do limite | Aprova no limite e recusa acima | OK |
| `CartaoCredito` | Máximo de parcelas citado como exemplo no template | Parcela não existe na API do projeto. O limite implementado é monetário. | Não se aplica |
| `PedidoUtils` | Frete grátis a partir de R$ 300 e teto de desconto de R$ 50 | Aplica ambos os limites nos valores esperados | OK |

## Parte 4 — Cobertura de linhas por classe

Medição local com JaCoCo 0.8.12 como agente externo do Surefire. A meta incide sobre `com.senai.ecommerce.modelo` e `com.senai.ecommerce.modelo.pagamento`. A execução comum do projeto continua sendo `mvn test`.

| Classe | Linhas cobertas / executáveis | Cobertura | Meta atingida? | Linhas fora da cobertura |
|---|---:|---:|---|---|
| `Produto` | 20 / 21 | 95,24% | Sim | `toString` |
| `Cliente` | 6 / 8 | 75,00% | Sim | Identificação textual e `toString` |
| `ItemPedido` | 10 / 12 | 83,33% | Sim | Getter do preço praticado e `toString` |
| `Pedido` | 51 / 53 | 96,23% | Sim | Getter do cliente e `toString` |
| `Pessoa` | 10 / 10 | 100% | Sim | — |
| `Funcionario` | 10 / 11 | 90,91% | Sim | Identificação textual |
| `FormaPagamento` | 7 / 7 | 100% | Sim | — |
| `ProcessadorPagamento` | 1 / 1 | 100% | Sim | — |
| `Pix` | 4 / 4 | 100% | Sim | — |
| `Boleto` | 4 / 4 | 100% | Sim | — |
| `Dinheiro` | 4 / 4 | 100% | Sim | — |
| `CartaoCredito` | 15 / 15 | 100% | Sim | — |
| **Domínio** | **142 / 150** | **94,67%** | **Sim** | Média ponderada por linhas |
| Java completo | 411 / 641 | 64,12% | Fora da meta de domínio | Inclui menu, logs, utilitários, exceções e aplicação |

A classe de domínio com menor cobertura é `Cliente`, com 75,00%. As linhas restantes montam a identificação textual. As validações de criação, alteração e cadastro foram exercitadas.

## Parte 5 — Ciclo TDD

| Etapa | Evidência |
|---|---|
| RED | `ProdutoTest.devePreservarEstoqueQuandoReposicaoExcedeLimite` foi escrito antes da correção. O teste prepara um estoque de `Integer.MAX_VALUE` e tenta repor uma unidade. |
| Mensagem observada | `Expected java.lang.IllegalArgumentException to be thrown, but nothing was thrown.` |
| GREEN | Foi adicionada uma checagem da capacidade restante antes da soma, preservando o estoque em caso de recusa. O teste isolado passou. |
| REFACTOR | A capacidade restante ganhou um nome e a mensagem foi movida para `Mensagens`, com a quantidade pedida e o máximo permitido. A suíte também confere que uma reposição exatamente no limite é aceita. |
| Aprendizado | Especificar o resultado e a pós-condição antes da implementação definiu tanto a falha esperada quanto o valor que deve permanecer armazenado. |

As duas demonstrações pedidas no roteiro foram executadas em uma cópia separada. Trocar propositalmente o estoque esperado de 3 para 4 produziu `expected: <4> but was: <3>`. Remover a validação de estoque insuficiente fez o teste informar que nenhuma `EstoqueInsuficienteException` havia sido lançada. A versão entregue passou com as asserções e validações corretas.

## Parte 6 — Defeitos encontrados pelos testes

| Teste | Defeito real | Resultado |
|---|---|---|
| `ProdutoTest.devePreservarEstoqueQuandoReposicaoExcedeLimite` | A soma excedia a faixa de `int` e convertia estoque máximo mais uma unidade em estoque negativo. | Corrigido. O método recusa a operação antes de alterar o produto. |

## Parte 7 — Fica para a Aula 11

- Fluxo de integração completo do cadastro ao pagamento pelas interfaces da aplicação.
- Integração com colaboradores externos e uso de Mockito. Os processadores exercitados na Aula 10 são simulados em memória.
- Relatório automatizado e versionável de cobertura integrado ao projeto. Nesta entrega, a medição foi feita em execução local separada.
- No domínio atual, a baixa de estoque ocorre ao adicionar o item. A suíte verifica essa regra e o resultado do pagamento em cenários de domínio.

Os resultados reproduzidos estão no [relatório de execução](RELATORIO_EXECUCAO_AULA_10.txt). O [checklist preenchido](CHECKLIST_TESTES_UNITARIOS_AULA_10.pdf) acompanha a entrega.
