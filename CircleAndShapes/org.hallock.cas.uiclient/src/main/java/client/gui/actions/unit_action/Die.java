package client.gui.actions.unit_action;

import client.app.UiClientContext;
import common.state.EntityReader;

import java.util.Collections;

public class Die extends UnitAction {
    public Die(UiClientContext context) {
        super(context, "Die");
    }

    @Override
    public boolean isEnabled(EntityReader entity) {
        return entity != null && entity.isOwnedBy(c.clientGameState.currentPlayer) && entity.getType() != null;
    }

    @Override
    public void run(EntityReader entity) {
        ar.setUnitActionToSuicide(entity);
        c.selectionManager.select(Collections.emptySet());
    }
}
