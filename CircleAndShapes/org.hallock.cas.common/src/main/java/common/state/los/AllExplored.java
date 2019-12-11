package common.state.los;

import common.state.spec.GameSpec;
import common.util.json.JsonReaderWrapperSpec;
import common.util.json.JsonWriterWrapperSpec;
import common.util.json.ReadOptions;
import common.util.json.WriteOptions;

import java.awt.*;
import java.io.IOException;

public class AllExplored implements Exploration {

    private final GameSpec spec;

    public AllExplored(GameSpec spec) {
        this.spec = spec;
    }

    @Override
    public void updateAll(Exploration exploration) {}

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

    @Override
    public void writeTo(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
        writer.writeBeginDocument();
        writer.writeEndDocument();
    }

    @Override
    public void updateAll(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
        reader.readBeginDocument();
        reader.readEndDocument();
    }

    @Override
    public void setExplored(Point p, Dimension size) {}

    @Override
    public void setExplored(int i, int j) {}
}
