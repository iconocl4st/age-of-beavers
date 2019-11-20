package common;

import common.state.Player;
import common.util.DPoint;

import java.awt.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class DebugGraphics {
    /** TODO **/
    public static final HashMap<Player, List<DebugGraphics>> byPlayer = new HashMap<>();


    public final DPoint center;
    public final List<Point> list = new LinkedList<>();


    public DebugGraphics(DPoint center) {
        this.center = center;
    }
}
