package client.ai;

import client.state.ClientGameState;
import common.AiAttemptResult;
import common.state.EntityId;
import common.state.EntityReader;

import java.util.HashMap;

public class AiManager {

    private final ClientGameState context;
    private final HashMap<EntityId, Ai> currentAis = new HashMap<>();

    public AiManager(ClientGameState context) {
        this.context = context;
    }

    void setAi(EntityId entity, Ai ai) {
        removeAi(entity);
        currentAis.put(entity, ai);
        ai.registerListeners();
    }

    public void removeAi(EntityId unitId) {
        Ai ai = currentAis.remove(unitId);
        if (ai != null) {
            ai.removeListeners();
        }
    }

    public Ai getCurrentAi(EntityId unit) {
        synchronized (currentAis) {
            return currentAis.get(unit);
        }
    }

    public void startAi(EntityId entity, Ai ai) {
        setAi(entity, ai);
        if (!ai.setActions(context.actionRequester).equals(AiAttemptResult.Successful)) {
            removeAi(entity);
        }
    }

    public boolean isControlling(EntityReader entity) {
        return currentAis.containsKey(entity.entityId);
    }
}
