package client.gui.actions;

import client.ai.ai2.AiContext;
import client.app.UiClientContext;
import client.event.AiEventListener;
import client.state.SelectionManager;
import common.event.AiEvent;
import common.state.EntityReader;
import common.state.spec.GameSpec;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.*;
import java.util.List;

public class UnitActions extends JPanel implements SelectionManager.SelectionListener, AiEventListener {

    private final Object sync = new Object();

    private final UiClientContext context;

    private final Set<EntityReader> currentlySelected = new HashSet<>();
    private final List<CreatedButton> currentButtons = new LinkedList<>();
    private final LinkedList<StackItemWithArgs> stack = new LinkedList<>();
    private Actions actions;

    private final DemandsView demands;
    private final EvolutionView evolutionView;

    // make sure they all fit...
    private static final int NUM_ROWS = 3;
    private static final int NUM_COLS = 4;
    private static final String keys = "qwerasdfzxcv";
    private static final JPanel[] fillerPanels = new JPanel[NUM_ROWS * NUM_COLS];
    static { for (int i = 0; i < fillerPanels.length; i++) { fillerPanels[i] = new JPanel(); fillerPanels[i].setBorder(new EtchedBorder()); } }

    private final Runnable[][] currentHotkeys = new Runnable[NUM_ROWS][NUM_COLS]; // 5 might have to be changed

    private UnitActions(UiClientContext context) {
        this.context = context;
        stack.addLast(new StackItemWithArgs(StackItem.GlobalActions));
        demands = new DemandsView(context);
        evolutionView = new EvolutionView(context);
    }


    public void initialize(GameSpec spec) {
        actions = Actions.createActions(context);
        evolutionView.initialize(spec);
        demands.initialize(spec);
    }

    void push(StackItem item) {
        synchronized (sync) {
            stack.addLast(new StackItemWithArgs(item));
            drawCurrentButtons(true);
        }
    }

    void pushArg(String stackArg) {
        synchronized (sync) {
            stack.addLast(new StackItemWithArgs(stack.getLast(), stackArg));
            drawCurrentButtons(true);
        }
    }

    void pop() {
        synchronized (sync) {
            if (stack.size() > 1)
                stack.removeLast();
            if (stack.size() == 1)
                context.selectionManager.select(Collections.emptySet());
            drawCurrentButtons(true);
        }
    }
    public void popAll() {
        synchronized (sync) {
            stack.clear();
            stack.push(new StackItemWithArgs(StackItem.GlobalActions));
            context.selectionManager.select(Collections.emptySet());
            drawCurrentButtons(true);
        }
    }

    private JButton createCancelButton(char c) {
        JButton die = new JButton("("+ c + ") Back");
        die.addActionListener(actionEvent -> pop());
        return die;
    }

    private void removeCurrentButtons() {
        currentButtons.clear();
        for (int i = 0; i < currentHotkeys.length; i++)
            for (int j = 0; j < currentHotkeys[i].length; j++)
                currentHotkeys[i][j] = null;
        removeAll();
    }

    private void createButtons(Action[] actions, boolean createCancel) {
        removeCurrentButtons();
        int index = 0;
        int idx1 = 0; int idx2 = 0;
        if (createCancel) {
            add(createCancelButton(keys.charAt(index++)));
            currentHotkeys[idx1][idx2] = this::pop;
            ++idx2; if (idx2 >= NUM_COLS) { idx2 = 0; ++idx1; }
        }

        for (Action action : actions) {
            CreatedButton created = new CreatedButton();
            created.button = new JButton("(" + keys.charAt(index++) + ") " + action.label);
            created.button.addActionListener(e -> {
                action.run(currentlySelected);
                updateButtons();
            });
            created.action = action;
            currentButtons.add(created);
            add(created.button);

            currentHotkeys[idx1][idx2] = () -> {
                action.run(currentlySelected);
                updateButtons();
            };
            ++idx2; if (idx2 >= NUM_COLS) { idx2 = 0; ++idx1; }
            if (idx1 >= NUM_ROWS) throw new IllegalStateException("Adjust the constants here...");
        }
        for (; index < fillerPanels.length; index++) {
            add(fillerPanels[index]);
        }
    }

    public void runHotKey(int i, int j) {
        synchronized (sync) {
            if (currentHotkeys[i][j] == null) { System.out.println("null"); return; }
            currentHotkeys[i][j].run();
        }
    }

    private void updateButtons() {
        synchronized (sync) {
            for (CreatedButton createdButton : currentButtons) {
                createdButton.button.setEnabled(createdButton.action.isEnabled(currentlySelected));
            }
        }
    }

    private void drawCurrentButtons(boolean recreate, Action[] buttons, boolean drawCancel) {
        synchronized (sync) {
            setLayout(new GridLayout(0, NUM_COLS));
            if (recreate) {
                createButtons(buttons, drawCancel);
            }
            updateButtons();
        }
    }

    @Override
    public void receiveEvent(AiContext aiContext, AiEvent event) {
        drawCurrentButtons(false);
    }

    public void drawCurrentButtons(boolean recreate) {
        EventQueue.invokeLater(() -> {
            synchronized (sync) {
                if (recreate) removeAll();

                if (stack.isEmpty()) {
                    throw new RuntimeException("Should not be able to happen.");
                }

                StackItemWithArgs last = stack.getLast();
                switch (last.item) {
                    case Duplicate:
                        drawCurrentButtons(recreate, actions.duplicates, true);
                        break;
                    case SingleUnit:
                        drawCurrentButtons(recreate, actions.singleUnitActions, true);
                        break;
                    case Pickup:
                        drawCurrentButtons(recreate, actions.pickupActions, true);
                        break;
                    case Create:
                        drawCurrentButtons(recreate, actions.getCreateButtons(context, currentlySelected.iterator().next(), last.args), true);
                        break;
                    case Craft:
                        drawCurrentButtons(recreate, actions.getCraftButtons(context, currentlySelected.iterator().next(), last.args), true);
                        break;
                    case GateOptions:
                        drawCurrentButtons(recreate, actions.gateButtons, true);
                        break;
                    case Garrisons:
                        drawCurrentButtons(recreate, actions.garrisonButtons, true);
                        break;
                    case GlobalActions:
                        drawCurrentButtons(recreate, actions.globalActions, false);
                        break;
                    case MultiUnit:
                        drawCurrentButtons(recreate, actions.multiUnitButtons, true);
                        break;
                    case SetAi:
                        drawCurrentButtons(recreate, actions.setAi, true);
                        break;
                    case PlaceBuilding:
                        drawCurrentButtons(recreate, actions.getBuildingButtons(context, last.args), true);
                        break;
                    case SetDemands:
                        drawDemands(recreate);
                        break;
                    case SetEvolutionSpec:
                        if (recreate)
                            drawEvolutions();
                        break;
                }
                revalidate();
                repaint();
            }
        });
    }

    private void drawDemands(boolean recreate) {
        if (recreate) {
            removeCurrentButtons();
            setLayout(new GridLayout(0, 1));
            add(demands);
        }
        demands.drawDemands(currentlySelected.iterator().next(), recreate);
    }

    private void drawEvolutions() {
        removeCurrentButtons();
        setLayout(new GridLayout(0, 1));
        add(evolutionView);
        evolutionView.drawEvolution(currentlySelected.iterator().next());
    }

    @Override
    public void selectionChanged(List<EntityReader> newSelectedUnits) {
        synchronized (sync) {
            for (EntityReader reader : currentlySelected) {
                context.clientGameState.eventManager.stopListeningTo(this, reader.entityId);
            }
            currentlySelected.clear();
            currentlySelected.addAll(newSelectedUnits);

            for (EntityReader reader : currentlySelected) {
                context.clientGameState.eventManager.listenForEventsFrom(this, reader.entityId);
            }

            stack.clear();
            stack.add(new StackItemWithArgs(StackItem.GlobalActions));
            if (newSelectedUnits.isEmpty()) {
                drawCurrentButtons(true);
                return;
            }

            if (newSelectedUnits.size() == 1) {
                stack.add(new StackItemWithArgs(StackItem.SingleUnit));
                drawCurrentButtons(true);
                return;
            }

            stack.add(new StackItemWithArgs(StackItem.MultiUnit));
            drawCurrentButtons(true);
        }
    }

    public static UnitActions createUnitActions(final UiClientContext clientContext) {
        final UnitActions ret = new UnitActions(clientContext);
        clientContext.selectionManager.addListener(ret);
        ret.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent componentEvent) {
                ret.drawCurrentButtons(true);
            }

            @Override
            public void componentMoved(ComponentEvent componentEvent) {
                ret.drawCurrentButtons(true);
            }
        });
        return ret;
    }

    public enum StackItem {
        GlobalActions,
        Pickup,
        Create,
        GateOptions,
        Garrisons,
        SingleUnit,
        MultiUnit,
        SetAi,
        PlaceBuilding,
        Duplicate,
        SetDemands,
        SetEvolutionSpec,
        Craft,
    }


    private static final class StackItemWithArgs {
        private final StackItem item;
        private final String[] args;

        private StackItemWithArgs(StackItem item) {
            this.item = item;
            this.args = new String[0];
        }

        private StackItemWithArgs(StackItemWithArgs prev, String newArg) {
            this.item = prev.item;
            this.args = new String[prev.args.length + 1];
            System.arraycopy(prev.args, 0, this.args, 0, prev.args.length);
            this.args[this.args.length - 1] = newArg;
        }
    }

    private static final class CreatedButton {
        Action action;
        JButton button;
    }
}
