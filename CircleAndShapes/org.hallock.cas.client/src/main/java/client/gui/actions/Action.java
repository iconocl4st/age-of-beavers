package client.gui.actions;

import client.ai.ActionRequester;
import client.app.ClientContext;
import common.state.EntityReader;

import java.util.Collection;

public abstract class Action {

    /* TODO: Is there some connection between an action and an Ai? */

    public final String label;
    protected final ClientContext c;
    protected final ActionRequester ar;

    protected Action(ClientContext context, String label) {
        this.c = context;
        this.ar = new ActionRequester(context);
        this.label = label;
    }

    public abstract boolean isEnabled(Collection<EntityReader> currentlySelected);

    public abstract void run(Collection<EntityReader> currentlySelected);

//    public enum ActionTypes {
//        SingleUnit,
//        UnitToLocation,
//        UnitToUnit,
//        MultiUnit, // entities by player
//    }
}
