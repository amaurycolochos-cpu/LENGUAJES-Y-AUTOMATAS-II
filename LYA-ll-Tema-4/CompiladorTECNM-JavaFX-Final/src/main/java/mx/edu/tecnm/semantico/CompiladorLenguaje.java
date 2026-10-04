package mx.edu.tecnm.semantico;

import java.util.ArrayList;
import java.util.List;

public final class CompiladorLenguaje {
    private final AnalizadorLexico lexer = new AnalizadorLexico();
    private final AnalizadorSintactico parser = new AnalizadorSintactico();
    private final AnalizadorSemanticoLenguaje semantico = new AnalizadorSemanticoLenguaje();
    private final TraductorJava traductor = new TraductorJava();
    private final CompiladorJavaReal javac = new CompiladorJavaReal();

    public ResultadoCompiladorLenguaje compilar(String fuente) {
        List<ErrorCompilador> errores = new ArrayList<>();
        ResultadoLexico lexico = lexer.analizar(fuente);
        if (!lexico.correcto()) {
            errores.addAll(lexico.errores());
            return new ResultadoCompiladorLenguaje(lexico, null, null, "", null, List.copyOf(errores));
        }
        ResultadoSintactico sintactico = parser.analizar(lexico.tokens());
        if (!sintactico.correcto()) {
            errores.addAll(sintactico.errores());
            return new ResultadoCompiladorLenguaje(lexico, sintactico, null, "", null, List.copyOf(errores));
        }
        ResultadoSemanticoLenguaje resultadoSem = semantico.analizar(sintactico.programa());
        if (!resultadoSem.correcto()) {
            errores.addAll(resultadoSem.errores());
            return new ResultadoCompiladorLenguaje(lexico, sintactico, resultadoSem, "", null, List.copyOf(errores));
        }
        String codigoJava = traductor.traducir(sintactico.programa());
        ResultadoJavac compilacion = javac.compilar(sintactico.programa().nombreClase(), codigoJava);
        if (!compilacion.correcto()) {
            for (String d : compilacion.diagnosticos()) errores.add(new ErrorCompilador(ErrorCompilador.Fase.JAVAC,0,0,d));
        }
        return new ResultadoCompiladorLenguaje(lexico, sintactico, resultadoSem, codigoJava, compilacion, List.copyOf(errores));
    }

    public String ejecutar(ResultadoCompiladorLenguaje resultado) throws Exception {
        if (resultado == null || !resultado.correcto()) throw new IllegalStateException("Primero se necesita una compilación correcta.");
        return javac.ejecutar(resultado.javac());
    }
}
