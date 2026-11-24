package com.senai.ecommerce.util;

/**
 * Mensagens de erro centralizadas (requisito desejável da Aula 09).
 *
 * Toda mensagem segue a mesma estrutura: diz O QUE aconteceu, mostra o valor
 * recebido quando houver e orienta O QUE o usuário pode fazer. Centralizar aqui
 * deixa o texto pronto para internacionalização (basta trocar por um ResourceBundle).
 */
public final class Mensagens {

    private Mensagens() {
    }

    // ----------------------------------------------------- Validações genéricas
    public static final String CAMPO_OBRIGATORIO =
            "%s é obrigatório. Recebido: %s. Informe um texto não vazio.";
    public static final String OBJETO_OBRIGATORIO =
            "%s é obrigatório. Recebido: nulo. Informe um valor.";
    public static final String VALOR_POSITIVO =
            "%s inválido. Recebido: %s. Informe um valor maior que zero.";
    public static final String QUANTIDADE_POSITIVA =
            "%s inválida. Recebido: %d. Informe uma quantidade maior que zero.";
    public static final String ESTOQUE_NAO_NEGATIVO =
            "%s inválido. Recebido: %d. Informe zero ou mais unidades.";
    public static final String EMAIL_INVALIDO =
            "E-mail inválido. Recebido: %s. Use o formato nome@dominio.com.";
    public static final String DOCUMENTO_INVALIDO =
            "Documento inválido. Recebido: %s. Informe um CPF (11 dígitos) ou CNPJ (14 dígitos) válido.";
    public static final String NUMERO_INVALIDO =
            "Valor numérico inválido. Recebido: '%s'. Digite apenas números (ex.: 19.90).";

    // ----------------------------------------------------- Pedido
    public static final String PEDIDO_NUMERO_INVALIDO =
            "Número do pedido inválido. Recebido: %s. Informe um número inteiro maior que zero.";
    public static final String PEDIDO_JA_PAGO_ITEM =
            "já está pago e não aceita novos itens. Abra um novo pedido para continuar comprando.";
    public static final String PEDIDO_JA_PAGO =
            "já está pago; a cobrança não será repetida. Nenhuma ação é necessária.";
    public static final String PEDIDO_SEM_ITENS =
            "não possui itens e não pode ser pago. Adicione ao menos um item antes de pagar.";
    public static final String PEDIDO_INEXISTENTE =
            "não existe. Confira o número informado ou crie um novo pedido.";

    // ----------------------------------------------------- Pagamento
    public static final String FORMA_PAGAMENTO_OBRIGATORIA =
            "Forma de pagamento é obrigatória. Recebido: nulo. Escolha Pix, Boleto, Cartão de crédito ou Dinheiro.";
    public static final String VALOR_DIVERGENTE =
            "valor informado R$ %s diverge do total do pedido R$ %s. Confira o valor e tente novamente.";
    public static final String TRANSACAO_NAO_AUTORIZADA =
            "transação não autorizada. Tente outra forma de pagamento.";
    public static final String CARTAO_LIMITE_INSUFICIENTE =
            "limite disponível do cartão (R$ %s) é menor que o valor da compra (R$ %s). Use outro cartão ou outra forma de pagamento.";

    // ----------------------------------------------------- Cadastros
    public static final String CLIENTE_NAO_ENCONTRADO =
            "Cliente não encontrado para o identificador '%s'. Confira o CPF/CNPJ ou cadastre o cliente.";
    public static final String CADASTRO_DUPLICADO =
            "%s já cadastrado: '%s'. Use o cadastro existente em vez de criar outro.";
    public static final String PRODUTO_NAO_ENCONTRADO =
            "Produto '%s' não encontrado. Escolha um dos produtos listados no cadastro.";

    // ----------------------------------------------------- Apresentação
    public static final String ENTRADA_ENCERRADA =
            "A entrada de dados foi encerrada. Reinicie o programa para continuar.";
    public static final String ERRO_INESPERADO =
            "Ocorreu um erro inesperado e a operação foi cancelada. Nada foi alterado; tente novamente.";
}
