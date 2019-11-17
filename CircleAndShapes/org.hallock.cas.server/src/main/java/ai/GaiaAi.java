package ai;

import common.state.EntityId;
import server.state.ServerStateManipulator;

import java.util.HashMap;

public class GaiaAi {
    private HashMap<EntityId, RandomlyWaitAndMove> ais = new HashMap<>();

    public void setAi(EntityId entityId, RandomlyWaitAndMove ai, ServerStateManipulator ssm) {
        this.ais.put(entityId, ai);
        if (!ai.actionCompleted(ssm)) {
            ais.remove(entityId);
        }
    }

    public void unitCompletedAction(ServerStateManipulator manipulator, EntityId entity) {
        RandomlyWaitAndMove deerAi = ais.get(entity);
        if  (deerAi == null)
            return;
        if (!deerAi.actionCompleted(manipulator)) {
            ais.remove(entity);
        }
    }
}
