package mx.edu.tecnm.semantico;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Generador educativo de código objeto para la Unidad 4.
 *
 * La arquitectura objetivo TECNM-32 es una máquina didáctica de 32 bits que
 * permite visualizar claramente selección de instrucciones, uso de registros,
 * direccionamiento de memoria, ensamblador y codificación máquina.
 *
 * Formato de instrucción de 32 bits:
 * [ opcode:8 ][ regA:4 ][ regB:4 ][ operando/dirección:16 ]
 */
public class GeneradorCodigoObjeto {
    private static final int BASE_TEMPORALES = 2000;
    private static final int BASE_CONSTANTES = 3000;
    private static final int PASO_MEMORIA = 4;

    private static final int OP_LD  = 0x01;
    private static final int OP_ST  = 0x02;
    private static final int OP_ADD = 0x10;
    private static final int OP_SUB = 0x11;
    private static final int OP_MUL = 0x12;
    private static final int OP_DIV = 0x13;
    private static final int OP_NEG = 0x14;
    private static final int OP_HLT = 0xFF;

    private final Map<String, Integer> variables = new LinkedHashMap<>();
    private final Map<String, Integer> temporales = new LinkedHashMap<>();
    private final Map<String, Integer> constantes = new LinkedHashMap<>();
    private final List<String> mapaMemoria = new ArrayList<>();
    private final List<String> ensamblador = new ArrayList<>();
    private final List<InstruccionObjeto> instrucciones = new ArrayList<>();
    private int siguienteTemporal;
    private int siguienteConstante;
    private int contadorPrograma;

    public ResultadoCodigoObjeto generar(
            ResultadoOptimizacion optimizacion,
            Collection<Simbolo> simbolos) {
        reiniciar();
        registrarVariables(simbolos);

        if (optimizacion == null || optimizacion.optimizado().isEmpty()) {
            return new ResultadoCodigoObjeto(
                    "TECNM-32",
                    List.copyOf(mapaMemoria),
                    List.of("; No hay código intermedio válido para traducir."),
                    List.of());
        }

        ensamblador.add("; Arquitectura objetivo: TECNM-32 (didáctica, palabra de 32 bits)");
        ensamblador.add("; R0 = acumulador principal | R1 = segundo operando");
        ensamblador.add("; El código se genera a partir del código intermedio optimizado de la Unidad 3.");
        ensamblador.add("");
        ensamblador.add(".text");

        for (Cuadruplo q : optimizacion.optimizado()) {
            traducir(q);
        }
        emitir("HLT", OP_HLT, 0, 0, 0);

        // Añadir temporales y constantes al mapa una vez que ya conocemos todos.
        reconstruirMapaMemoria(simbolos);

        return new ResultadoCodigoObjeto(
                "TECNM-32",
                List.copyOf(mapaMemoria),
                List.copyOf(ensamblador),
                List.copyOf(instrucciones));
    }

    private void reiniciar() {
        variables.clear();
        temporales.clear();
        constantes.clear();
        mapaMemoria.clear();
        ensamblador.clear();
        instrucciones.clear();
        siguienteTemporal = BASE_TEMPORALES;
        siguienteConstante = BASE_CONSTANTES;
        contadorPrograma = 0;
    }

    private void registrarVariables(Collection<Simbolo> simbolos) {
        for (Simbolo simbolo : simbolos) {
            variables.put(simbolo.getNombre(), simbolo.getDireccion());
        }
    }

    private void reconstruirMapaMemoria(Collection<Simbolo> simbolos) {
        mapaMemoria.clear();
        mapaMemoria.add("SEGMENTO DE VARIABLES");
        for (Simbolo simbolo : simbolos) {
            mapaMemoria.add(String.format(
                    "[%04d] %-14s tipo=%-7s tamaño=%d byte(s) valor=%s",
                    simbolo.getDireccion(),
                    simbolo.getNombre(),
                    simbolo.getTipo().getNombre(),
                    simbolo.getTipo().getBytes(),
                    simbolo.valorComoTexto()));
        }

        mapaMemoria.add("");
        mapaMemoria.add("SEGMENTO DE TEMPORALES");
        if (temporales.isEmpty()) {
            mapaMemoria.add("(sin temporales requeridos después de optimizar)");
        } else {
            temporales.forEach((nombre, direccion) -> mapaMemoria.add(
                    String.format("[%04d] %-14s temporal de 32 bits", direccion, nombre)));
        }

        mapaMemoria.add("");
        mapaMemoria.add("POOL DE CONSTANTES");
        if (constantes.isEmpty()) {
            mapaMemoria.add("(sin constantes)");
        } else {
            constantes.forEach((literal, direccion) -> mapaMemoria.add(
                    String.format("[%04d] %-14s valor constante", direccion, literal)));
        }
    }

    private void traducir(Cuadruplo q) {
        switch (q.operador()) {
            case "=" -> {
                cargar(q.argumento1(), 0);
                guardar(q.resultado(), 0);
            }
            case "NEG" -> {
                cargar(q.argumento1(), 0);
                emitir("NEG R0", OP_NEG, 0, 0, 0);
                guardar(q.resultado(), 0);
            }
            case "+", "-", "*", "/" -> {
                cargar(q.argumento1(), 0);
                cargar(q.argumento2(), 1);
                int opcode = switch (q.operador()) {
                    case "+" -> OP_ADD;
                    case "-" -> OP_SUB;
                    case "*" -> OP_MUL;
                    case "/" -> OP_DIV;
                    default -> throw new IllegalStateException("Operador inesperado");
                };
                String mnemonico = switch (q.operador()) {
                    case "+" -> "ADD";
                    case "-" -> "SUB";
                    case "*" -> "MUL";
                    case "/" -> "DIV";
                    default -> "";
                };
                emitir(mnemonico + " R0, R1", opcode, 0, 1, 0);
                guardar(q.resultado(), 0);
            }
            default -> ensamblador.add("; Operación no traducida: " + q.comoTresDirecciones());
        }
    }

    private void cargar(String operando, int registro) {
        int direccion = direccionDe(operando);
        emitir("LD R" + registro + ", [" + direccion + "]" + comentarioOperando(operando),
                OP_LD, registro, 0, direccion);
    }

    private void guardar(String destino, int registro) {
        int direccion = direccionDeDestino(destino);
        emitir("ST [" + direccion + "], R" + registro + " ; " + destino,
                OP_ST, registro, 0, direccion);
    }

    private String comentarioOperando(String operando) {
        if (variables.containsKey(operando) || temporales.containsKey(operando)) {
            return " ; " + operando;
        }
        return " ; constante " + operando;
    }

    private int direccionDe(String valor) {
        Integer variable = variables.get(valor);
        if (variable != null) return variable;

        Integer temporal = temporales.get(valor);
        if (temporal != null) return temporal;

        if (esTemporal(valor)) {
            return temporales.computeIfAbsent(valor, clave -> reservarTemporal());
        }

        return constantes.computeIfAbsent(valor, clave -> reservarConstante());
    }

    private int direccionDeDestino(String valor) {
        Integer variable = variables.get(valor);
        if (variable != null) return variable;
        return temporales.computeIfAbsent(valor, clave -> reservarTemporal());
    }

    private int reservarTemporal() {
        int direccion = siguienteTemporal;
        siguienteTemporal += PASO_MEMORIA;
        return direccion;
    }

    private int reservarConstante() {
        int direccion = siguienteConstante;
        siguienteConstante += PASO_MEMORIA;
        return direccion;
    }

    private void emitir(
            String texto,
            int opcode,
            int regA,
            int regB,
            int operando16) {
        int palabra = ((opcode & 0xFF) << 24)
                | ((regA & 0xF) << 20)
                | ((regB & 0xF) << 16)
                | (operando16 & 0xFFFF);
        instrucciones.add(new InstruccionObjeto(contadorPrograma, texto, palabra));
        ensamblador.add(String.format("%04d  %s", contadorPrograma, texto));
        contadorPrograma++;
    }

    private boolean esTemporal(String valor) {
        return valor != null && valor.matches("t\\d+");
    }
}
