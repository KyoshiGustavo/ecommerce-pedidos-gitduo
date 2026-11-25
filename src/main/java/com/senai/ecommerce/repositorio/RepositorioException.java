package com.senai.ecommerce.repositorio;

public class RepositorioException extends RuntimeException {
    public RepositorioException(String mensagem) {
        super(mensagem);
    }

    public RepositorioException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
