package common.state.los;

import common.state.Player;
import common.state.spec.GameSpec;
import common.util.BitArray;
import common.util.DPoint;
import common.util.Marked;
import common.util.json.JsonReaderWrapperSpec;
import common.util.json.JsonWriterWrapperSpec;
import common.util.json.ReadOptions;
import common.util.json.WriteOptions;

import java.awt.*;
import java.io.IOException;

public class SinglePlayerLineOfSight implements LineOfSightSpec {
    int[][] losCounts;
    BitArray explored;
    public BitArray.BitArrayView exploredView;

    public SinglePlayerLineOfSight(GameSpec spec) {
        explored = new BitArray(spec.width, spec.height, 1, 1);
        losCounts = new int[spec.width][spec.height];
        exploredView = new BitArray.BitArrayView(explored, new int[]{0, 0});
    }


    @Override
    public Marked createExploredView() {
        return Marked.createMarked(exploredView);
    }

    private void increment(VisibilityChange change, Point center, int los, int amount) {
        if (center == null) {
            return;
        }
        for (int i = -los; i <= los; i++) {
            int x = center.x + i;
            if (x < 0 || x >= losCounts.length)
                continue;
            for (int j = -los; j <= los; j++) {
                int y = center.y + j;
                if (y < 0 || y >= losCounts[x].length)
                    continue;

                double dx = x - center.x;
                double dy = y - center.y;
                if (Math.sqrt(dx*dx+dy*dy) > los) {
                    continue;
                }

                if (amount > 0 && !exploredView.get(x, y)) {
                    change.becameExplored(x, y);
                    exploredView.set(x, y, true);
                }
                if (amount > 0 && losCounts[x][y] <= 0) {
                    change.addVisibility(x, y);
                }
                losCounts[x][y] += amount;
                if (amount < 0 && losCounts[x][y] <= 0) {
                    change.removeVisibility(x, y);
                }
                if (losCounts[x][y] < 0)
                    throw new IllegalStateException(x + ", " + y);
            }
        }
    }

    @Override
    public void updateAll(LineOfSightSpec o) {
        SinglePlayerLineOfSight other = (SinglePlayerLineOfSight) o;
        losCounts = other.losCounts;
        explored = other.explored;
        exploredView = new BitArray.BitArrayView(explored, new int[]{0, 0});
    }

    @Override
    public int getCount(Player player, int i, int j) {
        if (i < 0 || j < 0 || i >= losCounts.length || j >= losCounts[i].length)
            return 0;
        return losCounts[i][j];
    }

    @Override
    public boolean isVisible(Player player, Point location, Dimension size) {
        for (int i = 0; i < size.width; i++) {
            for (int j = 0; j < size.height; j++) {
                if (getCount(player, location.x + i, location.y + j) > 0) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isVisible(Player player, int x, int y) {
        return getCount(player, x, y) > 0;
    }

    @Override
    public boolean isExplored(Player player, int x, int y) {
        if (exploredView.isOutOfBounds(x, y))
            return false;
        return exploredView.get(x, y);
    }

    @Override
    public VisibilityChange addEntity(Player player, DPoint newId, double lineOfSight) {
        VisibilityChange change = new VisibilityChange();
        increment(change, newId.toPoint(), (int) lineOfSight, 1);
        return change;
    }

    @Override
    public VisibilityChange remove(Player player, DPoint id, double lineOfSight) {
        VisibilityChange change = new VisibilityChange();
        increment(change, id.toPoint(), (int) lineOfSight, -1);
        return change;
    }

    @Override
    public VisibilityChange updateLineOfSight(Player player, DPoint prevCenter, DPoint newCenter, double los) {
        VisibilityChange change = new VisibilityChange();
        if (prevCenter != null) {
            Point prevPoint = prevCenter.toPoint();
            increment(change, prevPoint, (int) los, -1);
        }
        if (newCenter != null) {
            Point newPoint = newCenter.toPoint();
            increment(change, newPoint, (int) los, 1);
        }
        return change;
    }

    @Override
    public void updateAll(JsonReaderWrapperSpec reader, ReadOptions options) throws IOException {
        reader.readBeginDocument();

        explored.zero();
        for (int i = 0; i < losCounts.length; i++)
            for (int j = 0; j < losCounts[i].length; j++)
                losCounts[i][j] = 0;

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

        reader.readName("explored");
        explored.updateAll(reader, options);

        reader.readEndDocument();
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
        explored.writeTo(writer, options);

        writer.writeEndDocument();
    }
}
