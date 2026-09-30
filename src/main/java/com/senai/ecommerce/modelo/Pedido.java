package com.senai.ecommerce.modelo;

import com.senai.ecommerce.excecao.EstoqueInsuficienteException;
import com.senai.ecommerce.excecao.PagamentoRecusadoException;
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

    public void adicionarItem(Produto produto) throws EstoqueInsuficienteException {
        adicionarItem(produto, 1);
    }

    public void adicionarItem(Produto produto, int quantidade) throws EstoqueInsuficienteException {
        if (produto == null) {
            throw new IllegalArgumentException("Produto não pode ser nulo.");
        }
        if (quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero.");
        }
        if ("PAGO".equals(this.status)) {
            throw new IllegalStateException("Não é possível adicionar itens a um pedido já pago.");
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

    public boolean pagar(ProcessadorPagamento processador) throws PagamentoRecusadoException {
        if (processador == null) {
            throw new IllegalArgumentException("Forma de pagamento é obrigatória.");
        }
        if (itens.isEmpty()) {
            throw new IllegalStateException("Pedido sem itens não pode ser pago.");
        }
        if ("PAGO".equals(this.status)) {
            throw new IllegalStateException("Pedido já se encontra pago.");
        }

        boolean aprovado = processador.processar(getTotal());
        if (!aprovado) {
            throw new PagamentoRecusadoException(processador.getDescricao(), "Transação não autorizada");
        }

        this.status = "PAGO";
        this.comprovante = processador.getComprovante();
        return true;
    }

    @Override
    public String toString() {
        return "Pedido #" + numero + " - Cliente: " + cliente.getNome() + " - Total: R$ " + getTotal() + " - Status: " + status;
    }
}