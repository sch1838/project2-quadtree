package ptui;

import model.FileLoader;
import model.QuadTree;
import model.RITQTNode;

import java.util.ArrayList;
import java.util.List;

/**
 * The RITUncompress class is a command line program that uncompresses images stored in a QuadTree format. File loading
 * is handled in {@link FileLoader}.
 *
 * @author Samuel Henderson
 */
public class RITUncompress {

    /**
     * Facilitates the process of uncompressing the file provided as an argument in the command line.
     */
    private static void uncompress() {
        System.out.println("Uncompressing: " + source);

        List<Integer> lineValues = FileLoader.secureLoadFileContents(FileLoader.COMP_HEAD + source);
        dimension = lineValues.remove(0);

        // Create the quadtree structure from the compressed file
        RITQTNode tree = QuadTree.fromCompressedContents(lineValues);
        System.out.println("QuadTree: " + QuadTree.preorder(tree));

        List<Integer> writeValues = new ArrayList<>();

        for (int[] row : QuadTree.extract(tree, (int) Math.sqrt(dimension))) {
            for (int value : row) {
                writeValues.add(value);
            }
        }

        FileLoader.secureWriteFileContents(writeValues, FileLoader.UNCM_HEAD + destination);
    }

    public static void uncompress(String path) {
        List<Integer> lineValues = FileLoader.secureLoadFileContents(path);
    }

    /** Respectively the source and destination file names for uncompression. **/
    private static String source, destination;

    /** The dimension of the image to uncompress. **/
    public static int dimension;

    public static void main(String[] args) {
        if (args.length != 2) {
            // Handle missing or invalid argument(s)
            System.out.println("Usage: java RITUncompress compressed.rit uncompressed.txt");
        } else {
            source = args[0]; destination = args[1];
            uncompress();
        }
    }
}