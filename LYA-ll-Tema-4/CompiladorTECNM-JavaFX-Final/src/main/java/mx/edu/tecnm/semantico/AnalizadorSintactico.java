package mx.edu.tecnm.semantico;

import static mx.edu.tecnm.semantico.TipoToken.*;
import java.util.ArrayList;
import java.util.List;

public final class AnalizadorSintactico {
    private List<TokenLenguaje> tokens;
    private int actual;
    private final List<ErrorCompilador> errores = new ArrayList<>();

    private static final class FalloSintactico extends RuntimeException { private static final long serialVersionUID = 1L; }

    public ResultadoSintactico analizar(List<TokenLenguaje> tokens) {
        this.tokens = tokens; this.actual = 0; errores.clear();
        AstLenguaje.Programa programa = null;
        try { programa = programa(); }
        catch (FalloSintactico ignored) { }
        return new ResultadoSintactico(programa, List.copyOf(errores));
    }

    private AstLenguaje.Programa programa() {
        consumir(PUBLICO, "Se esperaba 'publico' al iniciar la clase.");
        consumir(CLASE, "Se esperaba la palabra reservada 'clase'.");
        TokenLenguaje nombre = consumir(IDENTIFICADOR, "Se esperaba el nombre de la clase.");
        consumir(INICIO, "Se esperaba 'inicio' para abrir el bloque de la clase.");

        consumir(PUBLICO, "Se esperaba 'publico' en el método principal.");
        consumir(ESTATICO, "Se esperaba 'estatico' en el método principal.");
        consumir(VACIO, "Se esperaba 'vacio' en el método principal.");
        consumir(PRINCIPAL, "Se esperaba 'principal'.");
        consumir(PARENTESIS_IZQ, "Se esperaba '(' después de 'principal'.");
        consumir(CADENA, "El parámetro principal debe ser de tipo 'cadena[]'.");
        consumir(CORCHETE_IZQ, "Se esperaba '[' en cadena[].");
        consumir(CORCHETE_DER, "Se esperaba ']' en cadena[].");
        TokenLenguaje parametro = consumir(IDENTIFICADOR, "Se esperaba el nombre del parámetro principal.");
        consumir(PARENTESIS_DER, "Se esperaba ')' después del parámetro principal.");
        consumir(INICIO, "Se esperaba 'inicio' para abrir el método principal.");

        List<AstLenguaje.Sentencia> sentencias = bloqueHasta(FIN);
        consumir(FIN, "Se esperaba 'fin' para cerrar el método principal.");
        consumir(FIN, "Se esperaba un segundo 'fin' para cerrar la clase.");
        consumir(EOF, "Se encontró contenido después del cierre de la clase.");
        return new AstLenguaje.Programa(nombre.lexema(), parametro.lexema(), sentencias, nombre.linea());
    }

    private List<AstLenguaje.Sentencia> bloqueHasta(TipoToken cierre) {
        List<AstLenguaje.Sentencia> lista = new ArrayList<>();
        while (!comprobar(cierre) && !fin()) lista.add(sentencia());
        return lista;
    }

    private AstLenguaje.Sentencia sentencia() {
        if (coincidir(ENTERO, DECIMAL, CADENA, BOOLEANO)) return declaracion(anterior());
        if (coincidir(IMPRIMIR)) return imprimir(anterior());
        if (coincidir(SI)) return sentenciaSi(anterior());
        if (coincidir(MIENTRAS)) return mientras(anterior());
        if (comprobar(IDENTIFICADOR)) {
            TokenLenguaje identificador = ver();
            if (verSiguiente().tipo() == PARENTESIS_IZQ) {
                throw error(identificador, "Instrucción o función no reconocida: '" + identificador.lexema()
                        + "'. Si desea mostrar un valor en pantalla, use imprimir(...).");
            }
            return asignacion();
        }
        TokenLenguaje t = ver();
        throw error(t, "Instrucción no reconocida: '" + t.lexema() + "'.");
    }

    private AstLenguaje.Sentencia declaracion(TokenLenguaje tipoToken) {
        TipoLenguaje tipo = tipoDesdeToken(tipoToken.tipo());
        TokenLenguaje nombre = consumir(IDENTIFICADOR, "Se esperaba un identificador después del tipo.");
        AstLenguaje.Expresion inicializador = null;
        if (coincidir(ASIGNACION)) inicializador = expresion();
        consumir(PUNTO_COMA, "Se esperaba ';' al final de la declaración.");
        return new AstLenguaje.Declaracion(tipo, nombre.lexema(), inicializador, tipoToken.linea());
    }

    private AstLenguaje.Sentencia asignacion() {
        TokenLenguaje nombre = consumir(IDENTIFICADOR, "Se esperaba un identificador.");
        consumir(ASIGNACION, "Se esperaba '=' después del identificador.");
        AstLenguaje.Expresion valor = expresion();
        consumir(PUNTO_COMA, "Se esperaba ';' al final de la asignación.");
        return new AstLenguaje.Asignacion(nombre.lexema(), valor, nombre.linea());
    }

    private AstLenguaje.Sentencia imprimir(TokenLenguaje inicio) {
        consumir(PARENTESIS_IZQ, "Se esperaba '(' después de 'imprimir'.");
        AstLenguaje.Expresion valor = expresion();
        consumir(PARENTESIS_DER, "Se esperaba ')' después de la expresión de imprimir.");
        consumir(PUNTO_COMA, "Se esperaba ';' después de imprimir(...).");
        return new AstLenguaje.Imprimir(valor, inicio.linea());
    }

    private AstLenguaje.Sentencia sentenciaSi(TokenLenguaje inicio) {
        consumir(PARENTESIS_IZQ, "Se esperaba '(' después de 'si'.");
        AstLenguaje.Expresion condicion = expresion();
        consumir(PARENTESIS_DER, "Se esperaba ')' después de la condición.");
        consumir(INICIO, "Se esperaba 'inicio' para abrir el bloque del si.");
        List<AstLenguaje.Sentencia> entonces = bloqueHasta(FIN);
        consumir(FIN, "Se esperaba 'fin' para cerrar el bloque del si.");
        List<AstLenguaje.Sentencia> sino = List.of();
        if (coincidir(SINO)) {
            consumir(INICIO, "Se esperaba 'inicio' después de 'sino'.");
            sino = bloqueHasta(FIN);
            consumir(FIN, "Se esperaba 'fin' para cerrar el bloque sino.");
        }
        return new AstLenguaje.Si(condicion, entonces, sino, inicio.linea());
    }

    private AstLenguaje.Sentencia mientras(TokenLenguaje inicio) {
        consumir(PARENTESIS_IZQ, "Se esperaba '(' después de 'mientras'.");
        AstLenguaje.Expresion condicion = expresion();
        consumir(PARENTESIS_DER, "Se esperaba ')' después de la condición.");
        consumir(INICIO, "Se esperaba 'inicio' para abrir el bloque mientras.");
        List<AstLenguaje.Sentencia> cuerpo = bloqueHasta(FIN);
        consumir(FIN, "Se esperaba 'fin' para cerrar el bloque mientras.");
        return new AstLenguaje.Mientras(condicion, cuerpo, inicio.linea());
    }

    private AstLenguaje.Expresion expresion() { return o(); }
    private AstLenguaje.Expresion o() {
        AstLenguaje.Expresion e = y();
        while (coincidir(O_LOGICO)) { TokenLenguaje op=anterior(); AstLenguaje.Expresion d=y(); e=new AstLenguaje.Binaria(e,op,d,op.linea()); }
        return e;
    }
    private AstLenguaje.Expresion y() {
        AstLenguaje.Expresion e = igualdad();
        while (coincidir(Y_LOGICO)) { TokenLenguaje op=anterior(); AstLenguaje.Expresion d=igualdad(); e=new AstLenguaje.Binaria(e,op,d,op.linea()); }
        return e;
    }
    private AstLenguaje.Expresion igualdad() {
        AstLenguaje.Expresion e = comparacion();
        while (coincidir(IGUAL_IGUAL, DIFERENTE)) { TokenLenguaje op=anterior(); AstLenguaje.Expresion d=comparacion(); e=new AstLenguaje.Binaria(e,op,d,op.linea()); }
        return e;
    }
    private AstLenguaje.Expresion comparacion() {
        AstLenguaje.Expresion e = termino();
        while (coincidir(MENOR,MENOR_IGUAL,MAYOR,MAYOR_IGUAL)) { TokenLenguaje op=anterior(); AstLenguaje.Expresion d=termino(); e=new AstLenguaje.Binaria(e,op,d,op.linea()); }
        return e;
    }
    private AstLenguaje.Expresion termino() {
        AstLenguaje.Expresion e = factor();
        while (coincidir(MAS,MENOS)) { TokenLenguaje op=anterior(); AstLenguaje.Expresion d=factor(); e=new AstLenguaje.Binaria(e,op,d,op.linea()); }
        return e;
    }
    private AstLenguaje.Expresion factor() {
        AstLenguaje.Expresion e = unaria();
        while (coincidir(ASTERISCO,DIAGONAL,MODULO)) { TokenLenguaje op=anterior(); AstLenguaje.Expresion d=unaria(); e=new AstLenguaje.Binaria(e,op,d,op.linea()); }
        return e;
    }
    private AstLenguaje.Expresion unaria() {
        if (coincidir(NEGACION,MENOS)) { TokenLenguaje op=anterior(); return new AstLenguaje.Unaria(op,unaria(),op.linea()); }
        return primaria();
    }
    private AstLenguaje.Expresion primaria() {
        if (coincidir(FALSO)) return new AstLenguaje.Literal(false, TipoLenguaje.BOOLEANO, anterior().linea());
        if (coincidir(VERDADERO)) return new AstLenguaje.Literal(true, TipoLenguaje.BOOLEANO, anterior().linea());
        if (coincidir(NUMERO_ENTERO)) return new AstLenguaje.Literal(anterior().literal(), TipoLenguaje.ENTERO, anterior().linea());
        if (coincidir(NUMERO_DECIMAL)) return new AstLenguaje.Literal(anterior().literal(), TipoLenguaje.DECIMAL, anterior().linea());
        if (coincidir(TEXTO)) return new AstLenguaje.Literal(anterior().literal(), TipoLenguaje.CADENA, anterior().linea());
        if (coincidir(IDENTIFICADOR)) return new AstLenguaje.Variable(anterior().lexema(), anterior().linea());
        if (coincidir(PARENTESIS_IZQ)) {
            int linea=anterior().linea(); AstLenguaje.Expresion e=expresion();
            consumir(PARENTESIS_DER,"Se esperaba ')' después de la expresión.");
            return new AstLenguaje.Agrupacion(e,linea);
        }
        throw error(ver(), "Se esperaba una expresión válida.");
    }

    private TipoLenguaje tipoDesdeToken(TipoToken token) {
        return switch (token) {
            case ENTERO -> TipoLenguaje.ENTERO;
            case DECIMAL -> TipoLenguaje.DECIMAL;
            case CADENA -> TipoLenguaje.CADENA;
            case BOOLEANO -> TipoLenguaje.BOOLEANO;
            default -> TipoLenguaje.DESCONOCIDO;
        };
    }

    private TokenLenguaje consumir(TipoToken tipo, String mensaje) {
        if (comprobar(tipo)) return avanzar();
        throw error(ver(), mensaje);
    }
    private FalloSintactico error(TokenLenguaje token, String mensaje) {
        errores.add(new ErrorCompilador(ErrorCompilador.Fase.SINTACTICO, token.linea(), token.columna(), mensaje));
        return new FalloSintactico();
    }
    private boolean coincidir(TipoToken... tipos) {
        for (TipoToken t: tipos) if (comprobar(t)) { avanzar(); return true; }
        return false;
    }
    private boolean comprobar(TipoToken tipo) { return !fin() ? ver().tipo()==tipo : tipo==EOF; }
    private TokenLenguaje avanzar() { if (!fin()) actual++; return anterior(); }
    private boolean fin() { return ver().tipo()==EOF; }
    private TokenLenguaje ver() { return tokens.get(actual); }
    private TokenLenguaje verSiguiente() {
        int indice = Math.min(actual + 1, tokens.size() - 1);
        return tokens.get(indice);
    }
    private TokenLenguaje anterior() { return tokens.get(actual-1); }
}
