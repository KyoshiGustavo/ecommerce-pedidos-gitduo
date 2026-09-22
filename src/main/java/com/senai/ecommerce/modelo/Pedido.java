package com.senai.ecommerce.modelo;

import com.senai.ecommerce.modelo.pagamento.ProcessadorPagamento;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Pedido {
    private Integer numero;
    private Cliente cliente;
    private List<ItemPedido> itens = new ArrayList<>();
    private String status = "ABERTO";
    private String comprovante;

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

    public String getStatus() {
        return status;
    }

    public String getComprovante() {
        return comprovante;
    }

    // Sobrecarga exigida na aula: a versão simples delega para a completa
    public void adicionarItem(Produto produto) {
        adicionarItem(produto, 1);
    }

    public void adicionarItem(Produto produto, int quantidade) {
        if (produto == null) {
            throw new IllegalArgumentException("Produto não pode ser nulo.");
        }
        if (quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero.");
        }
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

    // Método de pagamento polimórfico sem citar nenhuma classe concreta
    public boolean pagar(ProcessadorPagamento processador) {
        if (processador == null) {
            throw new IllegalArgumentException("Forma de pagamento é obrigatória.");
        }
        if (itens.isEmpty()) {
            throw new IllegalStateException("Pedido sem itens não pode ser pago.");
        }

        boolean aprovado = processador.processar(getTotal());
        if (aprovado) {
            this.status = "PAGO";
            this.comprovante = processador.getComprovante();
        }
        return aprovado;
    }

    @Override
    public String toString() {
        return "Pedido #" + numero + " - Cliente: " + cliente.getNome() + " - Total: R$ " + getTotal() + " - Status: " + status;
    }
}