package client.gui.actions.unit_action;

import client.app.ClientContext;
import client.ai.ActionRequester;
import common.state.EntityReader;

import java.util.Collections;

public class Die extends UnitAction {
    public Die(ClientContext context) {
        super(context, "Die");
    }

    @Override
    public boolean isEnabled(EntityReader entity) {
        return entity != null && entity.isOwnedBy(c.currentPlayer) && entity.getType() != null;
    }

    @Override
    public void run(EntityReader entity) {
        ar.setUnitActionToSuicide(entity);
        c.selectionManager.select(Collections.emptySet());
    }
}
