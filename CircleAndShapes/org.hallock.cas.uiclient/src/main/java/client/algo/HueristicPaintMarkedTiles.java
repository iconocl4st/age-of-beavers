package client.algo;

import common.util.BitArray;
import common.util.Marked;

import java.util.LinkedList;

public class HueristicPaintMarkedTiles {
    // This algorithm does not find the minimum number of rectangles, but it should probably reduce the number quite a bit

    public interface RectangleReceiver {
        void markedRectangle(int xb, int yb, int xe, int ye);
        void unMarkedRectangle(int xb, int yb, int xe, int ye);
    }

    private static final class Job {
        final int xb, yb, xe, ye;
        Job(int xb, int yb, int xe, int ye) {
            this.xb = xb; this.yb = yb; this.xe = xe; this.ye = ye;
        }
        public String toString() {
            return "[" + xb + ", " + yb + "][" + xe + ", " + ye + "]";
        }
    }

    public static void enumerateRectangles(Marked marked, RectangleReceiver painter) {
        LinkedList<Job> remainingJobs = new LinkedList<>();
        remainingJobs.add(new Job(0, 0, marked.getWidth(), marked.getHeight()));
        while (!remainingJobs.isEmpty()) {
            enumerateRectangles(marked, painter, remainingJobs.removeFirst(), remainingJobs);
        }
    }

    public static void enumerateRectangles(Marked marked, RectangleReceiver painter, int xb, int yb, int xe, int ye) {
        LinkedList<Job> remainingJobs = new LinkedList<>();
        remainingJobs.add(new Job(xb, yb, xe, ye));
        while (!remainingJobs.isEmpty()) {
            enumerateRectangles(marked, painter, remainingJobs.removeFirst(), remainingJobs);
        }
    }

    private static void enumerateRectangles(Marked view, RectangleReceiver painter, Job j, LinkedList<Job> remainingJobs) {
        if (j.xb >= j.xe) return;
        if (j.yb >= j.ye) return;
        boolean shouldPaint = view.get(j.xb, j.yb);
        int cxe = j.xb + 1;
        int cye = j.yb + 1;
        boolean canExpandX = true;
        boolean canExpandY = true;
        while (canExpandX || canExpandY) {
            canExpandY &= cye < j.ye;
            for (int x = j.xb; x < cxe && canExpandY; x++) {
                if (view.get(x, cye) != shouldPaint) {
                    canExpandY = false;
                }
            }
            if (canExpandY) {
                cye += 1;
            }
            canExpandX &= cxe < j.xe;
            for (int y = j.yb; y < cye && canExpandX; y++) {
                if (view.get(cxe, y) != shouldPaint) {
                    canExpandX = false;
                }
            }
            if (canExpandX) {
                cxe += 1;
            }
        }
        if (shouldPaint) {
            painter.markedRectangle(j.xb, j.yb, cxe, cye);
        } else {
            painter.unMarkedRectangle(j.xb, j.yb, cxe, cye);
        }
        if (j.xb < cxe && cye < j.ye) remainingJobs.add(new Job(j.xb, cye, cxe, j.ye));
        if (cxe < j.xe) remainingJobs.add(new Job(cxe, j.yb, j.xe, j.ye));
    }


    public static void main(String[] args) {
        // todo: move to tests
        int size = 3;
        BitArray view = new BitArray(size, size);
        RectangleReceiver painter = new RectangleReceiver() {
            @Override
            public void markedRectangle(int xb, int yb, int xe, int ye) {
                System.out.println("M: " + new Job(xb, yb, xe, ye).toString());
            }
            @Override
            public void unMarkedRectangle(int xb, int yb, int xe, int ye) {
                System.out.println("U: " + new Job(xb, yb, xe, ye).toString());
            }
        };

        for (int j = size - 1; j >= 0; j--) {
            for (int i = 0; i < size; i++) {
                System.out.print(view.get(i, j) ? "t" : "f");
            }
            System.out.println();
        }
        enumerateRectangles(view, painter);
        System.out.println("-------------------------------");

        view.set(0, 0, true);
        view.set(0, 1, true);
        view.set(0, 2, true);
        view.set(1, 0, true);
        view.set(1, 1, true);
        view.set(1, 2, true);
        view.set(2, 0, true);
        view.set(2, 1, true);
        view.set(2, 2, true);

        for (int j = size - 1; j >= 0; j--) {
            for (int i = 0; i < size; i++) {
                System.out.print(view.get(i, j) ? "t" : "f");
            }
            System.out.println();
        }
        enumerateRectangles(view, painter);
        System.out.println("-------------------------------");

        view.set(0, 0, false);
        view.set(0, 1, false);
        view.set(0, 2, false);
        view.set(1, 0, false);
        view.set(1, 1, false);
        view.set(1, 2, false);
        view.set(2, 0, false);
        view.set(2, 1, false);
        view.set(2, 2, true);

        for (int j = size - 1; j >= 0; j--) {
            for (int i = 0; i < size; i++) {
                System.out.print(view.get(i, j) ? "t" : "f");
            }
            System.out.println();
        }
        enumerateRectangles(view, painter);
        System.out.println("-------------------------------");

        view.set(0, 0, true);
        view.set(0, 1, true);
        view.set(0, 2, true);
        view.set(1, 0, true);
        view.set(1, 1, true);
        view.set(1, 2, true);
        view.set(2, 0, true);
        view.set(2, 1, true);
        view.set(2, 2, false);

        for (int j = size - 1; j >= 0; j--) {
            for (int i = 0; i < size; i++) {
                System.out.print(view.get(i, j) ? "t" : "f");
            }
            System.out.println();
        }
        enumerateRectangles(view, painter);
        System.out.println("-------------------------------");

        view.set(0, 0, true);
        view.set(0, 1, true);
        view.set(0, 2, true);
        view.set(1, 0, true);
        view.set(1, 1, false);
        view.set(1, 2, true);
        view.set(2, 0, true);
        view.set(2, 1, true);
        view.set(2, 2, true);

        for (int j = size - 1; j >= 0; j--) {
            for (int i = 0; i < size; i++) {
                System.out.print(view.get(i, j) ? "t" : "f");
            }
            System.out.println();
        }
        enumerateRectangles(view, painter);
        System.out.println("-------------------------------");


        view.set(0, 0, false);
        view.set(0, 1, true);
        view.set(0, 2, false);
        view.set(1, 0, true);
        view.set(1, 1, false);
        view.set(1, 2, true);
        view.set(2, 0, false);
        view.set(2, 1, true);
        view.set(2, 2, false);

        for (int j = size - 1; j >= 0; j--) {
            for (int i = 0; i < size; i++) {
                System.out.print(view.get(i, j) ? "t" : "f");
            }
            System.out.println();
        }
        enumerateRectangles(view, painter);
        System.out.println("-------------------------------");


        view.set(0, 0, true);
        view.set(0, 1, false);
        view.set(0, 2, true);
        view.set(1, 0, false);
        view.set(1, 1, true);
        view.set(1, 2, false);
        view.set(2, 0, true);
        view.set(2, 1, false);
        view.set(2, 2, true);

        for (int j = size - 1; j >= 0; j--) {
            for (int i = 0; i < size; i++) {
                System.out.print(view.get(i, j) ? "t" : "f");
            }
            System.out.println();
        }
        enumerateRectangles(view, painter);
        System.out.println("-------------------------------");
    }
}
