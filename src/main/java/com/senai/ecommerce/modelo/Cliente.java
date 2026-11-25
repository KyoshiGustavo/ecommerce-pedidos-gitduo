package com.senai.ecommerce.modelo;

public class Cliente extends Pessoa {
    private final Long id;
    private String email;

    public Cliente(String nome, String documento, String email) {
        this(null, nome, documento, email);
    }

    public Cliente(Long id, String nome, String documento, String email) {
        super(nome, documento);
        if (id != null && id <= 0) {
            throw new IllegalArgumentException("Id do cliente deve ser positivo.");
        }
        this.id = id;
        setEmail(email);
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = validarEmail(email);
    }

    public static String validarEmail(String email) {
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("E-mail inválido.");
        }
        return email.trim();
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