package com.senai.ecommerce.modelo;

import com.senai.ecommerce.util.Validador;

public class Cliente extends Pessoa {
    private String email;

    // Ordem dos parâmetros: nome, documento, e-mail
    public Cliente(String nome, String documento, String email) {
        super(nome, documento);
        setEmail(email);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = Validador.emailValido(email);
    }

    @Override
    public String getIdentificacao() {
        return getNome() + " (CPF/CNPJ: " + getDocumento() + ")";
    }

    @Override
    public String toString() {
        return "Cliente: " + getIdentificacao() + " | Email: " + email;
    }
}
