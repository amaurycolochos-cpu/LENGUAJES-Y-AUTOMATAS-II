package mx.edu.tecnm.semantico;

import java.util.ArrayList;
import java.util.List;

public class AnalizadorExpresiones {
    private final TablaSimbolos tabla;
    private List<Token> tokens;
    private int posicion;

    public AnalizadorExpresiones(TablaSimbolos tabla) {
        this.tabla = tabla;
    }

    public ResultadoExpresion analizar(String expresion) throws ExcepcionSemantica {
        tokens = tokenizar(expresion);
        posicion = 0;

        ResultadoExpresion resultado = expresion();
        if (!actual().tipo.equals(TipoToken.FIN)) {
            throw new ExcepcionSemantica(
                    "se encontró un elemento inesperado: " + actual().texto);
        }
        return resultado;
    }

    private ResultadoExpresion expresion() throws ExcepcionSemantica {
        ResultadoExpresion izquierda = termino();

        while (coincide("+") || coincide("-")) {
            String operador = anterior().texto;
            ResultadoExpresion derecha = termino();
            izquierda = operar(izquierda, operador, derecha);
        }
        return izquierda;
    }

    private ResultadoExpresion termino() throws ExcepcionSemantica {
        ResultadoExpresion izquierda = factor();

        while (coincide("*") || coincide("/")) {
            String operador = anterior().texto;
            ResultadoExpresion derecha = factor();
            izquierda = operar(izquierda, operador, derecha);
        }
        return izquierda;
    }

    private ResultadoExpresion factor() throws ExcepcionSemantica {
        if (coincide("-")) {
            ResultadoExpresion valor = factor();
            if (!esNumerico(valor.tipo())) {
                throw new ExcepcionSemantica(
                        "el signo negativo solo puede usarse con números");
            }
            if (valor.valor() == null) {
                return new ResultadoExpresion(valor.tipo(), null);
            }
            if (valor.tipo() == TipoDato.INT) {
                return new ResultadoExpresion(TipoDato.INT, -(Integer) valor.valor());
            }
            return new ResultadoExpresion(TipoDato.FLOAT, -(Float) valor.valor());
        }

        if (coincide("(")) {
            ResultadoExpresion resultado = expresion();
            consumir(")", "falta cerrar el paréntesis");
            return resultado;
        }

        Token token = actual();
        avanzar();

        return switch (token.tipo) {
            case ENTERO -> new ResultadoExpresion(
                    TipoDato.INT, Integer.parseInt(token.texto));
            case DECIMAL -> new ResultadoExpresion(
                    TipoDato.FLOAT, Float.parseFloat(token.texto));
            case CADENA -> new ResultadoExpresion(TipoDato.STRING, token.texto);
            case BOOLEANO -> new ResultadoExpresion(
                    TipoDato.BOOLEAN, Boolean.parseBoolean(token.texto));
            case IDENTIFICADOR -> obtenerVariable(token.texto);
            default -> throw new ExcepcionSemantica(
                    "se esperaba un valor y se encontró: " + token.texto);
        };
    }

    private ResultadoExpresion obtenerVariable(String nombre)
            throws ExcepcionSemantica {
        Simbolo simbolo = tabla.buscar(nombre);
        if (simbolo == null) {
            throw new ExcepcionSemantica(
                    "la variable '" + nombre + "' no ha sido declarada");
        }
        if (simbolo.getValor() == null) {
            throw new ExcepcionSemantica(
                    "la variable '" + nombre + "' todavía no tiene un valor");
        }
        return new ResultadoExpresion(simbolo.getTipo(), simbolo.getValor());
    }

    private ResultadoExpresion operar(ResultadoExpresion izquierda,
                                      String operador,
                                      ResultadoExpresion derecha)
            throws ExcepcionSemantica {
        // El operador + también permite unir cadenas, igual que en Java.
        if (operador.equals("+")
                && (izquierda.tipo() == TipoDato.STRING
                || derecha.tipo() == TipoDato.STRING)) {
            if (izquierda.valor() == null || derecha.valor() == null) {
                return new ResultadoExpresion(TipoDato.STRING, null);
            }
            return new ResultadoExpresion(
                    TipoDato.STRING,
                    izquierda.valor().toString() + derecha.valor().toString());
        }

        if (!esNumerico(izquierda.tipo()) || !esNumerico(derecha.tipo())) {
            throw new ExcepcionSemantica(
                    "el operador '" + operador + "' necesita valores numéricos");
        }

        TipoDato tipoResultado = izquierda.tipo() == TipoDato.FLOAT
                || derecha.tipo() == TipoDato.FLOAT
                ? TipoDato.FLOAT : TipoDato.INT;

        if (izquierda.valor() == null || derecha.valor() == null) {
            return new ResultadoExpresion(tipoResultado, null);
        }

        float a = ((Number) izquierda.valor()).floatValue();
        float b = ((Number) derecha.valor()).floatValue();

        if (operador.equals("/") && b == 0) {
            throw new ExcepcionSemantica("no se puede dividir entre cero");
        }

        float resultado = switch (operador) {
            case "+" -> a + b;
            case "-" -> a - b;
            case "*" -> a * b;
            case "/" -> a / b;
            default -> throw new ExcepcionSemantica(
                    "operador no reconocido: " + operador);
        };

        if (tipoResultado == TipoDato.INT) {
            return new ResultadoExpresion(TipoDato.INT, (int) resultado);
        }
        return new ResultadoExpresion(TipoDato.FLOAT, resultado);
    }

    private boolean esNumerico(TipoDato tipo) {
        return tipo == TipoDato.INT || tipo == TipoDato.FLOAT;
    }

    private List<Token> tokenizar(String texto) throws ExcepcionSemantica {
        List<Token> resultado = new ArrayList<>();
        int i = 0;

        while (i < texto.length()) {
            char c = texto.charAt(i);
            if (Character.isWhitespace(c)) {
                i++;
                continue;
            }

            if (c == '"') {
                int inicio = ++i;
                while (i < texto.length() && texto.charAt(i) != '"') {
                    i++;
                }
                if (i >= texto.length()) {
                    throw new ExcepcionSemantica("falta cerrar la cadena de texto");
                }
                resultado.add(new Token(
                        TipoToken.CADENA, texto.substring(inicio, i)));
                i++;
                continue;
            }

            if (Character.isDigit(c)) {
                int inicio = i;
                boolean tienePunto = false;
                while (i < texto.length()
                        && (Character.isDigit(texto.charAt(i))
                        || texto.charAt(i) == '.')) {
                    if (texto.charAt(i) == '.') {
                        if (tienePunto) {
                            throw new ExcepcionSemantica("número decimal inválido");
                        }
                        tienePunto = true;
                    }
                    i++;
                }
                if (i < texto.length()
                        && (texto.charAt(i) == 'f' || texto.charAt(i) == 'F')) {
                    tienePunto = true;
                    i++;
                }
                String numero = texto.substring(inicio, i)
                        .replace("f", "").replace("F", "");
                resultado.add(new Token(
                        tienePunto ? TipoToken.DECIMAL : TipoToken.ENTERO,
                        numero));
                continue;
            }

            if (Character.isLetter(c) || c == '_') {
                int inicio = i;
                while (i < texto.length()
                        && (Character.isLetterOrDigit(texto.charAt(i))
                        || texto.charAt(i) == '_')) {
                    i++;
                }
                String palabra = texto.substring(inicio, i);
                TipoToken tipo = palabra.equals("true") || palabra.equals("false")
                        ? TipoToken.BOOLEANO : TipoToken.IDENTIFICADOR;
                resultado.add(new Token(tipo, palabra));
                continue;
            }

            if ("+-*/()".indexOf(c) >= 0) {
                resultado.add(new Token(TipoToken.OPERADOR, String.valueOf(c)));
                i++;
                continue;
            }

            throw new ExcepcionSemantica("símbolo no reconocido: " + c);
        }

        resultado.add(new Token(TipoToken.FIN, "fin"));
        return resultado;
    }

    private boolean coincide(String texto) {
        if (actual().texto.equals(texto)) {
            avanzar();
            return true;
        }
        return false;
    }

    private void consumir(String texto, String mensaje) throws ExcepcionSemantica {
        if (!coincide(texto)) {
            throw new ExcepcionSemantica(mensaje);
        }
    }

    private void avanzar() {
        if (posicion < tokens.size() - 1) {
            posicion++;
        }
    }

    private Token actual() {
        return tokens.get(posicion);
    }

    private Token anterior() {
        return tokens.get(posicion - 1);
    }

    private enum TipoToken {
        ENTERO, DECIMAL, CADENA, BOOLEANO, IDENTIFICADOR, OPERADOR, FIN
    }

    private record Token(TipoToken tipo, String texto) {
    }
}
