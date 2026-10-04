package mx.edu.tecnm.semantico;

import java.util.List;

public final class TraductorJava {
    public String traducir(AstLenguaje.Programa programa) {
        StringBuilder sb = new StringBuilder();
        sb.append("public class ").append(programa.nombreClase()).append(" {\n");
        sb.append("    public static void main(String[] ").append(programa.parametroPrincipal()).append(") {\n");
        traducirSentencias(programa.sentencias(), sb, 2);
        sb.append("    }\n");
        sb.append("}\n");
        return sb.toString();
    }

    private void traducirSentencias(List<AstLenguaje.Sentencia> sentencias, StringBuilder sb, int nivel) {
        for (AstLenguaje.Sentencia s : sentencias) {
            String ind = "    ".repeat(nivel);
            if (s instanceof AstLenguaje.Declaracion d) {
                sb.append(ind).append(d.tipo().java()).append(" ").append(d.nombre());
                if (d.inicializador()!=null) sb.append(" = ").append(expr(d.inicializador()));
                else sb.append(" = ").append(valorDefecto(d.tipo()));
                sb.append(";\n");
            } else if (s instanceof AstLenguaje.Asignacion a) {
                sb.append(ind).append(a.nombre()).append(" = ").append(expr(a.valor())).append(";\n");
            } else if (s instanceof AstLenguaje.Imprimir i) {
                sb.append(ind).append("System.out.println(").append(expr(i.valor())).append(");\n");
            } else if (s instanceof AstLenguaje.Si si) {
                sb.append(ind).append("if (").append(expr(si.condicion())).append(") {\n");
                traducirSentencias(si.entonces(),sb,nivel+1);
                sb.append(ind).append("}");
                if (!si.sino().isEmpty()) {
                    sb.append(" else {\n"); traducirSentencias(si.sino(),sb,nivel+1); sb.append(ind).append("}");
                }
                sb.append("\n");
            } else if (s instanceof AstLenguaje.Mientras m) {
                sb.append(ind).append("while (").append(expr(m.condicion())).append(") {\n");
                traducirSentencias(m.cuerpo(),sb,nivel+1);
                sb.append(ind).append("}\n");
            }
        }
    }

    private String expr(AstLenguaje.Expresion e) {
        if (e instanceof AstLenguaje.Literal l) {
            if (l.tipo()==TipoLenguaje.CADENA) return "\""+escapar(String.valueOf(l.valor()))+"\"";
            return String.valueOf(l.valor());
        }
        if (e instanceof AstLenguaje.Variable v) return v.nombre();
        if (e instanceof AstLenguaje.Agrupacion g) return "("+expr(g.expresion())+")";
        if (e instanceof AstLenguaje.Unaria u) return u.operador().lexema()+expr(u.derecha());
        if (e instanceof AstLenguaje.Binaria b) return expr(b.izquierda())+" "+b.operador().lexema()+" "+expr(b.derecha());
        throw new IllegalStateException("Expresión desconocida");
    }
    private String valorDefecto(TipoLenguaje t) {
        return switch(t) { case ENTERO -> "0"; case DECIMAL -> "0.0"; case CADENA -> "\"\""; case BOOLEANO -> "false"; default -> "null"; };
    }
    private String escapar(String s) { return s.replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n").replace("\t","\\t"); }
}
