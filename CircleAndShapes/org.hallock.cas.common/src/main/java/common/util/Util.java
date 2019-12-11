package common.util;

import common.state.Occupancy;
import common.state.sst.OccupancyView;

import java.awt.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;

public class Util {
    private static String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUIVWXYZ0123456789";
    public static String createRandomString(Random random, int length) {
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            builder.append(chars.charAt(random.nextInt(chars.length())));
        }
        return builder.toString();
    }

    public static String getDebugString() {
        try {
            throw new RuntimeException();
        } catch (Throwable t) {
            StringWriter sw = new StringWriter();
            try (PrintWriter pw = new PrintWriter(sw)) {
                t.printStackTrace(pw);
            }
            return sw.toString();
        }
    }

    public static boolean anyAreNull(Object... objs) {
        for (Object obj : objs) if (obj == null) return true;
        return false;
    }


    public static double projToInterval(double x, double xmin, double xmax) {
        return Math.min(xmax, Math.max(xmin, x));
    }

    public static double minimumDistanceToSquare(double x, double y, double xmin, double xmax, double ymin, double ymax) {
        double dx = x - projToInterval(x, xmin, xmax);
        double dy = y - projToInterval(y, ymin, ymax);
        return Math.sqrt(dx*dx + dy*dy);
    }

    public static Point getSpaceForBuilding(Point startingLocation, Dimension size, OccupancyView view, int maxWidth, int buffer) {
        return getSpaceForBuilding(new SpiralIterator(startingLocation), size, view, maxWidth, buffer);
    }
    public static Point getSpaceForBuilding(SpiralIterator spiralIterator, Dimension size, OccupancyView view, int maxWidth, int buffer) {
        Dimension largerSize = new Dimension(size.width + 2 * buffer, size.height + 2 * buffer);
        while (spiralIterator.getRadius() < maxWidth) {
            if (!Occupancy.isOccupied(view, spiralIterator.x, spiralIterator.y, largerSize)) {
                return new Point(spiralIterator.x + buffer, spiralIterator.y + buffer);
            }
            spiralIterator.next();
        }
        return null;
    }

    public static class CyclingIterator<T> implements Iterator<T> {
        T[] ts;
        int index;

        public CyclingIterator(T[] t) {
            this.ts = t;
            index = 0;
        }

        @Override
        public boolean hasNext() {
            return true;
        }

        @Override
        public T next() {
            T ret = ts[index++];
            if (index >= ts.length) index = 0;
            return ret;
        }

        public void resetIndex() {
            index = 0;
        }
    }


    private static final int[][] DIRECTIONS = new int[][] {
            {1, 0},
            {0, 1},
            {-1, 0},
            {0, -1}
    };
    public static class SpiralIterator {
        public int x;
        public int y;

        private int cDirection;
        private int progress = 0;
        private int currentLength = 1;


        public SpiralIterator(Point start) {
            this.x = start.x;
            this.y = start.y;
        }

        public void next() {
            x += DIRECTIONS[cDirection][0];
            y += DIRECTIONS[cDirection][1];

            if (++progress < currentLength)
                return;
            progress = 0;
            if (++cDirection >= DIRECTIONS.length)
                cDirection = 0;
            if (cDirection == 0 || cDirection == 2)
                ++currentLength;
        }

        public int getRadius() {
            return currentLength;
        }
    }

    private static void print(boolean[][] bss) {
        for (boolean[] bs : bss) {
            for (boolean b : bs)
                System.out.print(b ? "1" : "0");
            System.out.println();
        }
        System.out.println("========================================");
    }
    public static void main(String[] args) {
        SpiralIterator spiralIterator = new SpiralIterator(new Point(5, 5));

        boolean[][] bss = new boolean[11][11];
        print(bss);
        while (spiralIterator.x < 11 && spiralIterator.x >= 0 && spiralIterator.y < 11 && spiralIterator.y >= 0) {
            bss[spiralIterator.x][spiralIterator.y] = true;
            print(bss);
            spiralIterator.next();
        }
    }

    public static class IndexIterator implements Iterator<int[]> {
        private int[] maxs;
        private int[] current;
        private boolean rev;

        public IndexIterator(int[] upper) {
            this(upper, false);
        }
        public IndexIterator(int[] upper, boolean rev) {
            this.rev = rev;
            this.maxs = Arrays.copyOf(upper, upper.length);
            this.current = new int[upper.length];
            this.current[current.length - 1] = -1;
        }


        @Override
        public boolean hasNext() {
            for (int i = 0; i < maxs.length; i++) {
                if (current[i] < maxs[i] - 1) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public int[] next() {
            int idx = rev ? 0 : current.length - 1;
            int inc = rev ? 1 : -1;
            while (idx >= 0 && idx < current.length && current[idx] == maxs[idx] - 1) {
                current[idx] = 0;
                idx += inc;
            }
            if (idx < 0 || idx >= current.length) {
                throw new IllegalStateException("Called next while hasNext() is false");
            }
            current[idx]++;
            return Arrays.copyOf(current, current.length);
        }

        @Override
        public void remove() {
            throw new RuntimeException("Since when was this a method I had to implement?");
        }
    }

    public static double zin(Integer i) {
        if (i == null) return 0;
        return i;
    }

    public static double zin(Double d) {
        if (d == null) return 0.0;
        return d;
    }

    public static boolean fin(Boolean b) {
        if (b == null) return false;
        return b;
    }
}
