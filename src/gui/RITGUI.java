package gui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * The RITGUI class is an {@link Application} that facilitates the display, compression, and uncompression of certain
 * image file formats. All visual content is managed in {@link Display}.
 *
 * @author Samuel Henderson
 */
public class RITGUI extends Application {

    /** This is only true when the RITGUI Application has been launched. **/
    public static boolean active = false;

    @Override
    public void start(Stage stage) throws Exception {

        Display.initializePathSelection(stage);

        // Create and set the scene from the top of the pane hierarchy
        Scene mainScene = new Scene(Display.container());
        stage.setScene(mainScene);
        stage.setTitle("Compresora");
        active = true;
        stage.show();
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}
