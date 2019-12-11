package app.assignments;

import common.state.EntityReader;

import java.util.Set;

public interface UnitAssignment {
    Set<EntityReader> getUnits();

    void verify();

    EntityReader pop();

    int size();

    void remove(EntityReader reader);

    EntityReader peek();

    boolean contains(EntityReader riding);
}
