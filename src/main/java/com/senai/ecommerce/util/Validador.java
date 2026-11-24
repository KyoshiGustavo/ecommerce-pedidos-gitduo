package com.senai.ecommerce.util;

import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * Validações reaproveitadas pelo domínio inteiro (requisito desejável da Aula 09).
 *
 * Todos os métodos lançam {@link IllegalArgumentException} (unchecked): argumento
 * inválido é defeito de quem chamou, não uma situação de negócio recuperável.
 * Cada método devolve o valor já normalizado, o que permite validar e atribuir em uma linha.
 */
public final class Validador {

    private static final Pattern EMAIL =
            Pattern.compile("^[A-Za-z0-9._%+-]+@([A-Za-z0-9-]+\\.)+[A-Za-z]{2,}$");

    private Validador() {
    }

    // ----------------------------------------------------- Básicos
    public static <T> T naoNulo(T valor, String campo) {
        if (valor == null) {
            throw new IllegalArgumentException(String.format(Mensagens.OBJETO_OBRIGATORIO, campo));
        }
        return valor;
    }

    /** Exige texto não nulo e não vazio; devolve o texto sem espaços nas pontas. */
    public static String textoObrigatorio(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(
                    String.format(Mensagens.CAMPO_OBRIGATORIO, campo, descrever(valor)));
        }
        return valor.trim();
    }

    public static BigDecimal valorPositivo(BigDecimal valor, String campo) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(String.format(Mensagens.VALOR_POSITIVO, campo,
                    valor == null ? "nulo" : "R$ " + valor.toPlainString()));
        }
        return valor;
    }

    public static int quantidadePositiva(int quantidade, String campo) {
        if (quantidade <= 0) {
            throw new IllegalArgumentException(
                    String.format(Mensagens.QUANTIDADE_POSITIVA, campo, quantidade));
        }
        return quantidade;
    }

    public static int estoqueNaoNegativo(int quantidade, String campo) {
        if (quantidade < 0) {
            throw new IllegalArgumentException(
                    String.format(Mensagens.ESTOQUE_NAO_NEGATIVO, campo, quantidade));
        }
        return quantidade;
    }

    // ----------------------------------------------------- Formatos
    public static String emailValido(String email) {
        if (email == null || !EMAIL.matcher(email.trim()).matches()) {
            throw new IllegalArgumentException(String.format(Mensagens.EMAIL_INVALIDO, descrever(email)));
        }
        return email.trim();
    }

    /** Aceita CPF (11 dígitos) ou CNPJ (14 dígitos), com ou sem máscara, conferindo os dígitos verificadores. */
    public static String documentoValido(String documento) {
        if (documento != null) {
            String digitos = somenteDigitos(documento);
            boolean valido = (digitos.length() == 11 && cpfValido(digitos))
                    || (digitos.length() == 14 && cnpjValido(digitos));
            if (valido) {
                return documento.trim();
            }
        }
        throw new IllegalArgumentException(String.format(Mensagens.DOCUMENTO_INVALIDO, descrever(documento)));
    }

    public static String somenteDigitos(String texto) {
        return texto == null ? "" : texto.replaceAll("\\D", "");
    }

    /** Descreve o valor recebido para uso em mensagens: nulo, vazio ou o próprio texto entre aspas. */
    public static String descrever(Object valor) {
        if (valor == null) {
            return "nulo";
        }
        String texto = valor.toString();
        return texto.isBlank() ? "vazio" : "'" + texto + "'";
    }

    // ----------------------------------------------------- Dígitos verificadores
    private static boolean cpfValido(String cpf) {
        if (todosIguais(cpf)) {
            return false;
        }
        return digitoCpf(cpf, 9) == cpf.charAt(9) - '0' && digitoCpf(cpf, 10) == cpf.charAt(10) - '0';
    }

    private static int digitoCpf(String cpf, int tamanho) {
        int soma = 0;
        for (int i = 0; i < tamanho; i++) {
            soma += (cpf.charAt(i) - '0') * (tamanho + 1 - i);
        }
        int resto = (soma * 10) % 11;
        return resto == 10 ? 0 : resto;
    }

    private static boolean cnpjValido(String cnpj) {
        if (todosIguais(cnpj)) {
            return false;
        }
        int[] pesos1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int[] pesos2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        return digitoCnpj(cnpj, pesos1) == cnpj.charAt(12) - '0'
                && digitoCnpj(cnpj, pesos2) == cnpj.charAt(13) - '0';
    }

    private static int digitoCnpj(String cnpj, int[] pesos) {
        int soma = 0;
        for (int i = 0; i < pesos.length; i++) {
            soma += (cnpj.charAt(i) - '0') * pesos[i];
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }

    private static boolean todosIguais(String digitos) {
        return digitos.chars().distinct().count() == 1;
    }
}
