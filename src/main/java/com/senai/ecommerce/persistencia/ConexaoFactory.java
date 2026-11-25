package com.senai.ecommerce.persistencia;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class ConexaoFactory {
    private final String url;
    private final String usuario;
    private final String senha;

    public ConexaoFactory(String url, String usuario, String senha) {
        this.url = url;
        this.usuario = usuario;
        this.senha = senha;
    }

    public static ConexaoFactory doAmbiente() {
        return new ConexaoFactory(
            System.getenv().getOrDefault("ECOMMERCE_DB_URL", "jdbc:mysql://localhost:3306/ecommerce"),
            System.getenv().getOrDefault("ECOMMERCE_DB_USER", "root"),
            System.getenv().getOrDefault("ECOMMERCE_DB_PASSWORD", "")
        );
    }

    Connection abrir() throws SQLException {
        return DriverManager.getConnection(url, usuario, senha);
    }
}
