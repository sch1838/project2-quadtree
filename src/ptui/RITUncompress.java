package ptui;

import model.FileLoader;
import model.QuadTree;
import model.RITQTNode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * The RITUncompress class is a command line program that uncompresses images stored in a QuadTree format. File loading
 * is handled in {@link FileLoader}.
 *
 * @author Samuel Henderson
 */
public class RITUncompress {

    private static List<String> writeContents;

    private static RITQTNode treeContents;

    public static void uncompress(String source, String destination) {
        if(source == null || destination == null) {
            System.out.println("Failed to uncompress: null source or destination");
            return;
        }

        List<Integer> lineValues = FileLoader.secureLoadFileContents(FileLoader.COMP_HEAD + source);
        dimension = lineValues.remove(0);

        // Create the quadtree structure from the compressed file
        treeContents = QuadTree.fromCompressedContents(lineValues);

        List<String> writeValues = new ArrayList<>();

        for (int[] row : QuadTree.extract(treeContents, (int) Math.sqrt(dimension))) {
            for (int value : row) {
                writeValues.add(value + "");
            }
        }

        writeContents = writeValues;
    }

    public static List<String> writeContents() {
        return writeContents;
    }

    public static RITQTNode treeContents() {
        return treeContents;
    }

    /** The dimension of the image to uncompress. **/
    public static int dimension;

    public static void main(String[] args) {
        if (args.length != 2) {
            // Handle missing or invalid argument(s)
            System.out.println("Usage: java RITUncompress compressed.rit uncompressed.txt");
        } else {
            String source = args[0], destination = args[1];
            System.out.println("Uncompressing: " + source);
            uncompress(source, destination);
            System.out.println("QuadTree: " + QuadTree.preorder(treeContents()));
            FileLoader.secureWriteFileContents(writeContents(), FileLoader.UNCM_HEAD + destination);
            System.out.println("Output file: " + new File(FileLoader.UNCM_HEAD + destination).getAbsolutePath());
        }
    }
}