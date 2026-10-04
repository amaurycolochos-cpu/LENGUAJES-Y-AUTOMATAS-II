package mx.edu.tecnm.semantico;

import java.util.List;

public record ResultadoSemanticoLenguaje(
        List<SimboloLenguaje> simbolos,
        List<ErrorCompilador> errores) {
    public boolean correcto() { return errores.isEmpty(); }
}
