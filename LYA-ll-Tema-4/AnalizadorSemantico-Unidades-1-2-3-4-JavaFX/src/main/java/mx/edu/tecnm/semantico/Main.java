package mx.edu.tecnm.semantico;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        Path archivo = args.length > 0
                ? Path.of(args[0])
                : Path.of("ejemplos/programa_con_errores.txt");

        try {
            List<String> codigo = Files.readAllLines(archivo);
            AnalizadorSemantico analizador = new AnalizadorSemantico();
            analizador.analizar(codigo);

            imprimirCodigo(codigo);
            imprimirTabla(analizador.getTabla());
            imprimirSalidas(analizador.getSalidas());
            imprimirResultado(analizador.getErrores());
        } catch (IOException e) {
            System.out.println("No se pudo leer el archivo: - Main.java:24" + archivo);
            System.out.println(e.getMessage());
        }
    }

    private static void imprimirCodigo(List<String> codigo) {
        System.out.println("\nCódigo analizado - Main.java:30");
        System.out.println("");
        for (int i = 0; i < codigo.size(); i++) {
            System.out.printf("%2d | %s%n", i + 1, codigo.get(i));
        }
    }

    private static void imprimirTabla(TablaSimbolos tabla) {
        System.out.println("\nTabla de símbolos - Main.java:38");
        System.out.println("");
        System.out.printf("%-15s %-10s %-12s %-12s %-10s%n",
                "Nombre", "Tipo", "Ámbito", "Dirección", "Valor");
        System.out.println("");

        for (Simbolo simbolo : tabla.obtenerTodos()) {
            System.out.printf("%-15s %-10s %-12s %-12d %-10s%n",
                    simbolo.getNombre(),
                    simbolo.getTipo().getNombre(),
                    simbolo.getAmbito(),
                    simbolo.getDireccion(),
                    simbolo.valorComoTexto());
        }
    }

    private static void imprimirSalidas(List<String> salidas) {
        if (salidas.isEmpty()) {
            return;
        }
        System.out.println("\nSalidas del programa - Main.java:58");
        System.out.println("");
        salidas.forEach(System.out::println);
    }

    private static void imprimirResultado(List<ErrorSemantico> errores) {
        System.out.println("\nResultado del análisis - Main.java:64");
        System.out.println("");

        if (errores.isEmpty()) {
            System.out.println("El programa es semánticamente correcto. - Main.java:68");
            return;
        }

        System.out.println("Se encontraron - Main.java:72" + errores.size()
                + " error(es) semántico(s):");
        errores.forEach(error -> System.out.println("" + error));
    }
}
