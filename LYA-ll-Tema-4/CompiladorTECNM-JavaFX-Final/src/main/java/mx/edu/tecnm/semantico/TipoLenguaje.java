package mx.edu.tecnm.semantico;

public enum TipoLenguaje {
    ENTERO("entero", "int"),
    DECIMAL("decimal", "double"),
    CADENA("cadena", "String"),
    BOOLEANO("booleano", "boolean"),
    VACIO("vacio", "void"),
    DESCONOCIDO("desconocido", "Object");

    private final String nombre;
    private final String java;
    TipoLenguaje(String nombre, String java) { this.nombre = nombre; this.java = java; }
    public String nombre() { return nombre; }
    public String java() { return java; }
}
