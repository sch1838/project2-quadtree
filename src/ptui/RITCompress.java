package ptui;

import model.FileLoader;
import model.QuadTree;
import model.RITQTNode;

import java.io.File;
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

    private static int dimension;

    private static RITQTNode treeContents;

    /**
     * Facilitates the process of compressing the file provided as an argument in the command line.
     */
    public static List<String> compress(String source) {
        // Read the contents of the file to be compressed (the source) and convert into a QuadTree structure
        List<Integer> fileValues = FileLoader.secureLoadFileContents(source);
        dimension = fileValues.size();

        treeContents = QuadTree.fromUncompressedContents(fileValues, 0, 0, (int) Math.sqrt(dimension));
        String preorder = QuadTree.preorder(treeContents);
        String[] compressed = preorder.split(" ");

        List<String> writeValues = new ArrayList<>(Collections.singleton("" + dimension));
        writeValues.addAll(Arrays.asList(compressed));

        return writeValues;
    }

    public static RITQTNode treeContents() {
        return treeContents;
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            // Handle missing or invalid argument(s)
            System.out.println("Usage: java RITCompress compressed.rit uncompressed.txt");
        } else {
            String source = args[1], destination = args[0];
            System.out.println("Compressing: " + source);

            List<String> writeValues = compress(FileLoader.UNCM_HEAD + source);
            System.out.println("QuadTree: " + QuadTree.preorder(treeContents()));

            FileLoader.secureWriteFileContents(writeValues, FileLoader.COMP_HEAD + destination);
            System.out.println("Output file: " + new File(FileLoader.COMP_HEAD + destination).getAbsolutePath());

            int size = (int) Math.sqrt(dimension);
            // TODO: fix below
            System.out.println("Compression: " + dimension + " -> " + (size + 1) + " (" + (100 * (size + 1) / dimension));
        }
    }
}
