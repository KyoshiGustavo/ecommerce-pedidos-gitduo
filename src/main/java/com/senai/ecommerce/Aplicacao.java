package com.senai.ecommerce;

import com.senai.ecommerce.excecao.EstoqueInsuficienteException;
import com.senai.ecommerce.modelo.Cliente;
import com.senai.ecommerce.modelo.Pedido;
import com.senai.ecommerce.modelo.Produto;
import java.math.BigDecimal;

public class Aplicacao {
    public static void main(String[] args) {
        try {
            Cliente cliente = new Cliente("João Silva", "joao@email.com", "123.456.789-00");
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