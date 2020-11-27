package gui;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import model.FileLoader;

import java.util.List;

/**
 * The RITViewer class is an application that facilitates the display of a square image whose path is determined from
 * command line arguments. File loading is handled in {@link FileLoader}.
 *
 * @author Samuel Henderson
 */
public class RITViewer extends Application {

    private static List<Integer> lineValues;

    public static List<Integer> lineValues() {
        return lineValues;
    }

    @Override
    public void start(Stage stage) throws Exception {
        lineValues = FileLoader.secureLoadFileContents(FileLoader.UNCM_HEAD + path);

        stage.setScene(new Scene(new Group(fillCanvas(lineValues, 1))));
        stage.show();
    }

    public static Canvas fillCanvas(List<Integer> lineValues, int zoom) {
        // Acquire dimension and create canvas
        int dimension = (int) Math.sqrt(lineValues.size());
        Canvas canvas = new Canvas(dimension * zoom, dimension * zoom);

        GraphicsContext context = canvas.getGraphicsContext2D();

        zoom = Math.max(1, zoom);

        // Fill all of the pixels
        for (int row = 0; row < dimension; ++ row) {
            for (int col = 0; col < dimension; ++ col) {
                int rgb = lineValues.get(row * dimension + col);

                context.setFill(Color.rgb(rgb, rgb, rgb));
                context.fillRect(col * zoom, row * zoom, (col + 1) * zoom, (row + 1) * zoom);
            }
        }

        return canvas;
    }

    /** The file to be loaded as an image. **/
    private static String path;

    public static void main(String[] args) {
        if (args.length != 1) {
            // Handle missing or invalid argument(s)
            System.out.println("Usage: java RITViewer <uncompressed.txt>\nSpecified file must be within src/images/uncompressed or some subdirectory of it.");
        } else {
            path = args[0];
            Application.launch(args);
        }
    }
}
