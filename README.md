# Sistema de Gestão de Pedidos — E-commerce

> Projeto integrador da Unidade Curricular **Desenvolvimento Back-end**
> Curso Superior de Tecnologia em Análise e Desenvolvimento de Sistemas — Turma CSTADS601

## Equipe / Squad

| Nome | Papel na Aula 01 |
|---|---|
| Gustavo Kyoshi  | Responsável do dia |
| João Miranda | Desenvolvedor |

## Descrição do desafio

Uma equipe de desenvolvimento recebeu a demanda de construir um sistema de gestão de pedidos para um e-commerce, contemplando cadastro de produtos, clientes, pedidos e processamento de pagamentos.

## Funcionalidades previstas

- [ ] Cadastro e gerenciamento de produtos
- [ ] Cadastro e gerenciamento de clientes
- [ ] Criação e gerenciamento de pedidos
- [ ] Processamento de pagamentos (cartão, boleto, Pix)
- [ ] Testes automatizados (unitários e de integração)
- [ ] Pipeline de CI/CD
- [ ] API REST para consumo por um front-end

## Tecnologias

- Java
- Maven
- Git / GitHub

## Estrutura de pastas
```
ecommerce-pedidos-gitduo/
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com/senai/ecommerce/
│   │           ├── Aplicacao.java        (ponto de entrada)
│   │           ├── apresentacao/         (MenuConsole, CenariosDeErro)
│   │           ├── excecao/              (hierarquia de exceções do domínio)
│   │           ├── modelo/               (Produto, Cliente, Pedido... e pagamento/)
│   │           ├── servico/              (cadastros e pedidos em memória)
│   │           └── util/                 (PedidoUtils, Validador, Mensagens, RegistroErros)
│   └── test/
│       └── java/
│           └── com/senai/ecommerce/
├── docs/
│   ├── MAPA_DE_EXCECOES.md
│   └── CHECKLIST_TRATAMENTO_EXCECOES.md
├── pom.xml
├── README.md
└── .gitignore
```

## Como rodar o projeto

1. Clone o repositório.
2. Abra o projeto na sua IDE Java (VS Code ou Eclipse/IntelliJ) — requer Java 17+.
3. Menu interativo: `mvn compile exec:java`
4. Bateria de cenários de erro (previsto x obtido): `mvn compile exec:java -Dexec.args="--cenarios"`
5. Testes automatizados: `mvn test`

Dica para testar manualmente: no menu, a opção **8** carrega dados de exemplo (Notebook com 3 em estoque, cliente Ana Silva)
e a opção **7** roda a bateria de cenários de erro.

## Roadmap do projeto (por aula)

| Aula | Entrega |
|---|---|
| 01 | Repositório criado, estruturado, com README e commit inicial |
| 02 | Fluxo de branches e primeiro Pull Request revisado |
| 03 | Classe utilitária (Utils) do domínio |
| 04 | Classes de domínio inicial (Produto, Cliente, Pedido, ItemPedido) |
| 05 | Encapsulamento e abstração aplicados |
| 06 | Hierarquia de formas de pagamento (herança) |
| 07 | Relacionamentos entre classes do domínio |
| 08 | Módulo de pagamento polimórfico |
| 09 | Tratamento de exceções |
| 10 | Suíte de testes unitários |
| 11 | Suíte de testes de integração + relatório de cobertura |
| 12 | Persistência: conexão, Create e Read |
| 13 | Persistência: Update, Delete e padrão DAO/Repository |
| 14 | Migração para Spring Boot |
| 15 | API REST + pipeline CI/CD |
| 16 | Entrega final, documentação e apresentação |

## Combinado da equipe (ética e convivência)

1. Manter a comunicação aberta e transparente entre os integrantes da squad.
2. Dividir as tarefas de forma equilibrada e respeitar os prazos definidos.
3. Não alterar ou sobrescrever código do colega sem alinhamento prévio via Git/GitHub.
4. Conversar previamente alterações ou divergencias de ideia.

## Licença

Projeto acadêmico — Faculdade de Tecnologia SENAI "Antonio Adolpho Lobbe".

## Decisões de Modelagem (Aula 07)

- **Tratamento de produtos repetidos:** Decidimos somar a quantidade no item existente caso o mesmo produto seja adicionado mais de uma vez ao pedido, mantendo o recibo do pedido organizado e sem linhas duplicadas.

## Entrega Aula 08 — Módulo de Pagamento Polimórfico

- **Interface criada:** `ProcessadorPagamento` (`processar`, `getComprovante`, `getDescricao`).
- **Formas implementadas:** `Pix`, `Boleto`, `CartaoCredito` e `Dinheiro`.
- **Prova da Extensão (Aberto/Fechado):** 
  - **Novos arquivos adicionados:** `Dinheiro.java`.
  - **Arquivos existentes alterados para adicionar a 4ª forma:** **0 (zero)**[cite: 8, 12].
  - **Conclusão:** O método `pagar` na classe `Pedido` opera apenas sobre a interface `ProcessadorPagamento`, sem nenhum `instanceof` ou verificação de tipo concreta[cite: 8, 10, 12].
- **Critério de Exceção vs. Retorno False:** Retorna `false` para situações de negócio previstas (ex: saldo insuficiente) e lança `IllegalArgumentException`/`IllegalStateException` para erros de uso e estado inválido do objeto[cite: 10, 12].

## Entrega Aula 09 — Tratamento de Exceções

Documentos: [`docs/MAPA_DE_EXCECOES.md`](docs/MAPA_DE_EXCECOES.md) (mapa preenchido, com cenários testados) e [`docs/CHECKLIST_TRATAMENTO_EXCECOES.md`](docs/CHECKLIST_TRATAMENTO_EXCECOES.md).

**Hierarquia (pacote `com.senai.ecommerce.excecao`)** — raiz `ECommerceException` (checked):

| Exceção | Tipo | Dados que carrega | Quando |
|---|---|---|---|
| `EstoqueInsuficienteException` | checked | produto, qtd solicitada, qtd disponível | Quantidade pedida maior que a disponível |
| `PagamentoRecusadoException` | checked | forma de pagamento, motivo | Forma de pagamento não aprovou (ou valor divergente do total) |
| `ClienteNaoEncontradoException` | checked | identificador | Busca por cliente inexistente |
| `PedidoInvalidoException` | checked | número do pedido | Operação incompatível com a situação do pedido (sem itens, já pago, inexistente) |
| `CadastroDuplicadoException` | checked | tipo, identificador | Produto ou cliente já cadastrado |
| `IllegalArgumentException` / `IllegalStateException` | unchecked (da linguagem) | — | Argumento inválido / estado do objeto ou recurso |

**Critério checked × unchecked (único para o projeto):** se quem chamou o método consegue reagir de forma útil (oferecer outro produto, outra forma de pagamento), a exceção é **checked**. Se o problema só se resolve corrigindo o código ou a entrada que chamou (nulo, vazio, negativo, formato), é **unchecked**.

**Camadas:** o domínio (`Produto`, `Pedido`) detecta e lança; os serviços detectam duplicidade e inexistência; a apresentação (`MenuConsole`) trata tudo e converte em mensagem. Nenhuma exceção chega ao usuário como stack trace: falhas inesperadas vão para `logs/ecommerce.log`.

**Atualização da decisão da Aula 08:** pedido sem itens, pedido já pago e item em pedido pago passam a lançar `PedidoInvalidoException` (situação de negócio, checked), em vez de `IllegalStateException`, conforme o Roteiro Prático da Aula 09. `IllegalStateException` fica para estado inconsistente do próprio objeto/recurso.

**Requisitos desejáveis atendidos:** encadeamento de causa (`NumberFormatException` traduzida preservando a causa), try-with-resources (`Scanner`), multi-catch, classe utilitária `Validador` (inclui validação de dígitos de CPF/CNPJ) e mensagens centralizadas em `Mensagens`.
