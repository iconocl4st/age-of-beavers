package client.gui.game.gl;

import java.util.TreeMap;

class GlConstants {
    static final double INITIAL_Z = -6f;
    static final double ZOOM_SPEED = 0.1;
    static final double FOV_Y = 45d;
    static final int FPS = 60;


    static final double MAP_Z = 0.0;
    // unit z
    // resource z
    // unexplored z
    // line of sight z
    // debug z




    static final TreeMap<Double, Integer> SegmentsPerArc = new TreeMap<>();
    static final TreeMap<Double, Integer> SegmentsPerCircle = new TreeMap<>();
    static {
        SegmentsPerCircle.put(0d, 100);
        SegmentsPerCircle.put(-3d, 50);
        SegmentsPerCircle.put(-20d, 30);
        SegmentsPerCircle.put(-50d, 10);
        SegmentsPerCircle.put(-100d, 8);
        SegmentsPerCircle.put(-500d, 4);
    }
}
