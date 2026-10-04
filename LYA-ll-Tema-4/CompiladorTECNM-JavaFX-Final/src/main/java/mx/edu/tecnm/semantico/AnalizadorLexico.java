package mx.edu.tecnm.semantico;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class AnalizadorLexico {
    private static final Map<String, TipoToken> PALABRAS = new HashMap<>();
    static {
        PALABRAS.put("publico", TipoToken.PUBLICO);
        PALABRAS.put("privado", TipoToken.PRIVADO);
        PALABRAS.put("clase", TipoToken.CLASE);
        PALABRAS.put("estatico", TipoToken.ESTATICO);
        PALABRAS.put("vacio", TipoToken.VACIO);
        PALABRAS.put("principal", TipoToken.PRINCIPAL);
        PALABRAS.put("entero", TipoToken.ENTERO);
        PALABRAS.put("decimal", TipoToken.DECIMAL);
        PALABRAS.put("cadena", TipoToken.CADENA);
        PALABRAS.put("booleano", TipoToken.BOOLEANO);
        PALABRAS.put("verdadero", TipoToken.VERDADERO);
        PALABRAS.put("falso", TipoToken.FALSO);
        PALABRAS.put("imprimir", TipoToken.IMPRIMIR);
        PALABRAS.put("si", TipoToken.SI);
        PALABRAS.put("sino", TipoToken.SINO);
        PALABRAS.put("mientras", TipoToken.MIENTRAS);
        PALABRAS.put("inicio", TipoToken.INICIO);
        PALABRAS.put("fin", TipoToken.FIN);
    }

    private String fuente;
    private final List<TokenLenguaje> tokens = new ArrayList<>();
    private final List<ErrorCompilador> errores = new ArrayList<>();
    private int inicio;
    private int actual;
    private int linea;
    private int columna;
    private int columnaInicio;

    public ResultadoLexico analizar(String fuente) {
        this.fuente = fuente == null ? "" : fuente;
        tokens.clear(); errores.clear();
        inicio = 0; actual = 0; linea = 1; columna = 1; columnaInicio = 1;

        while (!fin()) {
            inicio = actual;
            columnaInicio = columna;
            escanearToken();
        }
        tokens.add(new TokenLenguaje(TipoToken.EOF, "", null, linea, columna));
        return new ResultadoLexico(List.copyOf(tokens), List.copyOf(errores));
    }

    private void escanearToken() {
        char c = avanzar();
        switch (c) {
            case '(' -> agregar(TipoToken.PARENTESIS_IZQ);
            case ')' -> agregar(TipoToken.PARENTESIS_DER);
            case '[' -> agregar(TipoToken.CORCHETE_IZQ);
            case ']' -> agregar(TipoToken.CORCHETE_DER);
            case ';' -> agregar(TipoToken.PUNTO_COMA);
            case ',' -> agregar(TipoToken.COMA);
            case '+' -> agregar(TipoToken.MAS);
            case '-' -> agregar(TipoToken.MENOS);
            case '*' -> agregar(TipoToken.ASTERISCO);
            case '%' -> agregar(TipoToken.MODULO);
            case '!' -> agregar(coincidir('=') ? TipoToken.DIFERENTE : TipoToken.NEGACION);
            case '=' -> agregar(coincidir('=') ? TipoToken.IGUAL_IGUAL : TipoToken.ASIGNACION);
            case '<' -> agregar(coincidir('=') ? TipoToken.MENOR_IGUAL : TipoToken.MENOR);
            case '>' -> agregar(coincidir('=') ? TipoToken.MAYOR_IGUAL : TipoToken.MAYOR);
            case '&' -> {
                if (coincidir('&')) agregar(TipoToken.Y_LOGICO);
                else error("Se esperaba '&' para formar el operador lógico &&.");
            }
            case '|' -> {
                if (coincidir('|')) agregar(TipoToken.O_LOGICO);
                else error("Se esperaba '|' para formar el operador lógico ||.");
            }
            case '/' -> {
                if (coincidir('/')) {
                    while (ver() != '\n' && !fin()) avanzar();
                } else agregar(TipoToken.DIAGONAL);
            }
            case ' ', '\r', '\t' -> { }
            case '\n' -> { linea++; columna = 1; }
            case '"' -> cadena();
            default -> {
                if (Character.isDigit(c)) numero();
                else if (esInicioIdentificador(c)) identificador();
                else error("Símbolo no reconocido: '" + c + "'.");
            }
        }
    }

    private void identificador() {
        while (esParteIdentificador(ver())) avanzar();
        String texto = fuente.substring(inicio, actual);
        agregar(PALABRAS.getOrDefault(texto, TipoToken.IDENTIFICADOR));
    }

    private void numero() {
        while (Character.isDigit(ver())) avanzar();
        boolean decimal = false;
        if (ver() == '.' && Character.isDigit(verSiguiente())) {
            decimal = true; avanzar();
            while (Character.isDigit(ver())) avanzar();
        }
        String texto = fuente.substring(inicio, actual);
        if (decimal) {
            agregar(TipoToken.NUMERO_DECIMAL, Double.parseDouble(texto));
        } else {
            agregar(TipoToken.NUMERO_ENTERO, Integer.parseInt(texto));
        }
    }

    private void cadena() {
        StringBuilder valor = new StringBuilder();
        boolean cerrada = false;
        while (!fin()) {
            char c = avanzar();
            if (c == '"') { cerrada = true; break; }
            if (c == '\n') { linea++; columna = 1; valor.append('\n'); continue; }
            if (c == '\\' && !fin()) {
                char n = avanzar();
                switch (n) {
                    case 'n' -> valor.append('\n');
                    case 't' -> valor.append('\t');
                    case '"' -> valor.append('"');
                    case '\\' -> valor.append('\\');
                    default -> { valor.append('\\'); valor.append(n); }
                }
            } else valor.append(c);
        }
        if (!cerrada) {
            error("Cadena sin cerrar. Falta una comilla doble (\").");
            return;
        }
        agregar(TipoToken.TEXTO, valor.toString());
    }

    private boolean esInicioIdentificador(char c) {
        return c == '_' || Character.isLetter(c);
    }
    private boolean esParteIdentificador(char c) {
        return c == '_' || Character.isLetterOrDigit(c);
    }
    private boolean fin() { return actual >= fuente.length(); }
    private char avanzar() { char c = fuente.charAt(actual++); columna++; return c; }
    private char ver() { return fin() ? '\0' : fuente.charAt(actual); }
    private char verSiguiente() { return actual + 1 >= fuente.length() ? '\0' : fuente.charAt(actual + 1); }
    private boolean coincidir(char esperado) {
        if (fin() || fuente.charAt(actual) != esperado) return false;
        actual++; columna++; return true;
    }
    private void agregar(TipoToken tipo) { agregar(tipo, null); }
    private void agregar(TipoToken tipo, Object literal) {
        tokens.add(new TokenLenguaje(tipo, fuente.substring(inicio, actual), literal, linea, columnaInicio));
    }
    private void error(String mensaje) {
        errores.add(new ErrorCompilador(ErrorCompilador.Fase.LEXICO, linea, columnaInicio, mensaje));
    }
}
