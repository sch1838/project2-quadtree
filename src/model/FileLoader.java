package model;

import ptui.RITUncompress;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FileLoader {

    public static final URL COMP = FileLoader.class.getClassLoader().getResource("images/compressed/");
    public static final URL UNCM = FileLoader.class.getClassLoader().getResource("images/uncompressed/");

    /**
     * Loads the contents of the specified file if the file exists.
     *
     * <p>The file read is specified as a single command line argument. Any exceptions that are thrown during
     * file loading should be caught here.</p>
     *
     * @return A list of integers read from each line in the file
     */
    public static List<Integer> secureLoadFileContents(URL directoryHeader, String path) {
        try {
            // Attempt to load the file from the uncompressed image directory

            List<Integer> lineValues = FileLoader.loadFileContents(directoryHeader, path);
            double dimension = Math.sqrt(lineValues.size());
            if (directoryHeader != COMP && Math.floor(dimension) != dimension) {
                // Do not check dimension when loading compressed files
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

            assert directoryHeader != null;
            System.out.println("Exception loading file in directory: " + directoryHeader.getPath() + path + "\nFile contains a non-integral value");
            System.exit(-1);
        }

        // An empty list is returned if an exception is caught
        return new ArrayList<>();
    }

    private static List<Integer> loadFileContents(URL directoryHeader, String path) throws IOException, LoaderException.IntegralColorException, NumberFormatException, LoaderException.UnreadablePathException {

        if (directoryHeader == null) {
            // Do not attempt to load the file with a null directory header
            throw new LoaderException.UnreadablePathException("null directory header");
        }

        File file = new File(directoryHeader.getPath() + path);

        List<Integer> lineValues = new ArrayList<>();

        if (file.exists()) {
            BufferedReader reader = new BufferedReader(new FileReader(file));

            String line;

            if (directoryHeader == COMP) {
                RITUncompress.dimension = Integer.parseInt(reader.readLine());
            }

            while ((line = reader.readLine()) != null) {
                int value = Integer.parseInt(line);

                if(directoryHeader != COMP && (value < 0 || 255 < value)) {
                    // Do not check colors when loading compressed files
                    throw new LoaderException.IntegralColorException(value);
                }

                lineValues.add(value);
            }

            reader.close();
        } else {
            throw new LoaderException.UnreadablePathException(file.getPath());
        }

        return lineValues;
    }

    public static void secureWriteFileContents(int[][] pixelGrid, URL directoryHeader, String path) {
        try {
            writeFileContents(pixelGrid, directoryHeader, path);
        } catch (LoaderException.DirectoryCreationException | LoaderException.UnreadablePathException | IOException e) {
            // Handle nonexistent file, unreadable file, and failure to create file
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private static void writeFileContents(int[][] pixelGrid, URL directoryHeader, String path) throws LoaderException.UnreadablePathException, IOException, LoaderException.DirectoryCreationException {

        if (directoryHeader == null) {
            // Do not attempt to load the file with a null directory header
            throw new LoaderException.UnreadablePathException("null directory header");
        }

        File file = new File(directoryHeader.getPath() + path);

        if (!file.exists()) {
            if(!file.mkdirs()) {
                throw new LoaderException.DirectoryCreationException(file.getPath());
            }
        }

        System.out.println("Output file: " + file.getPath());

        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));

        for (int[] rows : pixelGrid) {
            for (int value : rows) {
                writer.write("" + value);
                writer.newLine();
            }
        }

        writer.close();
    }
}
