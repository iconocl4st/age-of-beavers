import client.gui.game.Zoom;
import client.gui.mouse.ZoomListener;

import javax.swing.*;
import java.awt.*;
import java.util.HashSet;
import java.util.Random;

public class MakeSomeWater {
    static Zoom zoom;

    static int width = 500;
    static int height = 500;
    static int[][] depths = new int[width][height];


    private static final class PanelDisplayer extends JPanel {
        public void paint(Graphics g) {
            int w = getWidth();
            int h = getHeight();


            g.setColor(Color.blue);
            g.fillRect(0, 0, w, h);

            for (int i = 0; i < depths.length; i++) {
                for (int j = 0; j < depths[i].length; j++) {
                    int x1 = zoom.mapGameToScreenX(i);
                    int y1 = zoom.mapGameToScreenY(j);
                    int x2 = zoom.mapGameToScreenX(i + 1);
                    int y2 = zoom.mapGameToScreenY(j + 1);

                    g.setColor(new Color(0, 0, depths[i][j]));
                    g.fillRect(x1, y2, x2 - x1, y1 - y2);
                }
            }
        }
    }

    public static void main(String[] args) {
        Random random = new Random();

        JFrame frame = new JFrame("Testing");
        frame.setBounds(50, 50, 1000, 1000);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel panel = new PanelDisplayer();
        zoom = new Zoom(panel, null);
        frame.setContentPane(panel);
        frame.setVisible(true);

        createDepths(random);


        zoom.initialize(width, height);

        ZoomListener zoomListener = new ZoomListener(zoom, panel);
        panel.addMouseListener(zoomListener);
        panel.addMouseMotionListener(zoomListener);
        panel.addMouseWheelListener(zoomListener);
    }


    private static void createRandomDepths(Random random) {
        for (int i = 0; i < depths.length; i++) {
            for (int j = 0; j < depths[i].length; j++) {
                depths[i][j] = random.nextInt(255);
            }
        }
    }

    private static void createDepths(Random random) {
        for (int i = 0; i < depths.length; i++) {
            for (int j = 0; j < depths[i].length; j++) {
                depths[i][j] = 0;
            }
        }

        int x = 0;
        int y = height / 2; // random.nextInt(height);

        int dx = 1;
        int dy = 0;

        HashSet<Point> riverPoints = new HashSet<>();

        for (int i = 0; i < 5; i++) {
            while (inBounds(x, y)) {
                if (random.nextDouble() < 0.1)
                    dy = random.nextInt(3) - 1;

                depths[x][y] = 255;
                riverPoints.add(new Point(x, y));

                x += dx;
                y += dy;
            }
        }
    }

    private static boolean inBounds(int x, int y) {
        return 0 <= x && x < width && 0 <= y && y < height;
    }
}
