package mx.edu.tecnm.semantico;

import java.util.List;

public final class FormateadorAst {
    private FormateadorAst() {}

    public static String formatear(AstLenguaje.Programa p) {
        StringBuilder sb = new StringBuilder();
        sb.append("PROGRAMA\n");
        sb.append("└── CLASE ").append(p.nombreClase()).append("\n");
        sb.append("    └── METODO principal(cadena[] ").append(p.parametroPrincipal()).append(")\n");
        formatearSentencias(p.sentencias(), sb, "        ");
        return sb.toString();
    }

    private static void formatearSentencias(List<AstLenguaje.Sentencia> ss, StringBuilder sb, String prefijo) {
        for (int i=0;i<ss.size();i++) {
            AstLenguaje.Sentencia s=ss.get(i);
            boolean ultimo=i==ss.size()-1;
            String rama=ultimo?"└── ":"├── ";
            String hijo=prefijo+(ultimo?"    ":"│   ");
            if (s instanceof AstLenguaje.Declaracion d) {
                sb.append(prefijo).append(rama).append("DECLARACION ").append(d.tipo().nombre()).append(" ").append(d.nombre()).append("\n");
                if (d.inicializador()!=null) sb.append(hijo).append("└── valor: ").append(expr(d.inicializador())).append("\n");
            } else if (s instanceof AstLenguaje.Asignacion a) {
                sb.append(prefijo).append(rama).append("ASIGNACION ").append(a.nombre()).append(" = ").append(expr(a.valor())).append("\n");
            } else if (s instanceof AstLenguaje.Imprimir imp) {
                sb.append(prefijo).append(rama).append("IMPRIMIR ").append(expr(imp.valor())).append("\n");
            } else if (s instanceof AstLenguaje.Si si) {
                sb.append(prefijo).append(rama).append("SI ").append(expr(si.condicion())).append("\n");
                sb.append(hijo).append("├── ENTONCES\n");
                formatearSentencias(si.entonces(),sb,hijo+"│   ");
                if (!si.sino().isEmpty()) {
                    sb.append(hijo).append("└── SINO\n");
                    formatearSentencias(si.sino(),sb,hijo+"    ");
                }
            } else if (s instanceof AstLenguaje.Mientras m) {
                sb.append(prefijo).append(rama).append("MIENTRAS ").append(expr(m.condicion())).append("\n");
                formatearSentencias(m.cuerpo(),sb,hijo);
            }
        }
    }

    private static String expr(AstLenguaje.Expresion e) {
        if (e instanceof AstLenguaje.Literal l) return l.tipo()==TipoLenguaje.CADENA ? "\""+l.valor()+"\"" : String.valueOf(l.valor());
        if (e instanceof AstLenguaje.Variable v) return v.nombre();
        if (e instanceof AstLenguaje.Agrupacion g) return "("+expr(g.expresion())+")";
        if (e instanceof AstLenguaje.Unaria u) return u.operador().lexema()+expr(u.derecha());
        if (e instanceof AstLenguaje.Binaria b) return expr(b.izquierda())+" "+b.operador().lexema()+" "+expr(b.derecha());
        return "?";
    }
}
