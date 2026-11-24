package com.senai.ecommerce.excecao;

import com.senai.ecommerce.util.Mensagens;

/**
 * Tentativa de cadastrar produto ou cliente que já existe (checked).
 * Quem chamou pode reagir: usar o cadastro existente ou, no caso de produto, repor o estoque.
 */
public class CadastroDuplicadoException extends ECommerceException {
    private final String tipo;
    private final String identificador;

    public CadastroDuplicadoException(String tipo, String identificador) {
        super(String.format(Mensagens.CADASTRO_DUPLICADO, tipo, identificador));
        this.tipo = tipo;
        this.identificador = identificador;
    }

    public String getTipo() {
        return tipo;
    }

    public String getIdentificador() {
        return identificador;
    }
}
