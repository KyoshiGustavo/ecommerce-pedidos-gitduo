package com.senai.ecommerce;

import com.senai.ecommerce.modelo.Cliente;
import com.senai.ecommerce.modelo.ItemPedido;
import com.senai.ecommerce.modelo.Pedido;
import com.senai.ecommerce.modelo.Produto;

public class Aplicacao {

    public static void main(String[] args) {
        System.out.println("=== TESTE POO - E-COMMERCE DE PEDIDOS ===\n");

        // 1. Criando Cliente
        Cliente cliente1 = new Cliente(1L, "João Silva", "123.456.789-00", "joao@email.com", "(16) 99999-8888");
        System.out.println("Cliente cadastrado: " + cliente1.getIdentificacao());

        // 2. Criando Produtos
        Produto p1 = new Produto("P001", "Teclado Mecânico", "Teclado RGB Switch Blue", 250.00, 10);
        Produto p2 = new Produto("P002", "Mouse Gamer", "Mouse 10000 DPI", 120.00, 15);

        System.out.println("\n--- Produtos Disponíveis ---");
        System.out.println(p1);
        System.out.println(p2);

        // 3. Criando Pedido para o Cliente
        Pedido pedido = new Pedido(cliente1);

        // 4. Adicionando Itens ao Pedido
        if (p1.temEstoqueDisponivel(2)) {
            ItemPedido item1 = new ItemPedido(p1, 2);
            pedido.adicionarItem(item1);
            p1.baixarEstoque(2);
        }

        if (p2.temEstoqueDisponivel(1)) {
            ItemPedido item2 = new ItemPedido(p2, 1);
            pedido.adicionarItem(item2);
            p2.baixarEstoque(1);
        }

        // 5. Exibindo resumo do Pedido
        System.out.println("\n--- Resumo do Pedido ---");
        System.out.println("Número do Pedido: " + pedido.getNumero());
        System.out.println("Cliente: " + pedido.getCliente().getNome());
        System.out.println("Itens do Pedido:");
        for (ItemPedido item : pedido.getItens()) {
            System.out.println(" - " + item);
        }
        System.out.printf("VALOR TOTAL: R$ %.2f\n", pedido.calcularValorTotal());

        // 6. Verificando estoque atualizado
        System.out.println("\n--- Estoque Após a Compra ---");
        System.out.println(p1);
        System.out.println(p2);
    }
}