package com.senai.ecommerce.modelo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FuncionarioTest {
    private Funcionario funcionario;

    @BeforeEach
    void prepararCenario() {
        funcionario = new Funcionario("Ana Silva", "529.982.247-25", "F01", "Atendente");
    }

    @Test
    @DisplayName("Deve aceitar matrícula e cargo válidos na criação")
    void deveCriarFuncionarioQuandoDadosValidos() {
        Funcionario cadastrado = new Funcionario("Ana Silva", "529.982.247-25", "  F02  ", "  Gerente  ");

        assertAll(
            () -> assertEquals("F02", cadastrado.getMatricula()),
            () -> assertEquals("Gerente", cadastrado.getCargo())
        );
    }

    @Test
    @DisplayName("Deve atualizar o cargo quando o novo texto é válido")
    void deveAlterarCargoQuandoValido() {
        funcionario.setCargo("  Supervisor  ");

        assertEquals("Supervisor", funcionario.getCargo());
    }

    @Test
    @DisplayName("Deve atualizar a matrícula quando o novo texto é válido")
    void deveAlterarMatriculaQuandoValida() {
        funcionario.setMatricula("  F03  ");

        assertEquals("F03", funcionario.getMatricula());
    }

    @Test
    @DisplayName("Deve manter a matrícula quando a alteração é vazia")
    void deveManterMatriculaQuandoAlteracaoVazia() {
        assertThrows(IllegalArgumentException.class, () -> funcionario.setMatricula(" "));

        assertEquals("F01", funcionario.getMatricula());
    }

    @Test
    @DisplayName("Deve manter o cargo quando a alteração é nula")
    void deveManterCargoQuandoAlteracaoNula() {
        assertThrows(IllegalArgumentException.class, () -> funcionario.setCargo(null));

        assertEquals("Atendente", funcionario.getCargo());
    }
}
