package app;

import client.ai.Ai;
import client.state.ClientGameState;
import common.state.EntityReader;
import common.state.spec.ResourceType;

import java.util.HashMap;
import java.util.LinkedList;

public class UnitAssignment {

    private final PlayerAiContext context;
    private final LinkedList<EntityReader> entities = new LinkedList<>();
    private final ResourceType resourceType;
    private final Verifier verifier;

    public UnitAssignment(PlayerAiContext context, ResourceType resourceType, Verifier verifier) {
        this.context = context;
        this.resourceType = resourceType;
        this.verifier = verifier;
    }

    void verify() {
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

    public interface Verifier {
        boolean notAsAssigned(EntityReader entity);
    }

    static Verifier aiIsNotOfClass(ClientGameState clientGameState, Class<? extends Ai> aiClass) {
        return e -> e.noLongerExists() || !aiClass.isInstance(clientGameState.aiManager.getCurrentAi(e.entityId));
    }
}
