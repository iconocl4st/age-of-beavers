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
import common.state.spec.CreationSpec;
import common.state.spec.EntitySpec;
import common.state.spec.GameSpec;
import common.util.DPoint;
import common.util.Profiler;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.TimerTask;

public class GlGameScreen {
    public Focuser focuser;
    public GoToListener goToListener;
    public ContextKeyManager contextKeyListener;


    private Component canvas;
    private FPSAnimator animator;
    private BuildingPlacer placer;
    private CommandListener commander;

    public void queryBuildingLocation(CreationSpec spec) {
        placer.setBuilding(spec);
        commander.setCommand(null);
        SwingUtilities.invokeLater(() -> {
            canvas.requestFocus();
            canvas.requestFocusInWindow();
        });
    }

    public void setCurrentCommand(Command command) {
        placer.setBuilding(null);
        commander.setCommand(command);
    }

    public void clearCurrentCommand() {
        setCurrentCommand(null);
    }

    public Component getCanvas() {
        return canvas;
    }

    public void startAnimating() {
        animator.start();
    }

    public void stopAnimating() {
        if (animator.isAnimating()) animator.stop();
    }


    public static GlGameScreen createGlGameScreen(UiClientContext context, GameSpec spec, Point playerStart) {
        GlGameScreen glGameScreen = new GlGameScreen();

        GamePainter.RenderContext renderContext = new GamePainter.RenderContext();
        renderContext.context = context;
        renderContext.gameWidth = spec.width;
        renderContext.gameHeight = spec.height;

        GlZoom glZoom = new GlZoom();
        glGameScreen.focuser = glZoom;

        glGameScreen.contextKeyListener = new ContextKeyManager(context.executorService);
        GlRectangleListener recListener = new GlRectangleListener(context.executorService);
        GlPressListener pressListener = new GlPressListener(context.executorService, glGameScreen.contextKeyListener);
        Profiler profiler = new Profiler("rendering time");
        glGameScreen.goToListener = new GoToListener(glGameScreen.focuser);
        glGameScreen.placer = new BuildingPlacer(context);
        HotkeyListener hotkeyListener = new HotkeyListener(context);
        SelectedListener selectedListener = new SelectedListener(context);
        GlZoomListener glZoomListener = new GlZoomListener(glZoom);
        ControlGroupListener controlGroupListener = new ControlGroupListener(context.selectionManager, glGameScreen.contextKeyListener, glZoom);
        SelectionListener selectionListener = new SelectionListener(context, () -> new Rectangle2D.Double(
                glZoom.screenLowerX,
                glZoom.screenLowerY,
                glZoom.screenUpperX - glZoom.screenLowerX,
                glZoom.screenUpperY - glZoom.screenLowerY
        ));

        GLProfile.initSingleton();
        GLProfile profile = GLProfile.get(GLProfile.GL2);
        GLCapabilities capabilities = new GLCapabilities(profile);
        GLCanvas canvas = new GLCanvas(capabilities);
        glGameScreen.canvas = canvas;
        TextureCache textureCache = new TextureCache(profile);
        GlPainter painter = new GlPainter(
                textureCache,
                glZoom,
                recListener,
                pressListener,
                glZoomListener,
                profiler,
                glGameScreen.placer,
                spec,
                renderContext
        );

        canvas.setFocusable(true);
        canvas.setPreferredSize(new Dimension(1, 1));
        canvas.setMinimumSize(new Dimension(1, 1));

        GlMouseTracker mouseTracker = new GlMouseTracker(painter);
        GrabFocusListener grabFocusListener = new GrabFocusListener(canvas);
        canvas.addMouseMotionListener(mouseTracker);
        canvas.addGLEventListener(painter);
        canvas.addMouseWheelListener(glZoomListener);
        canvas.addMouseMotionListener(glZoomListener);
        canvas.addMouseListener(glZoomListener);
        canvas.addMouseListener(recListener);
        canvas.addMouseMotionListener(recListener);
        canvas.addMouseListener(pressListener);
        canvas.addKeyListener(glGameScreen.goToListener);
        canvas.addKeyListener(glGameScreen.contextKeyListener);
        canvas.addKeyListener(selectedListener);
        canvas.addKeyListener(hotkeyListener);
        canvas.addMouseListener(grabFocusListener);
        canvas.addKeyListener(controlGroupListener);
        canvas.addKeyListener(glGameScreen.placer);

        context.selectionManager.addListener(glGameScreen.goToListener);

        recListener.addRectangleListener(selectionListener);
        pressListener.addPressListener(selectionListener);
        pressListener.addPressListener(glGameScreen.placer);


        if (context.clientGameState.isSpectating()) {
            glGameScreen.commander = new CommandListener(
                    context,
                    new UnitToUnitAction[0],
                    new UnitToLocationAction[0]
            );
        } else {
            glGameScreen.commander = new CommandListener(
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
        pressListener.addPressListener(glGameScreen.commander);

        if (playerStart != null) {
            glGameScreen.focuser.focusOn(new DPoint(playerStart));
        }

        new java.util.Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                System.out.println(profiler.report());
                profiler.reset();
            }
        }, 10000, 10000);

        glGameScreen.animator = new FPSAnimator(canvas, GlConstants.FPS, true);

        return glGameScreen;
    }
}
