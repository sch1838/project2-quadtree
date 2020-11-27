package gui;

import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.FileLoader;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

public class Display {

    public static final FileChooser FILE_CHOOSER = new FileChooser();

    public static boolean validatePaths(String source, String destination) {
        return source != null && destination != null && !source.isEmpty() && !destination.isEmpty();
    }

    public static boolean validatePath(String path) {
        return path != null && !path.isEmpty();
    }

    public static String postFileSelectionAsPath(Stage stage, String title, boolean save, FileChooser.ExtensionFilter... filters) {
        FILE_CHOOSER.setTitle(title);
        FILE_CHOOSER.getExtensionFilters().clear();

        if(filters.length == 0) {
            FILE_CHOOSER.getExtensionFilters().add(Constants.BOTH_FILTERS);
        } else {
            FILE_CHOOSER.getExtensionFilters().addAll(filters);
        }

        File file = save ? FILE_CHOOSER.showSaveDialog(stage) : FILE_CHOOSER.showOpenDialog(stage);

        if (file != null) {
            return file.getPath();
        }

        return "";
    }

    public static void postFileSelection(Stage stage, String title, boolean save, Consumer<String> pathApplicator, FileChooser.ExtensionFilter... filters) {
        FILE_CHOOSER.setTitle(title);
        FILE_CHOOSER.getExtensionFilters().clear();

        if(filters.length == 0) {
            FILE_CHOOSER.getExtensionFilters().add(Constants.BOTH_FILTERS);
        } else {
            FILE_CHOOSER.getExtensionFilters().addAll(filters);
        }

        File file = save ? FILE_CHOOSER.showSaveDialog(stage) : FILE_CHOOSER.showOpenDialog(stage);

        if (file != null) {
            pathApplicator.accept(file.getPath());
        }
    }

    public static void saveAs(Stage stage) {
        String destination = postFileSelectionAsPath(stage, "Save As", true);
        destinationPathField.setText(destination);
        saveToDestination();
    }

    public static void saveToDestination() {
        if(!destinationPathField.getText().isEmpty() && !destinationPathField.getText().equals(Constants.NO_PATH)) {
            if(activeContents != null) {
                FileLoader.secureWriteFileContents(activeContents, destinationPathField.getText());
            }
        }
    }

    public static List<String> activeContents;

    public static Mode activeMode = Mode.DISPLAY;

    public static int zoom;

    public static final BorderPane container = new BorderPane();
    public static final ListView<Label> output = new ListView<>();

    public static final TextField sourcePathField = new TextField(Constants.NO_PATH), destinationPathField = new TextField(Constants.NO_PATH);

    static {
        // Add and remove rectangle to expand the window to fit an image
        container.setCenter(new Rectangle(1024, 384));
        Platform.runLater(() -> container.setCenter(null));

        Label welcome = new Label("Loaded Compresora");
        output.getItems().add(welcome);
        Platform.runLater(() -> output.prefHeightProperty().bind(welcome.heightProperty().multiply(7)));
        Display.container.setBottom(output);
    }

    public static void post(String message) {
        output.getItems().add(new Label(message));
    }

    public static GridPane createButtonDisplay(Stage stage) {
        GridPane view = new GridPane();

        GridPane pathDisplay = new GridPane();
            pathDisplay.setHgap(10);
            pathDisplay.setVgap(12);
            pathDisplay.add(new Label("Source: "), 0, 0);
            pathDisplay.add(new Label("Destination: "), 0, 1);
            pathDisplay.add(sourcePathField, 1, 0);
            pathDisplay.add(destinationPathField, 1, 1);
                Button sourceSelect = new Button("Select Path");
                    sourceSelect.setOnAction(actionEvent -> postFileSelection(stage, "Select Source File", false, sourcePathField::setText));
                MenuButton changeDestination = new MenuButton("Select Path");
                    MenuItem newFile = new MenuItem("New File");
                        newFile.setOnAction(actionEvent -> postFileSelection(stage, "Destination As", true, destinationPathField::setText));
                    MenuItem existingFile = new MenuItem("Existing File");
                        existingFile.setOnAction(actionEvent -> postFileSelection(stage, "Select Destination File", false, destinationPathField::setText));
                    changeDestination.getItems().addAll(newFile, existingFile);
            pathDisplay.add(sourceSelect, 2, 0);
            pathDisplay.add(changeDestination, 2, 1);

            sourceSelect.setMaxWidth(Double.MAX_VALUE);

            GridPane.setHgrow(sourcePathField, Priority.ALWAYS);
            GridPane.setHgrow(destinationPathField, Priority.ALWAYS);

            GridPane.setHgrow(sourceSelect, Priority.SOMETIMES);

        GridPane actionPane = new GridPane();
            TextField currentMode = new TextField(Mode.DISPLAY.name());
                currentMode.setMaxWidth(Double.MAX_VALUE);
            MenuButton modeSelect = new MenuButton("Mode");
                CheckMenuItem displayImage = new CheckMenuItem("Display Image");
                    displayImage.setSelected(true);
                CheckMenuItem compressSource = new CheckMenuItem("Compress Source");
                CheckMenuItem uncompressSource = new CheckMenuItem("Uncompress Source");
                    displayImage.setOnAction(actionEvent -> {
                        compressSource.setSelected(false);
                        uncompressSource.setSelected(false);
                        activeMode = Mode.DISPLAY;
                        currentMode.setText(Mode.DISPLAY.name());
                    });
                    compressSource.setOnAction(actionEvent -> {
                        displayImage.setSelected(false);
                        uncompressSource.setSelected(false);
                        activeMode = Mode.COMPRESS;
                        currentMode.setText(Mode.COMPRESS.name());
                    });
                    uncompressSource.setOnAction(actionEvent -> {
                        compressSource.setSelected(false);
                        displayImage.setSelected(false);
                        activeMode = Mode.UNCOMPRESS;
                        currentMode.setText(Mode.UNCOMPRESS.name());
                    });
                modeSelect.getItems().addAll(displayImage, compressSource, uncompressSource);
                modeSelect.setMaxWidth(Double.MAX_VALUE);
            Button run = new Button("Run");
                run.setMaxWidth(Double.MAX_VALUE);
            Button save = new Button("Save");
                save.setMaxWidth(Double.MAX_VALUE);
            Button saveAs = new Button("Save As");
                saveAs.setMaxWidth(Double.MAX_VALUE);

            actionPane.add(modeSelect, 0, 0);
            actionPane.add(currentMode, 0, 1);
            actionPane.add(run, 1, 0);
            actionPane.add(save, 2, 0);
            actionPane.add(saveAs, 3, 0);

            actionPane.setHgap(10);
            actionPane.setVgap(12);

            GridPane.setHgrow(run, Priority.ALWAYS);
            GridPane.setHgrow(save, Priority.ALWAYS);
            GridPane.setHgrow(saveAs, Priority.ALWAYS);

        // Add empty rectangles to grid to add spacing between grid objects and window edge
        view.add(new Rectangle(0, 0), 0, 0);
        view.add(new Rectangle(0, 0), 2, 0);

        // Set spacing for objects in the grid
        view.setVgap(12);
        view.setHgap(10);

        // Add the path display and the action box to the grid
        view.add(pathDisplay, 1, 1);
        view.add(actionPane, 1, 2);

        // Make sure all available space will be used by the path display
        GridPane.setHgrow(pathDisplay, Priority.ALWAYS);

        return view;
    }

    public static class Constants {
        public static final String NO_PATH = "No path selected";

        public static final FileChooser.ExtensionFilter UNCOMPRESSED_FILTER = new FileChooser.ExtensionFilter("Uncompressed Image Format", "*.txt");
        public static final FileChooser.ExtensionFilter COMPRESSED_FILTER = new FileChooser.ExtensionFilter("Compressed Image Format", "*.rit");

        public static final FileChooser.ExtensionFilter BOTH_FILTERS = new FileChooser.ExtensionFilter("Image Format", "*.rit", "*.txt");
    }

    enum Mode {
        COMPRESS, UNCOMPRESS, DISPLAY
    }
}
