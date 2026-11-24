package com.senai.ecommerce;

import com.senai.ecommerce.apresentacao.CenariosDeErro;
import com.senai.ecommerce.apresentacao.MenuConsole;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * Ponto de entrada.
 *
 *   (sem argumentos)   menu interativo de console
 *   --cenarios         executa a bateria de cenários de erro e imprime previsto x obtido
 *   --cenarios-md      idem, em linhas de tabela Markdown (para preencher o Mapa de Exceções)
 */
public class Aplicacao {
    public static void main(String[] args) {
        if (args.length > 0 && "--cenarios".equals(args[0])) {
            boolean todosOk = new CenariosDeErro().executarEImprimir(System.out);
            System.exit(todosOk ? 0 : 1);
        } else if (args.length > 0 && "--cenarios-md".equals(args[0])) {
            new CenariosDeErro().imprimirMarkdown(System.out);
            return;
        }

        // try-with-resources: o Scanner é fechado aconteça o que acontecer.
        try (Scanner entrada = new Scanner(System.in, StandardCharsets.UTF_8)) {
            new MenuConsole(entrada, System.out).iniciar();
        }
    }
}
