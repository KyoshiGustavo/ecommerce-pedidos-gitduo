package com.senai.ecommerce.excecao;

import com.senai.ecommerce.util.Mensagens;

/**
 * Busca de cliente por identificador inexistente (checked).
 * Quem chamou pode reagir: pedir para conferir o documento ou oferecer o cadastro.
 */
public class ClienteNaoEncontradoException extends ECommerceException {
    private final String identificador;

    public ClienteNaoEncontradoException(String identificador) {
        super(String.format(Mensagens.CLIENTE_NAO_ENCONTRADO, identificador));
        this.identificador = identificador;
    }

    public String getIdentificador() {
        return identificador;
    }
}
