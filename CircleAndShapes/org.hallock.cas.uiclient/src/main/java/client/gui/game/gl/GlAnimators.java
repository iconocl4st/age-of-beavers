// https://raw.githubusercontent.com/takuyozora/g43g3/master/src/JOGL2Nehe06Texture.java

package client.gui.game.gl;

import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.FPSAnimator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * NeHe Lesson #6 (JOGL 2 Port): Texture
 * @author Hock-Chuan Chua
 * @version May 2012
 */
@SuppressWarnings("serial")
public class GlAnimators extends GLCanvas {

    private static final int CANVAS_WIDTH = 800;
    private static final int CANVAS_HEIGHT = 800;
    private static final int FPS = 60;

    /** The entry main() method to setup the top-level container and animator */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GLCanvas canvas = new GLCanvas();
            canvas.addGLEventListener(new GlPainter2());
            canvas.setPreferredSize(new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT));

            final FPSAnimator animator = new FPSAnimator(canvas, FPS, true);

            final JFrame frame = new JFrame();
            frame.getContentPane().add(canvas);
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    new Thread(() -> {
                        if (animator.isStarted()) animator.stop();
                        System.exit(0);
                    }).start();
                }
            });
            frame.setTitle("NeHe Lesson #6: Texture");
            frame.pack();
            frame.setVisible(true);
            animator.start();
        });
    }
}