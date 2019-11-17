package client.gui.game;

import client.app.ClientContext;
import client.gui.actions.unit_action.*;
import client.gui.keys.*;
import client.gui.mouse.*;
import common.state.spec.EntitySpec;
import common.state.spec.GameSpec;

import javax.swing.*;
import java.awt.*;


public class GameScreen extends JPanel {
    private final ClientContext context;

    public Zoom zoom;
    public ContextKeyManager contextKeyListener;
    public GoToListener goToListener;

    private GamePainter renderer;

    private BuildingPlacer placer;
    private CommandListener commander;
    private SelectedListener selector;
    private HotkeyListener hotkeyListener;
    private ZoomListener zoomListener;
    private GrabFocusListener focusListener;
    private ControlGroupListener controlGroupListener;
    private SelectionListener selectionListener;


    public GameScreen(ClientContext context) {
        this.context = context;

    }

    public void setGameSpec(GameSpec spec) {
        zoom.initialize(spec);
        renderer.initialize(spec);
    }


    public void queryBuildingLocation(EntitySpec spec) {
        placer.setBuilding(spec);
        commander.setCommand(null);
        selectionListener.removeSelection();
        SwingUtilities.invokeLater(() -> {
            requestFocus();
            requestFocusInWindow();
            grabFocus();
        });
    }

    public void setCurrentCommand(Command command) {
        placer.setBuilding(null);
        commander.setCommand(command);
    }

    public void clearCurrentCommand() {
        setCurrentCommand(null);
    }


    public void paintComponent(Graphics graphics) {
        renderer.renderGame((Graphics2D) graphics, zoom);
    }


    public static GameScreen createGameScreen(final ClientContext context) {
        final GameScreen gs = new GameScreen(context);
        gs.setFocusable(true);
        gs.setRequestFocusEnabled(true);

        gs.zoom = new Zoom(gs, context);


//        Container container = gs;
//        while (container != null) {
//            gs.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, Collections.emptySet());
//            gs.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, Collections.emptySet());
//            gs.setFocusTraversalKeysEnabled(false);
//            container = container.getParent();
//        }

        gs.goToListener = new GoToListener(context);
        gs.addKeyListener(gs.goToListener);
        context.selectionManager.addListener(gs.goToListener);

        gs.contextKeyListener = new ContextKeyManager(context);
        gs.addKeyListener(gs.contextKeyListener);

        gs.placer = new BuildingPlacer(context,  gs.zoom);
        gs.addMouseMotionListener(gs.placer);
        gs.addMouseListener(gs.placer);

        gs.commander = new CommandListener(
                gs.zoom,
                context,
                new UnitToUnitAction[] {
                        new Gather(context),
                        new Hunt(context),
                        new Ride(context),
                        new Build(context),
                        new Deliver(context)
                },
                new UnitToLocationAction[] {
                        new Move(context),
                        new SetGatherPoint(context)
                }
        );
        gs.addMouseListener(gs.commander);

        gs.selector = new SelectedListener(context);
        gs.addKeyListener(gs.selector);

        gs.hotkeyListener = new HotkeyListener(context);
        gs.addKeyListener(gs.hotkeyListener);

        gs.zoomListener = new ZoomListener(gs.zoom, gs);
        gs.addMouseListener(gs.zoomListener);
        gs.addMouseMotionListener(gs.zoomListener);
        gs.addMouseWheelListener(gs.zoomListener);

        gs.focusListener = new GrabFocusListener(gs);
        gs.addMouseListener(gs.focusListener);

        gs.controlGroupListener = new ControlGroupListener(context);
        gs.addKeyListener(gs.controlGroupListener);

        gs.selectionListener = new SelectionListener(context, new RectangleListener(), gs.zoom, gs);
        gs.addMouseListener(gs.selectionListener);
        gs.addMouseMotionListener(gs.selectionListener);
        gs.selectionListener.removeSelection();

        gs.renderer = new GamePainter(context, gs.selectionListener, gs.placer, gs);

        return gs;
    }
}
