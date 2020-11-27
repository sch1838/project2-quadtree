package gui;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.FileLoader;
import ptui.RITCompress;
import ptui.RITUncompress;

import java.io.File;
import java.util.List;

public class RITGUI extends Application {

    private Stage stage;

    @Override
    public void start(Stage stage) throws Exception {

        // Create border pane to hold grid and label pane

        this.stage = stage;

        Display.container.setTop(createMenuBar());

        // Create and set the scene from the top of the pane hierarchy
        Scene mainScene = new Scene(Display.container);
        stage.setScene(mainScene);
        stage.setTitle("Compresora");
        stage.show();
    }

    private static final FileChooser.ExtensionFilter UNCOMPRESSED_FILTER = new FileChooser.ExtensionFilter("Uncompressed Image Format", "*.txt");
    private static final FileChooser.ExtensionFilter COMPRESSED_FILTER = new FileChooser.ExtensionFilter("Compressed Image Format", "*.rit");

    private static final String displayFailure = "Could not display image: Unreadable or nonexistent source file";

    private final BorderPane borders = new BorderPane();

    private final FileChooser fileChooser = new FileChooser();

    private String sourcePath, destinationPath;

    private boolean displayAlerts = true;

    private int zoom = 1;

    private List<String> activeContent;

    private MenuBar createMenuBar() {
        Menu file = new Menu("File");

            Menu open = new Menu("Open...");

            addTooltipMenuItemToMenu(open, "New", "Open an uncompressed image file other than that at the current source path", actionEvent -> {
                String selection = this.postFileSelectionAsPath(stage, "Open Image File", false, UNCOMPRESSED_FILTER);

                if (!selection.equals("")) {
                    this.sourcePath = selection;

                    List<Integer> fileContents = FileLoader.secureLoadFileContents(this.sourcePath);

                    this.borders.setCenter(new Group(RITViewer.fillCanvas(fileContents, this.zoom)));
                }
            });

            addTooltipMenuItemToMenu(open, "From Source", "Open the uncompressed image at the current source path", actionEvent -> {
                if (this.sourcePath != null && !this.sourcePath.contains(".rit")) {
                    List<Integer> fileContents = FileLoader.secureLoadFileContents(this.sourcePath);

                    this.borders.setCenter(new Group(RITViewer.fillCanvas(fileContents, this.zoom)));
                } else {
                    showAlert(new Alert(Alert.AlertType.ERROR, displayFailure));
                }
            });

            addTooltipMenuItemToMenu(file, "Save", "Save the currently loaded content to the current destination path", actionEvent -> {
                if(!Display.validatePath(this.destinationPath)) {
                    String selection = this.postFileSelectionAsPath(stage, "Select Destination Path", true, COMPRESSED_FILTER, UNCOMPRESSED_FILTER);
                    if(!selection.isEmpty()) {
                        this.destinationPath = selection;
                    }
                }

                if(this.activeContent != null && Display.validatePaths(this.sourcePath, this.destinationPath)) {
                    FileLoader.secureWriteFileContents(this.activeContent, this.destinationPath);
                }
            });
            addTooltipMenuItemToMenu(file, "Save As", "Save the currently loaded content to a different path than the current destination path", actionEvent -> {
                if(this.activeContent != null) {
                    String selection = this.postFileSelectionAsPath(stage, "Select Destination Path", true, COMPRESSED_FILTER, UNCOMPRESSED_FILTER);
                    if(!selection.isEmpty()) {
                        this.destinationPath = selection;
                        FileLoader.secureWriteFileContents(this.activeContent, this.destinationPath);
                    }
                } else {
                    showAlert(new Alert(Alert.AlertType.ERROR, "Save failed: null content"));
                }
            });

        file.getItems().addAll(open, new SeparatorMenuItem(), createSettings());

        Menu edit = new Menu("Edit");

            Menu path = new Menu("Path...");

            addTooltipMenuItemToMenu(path, "Source", "Edit the source path", actionEvent -> {
                String selection = this.postFileSelectionAsPath(stage, "Select Source File", false, COMPRESSED_FILTER, UNCOMPRESSED_FILTER);
                if (!selection.isEmpty()) {
                    this.sourcePath = selection;
                    showAlert(new Alert(Alert.AlertType.INFORMATION, "The source path has been updated: \n" + selection));
                }
            });

            addTooltipMenuItemToMenu(path, "Destination", "Edit the destination path", actionEvent -> {
                String selection = this.postFileSelectionAsPath(stage, "Select Destination File", false, COMPRESSED_FILTER, UNCOMPRESSED_FILTER);
                if (!selection.isEmpty()) {
                    this.destinationPath = selection;
                    showAlert(new Alert(Alert.AlertType.INFORMATION, "The destination path has been updated: \n" + selection));
                }
            });

            Menu compress = new Menu("Compression...");

            addTooltipMenuItemToMenu(compress, "Compress", "Compress the uncompressed image file at the current source path", actionEvent -> {
                if(Display.validatePath(this.sourcePath)) {
                    this.activeContent = RITCompress.compress(this.sourcePath);
                }
            });

            addTooltipMenuItemToMenu(compress, "Uncompress", "Uncompress the compressed image file at the current source path", actionEvent -> {
                if(Display.validatePath(this.sourcePath)) {
                    this.activeContent = RITUncompress.uncompress(this.sourcePath);
                }
            });

        edit.getItems().addAll(path, compress);

        Menu view = new Menu("View");

            MenuItem zoom = new MenuItem("Zoom");
            zoom.setOnAction(actionEvent -> {
                TextInputDialog prompt = new TextInputDialog("1");
                prompt.setContentText("Enter a zoom amount: [1, 100]");
                prompt.showAndWait();
                String input = prompt.getEditor().getText();
                try {
                    int val = Integer.parseInt(input);
                    this.zoom = Math.max(1, val);
                    this.zoom = Math.min(100, this.zoom);
                } catch (NumberFormatException e) {
                    System.out.println("Failed to update zoom value");
                }
            });

        view.getItems().addAll(zoom);

        Menu help = new Menu("Help");

        return new MenuBar(file, edit, view, help);
    }

    private void showAlert(Alert alert) {
        if(this.displayAlerts) {
            alert.show();
        }
    }

    private Menu createSettings() {
        Menu settings = new Menu("Settings");
        CheckMenuItem alerts = new CheckMenuItem("Show Alerts");
        alerts.setSelected(true);
        alerts.setOnAction(actionEvent -> {
            this.displayAlerts = !this.displayAlerts;
        });

        settings.getItems().add(alerts);
        return settings;
    }

    private String postFileSelectionAsPath(Stage stage, String title, boolean save, FileChooser.ExtensionFilter... filters) {
        this.fileChooser.setTitle(title);
        this.fileChooser.getExtensionFilters().clear();
        this.fileChooser.getExtensionFilters().addAll(filters);

        File file = save ? this.fileChooser.showSaveDialog(stage) : this.fileChooser.showOpenDialog(stage);

        if (file != null) {
            return file.getPath();
        }

        return "";
    }

    private void addTooltipMenuItemToMenu(Menu menu, String itemName, String tooltip, EventHandler<ActionEvent> eventHandler) {
        CustomMenuItem item = new CustomMenuItem(new Label(itemName));
        installTooltipMenuItem(menu, item, tooltip, eventHandler);
    }

    private void installTooltipMenuItem(Menu menu, CustomMenuItem item, String tooltip, EventHandler<ActionEvent> eventHandler) {
        Tooltip.install(item.getContent(), new Tooltip(tooltip));
        item.setOnAction(eventHandler);
        menu.getItems().add(item);
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}
