package mx.edu.tecnm.semantico;

/** Árbol sencillo de expresión usado para notaciones y código intermedio. */
public record NodoExpresion(String valor, NodoExpresion izquierdo, NodoExpresion derecho) {
    public static NodoExpresion operando(String valor) {
        return new NodoExpresion(valor, null, null);
    }

    public static NodoExpresion unario(String operador, NodoExpresion operando) {
        return new NodoExpresion(operador, operando, null);
    }

    public static NodoExpresion binario(String operador, NodoExpresion izquierdo, NodoExpresion derecho) {
        return new NodoExpresion(operador, izquierdo, derecho);
    }

    public boolean esOperando() {
        return izquierdo == null && derecho == null;
    }

    public boolean esUnario() {
        return izquierdo != null && derecho == null;
    }

    public String infija() {
        if (esOperando()) {
            return valor;
        }
        if (esUnario()) {
            return "(" + valor + izquierdo.infija() + ")";
        }
        return "(" + izquierdo.infija() + " " + valor + " " + derecho.infija() + ")";
    }

    public String prefija() {
        if (esOperando()) {
            return valor;
        }
        if (esUnario()) {
            return valor + " " + izquierdo.prefija();
        }
        return valor + " " + izquierdo.prefija() + " " + derecho.prefija();
    }

    public String postfija() {
        if (esOperando()) {
            return valor;
        }
        if (esUnario()) {
            return izquierdo.postfija() + " " + valor;
        }
        return izquierdo.postfija() + " " + derecho.postfija() + " " + valor;
    }
}
