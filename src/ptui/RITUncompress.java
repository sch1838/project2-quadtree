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

    private String source, destination;

    private List<String> writeContents;

    private RITQTNode treeContents;

    public void uncompress() {
        if(this.source == null || this.destination == null) {
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

        this.writeContents = writeValues;
    }

    public List<String> listContents() {
        return this.writeContents;
    }

    public RITQTNode treeContents() {
        return this.treeContents;
    }

    public RITUncompress(String source, String destination) {
        this.source = source; this.destination = destination;
    }

    public void modifySource(String path) {
        this.source = path;
    }

    public void modifyDestination(String path) {
        this.destination = path;
    }

    public String destination() {
        return this.destination;
    }

    /** The dimension of the image to uncompress. **/
    public static int dimension;

    public static void main(String[] args) {
        if (args.length != 2) {
            // Handle missing or invalid argument(s)
            System.out.println("Usage: java RITUncompress compressed.rit uncompressed.txt");
        } else {
            RITUncompress uncompress = new RITUncompress(args[0], args[1]);
            System.out.println("Uncompressing: " + uncompress.source);
            uncompress.uncompress();
            System.out.println("QuadTree: " + QuadTree.preorder(uncompress.treeContents()));
            FileLoader.secureWriteFileContents(uncompress.listContents(), FileLoader.UNCM_HEAD + uncompress.destination());
            System.out.println("Output file: " + new File(FileLoader.UNCM_HEAD + uncompress.destination()).getAbsolutePath());
        }
    }
}