package client.gui.actions;

import client.ai.ActionRequester;
import client.ai.ResponsiveConnectionWriter;
import client.app.UiClientContext;
import common.state.EntityReader;

import java.util.Collection;

public abstract class Action {

    /* TODO: Is there some connection between an action and an AiStack? */

    public final String label;
    protected final UiClientContext c;
    protected final ActionRequester ar;

    protected Action(UiClientContext context, String label) {
        this.c = context;
        this.ar = new ActionRequester(new ResponsiveConnectionWriter(context.writer, context.executorService));
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
