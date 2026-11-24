package com.senai.ecommerce.modelo;

import com.senai.ecommerce.util.Validador;

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
        this.matricula = Validador.textoObrigatorio(matricula, "Matrícula");
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = Validador.textoObrigatorio(cargo, "Cargo");
    }

    @Override
    public String getIdentificacao() {
        return getNome() + " (Matrícula: " + matricula + ")";
    }
}
