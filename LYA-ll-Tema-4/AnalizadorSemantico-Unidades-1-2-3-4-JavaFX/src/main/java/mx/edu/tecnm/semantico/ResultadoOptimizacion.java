package mx.edu.tecnm.semantico;

import java.util.List;

public record ResultadoOptimizacion(
        List<Cuadruplo> original,
        List<Cuadruplo> optimizado,
        List<String> cambios) {

    public int instruccionesEliminadas() {
        return original.size() - optimizado.size();
    }
}
