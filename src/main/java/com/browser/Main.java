import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        stage.setTitle("JavaFX OK");
        Label label = new Label("JavaFX marche");
        StackPane root = new StackPane(label);
        Scene scene = new Scene(root, 300, 200);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
