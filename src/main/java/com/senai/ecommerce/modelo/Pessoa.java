package com.senai.ecommerce.modelo;

import com.senai.ecommerce.util.Validador;

public abstract class Pessoa {
    private String nome;
    private String documento;

    public Pessoa(String nome, String documento) {
        setNome(nome);
        setDocumento(documento);
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = Validador.textoObrigatorio(nome, "Nome");
    }

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        this.documento = Validador.documentoValido(documento);
    }

    public abstract String getIdentificacao();
}
