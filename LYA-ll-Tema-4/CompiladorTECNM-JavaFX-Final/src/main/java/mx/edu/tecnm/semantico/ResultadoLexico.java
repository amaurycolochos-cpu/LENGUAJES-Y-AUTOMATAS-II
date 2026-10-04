package mx.edu.tecnm.semantico;

import java.util.List;

public record ResultadoLexico(
        List<TokenLenguaje> tokens,
        List<ErrorCompilador> errores) {
    public boolean correcto() { return errores.isEmpty(); }
}
