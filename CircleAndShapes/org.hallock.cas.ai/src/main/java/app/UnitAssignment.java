package app;

import client.ai.Ai;
import client.state.ClientGameState;
import common.state.EntityReader;
import common.state.spec.ResourceType;

import java.util.HashMap;
import java.util.LinkedList;

public class UnitAssignment {

    private final PlayerAiContext context;
    final LinkedList<EntityReader> entities = new LinkedList<>();
    private final ResourceType resourceType;
    private final Verifier verifier;

    UnitAssignment(PlayerAiContext context, ResourceType resourceType, Verifier verifier) {
        this.context = context;
        this.resourceType = resourceType;
        this.verifier = verifier;
    }

    void verify() {
        Verifier mini = minimal(context.clientGameState);
        entities.removeIf(mini::notAsAssigned);
        if (verifier != null)
            entities.removeIf(verifier::notAsAssigned);
    }

    void assigned(EntityReader entity) {
        entities.addLast(entity);
    }

    EntityReader pop() {
        return entities.removeLast();
    }

    void addNumContributing(HashMap<ResourceType,Integer> peopleOnResource) {
        if (resourceType == null) return;
        peopleOnResource.put(resourceType, peopleOnResource.getOrDefault(resourceType, 0) + entities.size());
    }

    public int size() {
        return entities.size();
    }

    public void remove(EntityReader reader) {
        entities.remove(reader);
    }

    public EntityReader peek() {
        return entities.getLast();
    }

    boolean contains(EntityReader riding) {
        return entities.contains(riding);
    }

    public interface Verifier {
        boolean notAsAssigned(EntityReader entity);
    }

    static Verifier aiIsNotOfClass(ClientGameState clientGameState, Class<? extends Ai> aiClass) {
        return e -> !aiClass.isInstance(clientGameState.aiManager.getCurrentAi(e.entityId));
    }

    static Verifier minimal(ClientGameState clientGameState) {
        return e -> e.noLongerExists() || (e.getCurrentAction() == null && !clientGameState.aiManager.isControlling(e));
    }
}
