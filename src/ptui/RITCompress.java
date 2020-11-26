package ptui;

import model.FileLoader;
import model.QuadTree;
import model.RITQTNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * The RITCompress class is a command line program that compresses images stored in a pixel color-per-line format. File
 * loading is handled in {@link FileLoader}.
 *
 * @author Samuel Henderson
 */
public class RITCompress {

    /**
     * Facilitates the process of compressing the file provided as an argument in the command line.
     */
    private static void compress() {
        System.out.println("Compressing: " + source);

        // Read the contents of the file to be compressed (the source) and convert into a QuadTree structure
        List<Integer> fileValues = FileLoader.secureLoadFileContents(FileLoader.UNCM_HEAD + source);
        int dimension = fileValues.size();

        RITQTNode tree = QuadTree.fromUncompressedContents(fileValues, 0, 0, (int) Math.sqrt(dimension));
        String preorder = QuadTree.preorder(tree); String[] compressed = preorder.split(" ");
        System.out.println("QuadTree: " + preorder);

        List<String> writeValues = new ArrayList<>(Collections.singleton("" + dimension));
        writeValues.addAll(Arrays.asList(compressed));

        // Write the QuadTree preorder output into the destination file
        FileLoader.secureWriteFileContents(writeValues, FileLoader.COMP_HEAD + destination);

        // Provide compression comparison and percentage
        System.out.println("Compression: " + dimension + " -> " + (compressed.length + 1) + " (" + (100 * (compressed.length + 1) / dimension));
    }

    /** Respectively the source and destination file names for compression. **/
    private static String source, destination;

    public static void main(String[] args) {
        if (args.length != 2) {
            // Handle missing or invalid argument(s)
            System.out.println("Usage: java RITUncompress compressed.rit uncompressed.txt");
        } else {
            source = args[1]; destination = args[0];
            compress();
        }
    }
}
