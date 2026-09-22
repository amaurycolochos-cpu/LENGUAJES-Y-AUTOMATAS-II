package mx.edu.tecnm.semantico;

import java.util.Scanner;

public class BancoEjercicios {
    private static final String[] NOMBRES = {
        "Tabla de símbolos",
        "Expresiones aritméticas",
        "Comprobación de tipos",
        "Concatenación de cadenas",
        "Variable duplicada",
        "Variable no declarada",
        "Asignación incompatible",
        "División entre cero",
        "Errores múltiples",
        "Caso integrador"
    };

    public static void main(String[] args) {
        int ejercicio;

        if (args.length > 0) {
            ejercicio = leerNumero(args[0]);
        } else {
            mostrarMenu();
            try (Scanner entrada = new Scanner(System.in)) {
                System.out.print("Selecciona un ejercicio: ");
                ejercicio = leerNumero(entrada.nextLine());
            }
        }

        if (ejercicio < 1 || ejercicio > NOMBRES.length) {
            System.out.println("Elige un número del 1 al 10.");
            return;
        }

        String numero = String.format("%02d", ejercicio);
        String archivo = "ejemplos/banco_ejercicios/ejercicio_" + numero + ".txt";

        System.out.println("\nEjercicio " + ejercicio + ": " + NOMBRES[ejercicio - 1]);
        System.out.println("Archivo: " + archivo);

        // el análisis se realiza con el mismo programa utilizado en la actividad 1.2
        Main.main(new String[] {archivo});
    }

    private static int leerNumero(String texto) {
        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static void mostrarMenu() {
        System.out.println("\nBanco de ejercicios de análisis semántico");
        System.out.println("-----------------------------------------");

        for (int i = 0; i < NOMBRES.length; i++) {
            System.out.printf("%2d. %s%n", i + 1, NOMBRES[i]);
        }

        System.out.println();
    }
}
