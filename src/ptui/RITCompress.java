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

    /** The size of the uncompressed file. **/
    private static int uncompressedSize;

    /** The uncompressed file contents parsed into a QuadTree. **/
    private static RITQTNode treeContents;

    /**
     * Facilitates the process of compressing the file provided as an argument in the command line.
     */
    public static List<String> compress(String source) {
        // Read the contents of the file to be compressed (the source) and convert into a QuadTree structure
        List<Integer> fileValues = FileLoader.secureLoadFileContents(source);
        uncompressedSize = fileValues.size();

        treeContents = QuadTree.fromUncompressedContents(fileValues, 0, 0, (int) Math.sqrt(uncompressedSize));
        String preorder = QuadTree.preorder(treeContents);
        String[] compressed = preorder.split(" ");

        List<String> writeValues = new ArrayList<>(Collections.singleton("" + uncompressedSize));
        writeValues.addAll(Arrays.asList(compressed));

        return writeValues;
    }

    /** Access treeContents. **/
    public static RITQTNode treeContents() {
        return treeContents;
    }

    /** Access uncompressedSize. **/
    public static int uncompressedSize() {
        return uncompressedSize;
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            // Handle missing or invalid argument(s)
            System.out.println("Usage: java RITCompress compressed.rit uncompressed.txt");
        } else {
            String source = args[1], destination = args[0];
            System.out.println("Compressing: " + source);

            // Perform compression
            List<String> writeValues = compress(source);
            System.out.println("QuadTree: " + QuadTree.preorder(treeContents()));

            // Write compressed data to the output file
            FileLoader.secureWriteFileContents(writeValues, destination);
            System.out.println("Output file: " + new File(destination).getAbsolutePath());

            // Print compression information
            double uncm = RITCompress.uncompressedSize(), comp = writeValues.size();
            System.out.println("Uncompressed image size: " + uncm);
            System.out.println("Compressed image size: " + comp);
            System.out.println("Compression: " + (uncm - comp) / uncm * 100.0D + "%");
        }
    }
}
