package mx.edu.tecnm.semantico;

import java.util.List;

public final class FormateadorResultados {
    private FormateadorResultados() {
    }

    public static String notaciones(List<ResultadoCodigoIntermedio> resultados) {
        if (resultados.isEmpty()) {
            return "No hay expresiones válidas para convertir.";
        }
        StringBuilder sb = new StringBuilder();
        for (ResultadoCodigoIntermedio r : resultados) {
            sb.append("Línea ").append(r.linea()).append(" · ")
                    .append(r.destino()).append(" = ").append(r.expresionOriginal()).append('\n');
            sb.append("Infija:   ").append(r.infija()).append('\n');
            sb.append("Prefija:  ").append(r.prefija()).append('\n');
            sb.append("Postfija: ").append(r.postfija()).append('\n');
            sb.append("Polaca:   ").append(r.prefija()).append('\n');
            sb.append('\n');
        }
        return sb.toString().trim();
    }

    public static String tresDirecciones(List<ResultadoCodigoIntermedio> resultados) {
        if (resultados.isEmpty()) {
            return "No se generó código de tres direcciones.";
        }
        StringBuilder sb = new StringBuilder();
        int i = 1;
        for (ResultadoCodigoIntermedio r : resultados) {
            sb.append("// Línea ").append(r.linea()).append('\n');
            for (String instruccion : r.tresDirecciones()) {
                sb.append(String.format("%02d  %s%n", i++, instruccion));
            }
            sb.append('\n');
        }
        return sb.toString().trim();
    }

    public static String cuadruplos(List<ResultadoCodigoIntermedio> resultados) {
        if (resultados.isEmpty()) {
            return "No se generaron cuádruplos.";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-5s %-10s %-16s %-16s %-16s%n",
                "No.", "Operador", "Argumento 1", "Argumento 2", "Resultado"));
        sb.append("--------------------------------------------------------------------------\n");
        int i = 0;
        for (ResultadoCodigoIntermedio r : resultados) {
            for (Cuadruplo q : r.cuadruplos()) {
                sb.append(String.format("%-5d %-10s %-16s %-16s %-16s%n",
                        i++, q.operador(), q.argumento1(),
                        vacioComoGuion(q.argumento2()), q.resultado()));
            }
        }
        return sb.toString();
    }

    public static String triplos(List<ResultadoCodigoIntermedio> resultados) {
        if (resultados.isEmpty()) {
            return "No se generaron triplos.";
        }
        StringBuilder sb = new StringBuilder();
        int base = 0;
        for (ResultadoCodigoIntermedio r : resultados) {
            sb.append("Línea ").append(r.linea()).append('\n');
            sb.append(String.format("%-7s %-10s %-18s %-18s%n",
                    "Índice", "Operador", "Argumento 1", "Argumento 2"));
            sb.append("---------------------------------------------------------------\n");
            for (Triplo t : r.triplos()) {
                String a1 = desplazarReferencia(t.argumento1(), base);
                String a2 = desplazarReferencia(t.argumento2(), base);
                sb.append(String.format("%-7d %-10s %-18s %-18s%n",
                        base + t.indice(), t.operador(), a1, vacioComoGuion(a2)));
            }
            base += r.triplos().size();
            sb.append('\n');
        }
        return sb.toString().trim();
    }

    public static String pCode(List<ResultadoCodigoIntermedio> resultados) {
        if (resultados.isEmpty()) {
            return "No se generó Código P.";
        }
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (ResultadoCodigoIntermedio r : resultados) {
            sb.append("; Línea ").append(r.linea()).append(" · ")
                    .append(r.destino()).append('\n');
            for (String instruccion : r.pCode()) {
                sb.append(String.format("%02d  %s%n", i++, instruccion));
            }
            sb.append('\n');
        }
        return sb.toString().trim();
    }

    public static String optimizacion(ResultadoOptimizacion resultado) {
        if (resultado.original().isEmpty()) {
            return "No hay código intermedio válido para optimizar.";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("CÓDIGO INTERMEDIO ORIGINAL\n");
        sb.append("==========================\n");
        for (int i = 0; i < resultado.original().size(); i++) {
            sb.append(String.format("%02d  %s%n", i + 1,
                    resultado.original().get(i).comoTresDirecciones()));
        }

        sb.append("\nCÓDIGO OPTIMIZADO\n");
        sb.append("=================\n");
        for (int i = 0; i < resultado.optimizado().size(); i++) {
            sb.append(String.format("%02d  %s%n", i + 1,
                    resultado.optimizado().get(i).comoTresDirecciones()));
        }

        sb.append("\nCAMBIOS APLICADOS\n");
        sb.append("=================\n");
        if (resultado.cambios().isEmpty()) {
            sb.append("No fue necesario aplicar reglas de optimización.\n");
        } else {
            for (String cambio : resultado.cambios()) {
                sb.append("• ").append(cambio).append('\n');
            }
        }

        sb.append("\nRESUMEN\n");
        sb.append("=======\n");
        sb.append("Instrucciones antes: ").append(resultado.original().size()).append('\n');
        sb.append("Instrucciones después: ").append(resultado.optimizado().size()).append('\n');
        sb.append("Reducción neta: ").append(resultado.instruccionesEliminadas()).append('\n');
        return sb.toString().trim();
    }

    private static String vacioComoGuion(String texto) {
        return texto == null || texto.isBlank() ? "—" : texto;
    }

    private static String desplazarReferencia(String valor, int base) {
        if (valor == null) return "";
        if (valor.matches("\\(\\d+\\)")) {
            int local = Integer.parseInt(valor.substring(1, valor.length() - 1));
            return "(" + (base + local) + ")";
        }
        return valor;
    }
}
