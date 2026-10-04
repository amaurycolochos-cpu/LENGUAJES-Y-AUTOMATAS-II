package mx.edu.tecnm.semantico;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static mx.edu.tecnm.semantico.TipoToken.*;

public final class AnalizadorSemanticoLenguaje {
    private final Deque<Map<String, SimboloLenguaje>> ambitos = new ArrayDeque<>();
    private final List<SimboloLenguaje> simbolos = new ArrayList<>();
    private final List<ErrorCompilador> errores = new ArrayList<>();

    public ResultadoSemanticoLenguaje analizar(AstLenguaje.Programa programa) {
        ambitos.clear(); simbolos.clear(); errores.clear();
        abrirAmbito();
        for (AstLenguaje.Sentencia s : programa.sentencias()) visitar(s);
        cerrarAmbito();
        return new ResultadoSemanticoLenguaje(List.copyOf(simbolos), List.copyOf(errores));
    }

    private void visitar(AstLenguaje.Sentencia s) {
        if (s instanceof AstLenguaje.Declaracion d) {
            Map<String, SimboloLenguaje> actual = ambitos.peek();
            if (actual.containsKey(d.nombre())) {
                SimboloLenguaje previo = actual.get(d.nombre());
                error(d.linea(), "La variable '" + d.nombre() + "' ya fue declarada en la línea " + previo.lineaDeclaracion() + ".");
                return;
            }
            if (d.inicializador() != null) {
                TipoLenguaje origen = tipoDe(d.inicializador());
                if (!compatible(d.tipo(), origen)) {
                    error(d.linea(), "No se puede asignar un valor de tipo '" + origen.nombre() + "' a la variable '" + d.nombre() + "' de tipo '" + d.tipo().nombre() + "'.");
                }
            }
            SimboloLenguaje sim = new SimboloLenguaje(d.nombre(), d.tipo(), d.linea(), ambitos.size()-1);
            actual.put(d.nombre(), sim); simbolos.add(sim);
        } else if (s instanceof AstLenguaje.Asignacion a) {
            SimboloLenguaje sim = buscar(a.nombre());
            if (sim == null) {
                error(a.linea(), "La variable '" + a.nombre() + "' no ha sido declarada.");
            } else {
                TipoLenguaje origen = tipoDe(a.valor());
                if (!compatible(sim.tipo(), origen)) {
                    error(a.linea(), "No se puede asignar un valor de tipo '" + origen.nombre() + "' a la variable '" + a.nombre() + "' de tipo '" + sim.tipo().nombre() + "'.");
                }
            }
        } else if (s instanceof AstLenguaje.Imprimir i) {
            tipoDe(i.valor());
        } else if (s instanceof AstLenguaje.Si si) {
            TipoLenguaje tipo = tipoDe(si.condicion());
            if (tipo != TipoLenguaje.BOOLEANO && tipo != TipoLenguaje.DESCONOCIDO) {
                error(si.linea(), "La condición de 'si' debe ser booleana, pero se obtuvo '" + tipo.nombre() + "'.");
            }
            visitarBloque(si.entonces());
            if (!si.sino().isEmpty()) visitarBloque(si.sino());
        } else if (s instanceof AstLenguaje.Mientras m) {
            TipoLenguaje tipo = tipoDe(m.condicion());
            if (tipo != TipoLenguaje.BOOLEANO && tipo != TipoLenguaje.DESCONOCIDO) {
                error(m.linea(), "La condición de 'mientras' debe ser booleana, pero se obtuvo '" + tipo.nombre() + "'.");
            }
            visitarBloque(m.cuerpo());
        }
    }

    private void visitarBloque(List<AstLenguaje.Sentencia> bloque) {
        abrirAmbito();
        for (AstLenguaje.Sentencia s : bloque) visitar(s);
        cerrarAmbito();
    }

    private TipoLenguaje tipoDe(AstLenguaje.Expresion e) {
        if (e instanceof AstLenguaje.Literal l) return l.tipo();
        if (e instanceof AstLenguaje.Variable v) {
            SimboloLenguaje sim = buscar(v.nombre());
            if (sim == null) {
                error(v.linea(), "La variable '" + v.nombre() + "' no ha sido declarada.");
                return TipoLenguaje.DESCONOCIDO;
            }
            return sim.tipo();
        }
        if (e instanceof AstLenguaje.Agrupacion g) return tipoDe(g.expresion());
        if (e instanceof AstLenguaje.Unaria u) {
            TipoLenguaje d = tipoDe(u.derecha());
            if (u.operador().tipo() == NEGACION) {
                if (d != TipoLenguaje.BOOLEANO && d != TipoLenguaje.DESCONOCIDO)
                    error(u.linea(), "El operador ! requiere un valor booleano.");
                return TipoLenguaje.BOOLEANO;
            }
            if (u.operador().tipo() == MENOS) {
                if (!numerico(d) && d != TipoLenguaje.DESCONOCIDO)
                    error(u.linea(), "El operador - requiere un valor numérico.");
                return d;
            }
        }
        if (e instanceof AstLenguaje.Binaria b) {
            TipoLenguaje izq = tipoDe(b.izquierda());
            TipoLenguaje der = tipoDe(b.derecha());
            TipoToken op = b.operador().tipo();

            if (op == MAS && (izq == TipoLenguaje.CADENA || der == TipoLenguaje.CADENA)) return TipoLenguaje.CADENA;
            if (op == MAS || op == MENOS || op == ASTERISCO || op == DIAGONAL || op == MODULO) {
                if ((!numerico(izq) || !numerico(der)) && izq != TipoLenguaje.DESCONOCIDO && der != TipoLenguaje.DESCONOCIDO) {
                    error(b.linea(), "El operador '" + b.operador().lexema() + "' requiere operandos numéricos.");
                    return TipoLenguaje.DESCONOCIDO;
                }
                return izq == TipoLenguaje.DECIMAL || der == TipoLenguaje.DECIMAL ? TipoLenguaje.DECIMAL : TipoLenguaje.ENTERO;
            }
            if (op == MENOR || op == MENOR_IGUAL || op == MAYOR || op == MAYOR_IGUAL) {
                if ((!numerico(izq) || !numerico(der)) && izq != TipoLenguaje.DESCONOCIDO && der != TipoLenguaje.DESCONOCIDO)
                    error(b.linea(), "La comparación '" + b.operador().lexema() + "' requiere operandos numéricos.");
                return TipoLenguaje.BOOLEANO;
            }
            if (op == IGUAL_IGUAL || op == DIFERENTE) {
                if (!comparables(izq, der) && izq != TipoLenguaje.DESCONOCIDO && der != TipoLenguaje.DESCONOCIDO)
                    error(b.linea(), "No se pueden comparar valores de tipo '" + izq.nombre() + "' y '" + der.nombre() + "'.");
                return TipoLenguaje.BOOLEANO;
            }
            if (op == Y_LOGICO || op == O_LOGICO) {
                if ((izq != TipoLenguaje.BOOLEANO || der != TipoLenguaje.BOOLEANO) && izq != TipoLenguaje.DESCONOCIDO && der != TipoLenguaje.DESCONOCIDO)
                    error(b.linea(), "El operador lógico '" + b.operador().lexema() + "' requiere valores booleanos.");
                return TipoLenguaje.BOOLEANO;
            }
        }
        return TipoLenguaje.DESCONOCIDO;
    }

    private boolean compatible(TipoLenguaje destino, TipoLenguaje origen) {
        return destino == origen || (destino == TipoLenguaje.DECIMAL && origen == TipoLenguaje.ENTERO) || origen == TipoLenguaje.DESCONOCIDO;
    }
    private boolean comparables(TipoLenguaje a, TipoLenguaje b) { return a == b || (numerico(a) && numerico(b)); }
    private boolean numerico(TipoLenguaje t) { return t == TipoLenguaje.ENTERO || t == TipoLenguaje.DECIMAL; }
    private void abrirAmbito() { ambitos.push(new LinkedHashMap<>()); }
    private void cerrarAmbito() { ambitos.pop(); }
    private SimboloLenguaje buscar(String nombre) {
        for (Map<String, SimboloLenguaje> ambito : ambitos) if (ambito.containsKey(nombre)) return ambito.get(nombre);
        return null;
    }
    private void error(int linea, String mensaje) { errores.add(new ErrorCompilador(ErrorCompilador.Fase.SEMANTICO, linea, 0, mensaje)); }
}
