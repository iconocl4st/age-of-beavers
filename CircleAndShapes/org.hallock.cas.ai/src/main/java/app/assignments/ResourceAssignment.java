package app.assignments;

import app.PlayerAiContext;
import common.state.spec.ResourceType;

import java.util.HashMap;

public class ResourceAssignment extends StackAssignment {

    private final ResourceType resourceType;

    public ResourceAssignment(String name, PlayerAiContext context, ResourceType resourceType, int priority, Verifier verifier) {
        super(name, context, priority, verifier);
        this.resourceType = resourceType;
    }

    public void addNumContributing(HashMap<ResourceType,Integer> peopleOnResource) {
        if (resourceType == null) return;
        peopleOnResource.put(resourceType, peopleOnResource.getOrDefault(resourceType, 0) + entities.size());
    }
}
