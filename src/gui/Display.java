package gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.FileLoader;
import model.QuadTree;
import ptui.RITCompress;
import ptui.RITUncompress;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * The Display Class holds all visual content to be used by the {@link RITGUI} application. Access to its components is
 * limited because they are largely specific to the RITGUI application.
 *
 * @author Samuel Henderson
 */
public class Display {

    //<editor-fold desc="File Selection Utility">
    private static final FileChooser FILE_CHOOSER = new FileChooser();

    // Filters for file selection
    private static final FileChooser.ExtensionFilter UNCOMPRESSED_FILTER = new FileChooser.ExtensionFilter("Uncompressed Image Format", "*.txt");
    private static final FileChooser.ExtensionFilter COMPRESSED_FILTER = new FileChooser.ExtensionFilter("Compressed Image Format", "*.rit");

    /**
     * Opens a file selection window with the provided title.
     *
     * @param stage The stage upon which the file selector will appear
     * @param save Whether a save dialog will be opened
     * @param pathApplicator The consumer that will use the path of the selected file
     * @param filters Any {@link javafx.stage.FileChooser.ExtensionFilter} instances that should be applied
     */
    private static void postFileSelection(Stage stage, String title, boolean save, Consumer<String> pathApplicator, FileChooser.ExtensionFilter... filters) {
        // Update title and apply extension filters
        FILE_CHOOSER.setTitle(title);
        FILE_CHOOSER.getExtensionFilters().clear();
        FILE_CHOOSER.getExtensionFilters().addAll(filters);

        // Access the file
        File file = save ? FILE_CHOOSER.showSaveDialog(stage) : FILE_CHOOSER.showOpenDialog(stage);

        if (file != null) {
            pathApplicator.accept(file.getPath());
            postOut("Applied new path: " + file.getPath());
        } else {
            postOut("Failed to apply path, invalid file");
        }
    }

    /**
     * Performs a save-as operation on the provided {@link Stage}.
     * The user is prompted to select a save location to be used in {@link Display#saveContentToDestination()}.
     */
    private static void saveContentAs(Stage stage) {
        postFileSelection(stage, "Save As", true, destinationPathField::setText, activeMode == Mode.UNCOMPRESS ? UNCOMPRESSED_FILTER : COMPRESSED_FILTER);
        saveContentToDestination();
    }

    /**
     * Performs a save operation.
     *
     * <p>If a destination has been selected and the active contents are existent, the active contents will be written
     * to the destination file using {@link FileLoader#secureWriteFileContents(List, String)}.</p>
     */
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
    /** The list of active contents that is updated by some operations. **/
    private static List<String> activeContents;

    /** The default operation mode is DISPLAY. **/
    private static Mode activeMode = Mode.DISPLAY;

    /** The default zoom value is 1. **/
    private static int zoom = 1;

    /** The root container of all gui elements. **/
    private static final BorderPane container = new BorderPane();

    /** Enables viewing of images larger than the default window size. **/
    private static final ScrollPane scrollView = new ScrollPane();

    /**
     * Provides {@link Display#container} to be used in {@link RITGUI}.
     */
    protected static BorderPane container() {
        return container;
    }

    /**
     * Resets all logical data to their default values.
     */
    private static void resetData() {
        if(activeContents != null) {
            activeContents = null;
        }
        changeMode(Mode.DISPLAY);
        zoom = 1;

        // Clear any displayed images
        scrollView.setContent(null);

        postOut("Reset program data");
    }

    /**
     * The Mode enum enumerates the three possible modes for the application.
     */
    enum Mode {
        COMPRESS, UNCOMPRESS, DISPLAY
    }
    //</editor-fold>

    //<editor-fold desc="Button Display Arrangement">

    /**
     * Organizes the main button display for the GUI. This includes all of the functional components of the application.
     */
    private static GridPane createButtonDisplay(Stage stage) {
        GridPane view = createUniformGridPane(3, 4, true);

        // Setup display for path selection
        GridPane pathDisplay = createUniformGridPane(3, 2, false);
            pathDisplay.add(new Label("Source: "), 0, 0);
            pathDisplay.add(new Label("Destination: "), 0, 1);

            pathDisplay.add(sourcePathField, 1, 0);
            pathDisplay.add(destinationPathField, 1, 1);

            pathDisplay.add(sourceSelect, 2, 0);
            pathDisplay.add(destinationSelect, 2, 1);

        // Setup display for operations/actions
        GridPane actionPane = createUniformGridPane(2, 2, false);
            actionPane.add(modeSelect, 0, 0);
            actionPane.add(currentMode, 0, 1);
            TilePane buttonPane = new TilePane();
                GridPane.setHgrow(buttonPane, Priority.ALWAYS);
                buttonPane.setHgap(10);
                buttonPane.setVgap(12);
                buttonPane.getChildren().addAll(run, save, saveAs, swap, reset);
            actionPane.add(buttonPane, 1, 0);

        // Add the path display and the action box to the grid
        view.add(pathDisplay, 1, 1);
        view.add(actionPane, 1, 2);

        // Make sure all available space will be used by the path display
        GridPane.setHgrow(pathDisplay, Priority.ALWAYS);

        return view;
    }

    /**
     * Provides a new GridPane with uniform spacing and empty indices to create space between grid contents and the
     * window borders.
     */
    private static GridPane createUniformGridPane(int width, int height, boolean spacers) {
        GridPane pane = new GridPane();
        if(spacers) {
            pane.add(new Rectangle(0, 0), 0, 0);
            pane.add(new Rectangle(0, 0), width - 1, 0);
            pane.add(new Rectangle(0, 0), 0, height - 1);
            pane.add(new Rectangle(0, 0), width - 1, height - 1);
        }
        pane.setVgap(12);
        pane.setHgap(10);
        return pane;
    }
    //</editor-fold>

    //<editor-fold desc="Path Selection and General Initialization">
    /** Container for the separate destination selection options. **/
    private static final MenuButton destinationSelect = new MenuButton("Select Path");

    /** Source path selection. **/
    private static final Button sourceSelect = new Button("Select Path");

    /** Destination selection options: newFile will create a new file, existingFile will modify an existing file. **/
    private static final MenuItem
        newFile = new MenuItem("New File"),
        existingFile = new MenuItem("Existing File")
    ;

    /** Constant reference value for the nonexistence of a path in either path field. **/
    private static final String NO_PATH = "No path selected";

    /** The two path fields for the source and destination paths. **/
    private static final TextField
        sourcePathField = new TextField(NO_PATH),
        destinationPathField = new TextField(NO_PATH)
    ;

    /**
     * The main initializer for the Display. This should be called from an {@link Application} that provides its main
     * {@link Stage}.
     *
     * <p>This method is responsible for initializing any fields that require a stage reference.</p>
     */
    protected static void initializePathSelection(Stage stage) {
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

        // Top of container is another borderpane to include a menubar and the button display
        BorderPane topPane = new BorderPane();
            Menu fileMenu = new Menu("File");
                fileMenu.getItems().add(exitItem);
            Menu viewMenu = new Menu("View");
                viewMenu.getItems().add(zoomItem);
            topPane.setTop(new MenuBar(fileMenu, viewMenu));
            topPane.setCenter(createButtonDisplay(stage));

        // Populate container
        container.setTop(topPane);
        container.setCenter(new Rectangle(1024, 512));
        Platform.runLater(() -> container.setCenter(scrollView));
        GridPane outputPane = createUniformGridPane(3, 4, true);
            outputPane.add(new Label("Program Output: "), 1, 1);
            outputPane.add(output, 1, 2);
        container.setBottom(outputPane);

        save.setOnAction(actionEvent -> {
            // Save-as if nonexistent destination
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
                            // Center the displayed image in a stackpane that is the same size as the scrollView
                            StackPane centeringPane = new StackPane(RITViewer.fillCanvas(content, zoom));
                            centeringPane.setMinWidth(scrollView.getWidth());
                            centeringPane.setMinHeight(scrollView.getHeight());
                            scrollView.setContent(centeringPane);
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
                        double uncm = RITCompress.uncompressedSize(), comp = activeContents.size();
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

        reset.setOnAction(actionEvent -> resetData());

        configureButtons(run, save, saveAs, swap, reset);

        // Set up modeSelect
        modeSelect.getItems().addAll(displayImage, compressSource, uncompressSource);
        modeSelect.setMaxWidth(Double.MAX_VALUE);

        currentMode.setEditable(false);
    }

    /**
     * Configures an array of buttons.
     */
    private static void configureButtons(Button... buttons) {
        for (Button button : buttons) {
            button.setMaxWidth(Double.MAX_VALUE);
            GridPane.setHgrow(button, Priority.ALWAYS);
        }
    }

    /**
     * Changes the current operational mode.
     */
    private static void changeMode(Mode mode) {
        displayImage.setSelected(mode == Mode.DISPLAY);
        compressSource.setSelected(mode == Mode.COMPRESS);
        uncompressSource.setSelected(mode == Mode.UNCOMPRESS);
        activeMode = mode;
        currentMode.setText(mode.name());
        postOut("Changed operation mode to: " + mode.name());
    }
    //</editor-fold>

    //<editor-fold desc="Menu Bar">
    private static final MenuItem
        exitItem = new MenuItem("Exit"),
        zoomItem = new MenuItem("Zoom")
    ;

    static {
        // Exit should exit the program
        exitItem.setOnAction(actionEvent -> System.exit(0));

        // Zoom opens dialogue that allows user to change zoom value
        zoomItem.setOnAction(actionEvent -> {
            TextInputDialog dialog = new TextInputDialog("" + zoom);
            dialog.setHeaderText("Input a new zoom value between 1 and 5.");
            Optional<String> input = dialog.showAndWait();
            if (input.isPresent()) {
                try {
                    int value = Integer.parseInt(input.get());
                    value = Math.max(1, value);
                    value = Math.min(5, value);
                    zoom = value;
                } catch (NumberFormatException n) {
                    postOut("Failed to update zoom: Non-integral value provided");
                }
            }
        });
    }
    //</editor-fold>

    //<editor-fold desc="Program Output">
    /** The program output text area. **/
    private static final TextArea output = new TextArea();

    static {
        // The output area should not be modifiable but should wrap text
        output.setWrapText(true);
        output.setEditable(false);

        GridPane.setHgrow(output, Priority.ALWAYS);
    }

    /**
     * Posts a message to {@link Display#output}.
     */
    public static void postOut(String message) {
        output.setText(output.getText() + "\n" + message);
    }

    /**
     * Posts an exception message as an alert and to {@link Display#output}.
     */
    public static void postException(String message) {
        postOut(message);
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("The process encountered a fatal exception.");
        alert.setContentText(message);
        alert.showAndWait();
    }
    //</editor-fold>
}
