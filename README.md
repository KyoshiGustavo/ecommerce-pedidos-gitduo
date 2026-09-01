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