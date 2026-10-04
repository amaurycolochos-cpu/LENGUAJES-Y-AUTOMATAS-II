package mx.edu.tecnm.semantico;

import java.util.List;

public record ResultadoCompiladorLenguaje(
        ResultadoLexico lexico,
        ResultadoSintactico sintactico,
        ResultadoSemanticoLenguaje semantico,
        String codigoJava,
        ResultadoJavac javac,
        List<ErrorCompilador> errores) {
    public boolean correcto() { return errores.isEmpty() && javac != null && javac.correcto(); }
}
