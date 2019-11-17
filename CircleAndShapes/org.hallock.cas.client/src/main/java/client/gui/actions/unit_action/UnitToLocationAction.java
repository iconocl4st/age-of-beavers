package client.gui.actions.unit_action;

import client.app.ClientContext;
import client.gui.game.Command;
import common.state.EntityId;
import common.state.EntityReader;
import common.util.DPoint;

public abstract class UnitToLocationAction extends UnitAction {

    protected UnitToLocationAction(ClientContext context, String label) {
        super(context, label);
    }

    public abstract void run(EntityReader entity, DPoint location);

    @Override
    public void run(EntityReader entity) {
        c.uiManager.gameScreen.setCurrentCommand(new Command() {
            @Override
            public void perform(DPoint location) {
                run(entity, location);
            }

            @Override
            public void perform(EntityId rId) {
            }
        });
    }
}
