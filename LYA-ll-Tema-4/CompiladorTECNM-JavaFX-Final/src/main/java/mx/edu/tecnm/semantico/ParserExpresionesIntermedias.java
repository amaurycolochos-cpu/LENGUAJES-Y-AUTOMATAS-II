package mx.edu.tecnm.semantico;

import java.util.ArrayList;
import java.util.List;

/**
 * Parser independiente para construir el árbol de expresiones sin sustituir
 * el analizador semántico original. Respeta precedencia y paréntesis.
 */
public class ParserExpresionesIntermedias {
    private List<Token> tokens;
    private int posicion;

    public NodoExpresion parsear(String texto) throws ExcepcionSemantica {
        tokens = tokenizar(texto);
        posicion = 0;
        NodoExpresion nodo = expresion();
        if (actual().tipo != TipoToken.FIN) {
            throw new ExcepcionSemantica(
                    "elemento inesperado al generar código intermedio: " + actual().texto);
        }
        return nodo;
    }

    private NodoExpresion expresion() throws ExcepcionSemantica {
        NodoExpresion izquierda = termino();
        while (coincide("+") || coincide("-")) {
            String op = anterior().texto;
            NodoExpresion derecha = termino();
            izquierda = NodoExpresion.binario(op, izquierda, derecha);
        }
        return izquierda;
    }

    private NodoExpresion termino() throws ExcepcionSemantica {
        NodoExpresion izquierda = factor();
        while (coincide("*") || coincide("/")) {
            String op = anterior().texto;
            NodoExpresion derecha = factor();
            izquierda = NodoExpresion.binario(op, izquierda, derecha);
        }
        return izquierda;
    }

    private NodoExpresion factor() throws ExcepcionSemantica {
        if (coincide("-")) {
            return NodoExpresion.unario("NEG", factor());
        }
        if (coincide("(")) {
            NodoExpresion nodo = expresion();
            consumir(")", "falta cerrar el paréntesis");
            return nodo;
        }

        Token token = actual();
        avanzar();
        return switch (token.tipo) {
            case ENTERO, DECIMAL, CADENA, BOOLEANO, IDENTIFICADOR ->
                    NodoExpresion.operando(token.texto);
            default -> throw new ExcepcionSemantica(
                    "se esperaba un operando al generar código intermedio");
        };
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
                int inicio = i;
                i++;
                boolean cerrado = false;
                while (i < texto.length()) {
                    if (texto.charAt(i) == '"') {
                        i++;
                        cerrado = true;
                        break;
                    }
                    i++;
                }
                if (!cerrado) {
                    throw new ExcepcionSemantica("falta cerrar la cadena de texto");
                }
                resultado.add(new Token(TipoToken.CADENA, texto.substring(inicio, i)));
                continue;
            }
            if (Character.isDigit(c)) {
                int inicio = i;
                boolean punto = false;
                while (i < texto.length()) {
                    char actual = texto.charAt(i);
                    if (Character.isDigit(actual)) {
                        i++;
                    } else if (actual == '.' && !punto) {
                        punto = true;
                        i++;
                    } else {
                        break;
                    }
                }
                if (i < texto.length() && (texto.charAt(i) == 'f' || texto.charAt(i) == 'F')) {
                    i++;
                    punto = true;
                }
                String numero = texto.substring(inicio, i).replace("f", "").replace("F", "");
                resultado.add(new Token(punto ? TipoToken.DECIMAL : TipoToken.ENTERO, numero));
                continue;
            }
            if (Character.isLetter(c) || c == '_') {
                int inicio = i;
                while (i < texto.length()
                        && (Character.isLetterOrDigit(texto.charAt(i)) || texto.charAt(i) == '_')) {
                    i++;
                }
                String palabra = texto.substring(inicio, i);
                TipoToken tipo = (palabra.equals("true") || palabra.equals("false"))
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
        resultado.add(new Token(TipoToken.FIN, "<fin>"));
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
