package com.senai.ecommerce.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Registro técnico de falhas inesperadas.
 *
 * O stack trace completo vai para o arquivo logs/ecommerce.log, para quem desenvolve;
 * o usuário vê apenas uma mensagem civilizada. Substitui a impressão do rastro na tela,
 * que é depuração esquecida no código e despeja o rastro na frente do usuário.
 */
public final class RegistroErros {

    private static final Logger LOGGER = Logger.getLogger("com.senai.ecommerce");
    private static final boolean EM_ARQUIVO = configurar();

    private RegistroErros() {
    }

    private static boolean configurar() {
        LOGGER.setUseParentHandlers(false); // nada de rastro no console do usuário
        try {
            Files.createDirectories(Path.of("logs"));
            FileHandler arquivo = new FileHandler("logs/ecommerce.log", true);
            arquivo.setEncoding("UTF-8");
            arquivo.setFormatter(new SimpleFormatter());
            LOGGER.addHandler(arquivo);
            return true;
        } catch (IOException | SecurityException e) {
            return false; // sem arquivo, a falha ainda é tratada; apenas não fica registrada
        }
    }

    /** Grava a falha com a causa completa. */
    public static void registrar(String contexto, Throwable erro) {
        LOGGER.log(Level.SEVERE, contexto, erro);
    }

    public static boolean registroEmArquivo() {
        return EM_ARQUIVO;
    }
}
