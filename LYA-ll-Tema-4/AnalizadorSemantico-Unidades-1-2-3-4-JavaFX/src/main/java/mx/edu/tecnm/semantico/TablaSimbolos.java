package mx.edu.tecnm.semantico;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class TablaSimbolos {
    private final Map<String, Simbolo> simbolos = new LinkedHashMap<>();
    private int siguienteDireccion = 1000;

    public Simbolo buscar(String nombre) {
        return simbolos.get(nombre);
    }

    public boolean contiene(String nombre) {
        return simbolos.containsKey(nombre);
    }

    public Simbolo agregar(String nombre, TipoDato tipo, int linea) {
        Simbolo simbolo = new Simbolo(
                nombre, tipo, "global", siguienteDireccion, linea);
        simbolos.put(nombre, simbolo);
        siguienteDireccion += tipo.getBytes();
        return simbolo;
    }

    public Collection<Simbolo> obtenerTodos() {
        return simbolos.values();
    }

    public void limpiar() {
        simbolos.clear();
        siguienteDireccion = 1000;
    }
}
