package client.event;

import common.state.EntityId;
import common.state.EntityReader;
import common.util.DPoint;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class RangeManager {

    private final HashMap<EntityId, HashSet<RangeListener>> listeners = new HashMap<>();

    public Set<InRangeEvent> entityMoved(EntityReader entity) {
        synchronized (listeners) {
            HashSet<RangeListener> listenerSet = listeners.get(entity.entityId);
            if (listenerSet == null) return Collections.emptySet();

            DPoint location1 = entity.getLocation();
            if (location1 == null) {
                remove(entity);
                return Collections.emptySet();
            }

            Set<InRangeEvent> ret = null;
            for (RangeListener listener : (Set<RangeListener>) listenerSet.clone()) {
                // TODO: what about going out of sight...
                DPoint location2 = listener.entity.getLocation();
                if (location2 == null) {
                    remove(listener.entity);
                    continue;
                }
                if (location1.distanceTo(location2) < listener.range) {
                    if (ret == null) {
                        ret = new HashSet<>();
                    }
                    ret.add(new InRangeEvent(entity, listener.entity, listener.range));
                }
            }
            if (listenerSet.isEmpty()) {
                remove(entity);
            }
            if (ret == null)
                return Collections.emptySet();
            return ret;
        }
    }

    public void remove(EntityReader entity) {
        synchronized (listeners) {
            Set<RangeListener> listenerSet = listeners.remove(entity.entityId);
            if (listenerSet == null) return;
            for (RangeListener listener : listenerSet) {
                HashSet<RangeListener> listenerSet2 = listeners.get(listener.entity.entityId);
                if (listenerSet2 == null) continue;
                listenerSet2.removeIf(l ->  l.entity.equals(entity));
                if (listenerSet2.isEmpty()) {
                    listeners.remove(listener.entity.entityId);
                }
            }
        }
    }

    public void listenTo(EntityReader entity1, EntityReader entity2, double range) {
        synchronized (listeners) {
            listeners.computeIfAbsent(entity1.entityId, k -> new HashSet<>()).add(new RangeListener(entity2, range));
            listeners.computeIfAbsent(entity2.entityId, k -> new HashSet<>()).add(new RangeListener(entity1, range));
        }
    }

    public static final class InRangeEvent {
        public final EntityReader entity1;
        public final EntityReader entity2;
        public final double range;


        public InRangeEvent(EntityReader entity1, EntityReader entity2, double range) {
            this.entity1 = entity1;
            this.entity2  = entity2;
            this.range = range;
        }

        public int hashCode() {
            return entity1.hashCode() * entity2.hashCode();
        }

        public boolean equals(Object other) {
            if (!(other instanceof InRangeEvent)) return false;
            InRangeEvent evt = (InRangeEvent) other;
            return range == evt.range &&
                    (entity1.equals(evt.entity1) && entity2.equals(evt.entity2)) ||
                    (entity2.equals(evt.entity1) && entity1.equals(evt.entity2));
        }
    }


    public static final class RangeListener {
        public final EntityReader entity;
        public final double range;

        public RangeListener(EntityReader entity, double range) {
            this.entity = entity;
            this.range = range;
        }
    }
}
