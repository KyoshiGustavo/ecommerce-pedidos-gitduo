package com.senai.ecommerce.modelo;

import com.senai.ecommerce.excecao.EstoqueInsuficienteException;
import com.senai.ecommerce.excecao.PagamentoRecusadoException;
import com.senai.ecommerce.excecao.PedidoInvalidoException;
import com.senai.ecommerce.modelo.pagamento.ProcessadorPagamento;
import com.senai.ecommerce.util.Mensagens;
import com.senai.ecommerce.util.Validador;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Pedido {
    private static final String ABERTO = "ABERTO";
    private static final String PAGO = "PAGO";

    private Integer numero;
    private Cliente cliente;
    private List<ItemPedido> itens = new ArrayList<>();
    private String status = ABERTO;
    private String comprovante;

    public Pedido(Integer numero, Cliente cliente) {
        if (numero == null || numero <= 0) {
            throw new IllegalArgumentException(
                    String.format(Mensagens.PEDIDO_NUMERO_INVALIDO, Validador.descrever(numero)));
        }
        this.numero = numero;
        this.cliente = Validador.naoNulo(cliente, "Cliente");
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

    public void adicionarItem(Produto produto)
            throws EstoqueInsuficienteException, PedidoInvalidoException {
        adicionarItem(produto, 1);
    }

    public void adicionarItem(Produto produto, int quantidade)
            throws EstoqueInsuficienteException, PedidoInvalidoException {
        // Validar cedo e ANTES de mexer em qualquer estado: se falhar, nada foi alterado.
        Validador.naoNulo(produto, "Produto");
        Validador.quantidadePositiva(quantidade, "Quantidade");
        if (PAGO.equals(this.status)) {
            throw new PedidoInvalidoException(numero, Mensagens.PEDIDO_JA_PAGO_ITEM);
        }

        produto.baixarEstoque(quantidade); // se faltar estoque, lança aqui e nada foi alterado

        // Decisão da equipe (Aula 07): produto repetido soma a quantidade no item existente.
        for (ItemPedido existente : itens) {
            if (existente.getProduto() == produto) {
                existente.setQuantidade(existente.getQuantidade() + quantidade);
                return;
            }
        }
        this.itens.add(new ItemPedido(produto, quantidade));
    }

    public BigDecimal getTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (ItemPedido item : itens) {
            total = total.add(item.getSubtotal());
        }
        return total;
    }

    /** Paga o pedido pelo total calculado. */
    public boolean pagar(ProcessadorPagamento processador)
            throws PagamentoRecusadoException, PedidoInvalidoException {
        return efetuarPagamento(processador, null);
    }

    /** Paga conferindo o valor informado (ex.: vindo de um gateway ou da tela) contra o total do pedido. */
    public boolean pagar(ProcessadorPagamento processador, BigDecimal valorInformado)
            throws PagamentoRecusadoException, PedidoInvalidoException {
        Validador.naoNulo(valorInformado, "Valor do pagamento");
        return efetuarPagamento(processador, valorInformado);
    }

    private boolean efetuarPagamento(ProcessadorPagamento processador, BigDecimal valorInformado)
            throws PagamentoRecusadoException, PedidoInvalidoException {
        if (processador == null) {
            throw new IllegalArgumentException(Mensagens.FORMA_PAGAMENTO_OBRIGATORIA);
        }
        if (PAGO.equals(this.status)) {
            throw new PedidoInvalidoException(numero, Mensagens.PEDIDO_JA_PAGO);
        }
        if (itens.isEmpty()) {
            throw new PedidoInvalidoException(numero, Mensagens.PEDIDO_SEM_ITENS);
        }

        BigDecimal total = getTotal();
        if (valorInformado != null && valorInformado.compareTo(total) != 0) {
            throw new PagamentoRecusadoException(processador.getDescricao(),
                    String.format(Mensagens.VALOR_DIVERGENTE,
                            valorInformado.toPlainString(), total.toPlainString()));
        }

        // A situação só muda DEPOIS da aprovação: pagamento recusado deixa o pedido ABERTO.
        if (!processador.processar(total)) {
            throw new PagamentoRecusadoException(processador.getDescricao(), processador.getMotivoRecusa());
        }

        this.status = PAGO;
        this.comprovante = processador.getComprovante();
        return true;
    }

    @Override
    public String toString() {
        return "Pedido #" + numero + " - Cliente: " + cliente.getNome() + " - Total: R$ " + getTotal() + " - Status: " + status;
    }
}
