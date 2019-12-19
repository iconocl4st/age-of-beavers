package client.gui.actions.global_action;

import client.app.UiClientContext;
import common.state.spec.CreationSpec;
import common.state.spec.EntitySpec;

public class PlaceBuilding extends GlobalAction {

    private final CreationSpec spec;

    public PlaceBuilding(UiClientContext context, CreationSpec spec) {
        super(context, spec.createdType.name);
        this.spec = spec;
    }

    @Override
    protected void run() {
        c.uiManager.gameScreen.queryBuildingLocation(spec);
        // what the other thing did...
    }
}
