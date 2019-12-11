package app.assignments;

import client.state.ClientGameState;
import common.state.EntityReader;

public interface Verifier {
    boolean notAsAssigned(EntityReader entity);


    //    // TODO: use...
//    static Verifier aiIsNotOfClass(ClientGameState clientGameState, Class<? extends Ai> aiClass) {
//        return e -> false; // !aiClass.isInstance(clientGameState.aiManager.getCurrentAi(e.entityId));
//    }

    public static Verifier minimal(ClientGameState clientGameState) {
        return e -> e.noLongerExists() || (e.getCurrentAction() == null && !clientGameState.aiManager.isControlling(e));
    }
}
