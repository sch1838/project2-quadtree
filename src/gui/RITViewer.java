package gui;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import model.LoaderException;
import model.FileLoader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RITViewer extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        displayImage(stage, loadValues());
    }

    /**
     * Displays a list of integral values as a square image whose side length is the square root of the size of the
     * provided list.
     */
    private void displayImage(Stage stage, List<Integer> lineValues) {
        int dimension = (int) Math.sqrt(lineValues.size());
        Canvas canvas = new Canvas(dimension, dimension);

        GraphicsContext context = canvas.getGraphicsContext2D();

        for (int row = 0; row < dimension; ++ row) {
            for (int col = 0; col < dimension; ++ col) {
                int rgb = lineValues.get(row * dimension + col);

                context.setFill(Color.rgb(rgb, rgb, rgb));
                context.fillRect(col, row, col + 1, row + 1);
            }
        }

        stage.setScene(new Scene(new Group(canvas)));

        stage.show();
    }

    /**
     * Loads the contents of the specified file if the file exists.
     *
     * <p>The file read is specified as a single command line argument. Any exceptions that are thrown during
     * file loading should be caught here.</p>
     *
     * @return A list of integers read from each line in the file
     */
    private List<Integer> loadValues() {
        try {
            // Attempt to load the file from the uncompressed image directory

            List<Integer> lineValues = FileLoader.loadFileContents(FileLoader.UNCM, path);
            double dimension = Math.sqrt(lineValues.size());
            if(Math.floor(dimension) != dimension) {
                throw new LoaderException.FileDimensionException(dimension);
            } else {
                return lineValues;
            }
        } catch (FileNotFoundException f) {
            // Handle nonexistent file

            f.printStackTrace();
            System.out.println("File does not exist: " + path);
            System.exit(-1);
        } catch (IOException | LoaderException.IntegralColorException | LoaderException.UnreadablePathException | LoaderException.FileDimensionException e) {
            // Handle generic IOException, unreadable files, nonsquare files, and color value not in valid range
            // All LoaderException extensions can be handled in the same way because they override printStackTrace

            e.printStackTrace();
            System.exit(-1);
        } catch (NumberFormatException n) {
            // Handle non-integral color values (NumberFormatException thrown when parsing lines as an integer)
            // If UNCM is null, this clause will not be reached - a null UNCM case is thrown as an
            // UnreadablePathException during the loadFileContents call that occurs in the try clause and is handled
            // before this one

            assert FileLoader.UNCM != null;
            System.out.println("Exception loading file in directory: " + FileLoader.UNCM.getPath() + path + "\nFile contains a non-integral value");
            System.exit(-1);
        }

        // An empty list is returned if an exception is caught
        return new ArrayList<>();
    }

    private static String path;

    public static void main(String[] args) {
        if (args.length != 1) {
            // Handle missing or invalid argument(s)
            System.out.println("Usage: ");
        } else {
            path = args[0];
            Application.launch(args);
        }
    }
}
