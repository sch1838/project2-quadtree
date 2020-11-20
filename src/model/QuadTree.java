package model;

import java.util.List;

public class QuadTree {

    public static RITQTNode fromContents(List<Integer> lineValues) {
        if(0 <= lineValues.get(0)) {
            return new RITQTNode(lineValues.remove(0));
        } else {
            return new RITQTNode(lineValues.remove(0), fromContents(lineValues), fromContents(lineValues), fromContents(lineValues), fromContents(lineValues));
        }
    }

    public static int[][] extract(RITQTNode root, int dimension) {

        int[][] pixelGrid = new int[dimension][dimension];

        extract(root, pixelGrid, 0, 0, dimension);

        return pixelGrid;
    }

    private static void extract(RITQTNode root, int[][] pixelGrid, int row, int col, int dimension) {
        if (0 <= root.getVal()) {
            for (int subRow = row; subRow < row + dimension; subRow ++) {
                for (int subCol = col; subCol < col + dimension; subCol ++ ) {
                    pixelGrid[subRow][subCol] = root.getVal();
                }
            }
        } else {
            int subDim = dimension / 2;

            extract(root.getUpperLeft(),  pixelGrid, row,          col,          subDim);
            extract(root.getUpperRight(), pixelGrid, row,          col + subDim, subDim);
            extract(root.getLowerLeft(),  pixelGrid, row + subDim, col,          subDim);
            extract(root.getLowerRight(), pixelGrid, row + subDim, col + subDim, subDim);
        }
    }

    public static String preorder(RITQTNode root) {
        if(0 <= root.getVal()) {
            return root.getVal() + " ";
        } else {
            return root.getVal() + " " + preorder(root.getUpperLeft()) + preorder(root.getUpperRight()) + preorder(root.getLowerLeft()) + preorder(root.getLowerRight());
        }
    }
}
