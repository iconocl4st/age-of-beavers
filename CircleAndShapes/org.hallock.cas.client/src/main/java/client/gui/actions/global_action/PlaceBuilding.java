package client.gui.actions.global_action;

import client.app.ClientContext;
import common.state.spec.EntitySpec;

public class PlaceBuilding extends GlobalAction {

    private final EntitySpec spec;

    public PlaceBuilding(ClientContext context, EntitySpec spec) {
        super(context, spec.name);
        this.spec = spec;
    }

    @Override
    protected void run() {
        c.uiManager.gameScreen.queryBuildingLocation(spec);
        // what the other thing did...
    }
}
