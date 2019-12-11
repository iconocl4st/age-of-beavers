package app.assign;

import app.assignments.Verifier;
import common.state.EntityReader;

public class ToEntityAssignment extends UnitAssignment {
    public final EntityReader associatedEntity;

    public ToEntityAssignment(
            double assignedTime,
            EntityReader entity,
            int priority,
            AssignmentType type,
            Verifier verifier,
            OnRemove onRemove,
            OnIdle onIdle,
            EntityReader associatedEntity
    ) {
        super(assignedTime, entity, priority, type, verifier, onRemove, onIdle);
        this.associatedEntity = associatedEntity;
    }

    public EntityReader getAssociated() { return associatedEntity; }
}
