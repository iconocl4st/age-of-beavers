package client.gui.mouse;

import client.app.UiClientContext;
import client.gui.actions.unit_action.UnitToLocationAction;
import client.gui.actions.unit_action.UnitToUnitAction;
import client.gui.game.Command;
import client.gui.game.Zoom;
import client.gui.game.gl.GlListeners;
import common.state.EntityReader;
import common.util.DPoint;

import java.util.Set;

public class CommandListener implements GlListeners.GameMousePressListener {
    private final UiClientContext context;

    private final UnitToUnitAction[] toUnitActions;
    private final UnitToLocationAction[] locationActions;

    private Command currentCommand;

    public CommandListener(
            UiClientContext context,
            UnitToUnitAction[] unitActions,
            UnitToLocationAction[] locationActions
    ) {
        this.context = context;
        this.toUnitActions = unitActions;
        this.locationActions = locationActions;
    }

    public void setCommand(Command command) {
        currentCommand = command;
    }

    @Override
    public void mousePressed(double x, double y, GlListeners.PressInfo info) {
        DPoint destination = new DPoint(x, y);
        Set<EntityReader> entities = context.clientGameState.gameState.locationManager.getEntities(
                destination,
                entity -> !entity.isHidden()
        );

        if (entities.size() > 1) {
            return;
        }
        if (info.isLeftButton) {
            if (currentCommand == null)
                return;
            if (entities.isEmpty())
                currentCommand.perform(destination);
            else
                currentCommand.perform(entities.iterator().next());
            context.uiManager.gameScreen.clearCurrentCommand();
            return;
        }

        if (!info.isRightButton)
            return;

        for (EntityReader entity : context.selectionManager.getSelectedUnits()) {
            if (entities.isEmpty()) {
                for (UnitToLocationAction action : locationActions) {
                    if (!action.isEnabled(entity))
                        continue;
                    action.run(entity, destination);
                    break;
                }
            } else {
                EntityReader target = entities.iterator().next();
                for (UnitToUnitAction action : toUnitActions) {
                    if (!action.isEnabled(entity))
                        continue;
                    if (!action.canRunOn(entity, target))
                        continue;
                    action.run(entity, target);
                    break;
                }
            }
        }
    }

    @Override
    public void mouseReleased(double x, double y, GlListeners.PressInfo info) {}
}
