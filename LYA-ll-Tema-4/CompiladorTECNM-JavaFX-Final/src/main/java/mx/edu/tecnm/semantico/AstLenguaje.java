package mx.edu.tecnm.semantico;

import java.util.List;

public final class AstLenguaje {
    private AstLenguaje() {}

    public record Programa(String nombreClase, String parametroPrincipal, List<Sentencia> sentencias, int linea) {}

    public sealed interface Sentencia permits Declaracion, Asignacion, Imprimir, Si, Mientras {}
    public record Declaracion(TipoLenguaje tipo, String nombre, Expresion inicializador, int linea) implements Sentencia {}
    public record Asignacion(String nombre, Expresion valor, int linea) implements Sentencia {}
    public record Imprimir(Expresion valor, int linea) implements Sentencia {}
    public record Si(Expresion condicion, List<Sentencia> entonces, List<Sentencia> sino, int linea) implements Sentencia {}
    public record Mientras(Expresion condicion, List<Sentencia> cuerpo, int linea) implements Sentencia {}

    public sealed interface Expresion permits Literal, Variable, Binaria, Unaria, Agrupacion {}
    public record Literal(Object valor, TipoLenguaje tipo, int linea) implements Expresion {}
    public record Variable(String nombre, int linea) implements Expresion {}
    public record Binaria(Expresion izquierda, TokenLenguaje operador, Expresion derecha, int linea) implements Expresion {}
    public record Unaria(TokenLenguaje operador, Expresion derecha, int linea) implements Expresion {}
    public record Agrupacion(Expresion expresion, int linea) implements Expresion {}
}
