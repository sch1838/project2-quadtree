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
import ptui.RITCompress;
import ptui.RITUncompress;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

public class Display {

    //<editor-fold desc="File Selection Utility">
    private static final FileChooser FILE_CHOOSER = new FileChooser();

    public static boolean validatePaths(String source, String destination) {
        return source != null && destination != null && !source.isEmpty() && !destination.isEmpty();
    }

    public static boolean validatePath(String path) {
        return path != null && !path.isEmpty();
    }

    public static void postFileSelection(Stage stage, String title, boolean save, Consumer<String> pathApplicator, FileChooser.ExtensionFilter... filters) {
        FILE_CHOOSER.setTitle(title);
        FILE_CHOOSER.getExtensionFilters().clear();

        if (filters.length == 0) {
            FILE_CHOOSER.getExtensionFilters().add(Constants.BOTH_FILTERS);
        } else {
            FILE_CHOOSER.getExtensionFilters().addAll(filters);
        }

        File file = save ? FILE_CHOOSER.showSaveDialog(stage) : FILE_CHOOSER.showOpenDialog(stage);

        if (file != null) {
            pathApplicator.accept(file.getPath());
        }
    }

    private static void saveContentAs(Stage stage) {
        postFileSelection(stage, "Save As", true, destinationPathField::setText);
        saveContentToDestination();
    }

    private static void saveContentToDestination() {
        if(!destinationPathField.getText().isEmpty() && !destinationPathField.getText().equals(Constants.NO_PATH)) {
            if(activeContents != null) {
                FileLoader.secureWriteFileContents(activeContents, destinationPathField.getText());
            }
        }
    }
    //</editor-fold>

    //<editor-fold desc="Logical Data">
    private static List<String> activeContents;
    private static Mode activeMode = Mode.DISPLAY;
    private static int zoom = 1;

    private static final BorderPane container = new BorderPane();

    public static BorderPane container() {
        return container;
    }
    //</editor-fold>

    //<editor-fold desc="Button Display Arrangement">
    public static GridPane createButtonDisplay(Stage stage) {
        GridPane view = new GridPane();

        GridPane pathDisplay = new GridPane();
            pathDisplay.setHgap(10);
            pathDisplay.setVgap(12);
            pathDisplay.add(new Label("Source: "), 0, 0);
            pathDisplay.add(new Label("Destination: "), 0, 1);

            pathDisplay.add(sourcePathField, 1, 0);
            pathDisplay.add(destinationPathField, 1, 1);

            pathDisplay.add(sourceSelect, 2, 0);
            pathDisplay.add(destinationSelect, 2, 1);

        GridPane actionPane = new GridPane();
            actionPane.add(modeSelect, 0, 0);
            actionPane.add(currentMode, 0, 1);
            actionPane.add(run, 1, 0);
            actionPane.add(save, 2, 0);
            actionPane.add(saveAs, 3, 0);

            actionPane.setHgap(10);
            actionPane.setVgap(12);

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
    //</editor-fold>

    //<editor-fold desc="Path Selection and General Initialization">
    private static final MenuButton destinationSelect = new MenuButton("Select Path");
    private static final Button sourceSelect = new Button("Select Path");

    private static final MenuItem
        newFile = new MenuItem("New File"),
        existingFile = new MenuItem("Existing File")
    ;

    public static final TextField
        sourcePathField = new TextField(Constants.NO_PATH),
        destinationPathField = new TextField(Constants.NO_PATH)
    ;

    public static void initializePathSelection(Stage stage) {
        newFile.setOnAction(actionEvent -> postFileSelection(stage, "Destination As", true, destinationPathField::setText));
        existingFile.setOnAction(actionEvent -> postFileSelection(stage, "Select Destination File", false, destinationPathField::setText));

        sourceSelect.setOnAction(actionEvent -> postFileSelection(stage, "Select Source File", false, sourcePathField::setText));
        sourceSelect.setMaxWidth(Double.MAX_VALUE);

        destinationSelect.getItems().addAll(newFile, existingFile);

        GridPane.setHgrow(sourceSelect, Priority.SOMETIMES);
        GridPane.setHgrow(sourcePathField, Priority.ALWAYS);
        GridPane.setHgrow(destinationPathField, Priority.ALWAYS);

        container.setTop(createButtonDisplay(stage));

        container.setCenter(new Rectangle(1024, 384));
        Platform.runLater(() -> container.setCenter(null));
    }
    //</editor-fold>

    //<editor-fold desc="Action Display">
    private static final TextField currentMode = new TextField(Mode.DISPLAY.name());

    private static final MenuButton modeSelect = new MenuButton("Mode Select");

    private static final CheckMenuItem
        // Initialize mode selection buttons
        displayImage = new CheckMenuItem("Display Image"),
        compressSource = new CheckMenuItem("Compress Source"),
        uncompressSource = new CheckMenuItem("Uncompress Source")
    ;

    private static final Button
        // Initialize action buttons
        run = new Button("Run"),
        save = new Button("Save"),
        saveAs = new Button("Save As")
    ;

    static {
        // Initialize default state of actionPane components
        displayImage.setSelected(true);
        run.setMaxWidth(Double.MAX_VALUE);
        save.setMaxWidth(Double.MAX_VALUE);
        saveAs.setMaxWidth(Double.MAX_VALUE);
        currentMode.setMaxWidth(Double.MAX_VALUE);

        // Attach actions to buttons
        displayImage.setOnAction(actionEvent -> changeMode(Mode.DISPLAY));
        compressSource.setOnAction(actionEvent -> changeMode(Mode.COMPRESS));
        uncompressSource.setOnAction(actionEvent -> changeMode(Mode.UNCOMPRESS));

        run.setOnAction(actionEvent -> {
            String sourcePath = sourcePathField.getText();

            if (!sourcePath.equals(Constants.NO_PATH)) {
                if (activeMode == Mode.DISPLAY) {
                    container.setCenter(RITViewer.fillCanvas(FileLoader.secureLoadFileContents(sourcePath), zoom));
                } else if (activeMode == Mode.COMPRESS && sourcePath.contains(".txt")) {
                    activeContents = RITCompress.compress(sourcePath);
                } else if (activeMode == Mode.UNCOMPRESS && sourcePath.contains(".rit")) {
                    activeContents = RITUncompress.uncompress(sourcePath);
                }
            }
        });

        // Ensure that action buttons will use all available space in a GridPane
        GridPane.setHgrow(run, Priority.ALWAYS);
        GridPane.setHgrow(save, Priority.ALWAYS);
        GridPane.setHgrow(saveAs, Priority.ALWAYS);

        // Set up modeSelect
        modeSelect.getItems().addAll(displayImage, compressSource, uncompressSource);
        modeSelect.setMaxWidth(Double.MAX_VALUE);
    }

    private static void changeMode(Mode mode) {
        displayImage.setSelected(mode == Mode.DISPLAY);
        compressSource.setSelected(mode == Mode.COMPRESS);
        uncompressSource.setSelected(mode == Mode.UNCOMPRESS);
        activeMode = mode;
        currentMode.setText(mode.name());
    }
    //</editor-fold>

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
