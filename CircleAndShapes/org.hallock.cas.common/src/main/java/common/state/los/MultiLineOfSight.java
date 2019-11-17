package common.state.los;

import common.state.Player;
import common.util.DPoint;
import common.util.Marked;
import common.util.json.JsonReaderWrapperSpec;
import common.util.json.JsonWriterWrapperSpec;
import common.util.json.ReadOptions;
import common.util.json.WriteOptions;

import javax.naming.OperationNotSupportedException;
import java.awt.*;
import java.io.IOException;

public class MultiLineOfSight implements LineOfSightSpec {
    // This class probably should not implement the LineOfSightSpec...

    public final LineOfSightSpec[] lineOfSights;

    public MultiLineOfSight(LineOfSightSpec[] lineOfSights) {
        this.lineOfSights = lineOfSights;
    }

    @Override
    public Marked createExploredView() {
        throw new RuntimeException("uh oh");
    }

    @Override
    public void updateAll(LineOfSightSpec other) {}

    @Override
    public int getCount(Player player, int i, int j) {
        return lineOfSights[player.number].getCount(null, i, j);
    }

    @Override
    public boolean isVisible(Player player, Point location, Dimension size) {
        return lineOfSights[player.number].isVisible(player, location, size);
    }

    @Override
    public boolean isVisible(Player player, int x, int y) {
        return lineOfSights[player.number].isVisible(player, x, y);
    }

    @Override
    public boolean isExplored(Player player, int x, int y) {
        return lineOfSights[player.number].isExplored(player, x, y);
    }

    @Override
    public VisibilityChange updateLineOfSight(Player player, DPoint prevCenter, DPoint newCenter, double los) {
        return lineOfSights[player.number].updateLineOfSight(player, prevCenter, newCenter, los);
    }

    @Override
    public VisibilityChange addEntity(Player player, DPoint newId, double lineOfSight) {
        return lineOfSights[player.number].addEntity(player, newId, lineOfSight);
    }

    @Override
    public VisibilityChange remove(Player player, DPoint id, double lineOfSight) {
        return lineOfSights[player.number].remove(player, id, lineOfSight);
    }

    @Override
    public void updateAll(JsonReaderWrapperSpec reader, ReadOptions options) throws IOException {
        throw new RuntimeException();
    }

    @Override
    public void writeTo(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
        throw new RuntimeException();
    }
}
