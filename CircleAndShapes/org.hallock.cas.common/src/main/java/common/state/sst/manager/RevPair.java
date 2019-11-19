package common.state.sst.manager;

import common.state.EntityId;

public final class RevPair<T> {
    public final EntityId entityId;
    public final T value;

    public RevPair(EntityId entityId, T value) {
        this.entityId = entityId;
        this.value = value;
    }

    public int hashCode() {
        return entityId.hashCode();
    }

    public boolean equals(Object other) {
        if (!(other instanceof RevPair)) return false;
        return ((RevPair<T>) other).entityId.equals(entityId);
    }
}
