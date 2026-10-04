package mx.edu.tecnm.semantico;

/**
 * Instrucción emitida por la arquitectura didáctica TECNM-32.
 *
 * Formato de palabra de 32 bits:
 * [ opcode:8 ][ regA:4 ][ regB:4 ][ operando/dirección:16 ]
 */
public record InstruccionObjeto(
        int direccion,
        String ensamblador,
        int palabra) {

    public String hexadecimal() {
        return String.format("%08X", palabra);
    }

    public String binario() {
        String bits = String.format("%32s", Integer.toBinaryString(palabra))
                .replace(' ', '0');
        return bits.substring(0, 8) + " "
                + bits.substring(8, 12) + " "
                + bits.substring(12, 16) + " "
                + bits.substring(16);
    }
}
