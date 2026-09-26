import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class App extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        Label texto = new Label("Sistema de Biblioteca");

        StackPane raiz = new StackPane();
        raiz.getChildren().add(texto);

        Scene cena = new Scene(raiz, 600, 400);
        stage.setScene(cena);
        stage.setTitle("Biblioteca");
        stage.show();
    }
}