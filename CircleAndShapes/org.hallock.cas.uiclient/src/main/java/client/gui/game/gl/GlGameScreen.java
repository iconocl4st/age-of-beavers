package client.gui.game.gl;

import client.app.UiClientContext;
import client.gui.actions.unit_action.*;
import client.gui.game.Command;
import client.gui.game.Focuser;
import client.gui.game.GamePainter;
import client.gui.keys.*;
import client.gui.mouse.BuildingPlacer;
import client.gui.mouse.CommandListener;
import client.gui.mouse.GrabFocusListener;
import client.gui.mouse.SelectionListener;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.FPSAnimator;
import common.state.spec.EntitySpec;
import common.state.spec.GameSpec;
import common.util.DPoint;
import common.util.Profiler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;
import java.util.TimerTask;

public class GlGameScreen {
    public Focuser focuser;
    public GoToListener goToListener;
    public ContextKeyManager contextKeyListener;

    private UiClientContext context;
    private FPSAnimator animator;
    private GlPainter painter;
    private Profiler profiler;
    private Component canvas;
    private GlRectangleListener recListener;
    private GlPressListener pressListener;
    private GlZoom glZoom;

    private BuildingPlacer placer;
    private CommandListener commander;
//    private SelectedListener selector;
//    private HotkeyListener hotkeyListener;
//    private ZoomListener zoomListener;
//    private GrabFocusListener focusListener;
//    private ControlGroupListener controlGroupListener;
//    private SelectionListener selectionListener;


    public Component getCanvas()  {
        return new JPanel();
//        return canvas;
    }

    public void run(GameSpec gameSpec) {
    }



    public void initialize(GameSpec spec, Point playerStart) {
//        zoom.initialize(spec, getWidth(),  getHeight());
//        renderer.initialize(spec);

        if (context.clientGameState.isSpectating()) {
            commander = new CommandListener(
                    context,
                    new UnitToUnitAction[0],
                    new UnitToLocationAction[0]
            );
        } else {
            commander = new CommandListener(
                    context,
                    new UnitToUnitAction[]{
                            new Gather(context),
                            new Hunt(context),
                            new Ride(context),
                            new Build(context),
                            new Deliver(context)
                    },
                    new UnitToLocationAction[]{
                            new Move(context),
                            new SetGatherPoint(context)
                    }
            );
        }
        pressListener.addPressListener(commander);

        run(spec);

        GamePainter.RenderContext renderContext = new GamePainter.RenderContext();
        renderContext.context = context;
        renderContext.gameWidth = spec.width;
        renderContext.gameHeight = spec.height;
        painter.renderContext = renderContext;

        if (playerStart != null) {
            focuser.focusOn(new DPoint(playerStart));
        }

        animator.start();

        new java.util.Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                System.out.println(profiler.report());
                profiler.reset();
            }
        }, 10000, 10000);
    }


    public void queryBuildingLocation(EntitySpec spec) {
        placer.setBuilding(spec);
        commander.setCommand(null);
        SwingUtilities.invokeLater(() -> {
//            canvas.requestFocus();
//            canvas.requestFocusInWindow();
        });
    }

    public void setCurrentCommand(Command command) {
        placer.setBuilding(null);
        commander.setCommand(command);
    }

    public void clearCurrentCommand() {
        setCurrentCommand(null);
    }



    public static GlGameScreen createGlGameScreen(UiClientContext context) {
        GlGameScreen glGameScreen = new GlGameScreen();
        glGameScreen.context = context;

        glGameScreen.glZoom = new GlZoom();
        glGameScreen.focuser = glGameScreen.glZoom;

        glGameScreen.contextKeyListener = new ContextKeyManager(context.executorService);
        glGameScreen.recListener = new GlRectangleListener(context.executorService);
        glGameScreen.pressListener = new GlPressListener(context.executorService, glGameScreen.contextKeyListener);
        glGameScreen.profiler = new Profiler("rendering time");
        glGameScreen.goToListener = new GoToListener(glGameScreen.focuser);
        glGameScreen.placer = new BuildingPlacer(context);
        HotkeyListener hotkeyListener = new HotkeyListener(context);
        SelectedListener selectedListener = new SelectedListener(context);
        GlZoomListener glZoomListener = new GlZoomListener(glGameScreen.glZoom);
        ControlGroupListener controlGroupListener = new ControlGroupListener(context.selectionManager, glGameScreen.contextKeyListener, glGameScreen.glZoom);
        SelectionListener selectionListener = new SelectionListener(context, () -> new Rectangle2D.Double(
                glGameScreen.glZoom.screenLowerX,
                glGameScreen.glZoom.screenLowerY,
                glGameScreen.glZoom.screenUpperX - glGameScreen.glZoom.screenLowerX,
                glGameScreen.glZoom.screenUpperY - glGameScreen.glZoom.screenLowerY
        ));





        GLProfile.initSingleton();
        GLProfile profile = GLProfile.get(GLProfile.GL2);
        GLCapabilities capabilities = new GLCapabilities(profile);
        GLCanvas canvas = new GLCanvas(capabilities);
        glGameScreen.canvas = canvas;
        TextureCache textureCache = new TextureCache(profile);
        glGameScreen.painter = new GlPainter(
                textureCache,
                glGameScreen.glZoom,
                glGameScreen.recListener,
                glGameScreen.pressListener,
                glZoomListener,
                glGameScreen.profiler,
                glGameScreen.placer
        );

        canvas.setFocusable(true);

        GlMouseTracker mouseTracker = new GlMouseTracker(glGameScreen.painter);
        GrabFocusListener grabFocusListener = new GrabFocusListener(canvas);
        canvas.addMouseMotionListener(mouseTracker);
        canvas.addGLEventListener(glGameScreen.painter);
        canvas.addMouseWheelListener(glZoomListener);
        canvas.addMouseMotionListener(glZoomListener);
        canvas.addMouseListener(glZoomListener);
        canvas.addMouseListener(glGameScreen.recListener);
        canvas.addMouseMotionListener(glGameScreen.recListener);
        canvas.addMouseListener(glGameScreen.pressListener);
        canvas.addKeyListener(glGameScreen.goToListener);
        canvas.addKeyListener(glGameScreen.contextKeyListener);
        canvas.addKeyListener(selectedListener);
        canvas.addKeyListener(hotkeyListener);
        canvas.addMouseListener(grabFocusListener);
        canvas.addKeyListener(controlGroupListener);

        context.selectionManager.addListener(glGameScreen.goToListener);


        glGameScreen.recListener.addRectangleListener(selectionListener);
        glGameScreen.pressListener.addPressListener(selectionListener);

        glGameScreen.canvas.setPreferredSize(new Dimension(800, 800));
        final JFrame frame = new JFrame();
        frame.getContentPane().add(canvas);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // Use a dedicate thread to run the stop() to ensure that the
                // animator stops before program exits.
                new Thread() {
                    @Override
                    public void run() {
                        if (glGameScreen.animator.isStarted()) glGameScreen.animator.stop();
                        System.exit(0);
                    }
                }.start();
            }
        });
        frame.setTitle("Testing");
        frame.pack();
        frame.setVisible(true);


        glGameScreen.animator = new FPSAnimator(canvas, GlConstants.FPS, true);

        return glGameScreen;
    }
}
