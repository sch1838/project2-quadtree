package model;

import ptui.RITUncompress;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * The FileLoader class provides methods to read and write file data.
 *
 * @author Samuel Henderson
 */
public class FileLoader {

    /** The directory header for compressed image files. **/
    public static final URL COMP = FileLoader.class.getClassLoader().getResource("images/compressed/");

    /** The directory header for uncompressed image files. **/
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

    /**
     * Loads the contents of a file at a provided path into a list of integers.
     *
     * <p>A URL representing the directory of a file must be provided in order for this method to function. The path
     * parameter may itself point to a subdirectory following the directoryHeader, but can otherwise simply be the name
     * and extension of the file to be read.</p>
     *
     * @throws LoaderException.IntegralColorException Thrown when a color value is not in the range [0, 255]
     * @throws NumberFormatException Thrown when a file contains a non-integral value
     * @throws LoaderException.UnreadablePathException Thrown when the provided path cannot be read
     */
    private static List<Integer> loadFileContents(URL directoryHeader, String path) throws IOException, LoaderException.IntegralColorException, NumberFormatException, LoaderException.UnreadablePathException {

        if (directoryHeader == null) {
            // Do not attempt to load the file with a null directory header
            throw new LoaderException.UnreadablePathException("null directory header");
        }

        File file = new File(directoryHeader.getPath() + path);

        List<Integer> lineValues = new ArrayList<>();

        if (file.exists()) {

            // Open a reader in the file
            BufferedReader reader = new BufferedReader(new FileReader(file));

            String line;

            if (directoryHeader == COMP) {
                // Send the first value in a compressed file to RITUncompress so it can be used as necessary
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

    /**
     * Writes a two dimensional array of integers to a file specified by the provided directoryHeader and path.
     *
     * <p>This method is used to call {@link FileLoader#writeFileContents(int[][], URL, String)} with automatic
     * attention to any thrown exceptions.</p>
     */
    public static void secureWriteFileContents(int[][] pixelGrid, URL directoryHeader, String path) {
        try {
            writeFileContents(pixelGrid, directoryHeader, path);
        } catch (LoaderException.DirectoryCreationException | LoaderException.UnreadablePathException | IOException e) {
            // Handle nonexistent file, unreadable file, and failure to create file
            e.printStackTrace();
            System.exit(-1);
        }
    }

    /**
     * Writes a two dimensional array of integers to a file specified by the provided directoryHeader and path.
     *
     * <p>Each integer in the pixelGrid will be written to its own line in an existent file. If the file does not exist,
     * this method will attempt to create it.</p>
     *
     * @throws LoaderException.UnreadablePathException Thrown when the provided path cannot be read or does not exist
     * @throws LoaderException.DirectoryCreationException Thrown when a file cannot be created at the provided path
     */
    private static void writeFileContents(int[][] pixelGrid, URL directoryHeader, String path) throws LoaderException.UnreadablePathException, IOException, LoaderException.DirectoryCreationException {

        if (directoryHeader == null) {
            // Do not attempt to load the file with a null directory header
            throw new LoaderException.UnreadablePathException("null directory header");
        }

        File file = new File(directoryHeader.getPath() + path);

        if (!file.exists()) {

            // Attempt to create file if it does not exist
            if(!file.mkdirs()) {

                // Except when the file cannot be created
                throw new LoaderException.DirectoryCreationException(file.getPath());
            }
        }

        // Notify user of output location
        System.out.println("Output file: " + file.getPath());

        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));

        for (int[] rows : pixelGrid) {
            for (int value : rows) {

                // Write contents to file
                writer.write("" + value);
                writer.newLine();
            }
        }

        writer.close();
    }
}
