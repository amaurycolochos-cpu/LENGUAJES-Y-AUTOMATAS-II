package mx.edu.tecnm.semantico;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public final class AplicacionCompilador extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(AplicacionCompilador.class.getResource("compilador-view.fxml"));
        Scene scene = new Scene(loader.load(), 1380, 820);
        scene.getStylesheets().add(AplicacionCompilador.class.getResource("compilador-styles.css").toExternalForm());
        stage.setTitle("Compilador TECNM - Amaury Gordillo · Lenguaje en Español");
        stage.setScene(scene);
        stage.setMinWidth(1100);
        stage.setMinHeight(700);
        stage.show();
    }

    public static void main(String[] args) { launch(args); }
}
