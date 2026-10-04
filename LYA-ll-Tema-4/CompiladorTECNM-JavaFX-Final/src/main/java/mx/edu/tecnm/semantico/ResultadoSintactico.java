package mx.edu.tecnm.semantico;

import java.util.List;

public record ResultadoSintactico(
        AstLenguaje.Programa programa,
        List<ErrorCompilador> errores) {
    public boolean correcto() { return errores.isEmpty() && programa != null; }
}
