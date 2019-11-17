package client.gui.actions.unit_action;

import client.ai.ActionRequester;
import client.app.ClientContext;
import client.gui.game.Command;
import common.state.EntityId;
import common.state.EntityReader;
import common.util.DPoint;

public abstract class UnitToUnitAction extends UnitAction {

    protected UnitToUnitAction(ClientContext context, String label) {
        super(context, label);
    }

    public abstract boolean canRunOn(EntityReader performer, EntityReader target);

    public abstract void run(EntityReader entity, EntityReader target);

    @Override
    public void run(EntityReader entity) {
        c.uiManager.gameScreen.setCurrentCommand(new Command() {
            @Override
            public void perform(DPoint location) {}

            @Override
            public void perform(EntityId rId) {
                EntityReader resource = new EntityReader(c.gameState, rId);
                if (!canRunOn(entity, resource)) return;
                run(entity, resource);
            }
        });
    }
}
