package common.algo;

import common.util.DPoint;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.TreeSet;

public class ObstacleSearch {



    public static void findPath(DPoint begin, Dimension size, DPoint location) {
        TreeSet<Path> currentPaths = new TreeSet<>(Path.CMP);

        while (!currentPaths.isEmpty()) {
            Path path = currentPaths.pollFirst();
            path.navigateObstacles(currentPaths, size);
        }

        // fail
    }






    public static class Path {
        ArrayList<PathSegment> segments = new ArrayList<>();
        double currentDistance;

        double getDistance() {
            return currentDistance;
        }





        public static Comparator<Path> CMP = Comparator.comparingDouble(Path::getDistance);

        public void navigateObstacles(TreeSet<Path> currentPaths, Dimension size) {
        }
    }

    public static class PathSegment {
        DPoint begin;
        DPoint end;
        boolean clear;
    }
}
