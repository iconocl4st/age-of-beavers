package app.assign;

import app.assignments.Verifier;
import common.state.EntityReader;
import common.state.spec.ResourceType;

import java.util.HashMap;
import java.util.Map;

public class UnitAssignment {
    public final double assignedTime;
    public final EntityReader entity;
    public final int priority;
    public final AssignmentType type;
    public final Verifier verifier;
    public final OnRemove onRemove;
    public final OnIdle onIdle;

    public AssignmentArgs args;

    public UnitAssignment(
            double assignedTime,
            EntityReader entity,
            int priority,
            AssignmentType type,
            Verifier verifier,
            OnRemove onRemove,
            OnIdle onIdle
    ) {
        this.assignedTime = assignedTime;
        this.entity = entity;
        this.priority = priority;
        this.type = type;
        this.verifier = verifier;
        this.onRemove = onRemove;
        this.onIdle = onIdle;
    }

    public void countPeopleOnResource(Map<ResourceType, Integer> counts, int priority) {}

    public EntityReader getAssociated() { return null; }

    public boolean isAsAssigned() {
        return !verifier.notAsAssigned(entity);
    }

    public AssignmentType getType() {
        return type;
    }

    public void remove() {
        onRemove.remove();
    }



    public interface AssignmentArgs {}
}
