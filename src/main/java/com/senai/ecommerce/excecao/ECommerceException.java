package com.senai.ecommerce.excecao;

/**
 * Raiz de todas as exceções de negócio do sistema (checked).
 *
 * Permite que a camada mais externa capture qualquer falha do negócio com um único
 * catch, sem capturar o que não é do negócio (NullPointerException, Error etc.).
 *
 * CRITÉRIO DA EQUIPE (único para o projeto inteiro):
 *  - CHECKED (herda desta classe): situação de negócio prevista, em que quem chamou
 *    consegue fazer algo útil (oferecer outro produto, outra forma de pagamento...).
 *  - UNCHECKED (IllegalArgumentException / IllegalStateException da linguagem):
 *    defeito de quem chamou o método (argumento nulo, vazio, negativo, fora do formato).
 */
public class ECommerceException extends Exception {
    public ECommerceException(String mensagem) {
        super(mensagem);
    }

    public ECommerceException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
