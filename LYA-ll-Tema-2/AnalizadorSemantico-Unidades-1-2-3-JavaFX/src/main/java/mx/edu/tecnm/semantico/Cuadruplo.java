package mx.edu.tecnm.semantico;

public record Cuadruplo(String operador, String argumento1, String argumento2, String resultado) {
    public String comoTresDirecciones() {
        return switch (operador) {
            case "=" -> resultado + " = " + argumento1;
            case "NEG" -> resultado + " = -" + argumento1;
            default -> resultado + " = " + argumento1 + " " + operador + " " + argumento2;
        };
    }

    public boolean resultadoTemporal() {
        return resultado != null && resultado.matches("t\\d+");
    }
}
