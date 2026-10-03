package mx.edu.tecnm.semantico;

import java.util.List;

/** Resultado de la Unidad 4: memoria, ensamblador y código objeto. */
public record ResultadoCodigoObjeto(
        String arquitectura,
        List<String> mapaMemoria,
        List<String> ensamblador,
        List<InstruccionObjeto> instrucciones) {

    public static ResultadoCodigoObjeto vacio() {
        return new ResultadoCodigoObjeto(
                "TECNM-32",
                List.of(),
                List.of(),
                List.of());
    }
}
