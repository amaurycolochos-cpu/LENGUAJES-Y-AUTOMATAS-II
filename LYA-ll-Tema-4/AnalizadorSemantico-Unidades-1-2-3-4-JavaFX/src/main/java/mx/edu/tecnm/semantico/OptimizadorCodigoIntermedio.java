package mx.edu.tecnm.semantico;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Optimizaciones educativas para Unidad 3: locales, propagación global,
 * reducción de fuerza y mirilla sobre el código de tres direcciones.
 */
public class OptimizadorCodigoIntermedio {

    public ResultadoOptimizacion optimizar(List<Cuadruplo> entrada) {
        List<Cuadruplo> original = List.copyOf(entrada);
        List<String> cambios = new ArrayList<>();
        Map<String, String> sustituciones = new HashMap<>();
        Map<String, String> expresiones = new LinkedHashMap<>();
        List<Cuadruplo> pasada = new ArrayList<>();

        for (Cuadruplo actual : entrada) {
            String a1 = resolver(actual.argumento1(), sustituciones);
            String a2 = resolver(actual.argumento2(), sustituciones);
            String op = actual.operador();
            String res = actual.resultado();

            Cuadruplo q = new Cuadruplo(op, a1, a2, res);

            if (esAritmetico(op) && esNumero(a1) && esNumero(a2)) {
                String valor = calcular(op, a1, a2);
                q = new Cuadruplo("=", valor, "", res);
                cambios.add("Local - plegado de constantes: "
                        + a1 + " " + op + " " + a2 + " -> " + valor);
            } else if (esAritmetico(op)) {
                Cuadruplo simplificado = simplificarAlgebra(q);
                if (!simplificado.equals(q)) {
                    cambios.add("Local - simplificación algebraica: "
                            + q.comoTresDirecciones() + " -> "
                            + simplificado.comoTresDirecciones());
                    q = simplificado;
                }

                Cuadruplo fuerza = reducirFuerza(q);
                if (!fuerza.equals(q)) {
                    cambios.add("Ciclos/reducción de fuerza: "
                            + q.comoTresDirecciones() + " -> " + fuerza.comoTresDirecciones());
                    q = fuerza;
                }
            }

            if (esAritmetico(q.operador())) {
                String clave = claveExpresion(q);
                String previo = expresiones.get(clave);
                if (previo != null) {
                    cambios.add("Global - subexpresión común: "
                            + q.comoTresDirecciones() + " reutiliza " + previo);
                    q = new Cuadruplo("=", previo, "", q.resultado());
                } else {
                    expresiones.put(clave, q.resultado());
                }
            }

            if (q.operador().equals("=") && q.argumento1().equals(q.resultado())) {
                cambios.add("Mirilla - asignación redundante eliminada: "
                        + q.comoTresDirecciones());
                continue;
            }

            if (!q.resultadoTemporal()) {
                // Una escritura solo invalida expresiones que dependían de esa variable.
                invalidarReferencias(q.resultado(), sustituciones);
                invalidarExpresiones(q.resultado(), expresiones);
            }

            pasada.add(q);

            if (q.operador().equals("=") && q.resultadoTemporal()) {
                // En esta primera pasada solo propagamos temporales para no ocultar
                // las reglas locales aplicadas sobre variables del programa fuente.
                sustituciones.put(q.resultado(), resolver(q.argumento1(), sustituciones));
            } else if (q.resultadoTemporal()) {
                sustituciones.remove(q.resultado());
            }
        }

        List<Cuadruplo> propagada = new ArrayList<>();
        Map<String, String> copias = new HashMap<>();
        for (Cuadruplo q : pasada) {
            String a1 = resolver(q.argumento1(), copias);
            String a2 = resolver(q.argumento2(), copias);
            Cuadruplo nuevo = new Cuadruplo(q.operador(), a1, a2, q.resultado());
            if (!nuevo.equals(q)) {
                cambios.add("Global - propagación de copias/constantes: "
                        + q.comoTresDirecciones() + " -> " + nuevo.comoTresDirecciones());
            }

            if (esAritmetico(nuevo.operador())
                    && esNumero(nuevo.argumento1()) && esNumero(nuevo.argumento2())) {
                String valor = calcular(nuevo.operador(), nuevo.argumento1(), nuevo.argumento2());
                cambios.add("Local - plegado tras propagación: "
                        + nuevo.comoTresDirecciones() + " -> "
                        + nuevo.resultado() + " = " + valor);
                nuevo = new Cuadruplo("=", valor, "", nuevo.resultado());
            } else if (esAritmetico(nuevo.operador())) {
                Cuadruplo simplificado = simplificarAlgebra(nuevo);
                if (!simplificado.equals(nuevo)) {
                    cambios.add("Local - simplificación tras propagación: "
                            + nuevo.comoTresDirecciones() + " -> "
                            + simplificado.comoTresDirecciones());
                    nuevo = simplificado;
                }
            }

            if (!nuevo.resultadoTemporal()) {
                invalidarReferencias(nuevo.resultado(), copias);
            }
            propagada.add(nuevo);
            if (nuevo.operador().equals("=")) {
                copias.put(nuevo.resultado(), resolver(nuevo.argumento1(), copias));
            } else {
                copias.remove(nuevo.resultado());
            }
        }

        List<Cuadruplo> sinMuertos = eliminarTemporalesMuertos(propagada, cambios);
        return new ResultadoOptimizacion(original, List.copyOf(sinMuertos), List.copyOf(cambios));
    }

    private List<Cuadruplo> eliminarTemporalesMuertos(
            List<Cuadruplo> entrada, List<String> cambios) {
        Set<String> necesarios = new HashSet<>();
        List<Cuadruplo> invertida = new ArrayList<>();

        for (int i = entrada.size() - 1; i >= 0; i--) {
            Cuadruplo q = entrada.get(i);
            boolean conservar = !q.resultadoTemporal() || necesarios.contains(q.resultado());

            if (conservar) {
                invertida.add(q);
                if (esTemporal(q.argumento1())) {
                    necesarios.add(q.argumento1());
                }
                if (esTemporal(q.argumento2())) {
                    necesarios.add(q.argumento2());
                }
            } else {
                cambios.add("Mirilla - temporal sin uso eliminado: " + q.comoTresDirecciones());
            }
        }

        List<Cuadruplo> resultado = new ArrayList<>();
        for (int i = invertida.size() - 1; i >= 0; i--) {
            resultado.add(invertida.get(i));
        }
        return resultado;
    }

    private Cuadruplo simplificarAlgebra(Cuadruplo q) {
        String op = q.operador();
        String a = q.argumento1();
        String b = q.argumento2();

        if (op.equals("+") && esCero(b)) return new Cuadruplo("=", a, "", q.resultado());
        if (op.equals("+") && esCero(a)) return new Cuadruplo("=", b, "", q.resultado());
        if (op.equals("-") && esCero(b)) return new Cuadruplo("=", a, "", q.resultado());
        if (op.equals("*") && esUno(b)) return new Cuadruplo("=", a, "", q.resultado());
        if (op.equals("*") && esUno(a)) return new Cuadruplo("=", b, "", q.resultado());
        if (op.equals("*") && (esCero(a) || esCero(b))) return new Cuadruplo("=", "0", "", q.resultado());
        if (op.equals("/") && esUno(b)) return new Cuadruplo("=", a, "", q.resultado());
        return q;
    }

    private Cuadruplo reducirFuerza(Cuadruplo q) {
        if (!q.operador().equals("*")) return q;
        if (esDos(q.argumento2()) && !esNumero(q.argumento1())) {
            return new Cuadruplo("+", q.argumento1(), q.argumento1(), q.resultado());
        }
        if (esDos(q.argumento1()) && !esNumero(q.argumento2())) {
            return new Cuadruplo("+", q.argumento2(), q.argumento2(), q.resultado());
        }
        return q;
    }

    private String claveExpresion(Cuadruplo q) {
        String a = q.argumento1();
        String b = q.argumento2();
        if ((q.operador().equals("+") || q.operador().equals("*"))
                && a.compareTo(b) > 0) {
            String tmp = a;
            a = b;
            b = tmp;
        }
        return q.operador() + "|" + a + "|" + b;
    }

    private String resolver(String valor, Map<String, String> sustituciones) {
        if (valor == null || valor.isBlank()) return valor == null ? "" : valor;
        String actual = valor;
        Set<String> visitados = new HashSet<>();
        while (sustituciones.containsKey(actual) && visitados.add(actual)) {
            actual = sustituciones.get(actual);
        }
        return actual;
    }

    private void invalidarReferencias(String variable, Map<String, String> mapa) {
        mapa.remove(variable);
        mapa.entrySet().removeIf(e -> e.getValue().equals(variable));
    }

    private void invalidarExpresiones(String variable, Map<String, String> expresiones) {
        String marcador = "|" + variable + "|";
        expresiones.entrySet().removeIf(e -> ("|" + e.getKey() + "|").contains(marcador));
    }

    private boolean esAritmetico(String op) {
        return op.equals("+") || op.equals("-") || op.equals("*") || op.equals("/");
    }

    private boolean esNumero(String texto) {
        if (texto == null || texto.isBlank()) return false;
        try {
            new BigDecimal(texto);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private String calcular(String op, String a, String b) {
        BigDecimal x = new BigDecimal(a);
        BigDecimal y = new BigDecimal(b);
        BigDecimal r = switch (op) {
            case "+" -> x.add(y);
            case "-" -> x.subtract(y);
            case "*" -> x.multiply(y);
            case "/" -> {
                if (y.compareTo(BigDecimal.ZERO) == 0) yield x;
                yield x.divide(y, 8, java.math.RoundingMode.HALF_UP).stripTrailingZeros();
            }
            default -> x;
        };
        return r.stripTrailingZeros().toPlainString();
    }

    private boolean esCero(String s) {
        return esNumero(s) && new BigDecimal(s).compareTo(BigDecimal.ZERO) == 0;
    }

    private boolean esUno(String s) {
        return esNumero(s) && new BigDecimal(s).compareTo(BigDecimal.ONE) == 0;
    }

    private boolean esDos(String s) {
        return esNumero(s) && new BigDecimal(s).compareTo(new BigDecimal("2")) == 0;
    }

    private boolean esTemporal(String s) {
        return s != null && s.matches("t\\d+");
    }
}
