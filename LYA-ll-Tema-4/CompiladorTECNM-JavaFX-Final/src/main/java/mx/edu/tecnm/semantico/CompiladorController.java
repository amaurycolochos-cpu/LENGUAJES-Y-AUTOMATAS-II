package mx.edu.tecnm.semantico;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

public final class CompiladorController {
    @FXML private TextArea codigoArea;
    @FXML private TextArea lineasArea;
    @FXML private TextArea salidaArea;
    @FXML private Label estadoLabel;
    @FXML private Label archivoLabel;
    @FXML private Label posicionLabel;
    @FXML private Label buildStatusLabel;
    @FXML private Button ejecutarButton;

    private final CompiladorLenguaje compilador = new CompiladorLenguaje();
    private ResultadoCompiladorLenguaje ultimaCompilacion;
    private String ultimaFuenteCompilada = "";
    private Path archivoActual;

    @FXML
    public void initialize() {
        codigoArea.textProperty().addListener((obs, anterior, actual) -> {
            actualizarLineas();
            actualizarPosicionCursor();
            if (ultimaCompilacion != null && !actual.equals(ultimaFuenteCompilada)) {
                ejecutarButton.setDisable(true);
                buildStatusLabel.setText("Código modificado");
                if (estadoLabel.getText().contains("correcta")) {
                    mostrarEstado("Modificado · compile de nuevo", "estado-neutral");
                }
            }
        });
        codigoArea.caretPositionProperty().addListener((obs, anterior, actual) -> actualizarPosicionCursor());
        codigoArea.scrollTopProperty().addListener((obs, anterior, actual) -> lineasArea.setScrollTop(actual.doubleValue()));

        nuevoArchivo();
        Platform.runLater(() -> {
            actualizarLineas();
            actualizarPosicionCursor();
            codigoArea.positionCaret(0);
        });
    }

    @FXML
    private void nuevoArchivo() {
        codigoArea.setText(plantilla());
        archivoActual = null;
        archivoLabel.setText("MiPrograma.tec");
        limpiarResultados();
        mostrarEstado("Listo", "estado-neutral");
        buildStatusLabel.setText("Sin compilar");
        Platform.runLater(() -> codigoArea.positionCaret(0));
    }

    @FXML
    private void abrirArchivo() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Abrir código fuente");
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Lenguaje TECNM (*.tec)", "*.tec"),
                new FileChooser.ExtensionFilter("Archivos de texto (*.txt)", "*.txt"),
                new FileChooser.ExtensionFilter("Todos los archivos", "*.*"));
        File f = fc.showOpenDialog(ventana());
        if (f == null) return;

        try {
            archivoActual = f.toPath();
            codigoArea.setText(Files.readString(archivoActual));
            archivoLabel.setText(archivoActual.getFileName().toString());
            limpiarResultados();
            mostrarEstado("Archivo abierto", "estado-neutral");
            buildStatusLabel.setText("Sin compilar");
            Platform.runLater(() -> codigoArea.positionCaret(0));
        } catch (IOException e) {
            salidaArea.setText("No se pudo abrir el archivo.\n" + e.getMessage());
            aplicarClaseSalida(true);
            mostrarEstado("Error al abrir", "estado-error");
            buildStatusLabel.setText("Error");
        }
    }

    @FXML
    private void guardarArchivo() {
        Path destino = archivoActual;
        if (destino == null) {
            FileChooser fc = new FileChooser();
            fc.setTitle("Guardar código fuente");
            fc.setInitialFileName(nombreClaseSugerido() + ".tec");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Lenguaje TECNM (*.tec)", "*.tec"));
            File f = fc.showSaveDialog(ventana());
            if (f == null) return;
            destino = f.toPath();
            if (!destino.getFileName().toString().contains(".")) destino = Path.of(destino + ".tec");
        }

        try {
            Files.writeString(destino, codigoArea.getText());
            archivoActual = destino;
            archivoLabel.setText(destino.getFileName().toString());
            mostrarEstado("Archivo guardado", "estado-correcto");
        } catch (IOException e) {
            salidaArea.setText("No se pudo guardar el archivo.\n" + e.getMessage());
            aplicarClaseSalida(true);
            mostrarEstado("Error al guardar", "estado-error");
            buildStatusLabel.setText("Error");
        }
    }

    @FXML
    private void compilarCodigo() {
        compilar(false);
    }

    @FXML
    private void ejecutarCodigo() {
        if (ultimaCompilacion == null
                || !codigoArea.getText().equals(ultimaFuenteCompilada)
                || !ultimaCompilacion.correcto()) {
            if (!compilar(true)) return;
        }

        try {
            String salida = compilador.ejecutar(ultimaCompilacion);
            StringBuilder texto = new StringBuilder();
            texto.append("BUILD SUCCESSFUL\n");
            texto.append("════════════════════════════════════════════════════════════\n");
            texto.append("Compilación terminada correctamente · 0 errores\n\n");
            texto.append("EJECUCIÓN DEL PROGRAMA\n");
            texto.append("────────────────────────────────────────────────────────────\n");
            texto.append(salida.isBlank()
                    ? "El programa terminó sin producir salida en consola."
                    : salida.stripTrailing());

            salidaArea.setText(texto.toString());
            aplicarClaseSalida(false);
            mostrarEstado("Ejecución correcta", "estado-correcto");
            buildStatusLabel.setText("Proceso finalizado");
            ejecutarButton.setDisable(false);
        } catch (Exception e) {
            salidaArea.setText("RUN FAILED\n"
                    + "════════════════════════════════════════════════════════════\n"
                    + "Error durante la ejecución:\n" + e.getMessage());
            aplicarClaseSalida(true);
            mostrarEstado("Error de ejecución", "estado-error");
            buildStatusLabel.setText("Ejecución fallida");
        }
    }

    @FXML
    private void limpiarSoloSalida() {
        salidaArea.clear();
        aplicarClaseSalida(false);
        buildStatusLabel.setText(ultimaCompilacion == null ? "Sin compilar" : "Salida limpiada");
    }

    @FXML
    private void verAnalisis() {
        if (ultimaCompilacion == null || !codigoArea.getText().equals(ultimaFuenteCompilada)) {
            compilar(false);
        }
        if (ultimaCompilacion == null) return;

        Stage detalle = new Stage();
        detalle.initOwner(ventana());
        detalle.setTitle("Análisis interno · Compilador TECNM - Amaury Gordillo");

        Label titulo = new Label("Análisis interno del compilador");
        titulo.getStyleClass().add("analysis-title");

        TabPane tabs = new TabPane();
        tabs.getStyleClass().add("analysis-tab-pane");
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.getTabs().add(tab("Tokens", textoTokens(ultimaCompilacion)));
        tabs.getTabs().add(tab("Árbol sintáctico", textoSintactico(ultimaCompilacion)));
        tabs.getTabs().add(tab("Semántica", textoSemantico(ultimaCompilacion)));
        tabs.getTabs().add(tab("Java generado", textoJava(ultimaCompilacion)));
        VBox.setVgrow(tabs, Priority.ALWAYS);

        VBox root = new VBox(10, titulo, tabs);
        root.getStyleClass().add("analysis-root");
        root.setPadding(new Insets(14));

        Scene scene = new Scene(root, 980, 650);
        scene.getStylesheets().add(AplicacionCompilador.class.getResource("compilador-styles.css").toExternalForm());
        detalle.setScene(scene);
        detalle.setMinWidth(760);
        detalle.setMinHeight(520);
        detalle.show();
    }

    private Tab tab(String titulo, String contenido) {
        TextArea area = new TextArea(contenido);
        area.setEditable(false);
        area.setWrapText(false);
        area.getStyleClass().add("analysis-area");
        return new Tab(titulo, area);
    }

    private boolean compilar(boolean paraEjecutar) {
        String fuente = codigoArea.getText();
        ultimaCompilacion = compilador.compilar(fuente);
        ultimaFuenteCompilada = fuente;

        if (ultimaCompilacion.correcto()) {
            salidaArea.setText(resumenExitoso(ultimaCompilacion)
                    + (paraEjecutar
                        ? "\n\nIniciando ejecución..."
                        : "\n\nEl programa está listo para ejecutarse."));
            aplicarClaseSalida(false);
            mostrarEstado("Compilación correcta", "estado-correcto");
            buildStatusLabel.setText("0 errores");
            ejecutarButton.setDisable(false);
            return true;
        }

        salidaArea.setText(formatearErroresBuild(ultimaCompilacion));
        aplicarClaseSalida(true);
        mostrarEstado("Compilación con errores", "estado-error");
        buildStatusLabel.setText(contarErrores(ultimaCompilacion) + (contarErrores(ultimaCompilacion) == 1 ? " error" : " errores"));
        ejecutarButton.setDisable(true);
        resaltarPrimerError();
        return false;
    }

    private String resumenExitoso(ResultadoCompiladorLenguaje r) {
        int tokens = r.lexico() == null ? 0 : Math.max(0, r.lexico().tokens().size() - 1);
        int simbolos = r.semantico() == null ? 0 : r.semantico().simbolos().size();
        String clase = r.sintactico() == null || r.sintactico().programa() == null
                ? "?"
                : r.sintactico().programa().nombreClase();

        return "BUILD SUCCESSFUL\n"
                + "════════════════════════════════════════════════════════════\n"
                + nombreArchivoActual() + "\n"
                + "Compilación terminada correctamente · 0 errores\n\n"
                + "[OK] Análisis léxico · " + tokens + " tokens\n"
                + "[OK] Análisis sintáctico · estructura válida\n"
                + "[OK] Análisis semántico · " + simbolos + " símbolos válidos\n"
                + "[OK] Traducción · clase Java " + clase + " generada\n"
                + "[OK] javac · archivo .class generado";
    }

    private String formatearErroresBuild(ResultadoCompiladorLenguaje r) {
        String detalle = r.errores().stream()
                .map(this::formatearError)
                .collect(Collectors.joining(System.lineSeparator() + System.lineSeparator()));
        int total = contarErrores(r);

        return "BUILD FAILED\n"
                + "════════════════════════════════════════════════════════════\n"
                + nombreArchivoActual() + "\n\n"
                + detalle + "\n\n"
                + "────────────────────────────────────────────────────────────\n"
                + total + (total == 1 ? " error" : " errores")
                + " · la compilación se detuvo automáticamente.";
    }

    private String formatearError(ErrorCompilador error) {
        String ubicacion = error.linea() > 0
                ? "Línea " + error.linea() + (error.columna() > 0 ? ", columna " + error.columna() : "")
                : "Sin ubicación";
        return "ERROR " + nombreFase(error.fase()) + " · " + ubicacion + "\n"
                + error.mensaje();
    }

    private String nombreFase(ErrorCompilador.Fase fase) {
        return switch (fase) {
            case LEXICO -> "LÉXICO";
            case SINTACTICO -> "SINTÁCTICO";
            case SEMANTICO -> "SEMÁNTICO";
            case TRADUCCION -> "DE TRADUCCIÓN";
            case JAVAC -> "DE JAVAC";
            case EJECUCION -> "DE EJECUCIÓN";
        };
    }

    private int contarErrores(ResultadoCompiladorLenguaje r) {
        return r == null ? 0 : r.errores().size();
    }

    private String textoTokens(ResultadoCompiladorLenguaje r) {
        if (r.lexico() == null) return "El análisis léxico no está disponible.";
        StringBuilder sb = new StringBuilder("TOKEN                LEXEMA               POSICIÓN\n");
        sb.append("────────────────────────────────────────────────────────\n");
        for (TokenLenguaje t : r.lexico().tokens()) {
            if (t.tipo() == TipoToken.EOF) continue;
            String lex = t.lexema().replace("\n", "\\n");
            sb.append(String.format("%-20s %-20s L%d:C%d%n", t.tipo(), lex, t.linea(), t.columna()));
        }
        return sb.toString();
    }

    private String textoSintactico(ResultadoCompiladorLenguaje r) {
        return r.sintactico() != null && r.sintactico().programa() != null
                ? FormateadorAst.formatear(r.sintactico().programa())
                : "El árbol sintáctico no está disponible porque el análisis sintáctico no terminó correctamente.";
    }

    private String textoSemantico(ResultadoCompiladorLenguaje r) {
        if (r.semantico() == null) return "La fase semántica no se ejecutó.";
        StringBuilder sb = new StringBuilder("TABLA DE SÍMBOLOS\n");
        sb.append("────────────────────────────────────────────────────────\n");
        sb.append(String.format("%-18s %-12s %-10s %-8s%n", "NOMBRE", "TIPO", "LÍNEA", "NIVEL"));
        for (SimboloLenguaje s : r.semantico().simbolos()) {
            sb.append(String.format("%-18s %-12s %-10d %-8d%n",
                    s.nombre(), s.tipo().nombre(), s.lineaDeclaracion(), s.nivel()));
        }
        if (r.semantico().simbolos().isEmpty()) sb.append("(Sin variables declaradas)\n");
        return sb.toString();
    }

    private String textoJava(ResultadoCompiladorLenguaje r) {
        return r.codigoJava() == null || r.codigoJava().isBlank()
                ? "El código Java solo se genera cuando el programa supera correctamente las fases anteriores."
                : r.codigoJava();
    }

    private void limpiarResultados() {
        salidaArea.clear();
        ultimaCompilacion = null;
        ultimaFuenteCompilada = "";
        ejecutarButton.setDisable(true);
        aplicarClaseSalida(false);
        actualizarLineas();
    }

    private void aplicarClaseSalida(boolean error) {
        salidaArea.getStyleClass().removeAll("salida-error", "salida-correcta");
        salidaArea.getStyleClass().add(error ? "salida-error" : "salida-correcta");
    }

    private void mostrarEstado(String texto, String clase) {
        estadoLabel.setText(texto);
        estadoLabel.getStyleClass().removeAll("estado-neutral", "estado-correcto", "estado-error");
        estadoLabel.getStyleClass().add(clase);
    }

    private void actualizarLineas() {
        if (lineasArea == null || codigoArea == null) return;
        String texto = codigoArea.getText();
        int totalLineas = texto.isEmpty() ? 1 : texto.split("\\R", -1).length;
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= totalLineas; i++) {
            sb.append(String.format("%3d", i));
            if (i < totalLineas) sb.append(System.lineSeparator());
        }
        lineasArea.setText(sb.toString());
        lineasArea.setScrollTop(codigoArea.getScrollTop());
    }

    private void actualizarPosicionCursor() {
        if (codigoArea == null || posicionLabel == null) return;
        int caret = codigoArea.getCaretPosition();
        String texto = codigoArea.getText();
        int linea = 1;
        int columna = 1;
        for (int i = 0; i < caret && i < texto.length(); i++) {
            if (texto.charAt(i) == '\n') {
                linea++;
                columna = 1;
            } else {
                columna++;
            }
        }
        posicionLabel.setText("Línea " + linea + ", columna " + columna);
    }

    private void resaltarPrimerError() {
        if (ultimaCompilacion == null || ultimaCompilacion.errores().isEmpty()) return;
        ErrorCompilador error = ultimaCompilacion.errores().get(0);
        if (error.linea() <= 0) return;
        int[] rango = calcularRango(error.linea(), error.columna());
        Platform.runLater(() -> {
            codigoArea.requestFocus();
            codigoArea.selectRange(rango[0], rango[1]);
        });
    }

    private int[] calcularRango(int lineaObjetivo, int columnaObjetivo) {
        String texto = codigoArea.getText();
        int indice = 0;
        int lineaActual = 1;
        while (lineaActual < lineaObjetivo && indice < texto.length()) {
            if (texto.charAt(indice) == '\n') lineaActual++;
            indice++;
        }

        int inicio = Math.min(indice + Math.max(0, columnaObjetivo - 1), texto.length());
        int fin = inicio;
        while (fin < texto.length()
                && texto.charAt(fin) != '\n'
                && !Character.isWhitespace(texto.charAt(fin))
                && ";(){}[]".indexOf(texto.charAt(fin)) == -1) {
            fin++;
        }
        if (fin == inicio && fin < texto.length()) fin++;
        return new int[] { inicio, Math.min(fin, texto.length()) };
    }

    private String nombreArchivoActual() {
        return archivoActual == null ? archivoLabel.getText() : archivoActual.getFileName().toString();
    }

    private String nombreClaseSugerido() {
        if (ultimaCompilacion != null
                && ultimaCompilacion.sintactico() != null
                && ultimaCompilacion.sintactico().programa() != null) {
            return ultimaCompilacion.sintactico().programa().nombreClase();
        }
        return "MiPrograma";
    }

    private Window ventana() {
        return codigoArea.getScene() == null ? null : codigoArea.getScene().getWindow();
    }

    private String plantilla() {
        return "publico clase MiPrograma inicio\n\n"
                + "    publico estatico vacio principal(cadena[] argumentos) inicio\n\n"
                + "        imprimir(\"Hola Mundo\");\n\n"
                + "    fin\n\n"
                + "fin\n";
    }
}
