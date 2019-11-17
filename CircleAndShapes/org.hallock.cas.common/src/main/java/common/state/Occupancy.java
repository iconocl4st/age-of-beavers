package common.state;

import common.state.los.LineOfSightSpec;
import common.state.spec.GameSpec;
import common.util.BitArray;
import common.util.json.*;

import java.awt.*;
import java.io.IOException;
import java.io.Serializable;

public class Occupancy implements Jsonable {
    private final BitArray bitArray;

    public Occupancy(int width, int height) {
        this.bitArray = new BitArray(width, height, 1, 1);
    }

    private BitArray.BitArrayView getOccupancy() {
        return new BitArray.BitArrayView(bitArray, new int[]{0, 0});
    }

    public void updateAll(Occupancy tilesState) {
        bitArray.updateAll(tilesState.bitArray);
    }

    public void updateAll(Occupancy occupancyState, LineOfSightSpec los) {
        BitArray.BitArrayView occupancy = getOccupancy();
        for (int i = 0; i < bitArray.getDimension(0); i++) {
            for (int j = 0; j < bitArray.getDimension(1); j++) {
                if (los.isVisible(null, i, j)) {
                    occupancy.set(i, j, occupancyState.isGloballyOccupied(i, j));
                }
            }
        }
    }

    public void setOccupancy(Point location, Dimension size, boolean occupancy) {
        for (int x = 0; x < size.width; x++) {
            for (int y = 0; y < size.height; y++) {
                bitArray.set(location.x + x, location.y + y, 0, 0, occupancy);
            }
        }
    }

    public boolean isGloballyOccupied(int x, int y) {
        return bitArray.get(x, y, 0, 0);
    }

    public boolean isOutOfBounds(int x, int y) {
        return bitArray.isOutOfBounds(x, y, 0, 0);
    }

    public void updateAll(JsonReaderWrapperSpec reader, ReadOptions gameSpec) throws IOException {
        reader.readBeginDocument();
        reader.readName("occupied");
        bitArray.updateAll(reader, gameSpec);
        reader.readEndDocument();
    }

    @Override
    public void writeTo(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
        writer.writeBeginDocument();
        writer.writeName("occupied");
        bitArray.writeTo(writer, options);
        writer.writeEndDocument();
    }



//    public Occupancy cloneFromPerspective(GateStateManager conditionalOccupancyManager, Player player, SinglePlayerLineOfSight los) {
//        int w = bitArray.getDimension(0);
//        int h = bitArray.getDimension(1);
//        Occupancy occ = new Occupancy(bitArray.getDimension(0), bitArray.getDimension(1));
//        for (int i = 0; i < w; i++) {
//            for (int j = 0; j < h; j++) {
//                if (!los.isVisible(null, i, j))
//                    continue;
//                occ.bitArray.set(i, j, 0, 0, bitArray.get(i, j, 0, 0));
//            }
//        }
//        return occ;
//    }
}
