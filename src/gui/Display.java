package gui;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.shape.Rectangle;

public class Display {

    public static boolean validatePaths(String source, String destination) {
        return source != null && destination != null && !source.isEmpty() && !destination.isEmpty();
    }

    public static boolean validatePath(String path) {
        return path != null && !path.isEmpty();
    }

    public static final BorderPane container = new BorderPane();
    public static final ListView<Label> output = new ListView<>();

    static {
        container.setCenter(new Rectangle(512, 512));
        Label welcome = new Label("Loaded Compresora");
        Display.output.getItems().add(welcome);

        Platform.runLater(() -> output.prefHeightProperty().bind(welcome.heightProperty().multiply(7)));

        Display.container.setBottom(output);

    }

    public static void post(String message) {
        output.getItems().add(new Label(message));
    }
}
