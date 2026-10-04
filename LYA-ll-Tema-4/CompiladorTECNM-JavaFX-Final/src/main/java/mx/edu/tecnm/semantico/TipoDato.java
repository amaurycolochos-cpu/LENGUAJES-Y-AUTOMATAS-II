package mx.edu.tecnm.semantico;

public enum TipoDato {
    INT("int", 4),
    FLOAT("float", 4),
    STRING("String", 8),
    BOOLEAN("boolean", 1);

    private final String nombre;
    private final int bytes;

    TipoDato(String nombre, int bytes) {
        this.nombre = nombre;
        this.bytes = bytes;
    }

    public String getNombre() {
        return nombre;
    }

    public int getBytes() {
        return bytes;
    }

    public static TipoDato desdeTexto(String texto) {
        for (TipoDato tipo : values()) {
            if (tipo.nombre.equals(texto)) {
                return tipo;
            }
        }
        throw new IllegalArgumentException("Tipo no reconocido: " + texto);
    }
}
