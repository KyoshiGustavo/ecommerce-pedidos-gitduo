package com.senai.ecommerce.persistencia;

import com.senai.ecommerce.repositorio.RepositorioException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Statement;

public final class EsquemaBanco {
    private EsquemaBanco() {}

    public static void preparar(ConexaoFactory conexoes) {
        String sql = lerEsquema();
        new Jdbc(conexoes).usar("Não foi possível preparar as tabelas do e-commerce.", conexao -> {
            try (Statement comando = conexao.createStatement()) {
                for (String trecho : sql.split(";")) {
                    if (!trecho.isBlank()) {
                        comando.execute(trecho.trim());
                    }
                }
            }
            return null;
        });
    }

    private static String lerEsquema() {
        try (InputStream entrada = EsquemaBanco.class.getResourceAsStream("/schema.sql")) {
            if (entrada == null) {
                throw new RepositorioException("O arquivo schema.sql não foi encontrado.");
            }
            return new String(entrada.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RepositorioException("Não foi possível ler o schema.sql.", e);
        }
    }
}
