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
ecommerce-pedidos-gitduo/
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com/senai/ecommerce/
│   │           ├── modelo/
│   │           ├── servico/
│   │           ├── repositorio/
│   │           └── util/
│   └── test/
│       └── java/
│           └── com/senai/ecommerce/
├── pom.xml
├── README.md
└── .gitignore

## Como rodar o projeto

1. Clone o repositório.
2. Abra o projeto na sua IDE Java (VS Code ou Eclipse/IntelliJ).

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

## Aula 13 — CRUD completo com JDBC

O back-end mantém a demonstração original e oferece um modo de CRUD e finalização pelo terminal. O front-end continua disponível como projeto separado, com os dados de demonstração que já usava.

### Configuração no Windows

Requer JDK 17 ou superior, Maven e um servidor MySQL 8. Crie o banco vazio com `CREATE DATABASE ecommerce CHARACTER SET utf8mb4;`. Em um terminal **PowerShell**, ajuste as variáveis para seu usuário e sua senha local.

```powershell
$env:ECOMMERCE_DB_URL = "jdbc:mysql://localhost:3306/ecommerce"
$env:ECOMMERCE_DB_USER = "root"
$env:ECOMMERCE_DB_PASSWORD = "SUA_SENHA_LOCAL"
mvn compile exec:java "-Dexec.args=--crud preparar"
```

O comando `preparar` aplica [schema.sql](src/main/resources/schema.sql) e pode ser repetido em um banco com esse mesmo esquema. Também é possível executar o arquivo diretamente no MySQL Workbench, com o banco `ecommerce` selecionado.

### Testar pelo terminal

```powershell
mvn compile exec:java "-Dexec.args=--crud ajuda"
mvn compile exec:java "-Dexec.args=--crud produto criar Notebook 3000.00 5"
mvn compile exec:java "-Dexec.args=--crud cliente criar Ana 52998224725 ana@exemplo.com"
mvn compile exec:java "-Dexec.args=--crud pedido criar 101 1"
mvn compile exec:java "-Dexec.args=--crud item criar 1 1 2"
mvn compile exec:java "-Dexec.args=--crud pedido finalizar 1"
mvn compile exec:java "-Dexec.args=--crud pedido pagar 1"
mvn compile exec:java "-Dexec.args=--crud produto listar"
```

Os ids `1` são exemplos para banco vazio. Use os ids que os comandos imprimirem no seu banco. O rascunho começa em `ABERTO`, reserva estoque no banco ao finalizar e passa para `FINALIZADO`. O comando `pagar` usa o Pix simulado do projeto e registra `PAGO`. Preço deve ser escrito com ponto decimal. A alteração de nome e preço do produto preserva o estoque; use a operação `estoque` para corrigi-lo.

### Verificar a suíte

```powershell
mvn clean test
```

Os testes de integração usam H2 em memória no modo MySQL por padrão. Para rodá-los em um **servidor de testes** com o Connector/J, aponte `aula13.jdbcServidor` para o servidor sem o nome do banco. A conta precisa criar e excluir esquemas temporários com nomes gerados pelos testes. Use `ECOMMERCE_TEST_DB_USER` e `ECOMMERCE_TEST_DB_PASSWORD` para configurar as credenciais desse servidor.

```powershell
mvn test "-Daula13.jdbcServidor=jdbc:mysql://localhost:3306/"
```

- [Políticas de exclusão](docs/POLITICAS_EXCLUSAO_AULA_13.md)
- [Registro de teste cruzado](docs/REGISTRO_DE_TESTE_CRUZADO_AULA_13.md)
- [Checklist preenchido](docs/CHECKLIST_CRUD_COMPLETO_AULA_13.docx)
- [Exercícios dos slides](docs/EXERCICIOS_AULA_13.md)
