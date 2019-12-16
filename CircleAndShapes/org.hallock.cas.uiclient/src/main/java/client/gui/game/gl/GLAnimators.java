// Taken from https://www3.ntu.edu.sg/home/ehchua/programming/opengl/JOGL2.0.html

package client.gui.game.gl;

import com.jogamp.nativewindow.AbstractGraphicsDevice;
import com.jogamp.newt.Screen;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLDrawableFactory;
import com.jogamp.opengl.GLProfile;
import common.util.ExecutorServiceWrapper;

import java.awt.*;
import java.util.Collection;
import java.util.concurrent.Executors;

@SuppressWarnings("serial")
public class GLAnimators {
    private static String TITLE = "JOGL 2.0 Setup (GLCanvas)";
    private static final int CANVAS_WIDTH = 640;
    private static final int CANVAS_HEIGHT = 480;

    static ExecutorServiceWrapper serviceWrapper = new ExecutorServiceWrapper(Executors.newFixedThreadPool(2));

    public static void main(String[] args) {

            GLProfile profile = GLProfile.get(GLProfile.GL2);
            GLCapabilities capabilities = new GLCapabilities(profile);

        Collection<Screen> allScreens = Screen.getAllScreens();
        System.out.println(allScreens.size());

        GLDrawableFactory desktopFactory = GLDrawableFactory.getDesktopFactory();
        AbstractGraphicsDevice defaultDevice = desktopFactory.getDefaultDevice();
        System.out.println(defaultDevice);



        Window[] windows = Window.getWindows();
        System.out.println(windows.length);
        for (Window window : windows) {
            System.out.println(window);
        }
//        SwingUtilities.invokeLater(() -> {
//            GlZoom glZoom = new GlZoom();
//
//            GlRectangleListener recListener = new GlRectangleListener(serviceWrapper);
//            GlPressListener pressListener = new GlPressListener(serviceWrapper);
//            GlZoomListener glZoomListener = new GlZoomListener(glZoom);
//            Profiler profiler = new Profiler();
//
//            GLProfile profile = GLProfile.get(GLProfile.GL2);
//            GLCapabilities capabilities = new GLCapabilities(profile);
//
//
//            GLCanvas canvas = new GLCanvas(capabilities);
//            TextureCache textureCache = new TextureCache(profile);
//            GlPainter painter = new GlPainter(
//                    textureCache,
//                    glZoom,
//                    recListener,
//                    pressListener,
//                    glZoomListener,
//                    profiler,
//                    placer
//            );
//
//            canvas.addGLEventListener(painter);
//            canvas.addMouseWheelListener(glZoomListener);
//            canvas.addMouseMotionListener(glZoomListener);
//            canvas.addMouseListener(glZoomListener);
//
//            canvas.addMouseListener(recListener);
//            canvas.addMouseMotionListener(recListener);
//            canvas.addMouseListener(pressListener);
//
//            recListener.addRectangleListener((xBegin, yBegin, xEnd, yEnd) -> {
//                System.out.println("Selected " + xBegin + ", " + yBegin + ", " + xEnd + ", " +  yEnd);
//            });
//            pressListener.addPressListener((x, y, info) -> {
//                System.out.println("Pressed " + x + ", " + y);
//            });
//
//            canvas.setPreferredSize(new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT));
//            final FPSAnimator animator = new FPSAnimator(canvas, GlConstants.FPS, true);
//
//            final JFrame frame = new JFrame();
//            frame.getContentPane().add(canvas);
//            frame.addWindowListener(new WindowAdapter() {
//                @Override
//                public void windowClosing(WindowEvent e) {
//                    // Use a dedicate thread to run the stop() to ensure that the
//                    // animator stops before program exits.
//                    new Thread() {
//                        @Override
//                        public void run() {
//                            if (animator.isStarted()) animator.stop();
//                            System.exit(0);
//                        }
//                    }.start();
//                }
//            });
//            frame.setTitle(TITLE);
//            frame.pack();
//            frame.setVisible(true);
//            animator.start();
//        });
    }
}
