package mx.edu.tecnm.semantico;

import java.util.ArrayList;
import java.util.List;

/** Genera las representaciones solicitadas en la Unidad 2. */
public class GeneradorCodigoIntermedio {
    private final ParserExpresionesIntermedias parser = new ParserExpresionesIntermedias();
    private int contadorTemporales;

    public void reiniciar() {
        contadorTemporales = 0;
    }

    public ResultadoCodigoIntermedio generar(int linea, String destino, String expresion)
            throws ExcepcionSemantica {
        NodoExpresion raiz = parser.parsear(expresion);

        List<Cuadruplo> cuadruplos = new ArrayList<>();
        String lugar = generarCuadruplos(raiz, cuadruplos);
        cuadruplos.add(new Cuadruplo("=", lugar, "", destino));

        List<String> tac = cuadruplos.stream()
                .map(Cuadruplo::comoTresDirecciones)
                .toList();

        List<Triplo> triplos = new ArrayList<>();
        String ref = generarTriplos(raiz, triplos);
        triplos.add(new Triplo(triplos.size(), "=", ref, destino));

        List<String> pCode = new ArrayList<>();
        generarPCode(raiz, pCode);
        pCode.add("STO " + destino);

        return new ResultadoCodigoIntermedio(
                linea,
                destino,
                expresion.trim(),
                raiz.infija(),
                raiz.prefija(),
                raiz.postfija(),
                List.copyOf(pCode),
                List.copyOf(tac),
                List.copyOf(cuadruplos),
                List.copyOf(triplos));
    }

    private String generarCuadruplos(NodoExpresion nodo, List<Cuadruplo> salida) {
        if (nodo.esOperando()) {
            return nodo.valor();
        }
        if (nodo.esUnario()) {
            String argumento = generarCuadruplos(nodo.izquierdo(), salida);
            String temporal = nuevoTemporal();
            salida.add(new Cuadruplo("NEG", argumento, "", temporal));
            return temporal;
        }

        String izquierda = generarCuadruplos(nodo.izquierdo(), salida);
        String derecha = generarCuadruplos(nodo.derecho(), salida);
        String temporal = nuevoTemporal();
        salida.add(new Cuadruplo(nodo.valor(), izquierda, derecha, temporal));
        return temporal;
    }

    private String generarTriplos(NodoExpresion nodo, List<Triplo> salida) {
        if (nodo.esOperando()) {
            return nodo.valor();
        }
        if (nodo.esUnario()) {
            String argumento = generarTriplos(nodo.izquierdo(), salida);
            int indice = salida.size();
            salida.add(new Triplo(indice, "NEG", argumento, ""));
            return "(" + indice + ")";
        }

        String izquierda = generarTriplos(nodo.izquierdo(), salida);
        String derecha = generarTriplos(nodo.derecho(), salida);
        int indice = salida.size();
        salida.add(new Triplo(indice, nodo.valor(), izquierda, derecha));
        return "(" + indice + ")";
    }

    private void generarPCode(NodoExpresion nodo, List<String> salida) {
        if (nodo.esOperando()) {
            if (esIdentificador(nodo.valor())) {
                salida.add("LOD " + nodo.valor());
            } else {
                salida.add("LIT " + nodo.valor());
            }
            return;
        }
        generarPCode(nodo.izquierdo(), salida);
        if (!nodo.esUnario()) {
            generarPCode(nodo.derecho(), salida);
        }
        salida.add(switch (nodo.valor()) {
            case "+" -> "ADD";
            case "-" -> "SUB";
            case "*" -> "MUL";
            case "/" -> "DIV";
            case "NEG" -> "NEG";
            default -> nodo.valor();
        });
    }

    private String nuevoTemporal() {
        contadorTemporales++;
        return "t" + contadorTemporales;
    }

    private boolean esIdentificador(String valor) {
        return valor.matches("[a-zA-Z_][a-zA-Z0-9_]*")
                && !valor.equals("true") && !valor.equals("false");
    }
}
