package model;

import gui.Display;
import gui.RITGUI;

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

    /**
     * Attempts to load the contents of a file at the provided path into a list of integers. This method is used to call
     * {@link FileLoader#loadFileContents(String)} with attention to thrown exceptions.
     */
    public static List<Integer> secureLoadFileContents(String path) {
        try {
            // Attempt to load the file from the uncompressed image directory

            List<Integer> lineValues = FileLoader.loadFileContents(path);
            double dimension = Math.sqrt(lineValues.size());
            if (!isFileCompressed(path) && Math.floor(dimension) != dimension) {
                // Do not check dimension when loading compressed files
                throw new LoaderException.FileDimensionException(dimension);
            } else {
                return lineValues;
            }
        } catch (FileNotFoundException f) {
            // Handle nonexistent file

            f.printStackTrace();
            System.out.println("File does not exist: " + path);
            if (!RITGUI.active) {
                System.exit(-1);
            } else {
                Display.postException(f.getMessage());
            }
        } catch (IOException | LoaderException.IntegralColorException | LoaderException.UnreadablePathException | LoaderException.FileDimensionException e) {
            // Handle generic IOException, unreadable files, nonsquare files, and color value not in valid range
            // All LoaderException extensions can be handled in the same way because they override printStackTrace

            e.printStackTrace();
            if (!RITGUI.active) {
                System.exit(-1);
            } else {
                Display.postException(e.getMessage());
            }
        } catch (NumberFormatException n) {
            // Handle non-integral color values (NumberFormatException thrown when parsing lines as an integer)

            System.out.println("Exception loading file: " + path + "\nFile contains a non-integral value");
            if (!RITGUI.active) {
                System.exit(-1);
            } else {
                Display.postException(n.getMessage());
            }
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
    private static List<Integer> loadFileContents(String path) throws IOException, LoaderException.IntegralColorException, NumberFormatException, LoaderException.UnreadablePathException {

        File file = new File(path);

        List<Integer> lineValues = new ArrayList<>();

        if(file.exists() && !file.isDirectory()) {

            // Open a reader in the file
            BufferedReader reader = new BufferedReader(new FileReader(file));

            String line;

            while ((line = reader.readLine()) != null) {
                int value = Integer.parseInt(line);

                if (!isFileCompressed(path) && (value < 0 || 255 < value)) {
                    // Do not check colors when loading compressed files

                    throw new LoaderException.IntegralColorException(value);
                }

                lineValues.add(value);
            }

            reader.close();
        } else {
            throw new LoaderException.UnreadablePathException(path);
        }

        return lineValues;
    }

    /**
     * Writes a list of objects to a file specified by the provided directoryHeader {@link URL} and path.
     *
     * <p>This method is used to call {@link FileLoader#writeFileContents(List, String)} with automatic attention
     * to any thrown exceptions.</p>
     */
    public static void secureWriteFileContents(List<?> lineValues, String path) {
        try {
            writeFileContents(lineValues, path);
        } catch (LoaderException.FileCreationException | IOException | LoaderException.UnreadablePathException e) {
            // Handle nonexistent file, unreadable file, and failure to create file

            e.printStackTrace();
            if(!RITGUI.active) {
                System.exit(-1);
            } else {
                Display.postException(e.getMessage());
            }
        }
    }

    /**
     * Writes a {@link List} of objects to a file specified by the provided absolute file path.
     *
     * <p>Each object in lineValues will be written to its own line in an existent file. If the file does not exist,
     * this method will attempt to create it.</p>
     *
     * @throws LoaderException.FileCreationException Thrown when a file cannot be created at the provided path
     * @throws LoaderException.UnreadablePathException Thrown when the file at the provided path is a directory
     */
    private static void writeFileContents(List<?> lineValues, String path) throws LoaderException.FileCreationException, IOException, LoaderException.UnreadablePathException {
        File file = new File(path);

        if (!file.exists()) {

            // Attempt to create file if it does not exist
            if (!file.createNewFile()) {

                // Except when the file cannot be created
                throw new LoaderException.FileCreationException(file.getPath());
            }
        }

        if (!file.isDirectory()) {
            // Cannot write text to a directory
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));

            for (Object value : lineValues) {
                // Write each value to its own line in the file
                writer.write(value.toString());
                writer.newLine();
            }

            writer.close();
        } else {
            throw new LoaderException.UnreadablePathException("Invalid path " + file.getPath() + " is a directory not a file");
        }
    }

    /** Stores file paths alongside boolean values representing whether or not they represent compressed images. **/
    private static final Map<String, Boolean> compressionReference = new HashMap<>();

    /** The file extension for compressed files **/
    private static final String COMP_EXTENSION = ".rit";

    /**
     * Evaluates the compression of a file based on its path. Returns true when the path represents a compressed file.
     */
    private static boolean isFileCompressed(String path) {
        if(compressionReference.containsKey(path)) {
            // Avoid startsWith call when path has already been checked
            return compressionReference.get(path);
        } else {
            if(path.startsWith(COMP_EXTENSION, path.length() - COMP_EXTENSION.length())) {
                compressionReference.put(path, true);
                return true;
            } else {
                compressionReference.put(path, false);
                return false;
            }
        }
    }
}
