package client.gui.game;

import java.awt.*;

public final class Colors {
    public static final Color WATER = new Color(85, 67, 219);
    public static final Color GRASS = new Color(47, 119, 50) ;
    public static final Color DESERT = new Color(163, 184, 121); // 192, 219, 136
    public static final Color UNEXPLORED = Color.black;
    public static final Color NOT_VISIBLE = new Color(35, 83, 39);
    public static final Color GRID_LINES = new Color(140, 165, 100); // 40, 110, 40);
    public static final Color CANNOT_PLACE = new Color(219, 124, 79);
    public static final Color CAN_PLACE = new Color(76, 97, 80);
    public static final Color MAP_BOUNDARY = Color.white;
    public static final Color BACKGROUND = new Color(88, 84, 160 );
    public static final Color DEPOSIT = Color.green;
    public static final Color COLLECT = Color.pink;
    public static final Color ATTACK = Color.red;
    public static final Color BUILD = Color.blue;
    public static final Color GATHER_POINT = new Color(243, 191, 38);

    public static final Color[] PLAYER_COLORS = new Color[] {
            Color.gray,
            Color.blue,
            Color.red,
            Color.yellow,
            Color.green,
            Color.pink,
    };
    public static final Color MINIMAP_TREE = CAN_PLACE;
}
