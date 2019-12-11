package common.state.los;

import common.state.spec.GameSpec;
import common.util.DPoint;
import common.util.json.JsonReaderWrapperSpec;
import common.util.json.JsonWriterWrapperSpec;
import common.util.json.ReadOptions;
import common.util.json.WriteOptions;

import java.awt.*;
import java.io.IOException;

public class LineOfSightImpl implements LineOfSight {

    int[][] losCounts;

    public LineOfSightImpl(GameSpec spec) {
        losCounts = new int[spec.width][spec.height];
    }

    private void increment(VisibilityChange change, DPoint center, int los, int amount) {
        if (center == null) {
            return;
        }
        for (int x = (int) Math.floor(center.x - los); x <= (int) Math.ceil(center.x + los); x++) {
            if (x < 0 || x >= losCounts.length)
                continue;
            for (int y = (int) Math.floor(center.y - los); y <= (int) Math.ceil(center.y + los); y++) {
                if (y < 0 || y >= losCounts[x].length)
                    continue;

                // could project...
                double dx = x + 0.5 - center.x;
                double dy = y + 0.5 - center.y;
                if (Math.sqrt(dx*dx+dy*dy) > los) {
                    continue;
                }
                if (amount > 0 && losCounts[x][y] == 0)
                    change.addVisibility(x, y);
                losCounts[x][y] += amount;
                if (amount < 0 && losCounts[x][y] == 0)
                    change.removeVisibility(x, y);
                if (losCounts[x][y] < 0)
                    throw new IllegalStateException(x + ", " + y);
            }
        }
    }

    @Override
    public void updateAll(LineOfSight o) {
        LineOfSightImpl other = (LineOfSightImpl) o;
        losCounts = other.losCounts;
    }

    @Override
    public int getCount(int i, int j) {
        if (i < 0 || j < 0 || i >= losCounts.length || j >= losCounts[i].length)
            return 0;
        return losCounts[i][j];
    }

    @Override
    public boolean isVisible(Point location, Dimension size) {
        for (int i = 0; i < size.width; i++) {
            for (int j = 0; j < size.height; j++) {
                if (get(location.x + i, location.y + j)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public VisibilityChange move(DPoint prevCenter, DPoint newCenter, double los) {
        VisibilityChange change = new VisibilityChange();
        increment(change, prevCenter, (int) los, -1);
        increment(change, newCenter, (int) los, 1);
        return change;
    }

    @Override
    public void updateAll(JsonReaderWrapperSpec reader, ReadOptions options) throws IOException {
        reader.readBeginDocument();
        zero();
        reader.readBeginArray("counts");
        while (reader.hasMoreInArray()) {
            reader.readBeginDocument();
            int i = reader.readInt32("i");
            int j = reader.readInt32("j");
            int count = reader.readInt32("count");
            reader.readEndDocument();
            losCounts[i][j] = count;
        }
        reader.readEndArray();
        reader.readEndDocument();
    }

    private void zero() {
        for (int i = 0; i < losCounts.length; i++)
            for (int j = 0; j < losCounts[i].length; j++)
                losCounts[i][j] = 0;
    }

    @Override
    public void writeTo(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
        writer.writeBeginDocument();

        writer.writeBeginArray("counts");
        for (int i = 0; i < losCounts.length; i++) {
            for (int j = 0; j < losCounts[i].length; j++) {
                if (losCounts[i][j] == 0)
                    continue;
                writer.writeBeginDocument();
                writer.write("i", i);
                writer.write("j", j);
                writer.write("count", losCounts[i][j]);
                writer.writeEndDocument();
            }
        }
        writer.writeEndArray();

        writer.writeName("explored");
        writer.writeEndDocument();
    }

    @Override
    public int getWidth() {
        return losCounts.length;
    }

    @Override
    public int getHeight() {
        return losCounts[0].length;
    }

    @Override
    public boolean get(int x, int y) {
        return getCount(x, y) > 0;
    }
}
