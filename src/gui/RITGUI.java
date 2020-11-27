package gui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class RITGUI extends Application {

    private Stage stage;

    @Override
    public void start(Stage stage) throws Exception {

        // Create border pane to hold grid and label pane

        this.stage = stage;

//        Display.container.setTop(createMenuBar());
        Display.container.setTop(Display.createButtonDisplay(stage));

        // Create and set the scene from the top of the pane hierarchy
        Scene mainScene = new Scene(Display.container);
        stage.setScene(mainScene);
        stage.setTitle("Compresora");
        stage.show();
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}
