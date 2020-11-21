package model;

import java.util.List;

/**
 * The QuadTree class contains utility methods to be used with QuadTree data structures created using {@link RITQTNode}.
 *
 * <p>Included functionality: QuadTree parsing from a formatted list of integers, extracting the contents of a QuadTree
 * into a grid of pixel color values, and producing a preorder String from a root node.</p>
 *
 * @author Samuel Henderson
 */
public class QuadTree {

    /**
     * Provides the root {@link RITQTNode} of a QuadTree data structure parsed from a correctly formatted list of
     * integral values that has been retrieved from a compressed image file.
     */
    public static RITQTNode fromCompressedContents(List<Integer> lineValues) {
        if(0 <= lineValues.get(0)) {
            return new RITQTNode(lineValues.remove(0));
        } else {
            return new RITQTNode(lineValues.remove(0), fromCompressedContents(lineValues), fromCompressedContents(lineValues), fromCompressedContents(lineValues), fromCompressedContents(lineValues));
        }
    }

    /**
     * Provides the root {@link RITQTNode} of a QuadTree data structure parsed from a list of
     * integral values that has been retrieved from an uncompressed image file.
     */
    public static RITQTNode fromUncompressedContents(List<Integer> lineValues, int row, int col, int dimension) {

        // Full dimension must remain as a constant value to access correct index in list
        int fullDim = (int) Math.sqrt(lineValues.size());
        int quadrantCheck = lineValues.get(row * fullDim + col);

        for (int r = row; r < row + dimension; ++ r) {
            for (int c = col; c < col + dimension; ++ c) {

                // Scan the current quadrant for a color different from the corner color
                // Quadrant dimension is reflected by the dimension parameter
                int val = lineValues.get(r * fullDim + c);

                if(val != quadrantCheck) {

                    // This quadrant cannot be compressed, so it becomes a root to four nodes that may be compressed
                    // Scan the four quadrants that compose this quadrant to see if compression is possible, then
                    // add them to the new root node as subsidiaries
                    int subDim = dimension / 2;
                    return new RITQTNode(-1,
                            fromUncompressedContents(lineValues, row,          col,          subDim),
                            fromUncompressedContents(lineValues, row,          col + subDim, subDim),
                            fromUncompressedContents(lineValues, row + subDim, col,          subDim),
                            fromUncompressedContents(lineValues, row + subDim, col + subDim, subDim)
                    );
                }
            }
        }

        // By this point, all values in the quadrant have been verified to be equal, so compression is possible
        return new RITQTNode(quadrantCheck);
    }

    /**
     * Converts a QuadTree data structure into a grid of pixel color values.
     *
     * @param root The root of the QuadTree structure
     * @param dimension The side length of the grid to be formed
     */
    public static int[][] extract(RITQTNode root, int dimension) {

        // Initialize and fill grid
        int[][] pixelGrid = new int[dimension][dimension];
        extract(root, pixelGrid, 0, 0, dimension);

        return pixelGrid;
    }

    /**
     * Recursively fills a provided grid of integers with values obtained from a QuadTree data structure. This method
     * should only be called from {@link QuadTree#extract(RITQTNode, int)} or a similarly functioning method.
     */
    private static void extract(RITQTNode root, int[][] pixelGrid, int row, int col, int dimension) {
        if (0 <= root.getVal()) {

            // Insert dimension * dimension values into the grid based on the provided row and column
            for (int subRow = row; subRow < row + dimension; subRow ++) {
                for (int subCol = col; subCol < col + dimension; subCol ++ ) {
                    pixelGrid[subRow][subCol] = root.getVal();
                }
            }
        } else {

            // Extract values from all four subsidiary nodes using halved dimension
            int subDim = dimension / 2;

            extract(root.getUpperLeft(),  pixelGrid, row,          col,          subDim);
            extract(root.getUpperRight(), pixelGrid, row,          col + subDim, subDim);
            extract(root.getLowerLeft(),  pixelGrid, row + subDim, col,          subDim);
            extract(root.getLowerRight(), pixelGrid, row + subDim, col + subDim, subDim);
        }
    }

    /**
     * Traverses a QuadTree data structure in preorder starting from a provided root {@link RITQTNode}.
     */
    public static String preorder(RITQTNode root) {
        if(0 <= root.getVal()) {
            return root.getVal() + " ";
        } else {
            return root.getVal() + " " + preorder(root.getUpperLeft()) + preorder(root.getUpperRight()) + preorder(root.getLowerLeft()) + preorder(root.getLowerRight());
        }
    }
}
