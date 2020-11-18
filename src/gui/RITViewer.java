package gui;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import model.QuadTree;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class RITViewer extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        List<Integer> pixelValues = new ArrayList<>();

        try {
            pixelValues = QuadTree.loadUncompressedFileContents(path);
        } catch (FileNotFoundException f) {
            f.printStackTrace();
            System.out.println("File does not exist at path: " + path);
            System.exit(-1);
        }

        // Image should always be square, so dimension is acquirable by square root of total pixels
        int dim = (int) Math.sqrt(pixelValues.size());

        Group group = new Group();
        stage.setScene(new Scene(group));

        Canvas canvas = new Canvas(dim, dim);

        int index = 0;

        GraphicsContext context = canvas.getGraphicsContext2D();
        for (int row = 0; row < dim; ++ row) {
            for (int col = 0; col < dim; ++ col) {
                int rgb = pixelValues.get(index);

                context.setFill(Color.rgb(rgb, rgb, rgb));
                context.fillRect(col, row, col + 1, row + 1);
                ++ index;
            }
        }

        group.getChildren().add(canvas);

        stage.show();
    }

    static String path;

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: ");
        } else {
            path = args[0];
            Application.launch(args);
        }
    }
}
