package common.state.los;

import common.util.BitArray;
import common.util.json.JsonReaderWrapperSpec;
import common.util.json.JsonWriterWrapperSpec;
import common.util.json.ReadOptions;
import common.util.json.WriteOptions;

import java.awt.*;
import java.io.IOException;

public class ExplorationImpl implements Exploration {

    private final BitArray explored;

    public ExplorationImpl(int w, int h) {
        explored = new BitArray(w,  h);
    }

    @Override
    public void updateAll(Exploration exploration) {
        explored.updateAll(((ExplorationImpl) exploration).explored);
    }

    @Override
    public void updateAll(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
        explored.updateAll(reader, spec);
    }

    @Override
    public void setExplored(Point p, Dimension size) {
        explored.set(p, size, true);
    }

    @Override
    public void writeTo(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
        explored.writeTo(writer, options);
    }

    @Override
    public void setExplored(int i, int j) {
        explored.set(i, j, true);
    }

    @Override
    public int getWidth() {
        return explored.getWidth();
    }

    @Override
    public int getHeight() {
        return explored.getHeight();
    }

    @Override
    public boolean get(int x, int y) {
        return explored.get(x, y);
    }
}
