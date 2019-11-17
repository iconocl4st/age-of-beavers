package common.state.sst.manager;

import common.state.EntityId;
import common.state.spec.GameSpec;
import common.state.sst.sub.GateInfo;
import common.util.json.*;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

public class ReversableManagerImpl<T, K> extends ManagerImpl<T> {

    private final HashMap<K, Set<EntityId>> reverseMap = new HashMap<>();
    private final Getter<T, K> getter;

    public ReversableManagerImpl(Getter<T, K> getter, DataSerializer<T> serializer) {
        super(serializer);
        this.getter = getter;
    }

    public void set(EntityId entityId, T value) {
        synchronized (map) {
            removeFromReverseMap(entityId, map.put(entityId, value));
            reverseMap.computeIfAbsent(getter.get(value), k1 -> new HashSet<>()).add(entityId);
        }
    }

    public Set<Pair<T>> getByType(K k) {
        Set<Pair<T>> ret = new HashSet<>();
        synchronized (map) {
            for (EntityId entityId : reverseMap.getOrDefault(k, Collections.emptySet())) {
                ret.add(new Pair<>(entityId, map.get(entityId)));
            }
        }
        return ret;
    }

    private void removeFromReverseMap(EntityId entityId, T oldValue) {
        if (oldValue == null) {
            return;
        }
        K k = getter.get(oldValue);
        Set<EntityId> entityIds = reverseMap.get(k);
        if (entityIds == null)
            return;

        entityIds.remove(entityId);
        if (!entityIds.isEmpty())
            return;
        reverseMap.remove(k);
    }

    public void remove(EntityId entityId) {
        synchronized (map) {
            removeFromReverseMap(entityId, map.remove(entityId));
        }
    }

    protected void removeAll() {
        map.clear();
        reverseMap.clear();
    }

    public Set<? extends Map.Entry<EntityId, T>> entrySet() {
        synchronized (map) {
            return new HashSet<>(map.entrySet());
        }
    }

    public interface Getter<T, K> extends Serializable {
        K get(T t);
    }

    public static final class Pair<T> {
        public final EntityId entityId;
        public final T value;

        public Pair(EntityId entityId, T value) {
            this.entityId = entityId;
            this.value = value;
        }

        public int hashCode() {
            return entityId.hashCode();
        }

        public boolean equals(Object other) {
            if (!(other instanceof Pair)) return false;
            return ((Pair<T>) other).entityId.equals(entityId);
        }
    }
}
