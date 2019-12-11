package app.assign;

import app.assignments.Verifier;
import common.state.EntityReader;
import common.state.spec.ResourceType;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ResourceAssignment extends UnitAssignment {
    private final Set<ResourceType> resourceTypes;

    public ResourceAssignment(
            double assignedTime,
            EntityReader entity,
            int priority,
            AssignmentType type,
            Verifier verifier,
            OnRemove onRemove,
            OnIdle onIdle,
            Set<ResourceType> resourceTypes
    ) {
        super(assignedTime, entity, priority, type, verifier, onRemove, onIdle);
        this.resourceTypes = resourceTypes;
    }

    @Override
    public void countPeopleOnResource(Map<ResourceType, Integer> counts, int priority) {
        if (this.priority >= priority)
            return;
        for (ResourceType resource : resourceTypes)
            counts.put(resource, counts.getOrDefault(resource, 0) + 1);
    }
}
