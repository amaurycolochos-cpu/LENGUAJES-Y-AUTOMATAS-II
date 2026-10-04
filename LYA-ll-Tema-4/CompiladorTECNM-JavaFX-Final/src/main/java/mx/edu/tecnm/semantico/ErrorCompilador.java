package mx.edu.tecnm.semantico;

public record ErrorCompilador(
        Fase fase,
        int linea,
        int columna,
        String mensaje) {

    public enum Fase { LEXICO, SINTACTICO, SEMANTICO, TRADUCCION, JAVAC, EJECUCION }

    @Override
    public String toString() {
        String ubicacion = linea > 0
                ? "Línea " + linea + (columna > 0 ? ", columna " + columna : "")
                : "Sin ubicación";
        return "ERROR " + fase + " · " + ubicacion + System.lineSeparator()
                + mensaje;
    }
}
