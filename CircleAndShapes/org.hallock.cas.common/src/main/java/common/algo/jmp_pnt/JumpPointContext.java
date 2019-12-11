package common.algo.jmp_pnt;

import common.CommonConstants;
import common.state.sst.OccupancyView;
import common.util.Bounds;

import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;

class JumpPointContext {
    int w;
    int h;

    Bounds bounds;

    HashSet<Integer> goals;

    HashMap<Integer, Double> fscores;
    HashMap<Integer, Double> gscores;
    HashSet<Integer> closedSet;
    PriorityQueue<JpsNode> openSet;

    RequiredSearches requiredSearches;
    ObstacleFinder obstacleFinder;
    OccupancyView view;

    HashMap<Integer, String> debugInfo;

    void append(int x, int y, String str) {
        if (!CommonConstants.PAINT_SEARCH_DEBUG) return;
        debugInfo.put(x * h + y, debugInfo.getOrDefault(x * h + y, "") + str + ",");
    }
}
