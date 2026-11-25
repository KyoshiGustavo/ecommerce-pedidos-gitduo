package com.senai.ecommerce.apresentacao;

import com.senai.ecommerce.excecao.ECommerceException;
import com.senai.ecommerce.modelo.Cliente;
import com.senai.ecommerce.modelo.ItemPedido;
import com.senai.ecommerce.modelo.Pedido;
import com.senai.ecommerce.modelo.Produto;
import com.senai.ecommerce.modelo.pagamento.Pix;
import com.senai.ecommerce.repositorio.FinalizadorPedido;
import com.senai.ecommerce.repositorio.RepositorioCliente;
import com.senai.ecommerce.repositorio.RepositorioException;
import com.senai.ecommerce.repositorio.RepositorioItemPedido;
import com.senai.ecommerce.repositorio.RepositorioPedido;
import com.senai.ecommerce.repositorio.RepositorioProduto;
import com.senai.ecommerce.servico.ClienteServico;

import java.math.BigDecimal;
import java.util.Optional;

public final class ConsoleCrud {
    private final RepositorioProduto produtos;
    private final RepositorioCliente clientes;
    private final RepositorioPedido pedidos;
    private final RepositorioItemPedido itens;
    private final FinalizadorPedido finalizador;

    public ConsoleCrud(RepositorioProduto produtos, RepositorioCliente clientes,
                      RepositorioPedido pedidos, RepositorioItemPedido itens, FinalizadorPedido finalizador) {
        this.produtos = produtos;
        this.clientes = clientes;
        this.pedidos = pedidos;
        this.itens = itens;
        this.finalizador = finalizador;
    }

    public void executar(String[] args) {
        if (args.length <= 1 || "ajuda".equals(args[1])) {
            mostrarAjuda();
            return;
        }
        try {
            if (args.length < 3) {
                throw new IllegalArgumentException("Informe a ação ou use --crud ajuda.");
            }
            switch (args[1]) {
                case "produto" -> produto(args);
                case "cliente" -> cliente(args);
                case "pedido" -> pedido(args);
                case "item" -> item(args);
                default -> throw new IllegalArgumentException("Entidade desconhecida. Use --crud ajuda.");
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("Faltam argumentos. Use --crud ajuda para conferir o formato.");
        } catch (RepositorioException | ECommerceException | IllegalArgumentException | IllegalStateException e) {
            System.err.println(e.getMessage());
        }
    }

    private void produto(String[] args) {
        switch (args[2]) {
            case "criar" -> exibir(produtos.salvar(new Produto(args[3], new BigDecimal(args[4]), inteiro(args[5]))));
            case "buscar" -> exibir(exigir(produtos.buscarPorId(id(args[3])), "Produto"));
            case "nome" -> exibir(exigir(produtos.buscarPorNome(args[3]), "Produto"));
            case "listar" -> produtos.listarTodos().forEach(ConsoleCrud::exibir);
            case "alterar" -> exibir(produtos.salvar(new Produto(id(args[3]), args[4], new BigDecimal(args[5]), 0)));
            case "estoque" -> {
                produtos.atualizarEstoque(id(args[3]), inteiro(args[4]));
                System.out.println("Estoque atualizado.");
            }
            case "inativar", "remover" -> {
                produtos.inativar(id(args[3]));
                System.out.println("Produto inativado.");
            }
            case "reativar" -> {
                produtos.reativar(id(args[3]));
                System.out.println("Produto reativado.");
            }
            default -> throw new IllegalArgumentException("Ação de produto desconhecida. Use --crud ajuda.");
        }
    }

    private void cliente(String[] args) {
        switch (args[2]) {
            case "criar" -> exibir(new ClienteServico(clientes).cadastrar(new Cliente(args[3], args[4], args[5])));
            case "buscar" -> exibir(exigir(clientes.buscarPorId(id(args[3])), "Cliente"));
            case "buscar-email" -> exibir(exigir(clientes.buscarPorEmail(args[3]), "Cliente"));
            case "listar" -> clientes.listarTodos().forEach(ConsoleCrud::exibir);
            case "alterar" -> exibir(clientes.salvar(new Cliente(id(args[3]), args[4], args[5], args[6])));
            case "email" -> {
                clientes.atualizarEmail(id(args[3]), args[4]);
                System.out.println("E-mail atualizado.");
            }
            case "remover" -> {
                clientes.remover(id(args[3]));
                System.out.println("Cliente excluído.");
            }
            default -> throw new IllegalArgumentException("Ação de cliente desconhecida. Use --crud ajuda.");
        }
    }

    private void pedido(String[] args) throws ECommerceException {
        switch (args[2]) {
            case "criar" -> exibir(pedidos.salvar(new Pedido(inteiro(args[3]), clientePorId(args[4]))));
            case "buscar" -> exibir(pedidoPorId(args[3]));
            case "listar" -> pedidos.listarTodos().forEach(ConsoleCrud::exibir);
            case "por-cliente" -> pedidos.listarPorCliente(id(args[3])).forEach(ConsoleCrud::exibir);
            case "por-situacao" -> pedidos.listarPorSituacao(args[3]).forEach(ConsoleCrud::exibir);
            case "alterar" -> exibir(pedidos.salvar(new Pedido(id(args[3]), inteiro(args[4]), clientePorId(args[5]))));
            case "remover" -> {
                pedidos.remover(id(args[3]));
                System.out.println("Pedido aberto excluído.");
            }
            case "finalizar" -> exibir(finalizador.finalizar(pedidoPorId(args[3])));
            case "pagar" -> {
                Pedido pedido = pedidoPorId(args[3]);
                if (!"FINALIZADO".equals(pedido.getStatus())) {
                    throw new IllegalStateException("Finalize o pedido antes de registrar o pagamento.");
                }
                pedido.pagar(new Pix());
                exibir(pedidos.salvar(pedido));
            }
            default -> throw new IllegalArgumentException("Ação de pedido desconhecida. Use --crud ajuda.");
        }
    }

    private void item(String[] args) {
        switch (args[2]) {
            case "criar" -> {
                Produto produto = exigir(produtos.buscarPorId(id(args[4])), "Produto ativo");
                exibir(itens.inserir(id(args[3]), new ItemPedido(produto, inteiro(args[5]))));
            }
            case "buscar" -> exibir(exigir(itens.buscarPorId(id(args[3])), "Item"));
            case "listar" -> itens.listarPorPedido(id(args[3])).forEach(ConsoleCrud::exibir);
            case "alterar" -> {
                ItemPedido item = exigir(itens.buscarPorId(id(args[3])), "Item");
                item.setQuantidade(inteiro(args[4]));
                itens.atualizar(item);
                exibir(item);
            }
            case "remover" -> {
                itens.remover(id(args[3]));
                System.out.println("Item excluído.");
            }
            default -> throw new IllegalArgumentException("Ação de item desconhecida. Use --crud ajuda.");
        }
    }

    private Cliente clientePorId(String valor) {
        return exigir(clientes.buscarPorId(id(valor)), "Cliente");
    }

    private Pedido pedidoPorId(String valor) {
        return exigir(pedidos.buscarPorId(id(valor)), "Pedido");
    }

    private static <T> T exigir(Optional<T> registro, String entidade) {
        return registro.orElseThrow(() -> new RepositorioException(entidade + " não encontrado."));
    }

    private static Long id(String valor) {
        return Long.valueOf(valor);
    }

    private static int inteiro(String valor) {
        return Integer.parseInt(valor);
    }

    private static void exibir(Produto p) {
        System.out.printf("%d | %s | R$ %s | estoque %d%n",
            p.getId(), p.getNome(), p.getPreco(), p.getQuantidadeEmEstoque());
    }

    private static void exibir(Cliente c) {
        System.out.printf("%d | %s%n", c.getId(), c);
    }

    private static void exibir(Pedido p) {
        System.out.printf("%d | %s%n", p.getId(), p);
        p.getItens().forEach(ConsoleCrud::exibir);
        if (p.getComprovante() != null) {
            System.out.println("Comprovante: " + p.getComprovante());
        }
    }

    private static void exibir(ItemPedido i) {
        System.out.printf("  Item %d | %s%n", i.getId(), i);
    }

    private static void mostrarAjuda() {
        System.out.print("""
            Aula 13 — comandos após --crud
              preparar
              produto criar <nome> <preço> <estoque>
              produto buscar <id> | nome <nome> | listar
              produto alterar <id> <nome> <preço> | estoque <id> <quantidade>
              produto inativar <id> | reativar <id>
              cliente criar <nome> <documento> <email> | buscar <id> | buscar-email <email> | listar
              cliente alterar <id> <nome> <documento> <email> | email <id> <email> | remover <id>
              pedido criar <número> <clienteId> | buscar <id> | listar
              pedido por-cliente <clienteId> | por-situacao <ABERTO|FINALIZADO|PAGO>
              pedido alterar <id> <número> <clienteId> | remover <id> | finalizar <id> | pagar <id>
              item criar <pedidoId> <produtoId> <quantidade> | buscar <id> | listar <pedidoId>
              item alterar <id> <quantidade> | remover <id>
            """);
    }
}
