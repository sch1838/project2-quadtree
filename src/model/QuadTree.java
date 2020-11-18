package model;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class QuadTree {

    public static final URL IMAGES = QuadTree.class.getClassLoader().getResource("images");

    public static List<Integer> loadUncompressedFileContents(String path) throws IOException {

        if(IMAGES == null) {
            throw new FileNotFoundException("Failed to load file contents, directory does not exist");
        }

        File file = new File(IMAGES.getPath() + "/uncompressed/" + path);

        List<Integer> pixelValues = new ArrayList<>();

        if(file.exists()) {
            FileReader fileReader = new FileReader(file);

            BufferedReader reader = new BufferedReader(fileReader);

            String line;

            while ((line = reader.readLine()) != null) {
                pixelValues.add(Integer.parseInt(line));
            }
        }

        return pixelValues;
    }
}
