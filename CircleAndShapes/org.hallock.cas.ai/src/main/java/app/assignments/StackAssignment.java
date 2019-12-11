package app.assignments;

import app.PlayerAiContext;
import common.state.EntityReader;
import common.state.spec.ResourceType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class StackAssignment implements UnitAssignment {

    private final PlayerAiContext context;
    protected final LinkedList<EntityReader> entities = new LinkedList<>();
    private final Verifier verifier;

    public StackAssignment(String name, PlayerAiContext context, int priority, Verifier verifier) {
        // refactor out the entities with interface...
        this.context = context;
        this.verifier = verifier;
    }

    @Override
    public Set<EntityReader> getUnits() {
        return new HashSet<>(entities);
    }

    @Override
    public void verify() {
        synchronized (entities) {
            Verifier mini = Verifier.minimal(context.clientGameState);
            entities.removeIf(mini::notAsAssigned);
            if (verifier != null)
                entities.removeIf(verifier::notAsAssigned);
        }
    }

    public void assigned(EntityReader entity) {
        synchronized (entities) {
            entities.addLast(entity);
        }
    }

    @Override
    public EntityReader pop() {
        synchronized (entities) {
            return entities.removeLast();
        }
    }

    void addNumContributing(HashMap<ResourceType,Integer> peopleOnResource) {}


    @Override
    public int size() {
        return entities.size();
    }

    @Override
    public void remove(EntityReader reader) {
        synchronized (entities) {
            entities.remove(reader);
        }
    }

    @Override
    public EntityReader peek() {
        return entities.getLast();
    }

    @Override
    public boolean contains(EntityReader riding) {
        return entities.contains(riding);
    }

}
