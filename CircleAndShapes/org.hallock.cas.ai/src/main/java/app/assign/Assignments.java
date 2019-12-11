package app.assign;

import app.DebugSnapshot;
import common.state.EntityId;
import common.state.EntityReader;
import common.state.spec.ResourceType;
import common.util.query.EntityReaderFilter;

import java.util.*;

public class Assignments {

    private final HashMap<EntityReader, UnitAssignment> byEntity = new HashMap<>();
    private final HashMap<AssignmentType, HashSet<EntityReader>> byType = new HashMap<>();
    private final HashMap<EntityReader, Set<UnitAssignment>> byAssociated = new HashMap<>(); // still needed?
    private final TreeSet<UnitAssignment> prioritizedAssignments = new TreeSet<>(CMP);


    public void verify() {
        LinkedList<EntityReader> entitiesToRemove = new LinkedList<>();
        for (UnitAssignment assignment : prioritizedAssignments) {
            if (assignment.isAsAssigned()) continue;
            entitiesToRemove.add(assignment.entity);
        }
        for (EntityReader entity : entitiesToRemove) {
            remove(entity);
        }
    }

    // the fact that the alreadyAssigned parameter has to exist makes me think the priority to pull should be strictly
    // greater than the current priority.

    public UnitAssignment nextWagon(int priority, AssignmentType alreadyAssigned) {
        return nextByEntityType("wagon", priority, alreadyAssigned);
    }

    public UnitAssignment nextHuman(int priority, AssignmentType alreadyAssigned) {
        return nextByEntityType("human", priority, alreadyAssigned);
    }

    private UnitAssignment nextByEntityType(String entityType, int priority, AssignmentType alreadyAssigned) {
        for (UnitAssignment assignment : prioritizedAssignments) {
            if (!assignment.entity.getType().name.equals(entityType))
                continue;
            if (assignment.type.equals(alreadyAssigned))
                continue;
            if (assignment.priority > priority)
                continue;
            return assignment;
        }
        return null;
    }

    public EntityReader pullHuman(int priority, AssignmentType alreadyAssigned) {
        return pullByEntityType("human", priority, alreadyAssigned);
    }

    EntityReader pullByEntityType(String entityType, int priority, AssignmentType alreadyAssigned) {
        for (UnitAssignment assignment : prioritizedAssignments) {
            if (!assignment.entity.getType().name.equals(entityType))
                continue;
            if (assignment.type.equals(alreadyAssigned))
                continue;
            if (assignment.priority > priority)
                continue;
            assignment.remove();
            return assignment.entity;
        }
        return null;
    }

    EntityReader pullByType(AssignmentType type, int priority) {
        for (UnitAssignment assignment : prioritizedAssignments) {
            if (!assignment.type.equals(type))
                continue;
            if (assignment.priority > priority)
                continue;
            assignment.remove();
            return assignment.entity;
        }
        return null;
    }

    public void clear(EntityReader entity) {
        UnitAssignment previousAssignment = byEntity.remove(entity);
        if (previousAssignment == null) return;
        byType.get(previousAssignment.getType()).remove(entity);
        prioritizedAssignments.remove(previousAssignment);
        EntityReader associated = previousAssignment.getAssociated();
        if (associated != null) byAssociated.remove(associated);
    }

    public void remove(EntityReader entity) {
        UnitAssignment previousAssignment = byEntity.get(entity);
        if (previousAssignment == null)
            return;
        previousAssignment.remove();
    }

    public void setAssignment(UnitAssignment assignment) {
        remove(assignment.entity);
        byEntity.put(assignment.entity, assignment);
        byType.computeIfAbsent(assignment.getType(), e -> new HashSet<>()).add(assignment.entity);
        if (!prioritizedAssignments.add(assignment)) {
            throw new IllegalStateException("Should have added.");
        }
        EntityReader associated = assignment.getAssociated();
        if (associated != null) byAssociated.computeIfAbsent(associated, e -> new HashSet<>()).add(assignment);
    }

    public int getNumberAssociated(EntityReader entity, AssignmentType at) {
        Set<UnitAssignment> unitAssignments = byAssociated.get(entity);
        if (unitAssignments == null) return 0;
        int count = 0;
        for (UnitAssignment ua : unitAssignments)
            if (ua.type.equals(at))
                ++count;
        return count;
    }

    public void collectResourceAssignments(Map<ResourceType, Integer> peopleOnResource, int priority) {
        for (UnitAssignment assignment : prioritizedAssignments)
            assignment.countPeopleOnResource(peopleOnResource, priority);
    }

    private static final Comparator<UnitAssignment> CMP = (o1, o2) -> {
        int cmp = Integer.compare(o1.priority, o2.priority);
        if (cmp != 0) return cmp;
        cmp = -Double.compare(o1.assignedTime, o2.assignedTime);
        if (cmp != 0) return cmp;
        return EntityId.COMPARATOR.compare(o1.entity.entityId, o2.entity.entityId);
    };

    public int getNumberOfAssignments(AssignmentType assignmentType) {
        HashSet<EntityReader> entityReaders = byType.get(assignmentType);
        if (entityReaders == null) return 0;
        return entityReaders.size();
    }

    public UnitAssignment get(EntityReader entity) {
        return byEntity.get(entity);
    }

    public void clearType(AssignmentType construct) {
        HashSet<EntityReader> entityReaders = byType.get(construct);
        if (entityReaders == null) return;
        for (EntityReader entity : (Set<EntityReader>) entityReaders.clone()) {
            remove(entity);
        }
    }

//    public Set<EntityReader> getEntities(AssignmentType type) {
//        HashSet<EntityReader> entityReaders = byType.get(type);
//        if (entityReaders == null) return Collections.emptySet();
//        return new HashSet<>(entityReaders);
//    }

    public Set<UnitAssignment> getAssignments(AssignmentType type) {
        HashSet<EntityReader> entityReaders = byType.get(type);
        if (entityReaders == null || entityReaders.isEmpty()) return Collections.emptySet();
        HashSet<UnitAssignment> ret = new HashSet<>();
        for (EntityReader entity : entityReaders) {
            UnitAssignment unitAssignment = byEntity.get(entity);
            if (unitAssignment == null) throw new IllegalStateException();
            ret.add(unitAssignment);
        }
        return ret;
    }

    public boolean isAssigned(EntityReader key, AssignmentType type) {
        UnitAssignment unitAssignment = byEntity.get(key);
        return unitAssignment != null && unitAssignment.type.equals(type);
    }

    public Set<EntityReader> collectIdle(EntityReaderFilter filter) {
        HashSet<EntityReader> entities = byType.get(AssignmentType.Idle);
        if (entities == null || entities.isEmpty())
            return Collections.emptySet();

        Set<EntityReader> ret = null;
        for (EntityReader entity : entities) {
            if (!filter.include(entity))
                continue;
            if (ret == null) {
                ret = new HashSet<>();
            }
            ret.add(entity);
        }
        if (ret == null)
            return Collections.emptySet();
        return ret;
    }

    public void addDebugAssignments(DebugSnapshot snapshot) {
        for (UnitAssignment assignment : prioritizedAssignments) {
            snapshot.information.get(assignment.entity).assignment = assignment.type.name();
            snapshot.information.get(assignment.entity).assignmentPriority = assignment.priority;
        }
    }
}
