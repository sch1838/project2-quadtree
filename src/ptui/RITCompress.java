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

    private String source, destination;

    private int dimension;

    private List<String> writeContents;

    private RITQTNode treeContents;

    /**
     * Facilitates the process of compressing the file provided as an argument in the command line.
     */
    public void compress() {
        // Read the contents of the file to be compressed (the source) and convert into a QuadTree structure
        List<Integer> fileValues = FileLoader.secureLoadFileContents(FileLoader.UNCM_HEAD + source);
        dimension = fileValues.size();

        treeContents = QuadTree.fromUncompressedContents(fileValues, 0, 0, (int) Math.sqrt(dimension));
        String preorder = QuadTree.preorder(treeContents);
        String[] compressed = preorder.split(" ");

        List<String> writeValues = new ArrayList<>(Collections.singleton("" + dimension));
        writeValues.addAll(Arrays.asList(compressed));

        this.writeContents = writeValues;
    }

    public RITCompress(String source, String destination) {
        this.source = source; this.destination = destination;
    }

    public List<String> listContents() {
        return this.writeContents;
    }

    public RITQTNode treeContents() {
        return this.treeContents;
    }

    public String destination() {
        return this.destination;
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            // Handle missing or invalid argument(s)
            System.out.println("Usage: java RITUncompress compressed.rit uncompressed.txt");
        } else {
            RITCompress compress = new RITCompress(args[1], args[0]);
            System.out.println("Compressing: " + compress.source);
            compress.compress();
            System.out.println("QuadTree: " + QuadTree.preorder(compress.treeContents()));
            FileLoader.secureWriteFileContents(compress.listContents(), FileLoader.COMP_HEAD + compress.destination());
            System.out.println("Output file: " + new File(FileLoader.COMP_HEAD + compress.destination()).getAbsolutePath());
            int size = (int) Math.sqrt(compress.dimension);
            // TODO: fix below
            System.out.println("Compression: " + compress.dimension + " -> " + (size + 1) + " (" + (100 * (size + 1) / compress.dimension));
        }
    }
}
