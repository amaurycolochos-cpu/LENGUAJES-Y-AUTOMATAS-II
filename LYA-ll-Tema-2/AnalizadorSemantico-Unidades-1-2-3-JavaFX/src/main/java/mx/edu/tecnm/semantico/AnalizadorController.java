package mx.edu.tecnm.semantico;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import javafx.stage.Window;

public class AnalizadorController {
    @FXML private ComboBox<String> selectorCaso;
    @FXML private TextArea codigoArea;
    @FXML private TextArea salidasArea;
    @FXML private TextArea erroresArea;
    @FXML private TextArea notacionesArea;
    @FXML private TextArea codigoIntermedioArea;
    @FXML private TextArea cuadruplosArea;
    @FXML private TextArea triplosArea;
    @FXML private TextArea pCodeArea;
    @FXML private TextArea optimizacionArea;
    @FXML private TableView<Simbolo> tablaSimbolos;
    @FXML private TableColumn<Simbolo, String> nombreColumn;
    @FXML private TableColumn<Simbolo, String> tipoColumn;
    @FXML private TableColumn<Simbolo, String> ambitoColumn;
    @FXML private TableColumn<Simbolo, Integer> direccionColumn;
    @FXML private TableColumn<Simbolo, String> valorColumn;
    @FXML private Label estadoLabel;
    @FXML private Label archivoLabel;

    private final Map<String, String> casos = new LinkedHashMap<>();

    @FXML
    public void initialize() {
        prepararCasos();
        prepararTabla();

        selectorCaso.setItems(FXCollections.observableArrayList(casos.keySet()));
        selectorCaso.getSelectionModel().selectedItemProperty().addListener(
                (observable, anterior, seleccionado) -> cargarCaso(seleccionado));
        selectorCaso.getSelectionModel().selectFirst();
    }

    private void prepararCasos() {
        // Unidad 1: se conserva exactamente como base del proyecto.
        casos.put("U1 · Actividad 1.2 - Programa correcto",
                "ejemplos/programa_correcto.txt");
        casos.put("U1 · Actividad 1.2 - Programa con errores",
                "ejemplos/programa_con_errores.txt");

        String[] nombresU1 = {
            "Tabla de símbolos", "Expresiones aritméticas", "Comprobación de tipos",
            "Concatenación de cadenas", "Variable duplicada", "Variable no declarada",
            "Asignación incompatible", "División entre cero", "Errores múltiples",
            "Caso integrador"
        };
        for (int i = 0; i < nombresU1.length; i++) {
            String numero = String.format("%02d", i + 1);
            casos.put("U1 · Actividad 1.3 - Ejercicio " + (i + 1) + ": " + nombresU1[i],
                    "ejemplos/banco_ejercicios/ejercicio_" + numero + ".txt");
        }

        String[] pruebasU2 = {
            "Operación simple", "Precedencia de operadores", "Uso de paréntesis",
            "Asociatividad", "Caso integrador"
        };
        for (int i = 0; i < pruebasU2.length; i++) {
            casos.put("U2 · Actividad 2.2 - Prueba " + (i + 1) + ": " + pruebasU2[i],
                    "ejemplos/unidad_2/actividad_2_2/prueba_"
                            + String.format("%02d", i + 1) + ".txt");
        }

        String[] ejerciciosU2 = {
            "Infija, prefija y postfija", "Notación polaca", "Código P",
            "Triplos", "Cuádruplos", "Temporales y precedencia",
            "Asignaciones consecutivas", "Expresiones con float",
            "Expresión con signo negativo", "Integrador de Unidad 2"
        };
        for (int i = 0; i < ejerciciosU2.length; i++) {
            casos.put("U2 · Actividad 2.3 - Ejercicio " + (i + 1) + ": " + ejerciciosU2[i],
                    "ejemplos/unidad_2/actividad_2_3/ejercicio_"
                            + String.format("%02d", i + 1) + ".txt");
        }

        String[] pruebasU3 = {
            "Plegado de constantes", "Simplificación algebraica",
            "Propagación de constantes", "Reducción de fuerza", "Caso integrador"
        };
        for (int i = 0; i < pruebasU3.length; i++) {
            casos.put("U3 · Actividad 3.2 - Prueba " + (i + 1) + ": " + pruebasU3[i],
                    "ejemplos/unidad_3/actividad_3_2/prueba_"
                            + String.format("%02d", i + 1) + ".txt");
        }

        String[] ejerciciosU3 = {
            "Optimización local", "Identidades algebraicas", "Constantes",
            "Copias", "Subexpresiones comunes", "Reducción de fuerza",
            "Código redundante", "Temporales", "Mirilla", "Integrador de Unidad 3"
        };
        for (int i = 0; i < ejerciciosU3.length; i++) {
            casos.put("U3 · Actividad 3.3 - Ejercicio " + (i + 1) + ": " + ejerciciosU3[i],
                    "ejemplos/unidad_3/actividad_3_3/ejercicio_"
                            + String.format("%02d", i + 1) + ".txt");
        }
    }

    private void prepararTabla() {
        tablaSimbolos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        nombreColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getNombre()));
        tipoColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getTipo().getNombre()));
        ambitoColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getAmbito()));
        direccionColumn.setCellValueFactory(data ->
                new SimpleIntegerProperty(data.getValue().getDireccion()).asObject());
        valorColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().valorComoTexto()));
    }

    private void cargarCaso(String nombreCaso) {
        if (nombreCaso != null) {
            cargarArchivo(Path.of(casos.get(nombreCaso)));
        }
    }

    private void cargarArchivo(Path archivo) {
        try {
            codigoArea.setText(Files.readString(archivo));
            archivoLabel.setText("Archivo: " + archivo.toString().replace('\\', '/'));
            limpiarResultados();
            mostrarEstado("Listo para analizar", "estado-neutral");
        } catch (IOException e) {
            codigoArea.clear();
            archivoLabel.setText("No se pudo abrir el archivo");
            erroresArea.setText(e.getMessage());
            mostrarEstado("No se pudo cargar el archivo", "estado-error");
        }
    }

    @FXML
    private void analizarCodigo() {
        List<String> lineas = codigoArea.getText().lines().toList();
        AnalizadorSemantico analizador = new AnalizadorSemantico();
        analizador.analizar(lineas);

        tablaSimbolos.setItems(FXCollections.observableArrayList(
                analizador.getTabla().obtenerTodos()));

        salidasArea.setText(analizador.getSalidas().isEmpty()
                ? "Sin salidas."
                : String.join(System.lineSeparator(), analizador.getSalidas()));

        notacionesArea.setText(FormateadorResultados.notaciones(
                analizador.getCodigoIntermedio()));
        codigoIntermedioArea.setText(FormateadorResultados.tresDirecciones(
                analizador.getCodigoIntermedio()));
        cuadruplosArea.setText(FormateadorResultados.cuadruplos(
                analizador.getCodigoIntermedio()));
        triplosArea.setText(FormateadorResultados.triplos(
                analizador.getCodigoIntermedio()));
        pCodeArea.setText(FormateadorResultados.pCode(
                analizador.getCodigoIntermedio()));
        optimizacionArea.setText(FormateadorResultados.optimizacion(
                analizador.getOptimizacion()));

        if (analizador.getErrores().isEmpty()) {
            erroresArea.setText("No se encontraron errores semánticos.");
            mostrarEstado("Análisis correcto", "estado-correcto");
        } else {
            StringBuilder texto = new StringBuilder();
            texto.append("Se encontraron ")
                    .append(analizador.getErrores().size())
                    .append(" error(es) semántico(s):")
                    .append(System.lineSeparator())
                    .append(System.lineSeparator());
            for (ErrorSemantico error : analizador.getErrores()) {
                texto.append(error).append(System.lineSeparator());
            }
            texto.append(System.lineSeparator())
                    .append("Las representaciones intermedias se muestran únicamente ")
                    .append("para las instrucciones que sí fueron válidas.");
            erroresArea.setText(texto.toString());
            mostrarEstado("Análisis con errores", "estado-error");
        }
    }

    @FXML
    private void abrirArchivo() {
        FileChooser selector = new FileChooser();
        selector.setTitle("Seleccionar archivo para analizar");
        selector.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Archivos de texto", "*.txt"));

        Window ventana = codigoArea.getScene().getWindow();
        File elegido = selector.showOpenDialog(ventana);
        if (elegido != null) {
            selectorCaso.getSelectionModel().clearSelection();
            cargarArchivo(elegido.toPath());
        }
    }

    @FXML
    private void limpiarTodo() {
        codigoArea.clear();
        selectorCaso.getSelectionModel().clearSelection();
        archivoLabel.setText("Sin archivo seleccionado");
        limpiarResultados();
        mostrarEstado("Editor limpio", "estado-neutral");
    }

    private void limpiarResultados() {
        tablaSimbolos.getItems().clear();
        salidasArea.clear();
        erroresArea.clear();
        notacionesArea.clear();
        codigoIntermedioArea.clear();
        cuadruplosArea.clear();
        triplosArea.clear();
        pCodeArea.clear();
        optimizacionArea.clear();
    }

    private void mostrarEstado(String mensaje, String clase) {
        estadoLabel.setText(mensaje);
        estadoLabel.getStyleClass().removeAll(
                "estado-neutral", "estado-correcto", "estado-error");
        estadoLabel.getStyleClass().add(clase);
    }
}
