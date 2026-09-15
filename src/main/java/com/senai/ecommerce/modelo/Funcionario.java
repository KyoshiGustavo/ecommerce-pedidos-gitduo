package com.senai.ecommerce.modelo;

public class Funcionario extends Pessoa {
    private String matricula;
    private String cargo;

    public Funcionario(String nome, String documento, String matricula, String cargo) {
        super(nome, documento);
        setMatricula(matricula);
        setCargo(cargo);
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        if (matricula == null || matricula.isBlank()) {
            throw new IllegalArgumentException("Matrícula é obrigatória");
        }
        this.matricula = matricula;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        if (cargo == null || cargo.isBlank()) {
            throw new IllegalArgumentException("Cargo é obrigatório");
        }
        this.cargo = cargo;
    }

    @Override
    public String getIdentificacao() {
        return getNome() + " (Matrícula: " + matricula + ")";
    }
}