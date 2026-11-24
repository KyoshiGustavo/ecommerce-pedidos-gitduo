# Checklist de Tratamento de Exceções

> Instrumento de avaliação formativa — Aula 09. Itens marcados foram **conferidos no código** (busca em `src/`, execução da bateria de cenários e da suíte de testes).

**Equipe:** Gustavo Kyoshi & João Miranda
**Integrante:** ______________________ **Fluxo sob minha responsabilidade:** ______________________ **Nº do PR:** ______

## A hierarquia de exceções

- [x] Existe um pacote próprio para as exceções do domínio (`com.senai.ecommerce.excecao`).
- [x] Todas as exceções de negócio descendem de uma raiz comum (`ECommerceException`).
- [x] Foram criadas ao menos duas exceções customizadas (`EstoqueInsuficienteException`, `PagamentoRecusadoException`, e ainda `ClienteNaoEncontradoException`, `PedidoInvalidoException`, `CadastroDuplicadoException`).
- [x] Cada exceção customizada carrega ao menos um dado de negócio.
- [x] Todos os nomes terminam em `Exception`.
- [x] Nenhuma exceção customizada duplica algo que a linguagem já oferece.

## Checked × unchecked

- [x] O critério de escolha está escrito e é o mesmo para todo o projeto (Mapa de Exceções, Parte 1; README).
- [x] Situações de negócio previstas são checked.
- [x] Erros de uso da classe são unchecked.
- [x] Nenhum método declara `throws` de exceção que ele nunca lança.

## Lançamento

- [x] Toda validação lança exceção com mensagem específica (`util/Mensagens`).
- [x] A mensagem informa o valor recebido e o esperado.
- [x] Construtores e setters validam os argumentos (`Validador`).
- [x] Nenhum objeto do domínio consegue nascer em estado inválido.
- [x] Nenhuma exceção é lançada para controlar fluxo normal (busca de produto devolve `Optional`).

## Tratamento

- [x] Nenhum `catch` está vazio.
- [x] Nenhum `printStackTrace` permaneceu no código entregue.
- [x] Os `catch` capturam o tipo mais específico possível (única exceção deliberada: o último recurso `RuntimeException` da fronteira com o usuário, que registra a causa em log).
- [x] O `catch` específico vem antes do genérico.
- [x] O tratamento acontece na camada que tem contexto para decidir (`MenuConsole`).
- [x] Nenhuma exceção chega ao usuário como stack trace.

## Recursos e encadeamento

- [x] Todo recurso aberto é fechado em `finally` ou por try-with-resources (`Scanner` em `Aplicacao`).
- [x] Onde houve tradução de exceção, a causa original foi preservada (`NumberFormatException` → `IllegalArgumentException(msg, e)`).
- [x] Onde dois tipos recebem o mesmo tratamento, foi usado multi-catch.

## Cenários testados

- [x] Estoque insuficiente: tratado, com o disponível informado.
- [x] Preço ou quantidade negativa: recusado no cadastro.
- [x] Pedido sem itens: pagamento recusado, situação inalterada.
- [x] Processador de pagamento nulo: recusado com mensagem clara.
- [x] Pagamento recusado: pedido permanece ABERTO.
- [x] Pedido já pago: segunda cobrança impedida.
- [x] E-mail inválido: cliente não é criado.
- [x] Caminho feliz: continua funcionando do início ao fim.

## Versionamento

_(estes itens dependem de você, no GitHub)_

- [ ] Trabalhei em branch própria, a partir de uma `main` atualizada.
- [ ] Commits pequenos e descritivos.
- [ ] Pull Request revisado por um colega e integrado com aprovação.
- [ ] O mapa de exceções foi versionado junto com o código (`docs/MAPA_DE_EXCECOES.md`).

## Registro para a revisão em pares (Momento 4)

Falhas que a outra equipe conseguiu provocar no nosso sistema: ______________________

A que não tínhamos previsto, e por quê: ______________________

Cenários deste mapa que virarão testes automatizados na Aula 10: todas as linhas da Parte 3 do Mapa de Exceções (24 cenários; os 24 já rodam em `CenariosDeErroTest`).
