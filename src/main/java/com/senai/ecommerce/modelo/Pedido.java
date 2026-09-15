package com.senai.ecommerce.modelo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Pedido {
    private Integer numero;
    private Cliente cliente;
    private List<ItemPedido> itens = new ArrayList<>();

    public Pedido(Integer numero, Cliente cliente) {
        if (numero == null || numero <= 0) {
            throw new IllegalArgumentException("Número do pedido inválido.");
        }
        if (cliente == null) {
            throw new IllegalArgumentException("Cliente é obrigatório.");
        }
        this.numero = numero;
        this.cliente = cliente;
    }

    public Integer getNumero() {
        return numero;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public List<ItemPedido> getItens() {
        return Collections.unmodifiableList(itens);
    }

    public void adicionarItem(Produto produto, int quantidade) {
        produto.baixarEstoque(quantidade);
        this.itens.add(new ItemPedido(produto, quantidade));
    }

    public BigDecimal getTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (ItemPedido item : itens) {
            total = total.add(item.getSubtotal());
        }
        return total;
    }

    @Override
    public String toString() {
        return "Pedido #" + numero + " - Cliente: " + cliente.getNome() + " - Total: R$ " + getTotal();
    }
}