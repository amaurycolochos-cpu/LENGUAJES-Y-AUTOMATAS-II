package mx.edu.tecnm.semantico;

public record ErrorSemantico(int linea, String mensaje) {
    @Override
    public String toString() {
        return "Línea " + linea + ": " + mensaje;
    }
}
