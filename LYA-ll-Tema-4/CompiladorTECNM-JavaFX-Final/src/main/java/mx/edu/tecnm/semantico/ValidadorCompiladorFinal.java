package mx.edu.tecnm.semantico;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class ValidadorCompiladorFinal {
    private record Caso(String archivo, boolean correcto, ErrorCompilador.Fase faseEsperada) {}

    public static void main(String[] args) throws Exception {
        List<Caso> casos = List.of(
            new Caso("01_hola_mundo.tec", true, null),
            new Caso("02_variables.tec", true, null),
            new Caso("03_condicional.tec", true, null),
            new Caso("04_mientras.tec", true, null),
            new Caso("05_error_lexico.tec", false, ErrorCompilador.Fase.LEXICO),
            new Caso("06_error_sintactico.tec", false, ErrorCompilador.Fase.SINTACTICO),
            new Caso("07_error_semantico.tec", false, ErrorCompilador.Fase.SEMANTICO),
            new Caso("08_integrador.tec", true, null)
        );
        CompiladorLenguaje compilador = new CompiladorLenguaje();
        int ok = 0;
        for (Caso c : casos) {
            String fuente = Files.readString(Path.of("ejemplos/compilador_final", c.archivo()));
            ResultadoCompiladorLenguaje r = compilador.compilar(fuente);
            boolean pasa = r.correcto() == c.correcto();
            if (!c.correcto() && !r.errores().isEmpty()) pasa &= r.errores().get(0).fase() == c.faseEsperada();
            System.out.println((pasa ? "OK   " : "FAIL ") + c.archivo() + (r.errores().isEmpty() ? "" : " -> " + r.errores().get(0).fase()));
            if (pasa) ok++;
        }
        String hello = Files.readString(Path.of("ejemplos/compilador_final/01_hola_mundo.tec"));
        ResultadoCompiladorLenguaje r = compilador.compilar(hello);
        String salida = compilador.ejecutar(r).trim();
        boolean ejecucion = salida.equals("Hola Mundo");
        System.out.println((ejecucion ? "OK   " : "FAIL ") + "ejecución -> " + salida);
        if (ejecucion) ok++;
        System.out.println("RESULTADO: " + ok + "/" + (casos.size()+1));
        if (ok != casos.size()+1) System.exit(1);
    }
}
