package mx.edu.tecnm.semantico;

public class Simbolo {
    private final String nombre;
    private final TipoDato tipo;
    private final String ambito;
    private final int direccion;
    private final int lineaDeclaracion;
    private Object valor;

    public Simbolo(String nombre, TipoDato tipo, String ambito,
                   int direccion, int lineaDeclaracion) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.ambito = ambito;
        this.direccion = direccion;
        this.lineaDeclaracion = lineaDeclaracion;
    }

    public String getNombre() {
        return nombre;
    }

    public TipoDato getTipo() {
        return tipo;
    }

    public String getAmbito() {
        return ambito;
    }

    public int getDireccion() {
        return direccion;
    }

    public int getLineaDeclaracion() {
        return lineaDeclaracion;
    }

    public Object getValor() {
        return valor;
    }

    public void setValor(Object valor) {
        this.valor = valor;
    }

    public String valorComoTexto() {
        if (valor == null) {
            return "sin asignar";
        }
        if (valor instanceof String) {
            return '"' + valor.toString() + '"';
        }
        return valor.toString();
    }
}
