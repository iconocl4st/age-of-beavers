package common.state.los;

import common.state.spec.GameSpec;
import common.util.Marked;
import common.util.json.JsonReaderWrapperSpec;
import common.util.json.Jsonable;
import common.util.json.ReadOptions;

import java.awt.*;
import java.io.IOException;

public interface Exploration extends Marked, Jsonable {
    void updateAll(Exploration exploration);
    void updateAll(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException;
    void setExplored(Point p, Dimension size);
    void setExplored(int i, int j);

    static Exploration createExploration(GameSpec spec, boolean isSpectator) {
        if (isSpectator) return new AllExplored(spec);
        switch (spec.visibility) {
            case ALL_VISIBLE:
            case EXPLORED:
                return new AllExplored(spec);
            case FOG:
                return new ExplorationImpl(spec.width, spec.height);
            default:
                throw new RuntimeException("Unhandled visibility");
        }
    }


    static void updateExploration(Exploration exploration, VisibilityChange change) {
        for (Point p  : change.gainedVision) {
            exploration.setExplored(p.x, p.y);
        }
    }
}
