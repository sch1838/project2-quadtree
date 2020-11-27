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
import model.QuadTree;
import ptui.RITCompress;
import ptui.RITUncompress;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

public class Display {

    //<editor-fold desc="File Selection Utility">
    private static final FileChooser FILE_CHOOSER = new FileChooser();

    private static final FileChooser.ExtensionFilter UNCOMPRESSED_FILTER = new FileChooser.ExtensionFilter("Uncompressed Image Format", "*.txt");
    private static final FileChooser.ExtensionFilter COMPRESSED_FILTER = new FileChooser.ExtensionFilter("Compressed Image Format", "*.rit");

    private static void postFileSelection(Stage stage, String title, boolean save, Consumer<String> pathApplicator, FileChooser.ExtensionFilter... filters) {
        FILE_CHOOSER.setTitle(title);
        FILE_CHOOSER.getExtensionFilters().clear();
        FILE_CHOOSER.getExtensionFilters().addAll(filters);

        File file = save ? FILE_CHOOSER.showSaveDialog(stage) : FILE_CHOOSER.showOpenDialog(stage);

        if (file != null) {
            pathApplicator.accept(file.getPath());
            postOut("Applied new path: " + file.getPath());
        } else {
            postOut("Failed to apply path, invalid file");
        }
    }

    private static void saveContentAs(Stage stage) {
        postFileSelection(stage, "Save As", true, destinationPathField::setText, activeMode == Mode.UNCOMPRESS ? UNCOMPRESSED_FILTER : COMPRESSED_FILTER);
        saveContentToDestination();
    }

    private static void saveContentToDestination() {
        if(!destinationPathField.getText().isEmpty() && !destinationPathField.getText().equals(NO_PATH)) {
            if(activeContents != null) {
                FileLoader.secureWriteFileContents(activeContents, destinationPathField.getText());
                postOut("Saved active contents to: " + destinationPathField.getText());
                return;
            }
        }

        postOut("Save failed: Empty contents or destination");
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

    private static void clearData() {
        if(activeContents != null) {
            activeContents = null;
        }
        changeMode(Mode.DISPLAY);
        zoom = 1;

        container.setCenter(null);

        postOut("Reset program data");
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
            actionPane.add(swap, 4, 0);
            actionPane.add(reset, 5, 0);

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

    private static final String NO_PATH = "No path selected";

    private static final TextField
        sourcePathField = new TextField(NO_PATH),
        destinationPathField = new TextField(NO_PATH)
    ;

    public static void initializePathSelection(Stage stage) {
        // Actions for selecting the destination file
        newFile.setOnAction(actionEvent -> postFileSelection(stage, "Destination As", true, destinationPathField::setText, activeMode == Mode.UNCOMPRESS ? UNCOMPRESSED_FILTER : COMPRESSED_FILTER));
        existingFile.setOnAction(actionEvent -> postFileSelection(stage, "Select Destination File", false, destinationPathField::setText, activeMode == Mode.UNCOMPRESS ? UNCOMPRESSED_FILTER : COMPRESSED_FILTER));

        // Action and configuration for selecting the source file
        sourceSelect.setOnAction(actionEvent -> postFileSelection(stage, "Select Source File", false, sourcePathField::setText, activeMode == Mode.UNCOMPRESS ? COMPRESSED_FILTER : UNCOMPRESSED_FILTER));
        sourceSelect.setMaxWidth(Double.MAX_VALUE);

        destinationSelect.getItems().addAll(newFile, existingFile);

        // Ensure components use all available space
        GridPane.setHgrow(sourceSelect, Priority.SOMETIMES);
        GridPane.setHgrow(sourcePathField, Priority.ALWAYS);
        GridPane.setHgrow(destinationPathField, Priority.ALWAYS);

        // Populate main container
        container.setTop(createButtonDisplay(stage));
        container.setCenter(new Rectangle(1024, 384));
        Platform.runLater(() -> container.setCenter(null));
        container.setBottom(output);

        save.setOnAction(actionEvent -> {
            if(destinationPathField.getText().equals(NO_PATH)) {
                saveContentAs(stage);
            } else {
                saveContentToDestination();
            }
        });

        saveAs.setOnAction(actionEvent -> saveContentAs(stage));
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
        saveAs = new Button("Save As"),
        swap = new Button("Swap Paths"),
        reset = new Button("Reset")
    ;

    static {
        // Initialize default state of actionPane components
        displayImage.setSelected(true);

        // Attach actions to buttons
        displayImage.setOnAction(actionEvent -> changeMode(Mode.DISPLAY));
        compressSource.setOnAction(actionEvent -> changeMode(Mode.COMPRESS));
        uncompressSource.setOnAction(actionEvent -> changeMode(Mode.UNCOMPRESS));

        run.setOnAction(actionEvent -> {
            String sourcePath = sourcePathField.getText();

            if (!sourcePath.equals(NO_PATH)) {
                if (activeMode == Mode.DISPLAY) {
                    if (sourcePath.contains(".txt")) {
                        List<Integer> content = FileLoader.secureLoadFileContents(sourcePath);
                        if(!content.isEmpty()) {
                            container.setCenter(RITViewer.fillCanvas(content, zoom));
                            postOut("Displayed uncompressed image at: " + sourcePath);
                        }
                    } else {
                        postOut("Display failed: Invalid source format");
                    }
                } else if (activeMode == Mode.COMPRESS) {
                    if (sourcePath.contains(".txt")) {
                        activeContents = RITCompress.compress(sourcePath);
                        postOut("Compressed file at: " + sourcePath);
                        postOut("QuadTree: " + QuadTree.preorder(RITCompress.treeContents()));
                        double uncm = RITCompress.dimension(), comp = activeContents.size();
                        postOut("Uncompressed image size: " + uncm);
                        postOut("Compressed image size: " + comp);
                        postOut("Compression: " + (uncm - comp) / uncm * 100.0D + "%");
                    } else {
                        postOut("Compress failed: Source file is not uncompressed");
                    }
                } else if (activeMode == Mode.UNCOMPRESS) {
                    if (sourcePath.contains(".rit")) {
                        activeContents = RITUncompress.uncompress(sourcePath);
                        postOut("Uncompressed file at: " + sourcePath);
                        postOut("QuadTree: " + QuadTree.preorder(RITUncompress.treeContents()));
                    } else {
                        postOut("Uncompress failed: Source file is not compressed");
                    }
                }
            }
        });

        swap.setOnAction(actionEvent -> {
            String tempPath = sourcePathField.getText();
            sourcePathField.setText(destinationPathField.getText());
            destinationPathField.setText(tempPath);

            if (activeMode == Mode.UNCOMPRESS) {
                changeMode(Mode.COMPRESS);
            } else {
                changeMode(Mode.UNCOMPRESS);
            }
        });

        reset.setOnAction(actionEvent -> clearData());

        configureButtons(run, save, saveAs, swap, reset);

        // Set up modeSelect
        modeSelect.getItems().addAll(displayImage, compressSource, uncompressSource);
        modeSelect.setMaxWidth(Double.MAX_VALUE);

        currentMode.setEditable(false);
    }

    private static void configureButtons(Button... buttons) {
        for (Button button : buttons) {
            button.setMaxWidth(Double.MAX_VALUE);
            GridPane.setHgrow(button, Priority.ALWAYS);
        }
    }

    private static void changeMode(Mode mode) {
        displayImage.setSelected(mode == Mode.DISPLAY);
        compressSource.setSelected(mode == Mode.COMPRESS);
        uncompressSource.setSelected(mode == Mode.UNCOMPRESS);
        activeMode = mode;
        currentMode.setText(mode.name());
        postOut("Changed operation mode to: " + mode.name());
    }
    //</editor-fold>

    private static final TextArea output = new TextArea();

    static {
        output.setWrapText(true);
        output.setEditable(false);
    }

    public static void postOut(String message) {
        output.setText(output.getText() + "\n" + message);
    }

    public static void postException(String message) {
        postOut(message);
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("The process encountered a fatal exception.");
        alert.setContentText(message);
        alert.showAndWait();
    }

    enum Mode {
        COMPRESS, UNCOMPRESS, DISPLAY
    }
}
