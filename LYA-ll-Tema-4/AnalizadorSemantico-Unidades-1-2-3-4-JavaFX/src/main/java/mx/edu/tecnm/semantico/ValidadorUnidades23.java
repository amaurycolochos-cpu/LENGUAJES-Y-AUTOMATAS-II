package mx.edu.tecnm.semantico;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Ejecuta en consola todos los ejemplos preparados de las Unidades 2 y 3. */
public class ValidadorUnidades23 {
    public static void main(String[] args) throws IOException {
        List<Path> carpetas = List.of(
                Path.of("ejemplos/unidad_2"),
                Path.of("ejemplos/unidad_3"));

        int total = 0;
        int correctos = 0;

        for (Path carpeta : carpetas) {
            try (var archivos = Files.walk(carpeta)) {
                for (Path archivo : archivos
                        .filter(p -> p.toString().endsWith(".txt"))
                        .sorted()
                        .toList()) {
                    total++;
                    AnalizadorSemantico analizador = new AnalizadorSemantico();
                    analizador.analizar(Files.readAllLines(archivo));
                    boolean ok = analizador.getErrores().isEmpty()
                            && !analizador.getCodigoIntermedio().isEmpty();
                    if (ok) {
                        correctos++;
                        System.out.println("[OK] " + archivo);
                    } else {
                        System.out.println("[FALLO] " + archivo);
                        analizador.getErrores().forEach(e -> System.out.println("  " + e));
                    }
                }
            }
        }

        System.out.println();
        System.out.println("Resultado: " + correctos + "/" + total + " archivos correctos.");
        if (correctos != total) {
            System.exit(1);
        }
    }
}
