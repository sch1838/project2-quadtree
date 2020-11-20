package ptui;

import model.FileLoader;
import model.QuadTree;
import model.RITQTNode;

public class RITUncompress {

    private static void uncompress() {
        System.out.println("Uncompressing: " + source);

        // Create the quadtree structure from the compressed file
        RITQTNode tree = QuadTree.fromContents(FileLoader.secureLoadFileContents(FileLoader.COMP, source));
        System.out.println("QuadTree: " + QuadTree.preorder(tree));
        FileLoader.secureWriteFileContents(QuadTree.extract(tree, (int) Math.sqrt(dimension)), FileLoader.UNCM, destination);
    }

    private static String source, destination;

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