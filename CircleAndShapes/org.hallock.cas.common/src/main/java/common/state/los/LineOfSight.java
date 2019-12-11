package common.state.los;

import common.state.spec.GameSpec;
import common.util.DPoint;
import common.util.Marked;
import common.util.json.JsonReaderWrapperSpec;
import common.util.json.Jsonable;
import common.util.json.ReadOptions;

import java.awt.*;
import java.io.IOException;

public interface LineOfSight extends Jsonable, Marked {
    int getCount(int i, int j);
    void updateAll(LineOfSight los);
    void updateAll(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException;

//    VisibilityChange add(DPoint center, double los);
//    VisibilityChange remove(DPoint center, double los);
    VisibilityChange move(DPoint prevCenter, DPoint newCenter, double los);

    boolean isVisible(Point location, Dimension size);


    static LineOfSight createLineOfSight(GameSpec spec, boolean isSpectator) {
        if (isSpectator) return new AllVisibleLos(spec);
        switch (spec.visibility) {
            case ALL_VISIBLE:
                return new AllVisibleLos(spec);
            case EXPLORED:
            case FOG:
                return new LineOfSightImpl(spec);
            default:
                throw new RuntimeException("Unhandled visibility");
        }
    }
}
