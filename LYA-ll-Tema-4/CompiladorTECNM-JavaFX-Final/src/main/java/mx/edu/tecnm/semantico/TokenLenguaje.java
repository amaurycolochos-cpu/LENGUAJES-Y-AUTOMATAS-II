package mx.edu.tecnm.semantico;

public record TokenLenguaje(
        TipoToken tipo,
        String lexema,
        Object literal,
        int linea,
        int columna) {

    @Override
    public String toString() {
        return String.format("%-18s %-18s línea %d, col. %d",
                tipo, lexema, linea, columna);
    }
}
