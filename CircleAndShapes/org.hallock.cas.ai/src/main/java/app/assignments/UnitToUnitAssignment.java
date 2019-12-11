package app.assignments;

import app.PlayerAiContext;
import common.state.EntityReader;

import java.util.HashMap;
import java.util.Set;

public class UnitToUnitAssignment implements UnitAssignment {


    private final PlayerAiContext context;
    protected final HashMap<EntityReader, EntityReader> assignments = new HashMap<>();
    private final Verifier verifier;

    public UnitToUnitAssignment(String name, PlayerAiContext context, int priority, Verifier verifier) {
        // refactor out the entities with interface...
        this.context = context;
        this.verifier = verifier;
    }


    @Override
    public Set<EntityReader> getUnits() {
        return null;
    }

    @Override
    public void verify() {

    }

    @Override
    public EntityReader pop() {
        return null;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public void remove(EntityReader reader) {

    }

    @Override
    public EntityReader peek() {
        return null;
    }

    @Override
    public boolean contains(EntityReader riding) {
        return false;
    }
}
