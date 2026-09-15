package com.senai.ecommerce.modelo;

public class Cliente extends Pessoa {
    private String email;

    public Cliente(String nome, String documento, String email) {
        super(nome, documento);
        setEmail(email);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("E-mail inválido.");
        }
        this.email = email.trim();
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