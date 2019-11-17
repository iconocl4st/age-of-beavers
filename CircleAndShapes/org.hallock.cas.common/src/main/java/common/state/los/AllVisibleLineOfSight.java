package common.state.los;

import common.state.spec.GameSpec;
import common.state.Player;
import common.util.DPoint;
import common.util.Marked;
import common.util.json.JsonReaderWrapperSpec;
import common.util.json.JsonWriterWrapperSpec;
import common.util.json.ReadOptions;
import common.util.json.WriteOptions;

import java.awt.*;
import java.io.IOException;

public class AllVisibleLineOfSight implements LineOfSightSpec {

    private final GameSpec spec;

    public AllVisibleLineOfSight(GameSpec spec) {
        this.spec = spec;
    }

    @Override
    public Marked createExploredView() {
        return new Marked() {
            @Override
            public int getWidth() {
                return spec.width;
            }

            @Override
            public int getHeight() {
                return spec.height;
            }

            @Override
            public boolean get(int x, int y) {
                return true;
            }
        };
    }

    @Override
    public void updateAll(LineOfSightSpec other) {
    }

    @Override
    public int getCount(Player player, int i, int j) {
        return 1;
    }

    @Override
    public boolean isVisible(Player player, Point location, Dimension size) {
        return true;
    }

    @Override
    public boolean isVisible(Player player, int x, int y) {
        return true;
    }

    @Override
    public boolean isExplored(Player player, int x, int y) {
        return true;
    }

    @Override
    public VisibilityChange updateLineOfSight(Player player, DPoint prevCenter, DPoint newCenter, double los) {
        return NO_CHANGE;
    }

    @Override
    public VisibilityChange addEntity(Player player, DPoint newId, double lineOfSight) {
        return NO_CHANGE;
    }

    @Override
    public VisibilityChange remove(Player player, DPoint id, double lineOfSight) {
        return NO_CHANGE;
    }

    @Override
    public void updateAll(JsonReaderWrapperSpec reader, ReadOptions options) throws IOException {
        reader.readBeginDocument();
        reader.readEndDocument();
    }

    @Override
    public void writeTo(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
        writer.writeBeginDocument();
        writer.writeEndDocument();
    }

    private static final VisibilityChange NO_CHANGE = new VisibilityChange();
}
