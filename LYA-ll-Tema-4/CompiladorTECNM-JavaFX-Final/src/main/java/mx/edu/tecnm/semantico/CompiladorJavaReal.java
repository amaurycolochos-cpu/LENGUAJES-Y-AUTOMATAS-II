package mx.edu.tecnm.semantico;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

public final class CompiladorJavaReal {
    public ResultadoJavac compilar(String nombreClase, String codigoJava) {
        List<String> mensajes = new ArrayList<>();
        JavaCompiler compilador = ToolProvider.getSystemJavaCompiler();
        if (compilador == null) {
            mensajes.add("No se encontró el compilador javac. Ejecuta la aplicación con un JDK completo, no solamente con un JRE.");
            return new ResultadoJavac(false, null, nombreClase, mensajes);
        }
        try {
            Path dir = Files.createTempDirectory("compilador-tecnm-");
            Path archivo = dir.resolve(nombreClase + ".java");
            Files.writeString(archivo, codigoJava, StandardCharsets.UTF_8);
            DiagnosticCollector<JavaFileObject> diagnosticos = new DiagnosticCollector<>();
            try (StandardJavaFileManager fm = compilador.getStandardFileManager(diagnosticos, Locale.getDefault(), StandardCharsets.UTF_8)) {
                Iterable<? extends JavaFileObject> unidades = fm.getJavaFileObjects(archivo.toFile());
                List<String> opciones = List.of("-d", dir.toString(), "-encoding", "UTF-8");
                boolean ok = Boolean.TRUE.equals(compilador.getTask(null, fm, diagnosticos, opciones, null, unidades).call());
                for (Diagnostic<? extends JavaFileObject> d : diagnosticos.getDiagnostics()) {
                    mensajes.add("línea " + d.getLineNumber() + ": " + d.getMessage(Locale.getDefault()));
                }
                return new ResultadoJavac(ok, dir, nombreClase, mensajes);
            }
        } catch (IOException e) {
            mensajes.add("No fue posible crear los archivos temporales de compilación: " + e.getMessage());
            return new ResultadoJavac(false, null, nombreClase, mensajes);
        }
    }

    public String ejecutar(ResultadoJavac compilacion) throws IOException, InterruptedException {
        if (compilacion == null || !compilacion.correcto() || compilacion.directorio() == null)
            throw new IllegalArgumentException("No existe una compilación válida para ejecutar.");
        String ejecutable = Path.of(System.getProperty("java.home"), "bin", esWindows() ? "java.exe" : "java").toString();
        ProcessBuilder pb = new ProcessBuilder(ejecutable, "-cp", compilacion.directorio().toString(), compilacion.nombreClase());
        pb.redirectErrorStream(true);
        Process p = pb.start();
        String salida = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        int codigo = p.waitFor();
        if (codigo != 0) return salida + System.lineSeparator() + "El programa terminó con código " + codigo + ".";
        return salida;
    }
    private boolean esWindows() { return System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win"); }
}
