package common;

import common.state.EntityReader;
import common.state.Player;
import common.util.DPoint;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class DebugGraphics {
    /** TODO **/
    public static final HashMap<Player, List<DebugGraphics>> byPlayer = new HashMap<>();

    public static final Object pleaseFocusSync = new Object();
    public static EntityReader pleaseFocus;


    public final DPoint center;
    public final List<DPoint> list = new LinkedList<>();


    public DebugGraphics(DPoint center) {
        this.center = center;
    }
}
