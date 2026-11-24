package com.senai.ecommerce.apresentacao;

import com.senai.ecommerce.excecao.CadastroDuplicadoException;
import com.senai.ecommerce.excecao.ClienteNaoEncontradoException;
import com.senai.ecommerce.excecao.ECommerceException;
import com.senai.ecommerce.excecao.EstoqueInsuficienteException;
import com.senai.ecommerce.excecao.PagamentoRecusadoException;
import com.senai.ecommerce.excecao.PedidoInvalidoException;
import com.senai.ecommerce.modelo.Cliente;
import com.senai.ecommerce.modelo.Pedido;
import com.senai.ecommerce.modelo.Produto;
import com.senai.ecommerce.modelo.pagamento.Boleto;
import com.senai.ecommerce.modelo.pagamento.CartaoCredito;
import com.senai.ecommerce.modelo.pagamento.Dinheiro;
import com.senai.ecommerce.modelo.pagamento.Pix;
import com.senai.ecommerce.modelo.pagamento.ProcessadorPagamento;
import com.senai.ecommerce.servico.ClienteServico;
import com.senai.ecommerce.servico.PedidoServico;
import com.senai.ecommerce.servico.ProdutoServico;
import com.senai.ecommerce.util.Mensagens;
import com.senai.ecommerce.util.RegistroErros;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;

/**
 * Camada de apresentação: é a ÚLTIMA que fala com o usuário, então é aqui que toda exceção
 * que sobrou vira mensagem civilizada. Domínio e serviços detectam e lançam; esta camada decide.
 */
public class MenuConsole {

    /** Uma ação do menu: pode lançar qualquer falha de negócio; o tratamento é único, em executarAcao. */
    @FunctionalInterface
    private interface Acao {
        void executar() throws ECommerceException;
    }

    private final Scanner entrada;
    private final PrintStream saida;
    private final ProdutoServico produtos = new ProdutoServico();
    private final ClienteServico clientes = new ClienteServico();
    private final PedidoServico pedidos = new PedidoServico();

    public MenuConsole(Scanner entrada, PrintStream saida) {
        this.entrada = entrada;
        this.saida = saida;
    }

    public void iniciar() {
        saida.println("=== E-commerce — Gestão de Pedidos ===");
        boolean continuar = true;
        while (continuar && entrada.hasNextLine()) {
            exibirMenu();
            continuar = tratarOpcao(entrada.nextLine().trim());
        }
        saida.println("Até logo!");
    }

    private void exibirMenu() {
        saida.println();
        saida.println("1) Cadastrar produto        5) Pagar pedido");
        saida.println("2) Cadastrar cliente        6) Listar tudo");
        saida.println("3) Criar pedido             7) Executar bateria de cenários de erro");
        saida.println("4) Adicionar item a pedido  8) Carregar dados de exemplo");
        saida.println("0) Sair");
        saida.print("Opção: ");
    }

    private boolean tratarOpcao(String opcao) {
        switch (opcao) {
            case "1" -> executarAcao(this::cadastrarProduto);
            case "2" -> executarAcao(this::cadastrarCliente);
            case "3" -> executarAcao(this::criarPedido);
            case "4" -> executarAcao(this::adicionarItem);
            case "5" -> executarAcao(this::pagarPedido);
            case "6" -> listarTudo();
            case "7" -> new CenariosDeErro().executarEImprimir(saida);
            case "8" -> executarAcao(this::carregarExemplos);
            case "0" -> {
                return false;
            }
            default -> saida.println("Opção inválida: '" + opcao + "'. Digite um número de 0 a 8.");
        }
        return true;
    }

    // ===================================================== Tratamento centralizado

    /**
     * Do catch mais específico para o mais genérico. O programa nunca morre: informa, orienta e volta ao menu.
     */
    private void executarAcao(Acao acao) {
        try {
            acao.executar();
        } catch (EstoqueInsuficienteException e) {
            // Tem contexto para decidir: informa o disponível e sugere alternativas.
            saida.println("Não foi possível adicionar: " + e.getMessage());
            saida.println("Disponível agora: " + e.getQuantidadeDisponivel() + " unidade(s) de " + e.getProduto().getNome() + ".");
            List<Produto> alternativas = produtos.comEstoqueMinimo(e.getQuantidadeSolicitada(), e.getProduto());
            if (!alternativas.isEmpty()) {
                saida.println("Produtos com estoque suficiente para " + e.getQuantidadeSolicitada() + " unidade(s):");
                alternativas.forEach(p -> saida.println("  - " + p));
            }
        } catch (PagamentoRecusadoException e) {
            saida.println("Pagamento não concluído: " + e.getMessage());
            saida.println("O pedido continua ABERTO. Tente outra forma de pagamento.");
        } catch (ClienteNaoEncontradoException | PedidoInvalidoException e) {
            // Multi-catch: mesmo tratamento para dois tipos de exceção.
            saida.println("Não foi possível concluir: " + e.getMessage());
        } catch (CadastroDuplicadoException e) {
            saida.println("Cadastro não realizado: " + e.getMessage());
        } catch (ECommerceException e) {
            // Rede de segurança do negócio: qualquer falha de negócio futura vira mensagem, nunca stack trace.
            saida.println("Não foi possível concluir: " + e.getMessage());
        } catch (IllegalArgumentException | IllegalStateException e) {
            saida.println("Dado inválido: " + e.getMessage());
        } catch (RuntimeException e) {
            // Último recurso, só nesta fronteira com o usuário: o rastro completo vai para o log
            // (nunca para a tela) e o usuário recebe uma mensagem útil. Nada de catch vazio.
            RegistroErros.registrar("Falha inesperada no menu", e);
            saida.println(Mensagens.ERRO_INESPERADO);
            if (RegistroErros.registroEmArquivo()) {
                saida.println("(Detalhe técnico registrado em logs/ecommerce.log)");
            }
        }
    }

    // ===================================================== Ações

    private void cadastrarProduto() throws CadastroDuplicadoException {
        String nome = lerTexto("Nome do produto: ");
        BigDecimal preco = lerDecimal("Preço (ex.: 19.90): ");
        int estoque = lerInteiro("Quantidade em estoque: ");
        Produto produto = produtos.cadastrar(nome, preco, estoque);
        saida.println("Produto cadastrado: " + produto);
    }

    private void cadastrarCliente() throws CadastroDuplicadoException {
        String nome = lerTexto("Nome do cliente: ");
        String documento = lerTexto("CPF ou CNPJ: ");
        String email = lerTexto("E-mail: ");
        Cliente cliente = clientes.cadastrar(nome, documento, email);
        saida.println("Cliente cadastrado: " + cliente);
    }

    private void criarPedido() throws ClienteNaoEncontradoException {
        Cliente cliente = clientes.buscarPorDocumento(lerTexto("CPF ou CNPJ do cliente: "));
        Pedido pedido = pedidos.criar(cliente);
        saida.println("Pedido criado: " + pedido);
    }

    private void adicionarItem() throws EstoqueInsuficienteException, PedidoInvalidoException {
        Pedido pedido = pedidos.buscar(lerInteiro("Número do pedido: "));
        String nomeProduto = lerTexto("Nome do produto: ");
        Produto produto = produtos.buscarPorNome(nomeProduto).orElseThrow(
                () -> new IllegalArgumentException(String.format(Mensagens.PRODUTO_NAO_ENCONTRADO, nomeProduto)));
        int quantidade = lerInteiro("Quantidade: ");
        pedido.adicionarItem(produto, quantidade);
        saida.println("Item adicionado. " + pedido);
    }

    private void pagarPedido() throws PedidoInvalidoException, PagamentoRecusadoException {
        Pedido pedido = pedidos.buscar(lerInteiro("Número do pedido: "));
        saida.println("Total do pedido: R$ " + pedido.getTotal().toPlainString());
        ProcessadorPagamento forma = escolherForma();
        String valorDigitado = lerTexto("Valor a pagar (Enter = usar o total): ", true);
        if (valorDigitado.isBlank()) {
            pedido.pagar(forma);
        } else {
            pedido.pagar(forma, converterDecimal(valorDigitado));
        }
        saida.println("Pagamento aprovado! Comprovante: " + pedido.getComprovante());
        saida.println(pedido);
    }

    /** Opção desconhecida devolve null de propósito: o domínio é quem recusa, com mensagem clara. */
    private ProcessadorPagamento escolherForma() {
        String opcao = lerTexto("Forma de pagamento (1-Pix, 2-Boleto, 3-Cartão, 4-Dinheiro): ");
        switch (opcao) {
            case "1":
                return new Pix();
            case "2":
                return new Boleto();
            case "3":
                String limite = lerTexto("Limite disponível do cartão (Enter = sem limite): ", true);
                return limite.isBlank() ? new CartaoCredito() : new CartaoCredito(converterDecimal(limite));
            case "4":
                return new Dinheiro();
            default:
                return null;
        }
    }

    private void listarTudo() {
        saida.println("--- Produtos ---");
        produtos.listar().forEach(p -> saida.println("  " + p));
        saida.println("--- Clientes ---");
        clientes.listar().forEach(c -> saida.println("  " + c));
        saida.println("--- Pedidos ---");
        pedidos.listar().forEach(p -> saida.println("  " + p));
    }

    private void carregarExemplos() throws CadastroDuplicadoException {
        produtos.cadastrar("Notebook", new BigDecimal("3500.00"), 3);
        produtos.cadastrar("Mouse", new BigDecimal("100.00"), 20);
        produtos.cadastrar("Teclado", new BigDecimal("250.00"), 10);
        clientes.cadastrar("Ana Silva", "529.982.247-25", "ana.silva@email.com");
        saida.println("Exemplos carregados: Notebook (estoque 3), Mouse, Teclado e a cliente Ana Silva (CPF 529.982.247-25).");
    }

    // ===================================================== Leitura de entrada

    private String lerTexto(String pergunta) {
        return lerTexto(pergunta, false);
    }

    private String lerTexto(String pergunta, boolean permitirVazio) {
        saida.print(pergunta);
        if (!entrada.hasNextLine()) {
            throw new IllegalStateException(Mensagens.ENTRADA_ENCERRADA);
        }
        String texto = entrada.nextLine();
        return permitirVazio ? texto.trim() : texto;
    }

    private int lerInteiro(String pergunta) {
        String texto = lerTexto(pergunta);
        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException e) {
            // Tradução com causa preservada: a mensagem fala a língua do usuário, o rastro original fica.
            throw new IllegalArgumentException(String.format(Mensagens.NUMERO_INVALIDO, texto), e);
        }
    }

    private BigDecimal lerDecimal(String pergunta) {
        return converterDecimal(lerTexto(pergunta));
    }

    private BigDecimal converterDecimal(String texto) {
        try {
            return new BigDecimal(texto.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(String.format(Mensagens.NUMERO_INVALIDO, texto), e);
        }
    }
}
