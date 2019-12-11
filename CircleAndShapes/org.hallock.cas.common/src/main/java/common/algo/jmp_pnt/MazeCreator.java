package common.algo.jmp_pnt;

import common.algo.UnionFind2d;
import common.state.sst.OccupancyView;

import java.awt.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;

public class MazeCreator {

    static class Maze implements OccupancyView {
        boolean[][] marked;

        Maze(int x, int y) {
            marked = new boolean[x][y];
        }

        public int getNumSets(UnionFind2d uf) {
            uf.clear();
            for (int i = 0; i < marked.length - 1; i++) {
                for (int j = 0; j < marked[i].length - 1; j++) {
                    if (marked[i][j])
                        continue;
                    if (!marked[i][j + 1])
                        uf.connect(i, j, i, j + 1);
                    if (!marked[i + 1][j])
                        uf.connect(i, j, i + 1, j);
                }
            }
            int w = marked.length - 1;
            int h = marked[0].length - 1;
            if (!marked[w][h]) {
                if (!marked[w-1][h])
                    uf.connect(w, h, w - 1, h);
                if (!marked[w][h-1])
                    uf.connect(w, h, w, h - 1);
            }


            HashSet<Integer> sets = new HashSet<>();
            for (int i = 0; i < marked.length; i += 2) {
                for (int j = 0; j < marked[i].length; j += 2) {
                    sets.add(uf.getRoot(i, j));
                }
            }
            return sets.size();
        }

        @Override
        public boolean isOccupied(int x, int y) {
            try {
                return marked[x][y];
            } catch (Exception e) {
                throw new IndexOutOfBoundsException();
            }
        }
    }


    private static LinkedList<Point> getMarkable(int w, int h, int bx, int by, int ex, int ey) {
        LinkedList<Point> unmarked = new LinkedList<>();
        for (int i = 0; i < w; i++)
            for (int j = 0; j < h; j++) {
                if (i == bx && j == by)
                    continue;
                if (i == ex && j == ey)
                    continue;
                unmarked.add(new Point(i, j));
            }
        return unmarked;
    }

    static Maze createRandomMaze(Random random, int w, int h, int bx,  int by, int ex, int ey) {
        LinkedList<Point> unmarked = getMarkable(w, h, bx, by, ex, ey);
        Collections.shuffle(unmarked, random);

        Maze maze = new Maze(w, h);
        while (!unmarked.isEmpty()) {
            Point point = unmarked.removeLast();
            if (random.nextDouble() < 0.1)
                maze.marked[point.x][point.y] = true;
        }
        return maze;
    }


    static Maze createDenseMaze(Random random, int w, int h, int bx,  int by, int ex, int ey) {
        LinkedList<Point> unmarked = getMarkable(w, h, bx, by, ex, ey);
        Collections.shuffle(unmarked, random);

        UnionFind2d uf = new UnionFind2d(w, h);
        Maze maze = new Maze(w, h);
        int pNumSets = 1;
        while (!unmarked.isEmpty()) {
            Point point = unmarked.removeLast();
            maze.marked[point.x][point.y] = true;
           int numSets = maze.getNumSets(uf);
           if (numSets != pNumSets + 1) {
               maze.marked[point.x][point.y] = false;
           }
        }
        return maze;
    }
}
