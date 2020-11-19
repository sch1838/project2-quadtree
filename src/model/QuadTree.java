package model;

import javafx.scene.Node;

import java.util.List;

public class QuadTree {

    public static RITQTNode fromContents(List<Integer> lineValues) {
        if(0 <= lineValues.get(0)) {
            return new RITQTNode(lineValues.remove(0));
        } else {
            return new RITQTNode(lineValues.remove(0), fromContents(lineValues), fromContents(lineValues), fromContents(lineValues), fromContents(lineValues));
        }
    }
}
