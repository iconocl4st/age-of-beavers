package common.util;

import common.state.los.Exploration;
import common.util.json.*;

import java.awt.*;
import java.io.IOException;
import java.util.Random;

public class BitArray implements Jsonable, Marked {
    private byte[] bytes;
    private int len1;
    private int len2;

    public BitArray(int len1, int len2) {
        bytes = new byte[(int) Math.ceil(len1 * len2 / (double) Byte.SIZE)];
        this.len1 = len1;
        this.len2 = len2;
    }

    public int getWidth() {
        return len1;
    }

    public int getHeight() {
        return len2;
    }

    public void zero() {
        for (int i = 0; i < bytes.length; i++)
            bytes[i] = 0;
    }

    public boolean any(Point p, Dimension size) {
        for (int i = 0; i < size.width; i++)
            for (int j = 0; j < size.height; j++)
                if (get(p.x + i, p.y + j))
                    return true;
        return false;
    }

    public void set(Point p, Dimension size, boolean value) {
        for (int i = 0; i < size.width; i++)
            for (int j = 0; j < size.height; j++)
                set(p.x + i, p.y + j, value);
    }

    public boolean get(int i, int j) {
        checkBounds(i, j);
        int index = i + j * len1;
        int byteIndex = index / Byte.SIZE;
        int byteOffset = index % Byte.SIZE;
        int b = 0xff & bytes[byteIndex];
        int mask = 1 << byteOffset;
        return (b & mask) == mask;
    }

    public void set(int i,  int j, boolean val) {
        checkBounds(i, j);
        int index = i + j * len1;
        int byteIndex = index / Byte.SIZE;
        int byteOffset = index % Byte.SIZE;
        int mask = 1 << byteOffset;
        if (val) {
            bytes[byteIndex] |= mask;
        } else {
            bytes[byteIndex] &= ~mask;
        }
    }

    public boolean isOutOfBounds(int i, int j) {
        if (i<0 || i >= len1) return true;
        if (j<0 || j >= len2) return true;
        return false;
    }

    private void checkBounds(int i, int j) {
        if (i<0 || i >= len1) throw new IndexOutOfBoundsException(i + " of " + len1);
        if (j<0 || j >= len2) throw new IndexOutOfBoundsException(j + " of " + len2);
    }

    public void updateAll(BitArray ba) {
        System.arraycopy(ba.bytes, 0, bytes, 0,  bytes.length);
    }

    public void updateAll(JsonReaderWrapperSpec reader, ReadOptions options) throws IOException {
        zero(); // could be one pass through...
        reader.readBeginArray();
        while (reader.hasMoreInArray()) {
            reader.readBeginDocument();
            int i = reader.readInt32("i");
            int j = reader.readInt32("j");
            reader.readEndDocument();
            set(i, j, true);
        }
        reader.readEndArray();
    }

    @Override
    public void writeTo(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
        writer.writeBeginArray();
        for (int i = 0; i < len1; i++) {
            for (int j = 0; j < len2; j++) {
                if (!get(i, j))
                    continue;
                writer.writeBeginDocument();
                writer.write("i", i);
                writer.write("j", j);
                writer.writeEndDocument();
            }
        }
        writer.writeEndArray();
    }




    public static void main(String[] args) {
        testBitArray(1, 1);
        testBitArray(1, 4);
        testBitArray(5, 1);
        testBitArray(10, 20);
    }

    private static void testBitArray(int i, int j) {
        Random random = new Random();
        boolean[][] bs = new boolean[i][j];
        BitArray ba = new BitArray(i, j);
        for (int ii = 0; ii<10000; ii++) {
            int idx1 = random.nextInt(i);
            int idx2 = random.nextInt(j);

            if (ba.get(idx1, idx2) != bs[idx1][idx2]) {
                throw new RuntimeException("error");
            }

            boolean s = random.nextBoolean();
            bs[idx1][idx2] = s;
            ba.set(idx1, idx2, s);
        }
    }
//
//    public static class BitArrayView {
//
//        BitArray bitArray;
//        int[] lastIndices;
//
//        public BitArrayView(BitArray array, int[] lastIndices) {
//            this.bitArray = array;
//            this.lastIndices = lastIndices;
//        }
//
//
//        public boolean isOutOfBounds(int i) {
//            assert lastIndices.length == 3;
//            return bitArray.isOutOfBounds(i, lastIndices[0], lastIndices[1], lastIndices[2]);
//        }
//        public boolean isOutOfBounds(int i, int j) {
//            assert lastIndices.length == 2;
//            return bitArray.isOutOfBounds(i, j, lastIndices[0], lastIndices[1]);
//        }
//        public boolean isOutOfBounds(int i, int j, int k) {
//            assert lastIndices.length == 1;
//            return bitArray.isOutOfBounds(i, j, k, lastIndices[0]);
//        }
//
//        public boolean get(int i) {
//            assert lastIndices.length == 3;
//            return bitArray.get(i, lastIndices[0], lastIndices[1], lastIndices[2]);
//        }
//        public boolean get(int i, int j) {
//            assert lastIndices.length == 2;
//            return bitArray.get(i, j, lastIndices[0], lastIndices[1]);
//        }
//        public boolean get(int i, int j, int k) {
//            assert lastIndices.length == 1;
//            return bitArray.get(i, j, k, lastIndices[0]);
//        }
//
//        public void set(int i, boolean val) {
//            assert lastIndices.length == 3;
//            bitArray.set(i, lastIndices[0], lastIndices[1], lastIndices[2], val);
//        }
//        public void set(int i, int j, boolean val) {
//            assert lastIndices.length == 2;
//            bitArray.set(i, j, lastIndices[0], lastIndices[1], val);
//        }
//        public void set(int i, int j, int k, boolean val) {
//            assert lastIndices.length == 1;
//            bitArray.set(i, j, k, lastIndices[0], val);
//        }
//
//        public boolean bad(int i, int j) {
//            if (isOutOfBounds(i, j)) return true;
//            if (get(i, j)) return true;
//            return false;
//        }
//
//        public int getDimension(int i) {
//            // should also check length of last indices
//            if (i == 0) return bitArray.len1;
//            if (i == 1) return bitArray.len2;
//            if (i == 2) return bitArray.len3;
//            if (i == 3) return bitArray.len4;
//            throw new IllegalStateException();
//        }
//    }
}
