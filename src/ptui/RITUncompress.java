package ptui;

import model.FileLoader;
import model.QuadTree;
import model.RITQTNode;

import java.net.URL;

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

        // Create the quadtree structure from the compressed file
        RITQTNode tree = QuadTree.fromContents(FileLoader.secureLoadFileContents(FileLoader.COMP, source));
        System.out.println("QuadTree: " + QuadTree.preorder(tree));
        FileLoader.secureWriteFileContents(QuadTree.extract(tree, (int) Math.sqrt(dimension)), FileLoader.UNCM, destination);
    }

    /** Respectively the source and destination file names for uncompression. **/
    private static String source, destination;

    /** The dimension of the image to uncompress. This is updated by {@link FileLoader#loadFileContents(URL, String)} **/
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