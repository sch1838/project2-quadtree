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

    private static RITQTNode treeContents;

    public static List<String> uncompress(String source) {
        if(source == null) {
            System.out.println("Failed to uncompress: null source or destination");
            return new ArrayList<>();
        }

        List<Integer> lineValues = FileLoader.secureLoadFileContents(source);
        dimension = lineValues.remove(0);

        // Create the quadtree structure from the compressed file
        treeContents = QuadTree.fromCompressedContents(lineValues);

        List<String> writeValues = new ArrayList<>();

        for (int[] row : QuadTree.extract(treeContents, (int) Math.sqrt(dimension))) {
            for (int value : row) {
                writeValues.add(value + "");
            }
        }

        return writeValues;
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

            List<String> writeValues = uncompress(FileLoader.COMP_HEAD + source);
            System.out.println("QuadTree: " + QuadTree.preorder(treeContents()));

            FileLoader.secureWriteFileContents(writeValues, FileLoader.UNCM_HEAD + destination);
            System.out.println("Output file: " + new File(FileLoader.UNCM_HEAD + destination).getAbsolutePath());
        }
    }
}