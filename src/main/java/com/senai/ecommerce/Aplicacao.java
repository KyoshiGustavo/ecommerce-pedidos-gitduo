package com.senai.ecommerce;

import com.senai.ecommerce.apresentacao.ConsoleCrud;
import com.senai.ecommerce.excecao.EstoqueInsuficienteException;
import com.senai.ecommerce.persistencia.ClienteDAO;
import com.senai.ecommerce.persistencia.ConexaoFactory;
import com.senai.ecommerce.persistencia.EsquemaBanco;
import com.senai.ecommerce.persistencia.FinalizadorPedidoJdbc;
import com.senai.ecommerce.persistencia.ItemPedidoDAO;
import com.senai.ecommerce.persistencia.PedidoDAO;
import com.senai.ecommerce.persistencia.ProdutoDAO;
import com.senai.ecommerce.repositorio.RepositorioException;
import com.senai.ecommerce.modelo.Cliente;
import com.senai.ecommerce.modelo.Pedido;
import com.senai.ecommerce.modelo.Produto;
import java.math.BigDecimal;

public class Aplicacao {
    public static void main(String[] args) {
        if (args.length > 0 && "--crud".equals(args[0])) {
            executarCrud(args);
            return;
        }
        demonstrarDominio();
    }

    private static void executarCrud(String[] args) {
        ConexaoFactory conexoes = ConexaoFactory.doAmbiente();
        if (args.length == 2 && "preparar".equals(args[1])) {
            try {
                EsquemaBanco.preparar(conexoes);
                System.out.println("Tabelas preparadas.");
            } catch (RepositorioException e) {
                System.err.println(e.getMessage());
            }
            return;
        }
        new ConsoleCrud(new ProdutoDAO(conexoes), new ClienteDAO(conexoes), new PedidoDAO(conexoes),
            new ItemPedidoDAO(conexoes), new FinalizadorPedidoJdbc(conexoes)).executar(args);
    }

    private static void demonstrarDominio() {
        try {
            Cliente cliente = new Cliente("João Silva", "123.456.789-00", "joao@email.com");
            Produto notebook = new Produto("Notebook", new BigDecimal("3500.00"), 3);

            Pedido pedido = new Pedido(1, cliente);

            System.out.println("--- Tentando adicionar 2 notebooks (Estoque: 3) ---");
            pedido.adicionarItem(notebook, 2);
            System.out.println("Item adicionado com sucesso! Estoque restante: " + notebook.getQuantidadeEmEstoque());

            System.out.println("\n--- Tentando adicionar + 5 notebooks (Ultrapassa estoque) ---");
            pedido.adicionarItem(notebook, 5);

        } catch (EstoqueInsuficienteException e) {
            System.out.println("Erro de Negócio Capturado: " + e.getMessage());
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("Erro de Validação: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Erro inesperado: " + e.getMessage());
        }

        System.out.println("\n--- O programa continuou executando normalmente sem fechar! ---");
    }
}