package model;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FileLoader {

    public static final URL COMP = FileLoader.class.getClassLoader().getResource("images/compressed/");
    public static final URL UNCM = FileLoader.class.getClassLoader().getResource("images/uncompressed/");

    public static List<Integer> loadFileContents(URL directoryHeader, String path) throws IOException, LoaderException.IntegralColorException, NumberFormatException, LoaderException.UnreadablePathException {

        if(directoryHeader == null) {
            // Do not attempt to load the file with a null directory header
            throw new LoaderException.UnreadablePathException("null directory header");
        }

        File file = new File(directoryHeader.getPath() + path);

        List<Integer> lineValues = new ArrayList<>();

        if(file.exists()) {
            BufferedReader reader = new BufferedReader(new FileReader(file));

            String line;

            while ((line = reader.readLine()) != null) {
                int value = Integer.parseInt(line);

                if(value < 0 || 255 < value) {
                    throw new LoaderException.IntegralColorException(value);
                }

                lineValues.add(value);
            }
        } else {
            throw new LoaderException.UnreadablePathException(file.getPath());
        }

        return lineValues;
    }
}
