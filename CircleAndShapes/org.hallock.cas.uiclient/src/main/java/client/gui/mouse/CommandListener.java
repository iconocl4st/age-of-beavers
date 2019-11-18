package client.gui.mouse;

import client.app.UiClientContext;
import client.gui.actions.unit_action.UnitToLocationAction;
import client.gui.actions.unit_action.UnitToUnitAction;
import client.gui.game.Command;
import client.gui.game.Zoom;
import common.state.EntityId;
import common.state.EntityReader;
import common.util.DPoint;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Set;

public class CommandListener implements MouseListener {
    private final Zoom zoom;
    private final UiClientContext context;

    private final UnitToUnitAction[] toUnitActions;
    private final UnitToLocationAction[] locationActions;

    private Command currentCommand;

    public CommandListener(Zoom zoom, UiClientContext context, UnitToUnitAction[] unitActions, UnitToLocationAction[] locationActions) {
        this.zoom = zoom;
        this.context = context;
        this.toUnitActions = unitActions;
        this.locationActions = locationActions;
    }

    public void setCommand(Command command) {
        currentCommand = command;
    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {

    }

    @Override
    public void mousePressed(MouseEvent mouseEvent) {
        boolean isRightClick = (mouseEvent.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK;
        boolean isLeftClick = (mouseEvent.getModifiers() & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK;

        DPoint destination = new DPoint(
                zoom.mapScreenToGameX(mouseEvent.getX()),
                zoom.mapScreenToGameY(mouseEvent.getY())
        );
        Set<EntityId> entities = context.clientGameState.gameState.locationManager.getEntities(
                destination,
                entity -> !context.clientGameState.gameState.hiddenManager.get(entity)
        );

        if (entities.size() > 1) {
            return;
        }
        if (isLeftClick) {
            if (currentCommand == null)
                return;
            if (entities.isEmpty())
                currentCommand.perform(destination);
            else
                currentCommand.perform(entities.iterator().next());
            context.uiManager.gameScreen.clearCurrentCommand();
            mouseEvent.consume();
            return;
        }

        if (!isRightClick)
            return;

        boolean handled = false;
        for (EntityReader entity : context.selectionManager.getSelectedUnits()) {
            if (entities.isEmpty()) {
                for (UnitToLocationAction action : locationActions) {
                    if (!action.isEnabled(entity))
                        continue;
                    action.run(entity, destination);
                    handled = true;
                    break;
                }
            } else {
                EntityReader target = new EntityReader(context.clientGameState.gameState, entities.iterator().next());
                for (UnitToUnitAction action : toUnitActions) {
                    if (!action.isEnabled(entity))
                        continue;
                    if (!action.canRunOn(entity, target))
                        continue;
                    action.run(entity, target);
                    handled = true;
                    break;
                }
            }
        }
        if (handled)
            mouseEvent.consume();
    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {

    }

    @Override
    public void mouseEntered(MouseEvent mouseEvent) {

    }

    @Override
    public void mouseExited(MouseEvent mouseEvent) {

    }
}
