package mx.edu.tecnm.semantico;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AplicacionAnalizador extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                AplicacionAnalizador.class.getResource("analizador-view.fxml"));

        Scene scene = new Scene(loader.load(), 1280, 760);
        scene.getStylesheets().add(
                AplicacionAnalizador.class.getResource("styles.css").toExternalForm());

        stage.setTitle("Analizador semántico - Unidades 1, 2 y 3");
        stage.setScene(scene);
        stage.setMinWidth(1050);
        stage.setMinHeight(680);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
