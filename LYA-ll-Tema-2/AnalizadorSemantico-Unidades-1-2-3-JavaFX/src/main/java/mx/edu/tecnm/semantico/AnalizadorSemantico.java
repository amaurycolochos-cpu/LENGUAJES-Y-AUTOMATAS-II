package mx.edu.tecnm.semantico;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnalizadorSemantico {
    private static final Pattern DECLARACION = Pattern.compile(
            "^(int|float|String|boolean)\\s+([a-zA-Z_][a-zA-Z0-9_]*)"
                    + "(?:\\s*=\\s*(.+))?;$");
    private static final Pattern ASIGNACION = Pattern.compile(
            "^([a-zA-Z_][a-zA-Z0-9_]*)\\s*=\\s*(.+);$");
    private static final Pattern MOSTRAR = Pattern.compile(
            "^mostrar\\s*\\((.+)\\);$");

    private final TablaSimbolos tabla = new TablaSimbolos();
    private final List<ErrorSemantico> errores = new ArrayList<>();
    private final List<String> salidas = new ArrayList<>();
    private final List<ResultadoCodigoIntermedio> codigoIntermedio = new ArrayList<>();
    private final AnalizadorExpresiones expresiones =
            new AnalizadorExpresiones(tabla);
    private final GeneradorCodigoIntermedio generadorIntermedio =
            new GeneradorCodigoIntermedio();
    private ResultadoOptimizacion optimizacion =
            new ResultadoOptimizacion(List.of(), List.of(), List.of());

    public void analizar(List<String> lineas) {
        tabla.limpiar();
        errores.clear();
        salidas.clear();
        codigoIntermedio.clear();
        generadorIntermedio.reiniciar();

        for (int i = 0; i < lineas.size(); i++) {
            analizarLinea(lineas.get(i), i + 1);
        }

        List<Cuadruplo> todos = codigoIntermedio.stream()
                .flatMap(resultado -> resultado.cuadruplos().stream())
                .toList();
        optimizacion = new OptimizadorCodigoIntermedio().optimizar(todos);
    }

    private void analizarLinea(String lineaOriginal, int numeroLinea) {
        String linea = quitarComentario(lineaOriginal).trim();
        if (linea.isEmpty()) {
            return;
        }

        Matcher declaracion = DECLARACION.matcher(linea);
        Matcher asignacion = ASIGNACION.matcher(linea);
        Matcher mostrar = MOSTRAR.matcher(linea);

        try {
            if (declaracion.matches()) {
                procesarDeclaracion(declaracion, numeroLinea);
            } else if (mostrar.matches()) {
                procesarMostrar(mostrar.group(1));
            } else if (asignacion.matches()) {
                procesarAsignacion(
                        asignacion.group(1), asignacion.group(2), numeroLinea);
            } else {
                agregarError(numeroLinea,
                        "la instrucción no tiene una estructura válida");
            }
        } catch (ExcepcionSemantica e) {
            agregarError(numeroLinea, e.getMessage());
        }
    }

    private void procesarDeclaracion(Matcher declaracion, int linea)
            throws ExcepcionSemantica {
        TipoDato tipo = TipoDato.desdeTexto(declaracion.group(1));
        String nombre = declaracion.group(2);
        String expresionInicial = declaracion.group(3);

        if (tabla.contiene(nombre)) {
            Simbolo anterior = tabla.buscar(nombre);
            throw new ExcepcionSemantica(
                    "la variable '" + nombre + "' ya fue declarada en la línea "
                            + anterior.getLineaDeclaracion());
        }

        Simbolo simbolo = tabla.agregar(nombre, tipo, linea);
        if (expresionInicial != null) {
            ResultadoExpresion resultado = expresiones.analizar(expresionInicial);
            validarAsignacion(simbolo, resultado);
            simbolo.setValor(convertirValor(tipo, resultado));
            codigoIntermedio.add(generadorIntermedio.generar(
                    linea, nombre, expresionInicial));
        }
    }

    private void procesarAsignacion(String nombre, String expresion, int linea)
            throws ExcepcionSemantica {
        Simbolo simbolo = tabla.buscar(nombre);
        if (simbolo == null) {
            throw new ExcepcionSemantica(
                    "la variable '" + nombre + "' no ha sido declarada");
        }

        ResultadoExpresion resultado = expresiones.analizar(expresion);
        validarAsignacion(simbolo, resultado);
        simbolo.setValor(convertirValor(simbolo.getTipo(), resultado));
        codigoIntermedio.add(generadorIntermedio.generar(
                linea, nombre, expresion));
    }

    private void procesarMostrar(String expresion) throws ExcepcionSemantica {
        ResultadoExpresion resultado = expresiones.analizar(expresion);
        salidas.add(String.valueOf(resultado.valor()));
    }

    private void validarAsignacion(Simbolo simbolo, ResultadoExpresion resultado)
            throws ExcepcionSemantica {
        TipoDato destino = simbolo.getTipo();
        TipoDato origen = resultado.tipo();

        // Un entero puede guardarse en float sin perder información.
        boolean compatible = destino == origen
                || (destino == TipoDato.FLOAT && origen == TipoDato.INT);

        if (!compatible) {
            throw new ExcepcionSemantica(
                    "no se puede asignar un valor " + origen.getNombre()
                            + " a la variable '" + simbolo.getNombre()
                            + "' de tipo " + destino.getNombre());
        }
    }

    private Object convertirValor(TipoDato destino, ResultadoExpresion resultado) {
        if (resultado.valor() == null) {
            return null;
        }
        if (destino == TipoDato.FLOAT && resultado.tipo() == TipoDato.INT) {
            return ((Integer) resultado.valor()).floatValue();
        }
        return resultado.valor();
    }

    private String quitarComentario(String linea) {
        boolean dentroCadena = false;
        for (int i = 0; i < linea.length() - 1; i++) {
            if (linea.charAt(i) == '"') {
                dentroCadena = !dentroCadena;
            }
            if (!dentroCadena && linea.charAt(i) == '/'
                    && linea.charAt(i + 1) == '/') {
                return linea.substring(0, i);
            }
        }
        return linea;
    }

    private void agregarError(int linea, String mensaje) {
        errores.add(new ErrorSemantico(linea, mensaje));
    }

    public TablaSimbolos getTabla() {
        return tabla;
    }

    public List<ErrorSemantico> getErrores() {
        return List.copyOf(errores);
    }

    public List<String> getSalidas() {
        return List.copyOf(salidas);
    }

    public List<ResultadoCodigoIntermedio> getCodigoIntermedio() {
        return List.copyOf(codigoIntermedio);
    }

    public ResultadoOptimizacion getOptimizacion() {
        return optimizacion;
    }
}
