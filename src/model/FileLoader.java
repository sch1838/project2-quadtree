package model;

import gui.Display;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The FileLoader class provides methods to read and write file data.
 *
 * @author Samuel Henderson
 */
public class FileLoader {

    public static final String COMP_HEAD = "images/compressed/", UNCM_HEAD = "images/uncompressed/";

    public static List<Integer> secureLoadFileContents(String absolutePath) {
        try {
            // Attempt to load the file from the uncompressed image directory

            List<Integer> lineValues = FileLoader.loadFileContents(absolutePath);
            double dimension = Math.sqrt(lineValues.size());
            if (!isFileCompressed(absolutePath) && Math.floor(dimension) != dimension) {
                // Do not check dimension when loading compressed files
                throw new LoaderException.FileDimensionException(dimension);
            } else {
                return lineValues;
            }
        } catch (FileNotFoundException f) {
            // Handle nonexistent file

            f.printStackTrace();
            Display.postOut(f.getMessage());
            System.out.println("File does not exist: " + absolutePath);
            System.exit(-1);
        } catch (IOException | LoaderException.IntegralColorException | LoaderException.UnreadablePathException | LoaderException.FileDimensionException e) {
            // Handle generic IOException, unreadable files, nonsquare files, and color value not in valid range
            // All LoaderException extensions can be handled in the same way because they override printStackTrace

            e.printStackTrace();
            Display.postOut(e.getMessage());
            System.exit(-1);
        } catch (NumberFormatException n) {
            // Handle non-integral color values (NumberFormatException thrown when parsing lines as an integer)
            // If UNCM is null, this clause will not be reached - a null UNCM case is thrown as an
            // UnreadablePathException during the loadFileContents call that occurs in the try clause and is handled
            // before this one

            Display.postOut(n.getMessage());
            System.out.println("Exception loading file: " + absolutePath + "\nFile contains a non-integral value");
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
    private static List<Integer> loadFileContents(String absolutePath) throws IOException, LoaderException.IntegralColorException, NumberFormatException, LoaderException.UnreadablePathException {

        File file = new File(absolutePath);

        List<Integer> lineValues = new ArrayList<>();

        if(file.exists()) {

            // Open a reader in the file
            BufferedReader reader = new BufferedReader(new FileReader(file));

            String line;

            while ((line = reader.readLine()) != null) {
                int value = Integer.parseInt(line);

                if (!isFileCompressed(absolutePath) && (value < 0 || 255 < value)) {
                    // Do not check colors when loading compressed files

                    throw new LoaderException.IntegralColorException(value);
                }

                lineValues.add(value);
            }

            reader.close();
        } else {
            throw new LoaderException.UnreadablePathException(absolutePath);
        }

        return lineValues;
    }

    /**
     * Writes a list of objects to a file specified by the provided directoryHeader {@link URL} and path.
     *
     * <p>This method is used to call {@link FileLoader#writeFileContents(List, String)} with automatic attention
     * to any thrown exceptions.</p>
     */
    public static void secureWriteFileContents(List<?> lineValues, String absolutePath) {
        try {
            writeFileContents(lineValues, absolutePath);
        } catch (LoaderException.DirectoryCreationException | IOException e) {
            // Handle nonexistent file, unreadable file, and failure to create file
            Display.postOut(e.getMessage());
            e.printStackTrace();
            System.exit(-1);
        }
    }

    /**
     * Writes a {@link List} of objects to a file specified by the provided absolute file path.
     *
     * <p>Each object in lineValues will be written to its own line in an existent file. If the file does not exist,
     * this method will attempt to create it.</p>
     *
     * @throws LoaderException.DirectoryCreationException Thrown when a file cannot be created at the provided path
     */
    private static void writeFileContents(List<?> lineValues, String absolutePath) throws LoaderException.DirectoryCreationException, IOException {
        File file = new File(absolutePath);

        if (!file.exists()) {

            // Attempt to create file if it does not exist
            if(!file.mkdirs()) {

                // Except when the file cannot be created
                throw new LoaderException.DirectoryCreationException(file.getPath());
            }
        }

        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));

        for (Object value : lineValues) {
            // Write each value to its own line in the file
            writer.write(value.toString());
            writer.newLine();
        }

        writer.close();
    }


    private static final Map<String, Boolean> compressionReference = new HashMap<>();

    private static final String COMP_EXTENSION = ".rit";

    private static boolean isFileCompressed(String path) {
        if(compressionReference.containsKey(path)) {
            return compressionReference.get(path);
        } else {
            if(path.contains(COMP_EXTENSION)) {
                compressionReference.put(path, true);
                return true;
            } else {
                compressionReference.put(path, false);
                return false;
            }
        }
    }
}
