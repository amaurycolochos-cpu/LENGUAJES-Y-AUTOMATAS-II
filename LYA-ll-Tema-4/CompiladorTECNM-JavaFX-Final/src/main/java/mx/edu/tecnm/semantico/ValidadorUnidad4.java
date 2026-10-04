package mx.edu.tecnm.semantico;

import java.nio.file.Files;
import java.nio.file.Path;

/** Validación rápida de los casos preparados para el proyecto de Unidad 4. */
public class ValidadorUnidad4 {
    public static void main(String[] args) throws Exception {
        Path carpeta = Path.of("ejemplos/unidad_4/proyecto");
        int correctos = 0;

        for (int i = 1; i <= 4; i++) {
            Path archivo = carpeta.resolve("prueba_" + String.format("%02d", i) + ".txt");
            AnalizadorSemantico analizador = new AnalizadorSemantico();
            analizador.analizar(Files.readAllLines(archivo));

            if (!analizador.getErrores().isEmpty()) {
                throw new IllegalStateException(
                        archivo + " contiene errores: " + analizador.getErrores());
            }
            if (analizador.getCodigoObjeto().instrucciones().isEmpty()) {
                throw new IllegalStateException(
                        archivo + " no generó código objeto");
            }

            correctos++;
            System.out.printf(
                    "OK %s | símbolos=%d | optimizadas=%d | objeto=%d%n",
                    archivo.getFileName(),
                    analizador.getTabla().obtenerTodos().size(),
                    analizador.getOptimizacion().optimizado().size(),
                    analizador.getCodigoObjeto().instrucciones().size());
        }

        System.out.println("Unidad 4 validada: " + correctos + "/4 casos correctos.");
    }
}
