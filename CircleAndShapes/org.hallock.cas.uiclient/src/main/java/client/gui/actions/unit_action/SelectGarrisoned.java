package client.gui.actions.unit_action;

import client.app.UiClientContext;
import common.state.EntityReader;

public class SelectGarrisoned extends UnitAction {
    public SelectGarrisoned(UiClientContext context) {
        super(context, "Select garrisoned");
    }

    @Override
    public boolean isEnabled(EntityReader entity) {
        return entity != null && !entity.isHidden() && !entity.getGarrisoned().isEmpty();
    }

    @Override
    public void run(EntityReader entity) {
        c.selectionManager.select(entity.getGarrisoned());
    }
}
