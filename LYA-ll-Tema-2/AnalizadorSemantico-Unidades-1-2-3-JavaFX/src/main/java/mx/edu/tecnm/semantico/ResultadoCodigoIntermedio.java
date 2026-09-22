package mx.edu.tecnm.semantico;

import java.util.List;

public record ResultadoCodigoIntermedio(
        int linea,
        String destino,
        String expresionOriginal,
        String infija,
        String prefija,
        String postfija,
        List<String> pCode,
        List<String> tresDirecciones,
        List<Cuadruplo> cuadruplos,
        List<Triplo> triplos) {
}
