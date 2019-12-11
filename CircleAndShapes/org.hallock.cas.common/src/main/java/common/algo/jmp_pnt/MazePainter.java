package common.algo.jmp_pnt;

import common.util.Bounds;
import common.util.DPoint;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.*;
import java.util.List;

public class MazePainter {

    public static class MazePanel extends JPanel {

        boolean[][] marked;
        Collection<Point> closedSet = Collections.emptyList();
        List<DPoint> solution = Collections.emptyList();
        private Point begin;
        private Point end;

        void setMaze(MazeCreator.Maze maze) {
            marked = maze.marked;
        }

        void setSolution(List<DPoint> solution) {
            if (solution == null) return;
            this.solution = solution;
        }

        void setClosedSet(Collection<Point> solution) {
            this.closedSet = solution;
        }

        public void paint(Graphics gr) {
            Graphics2D g = (Graphics2D) gr;

            int w = getWidth();
            int h = getHeight();

            g.setColor(Color.white);
            g.fillRect(0, 0, w, h);

            int pptx = w / marked.length;
            int ppty = h / marked[0].length;

            g.setColor(Color.black);
            for (int i = 0; i < marked.length; i++) {
                for (int j = 0; j < marked[i].length; j++) {
                    if (!marked[i][j])
                        continue;
                    g.fillRect(i * pptx, j * ppty, pptx, ppty);
                }
            }
            g.setColor(Color.yellow);
            for (Point p : closedSet)
                g.fillRect((int)p.x * pptx, (int)p.y * ppty, pptx, ppty);
            g.setColor(Color.blue);
            for (DPoint p : solution)
                g.fillRect((int)p.x * pptx, (int) p.y * ppty, pptx, ppty);
            if (begin != null) {
                g.setColor(Color.red);
                g.fillRect(begin.x * pptx, begin.y * ppty, pptx, ppty);
            }
            if (end != null) {
                g.setColor(Color.green);
                g.fillRect(end.x * pptx, end.y * ppty, pptx, ppty);
            }
        }

        public void setBegin(Point point) {
            this.begin = point;
        }

        public void setEnd(Point point) {
            this.end = point;
        }
    }

    public static MazePanel showMaze() {
        JFrame frame = new JFrame("Maze");
        MazePanel mp = new MazePanel();
        frame.setContentPane(mp);
        frame.setBounds(50, 50, 500, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        mp.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent componentEvent) {
                mp.repaint();
            }
        });
        return mp;
    }


    public static void main(String[] args) {
        Random random = new Random(1778);

        int w = 7 * 19;
        int h = 7 * 10;

        int bx, by, ex, ey;
        bx = by = 0;
        ex = 2 * (w / 2 - 1);
        ey = 2 * (h / 2 - 1);

        MazeCreator.Maze denseMaze;
        if (false) {
           denseMaze = MazeCreator.createRandomMaze(random, w, h, bx, by, ex, ey);
        } else {
            denseMaze = MazeCreator.createDenseMaze(random, w, h, bx, by, ex, ey);
        }
        MazePanel mp = showMaze();

        mp.setMaze(denseMaze);
        mp.setBegin(new Point(bx, by));
        mp.setEnd(new Point(ex, ey));

        HashSet<Integer> closedSet = new HashSet<>();
        mp.setSolution(JumpPointSearch.search(denseMaze, w, h, Collections.singleton(new Point(bx, by)), Collections.singleton(new Point(ex, ey)), closedSet, Bounds.None, null).points);
        mp.setClosedSet(toPoints(closedSet, h));
        mp.repaint();
    }

    public static Collection<Point> toPoints(Collection<Integer> closedSet, int h) {
        List<Point> l = new LinkedList<>();
        for (Integer ndx : closedSet)
            l.add(new Point(ndx / h, ndx % h));
        return l;
    }
}
