package common.state.los;

import common.state.spec.GameSpec;
import common.util.DPoint;
import common.util.json.JsonReaderWrapperSpec;
import common.util.json.JsonWriterWrapperSpec;
import common.util.json.ReadOptions;
import common.util.json.WriteOptions;

import java.awt.*;
import java.io.IOException;

public class AllVisibleLos implements LineOfSight {

    private final GameSpec spec;

    public AllVisibleLos(GameSpec spec) {
        this.spec = spec;
    }

    @Override
    public int getCount(int i, int j) { return 1; }
    @Override
    public void updateAll(LineOfSight los) {}
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

    @Override
    public VisibilityChange move(DPoint prevCenter, DPoint newCenter, double los) {
        return NO_CHANGE;
    }

    @Override
    public boolean isVisible(Point location, Dimension size) {
        return true;
    }

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

    private static final VisibilityChange NO_CHANGE = new VisibilityChange();
}
