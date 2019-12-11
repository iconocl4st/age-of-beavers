//package common.state.los;
//
//import common.state.Player;
//import common.util.DPoint;
//import common.util.Marked;
//import common.util.json.JsonReaderWrapperSpec;
//import common.util.json.Jsonable;
//import common.util.json.ReadOptions;
//
//import java.awt.*;
//import java.io.IOException;
//
//public interface LineOfSightSpec extends Jsonable {
//
//    void updateAll(LineOfSightSpec other);
//
//    int getCount(Player player, int i, int j);
//
//    boolean isVisible(Player player, Point location, Dimension size);
//
//    boolean isVisible(Player player, int x, int y);
//
//    boolean isExplored(Player player, int x, int y);
//
//    VisibilityChange updateLineOfSight(Player player, DPoint prevCenter, DPoint newCenter, double los);
//
//    VisibilityChange addEntity(Player player, DPoint newId, double lineOfSight);
//
//    VisibilityChange remove(Player player, DPoint id, double lineOfSight);
//
//    void updateAll(JsonReaderWrapperSpec reader, ReadOptions options) throws IOException;
//}
