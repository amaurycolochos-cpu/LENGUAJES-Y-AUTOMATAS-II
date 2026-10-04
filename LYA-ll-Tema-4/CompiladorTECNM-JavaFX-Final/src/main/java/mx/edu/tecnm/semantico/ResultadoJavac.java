package mx.edu.tecnm.semantico;

import java.nio.file.Path;
import java.util.List;

public record ResultadoJavac(boolean correcto, Path directorio, String nombreClase, List<String> diagnosticos) {}
