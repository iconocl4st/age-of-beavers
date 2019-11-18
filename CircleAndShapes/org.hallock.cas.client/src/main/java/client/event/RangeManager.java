package client.event;

import common.state.EntityId;
import common.state.sst.manager.LocationManager;
import common.util.DPoint;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class RangeManager {

    private final HashMap<EntityId, HashSet<RangeListener>> listeners = new HashMap<>();
    private final LocationManager locationManager;

    public RangeManager(LocationManager locationManager) {
        this.locationManager = locationManager;
    }

    public Set<RangeListener> entityMoved(EntityId entityId) {
        synchronized (listeners) {
            HashSet<RangeListener> listenerSet = listeners.get(entityId);
            if (listenerSet == null) return Collections.emptySet();

            DPoint location1 = locationManager.getLocation(entityId);
            if (location1 == null) {
                remove(entityId);
                return Collections.emptySet();
            }

            Set<RangeListener> ret = null;
            for (RangeListener listener : (Set<RangeListener>) listenerSet.clone()) {
                DPoint location2 = locationManager.getLocation(listener.entity);
                if (location2 == null) {
                    remove(listener.entity);
                    continue;
                }
                if (location1.distanceTo(location2) < listener.range) {
                    if (ret == null) {
                        ret = new HashSet<>();
                    }
                    ret.add(listener);
                }
            }
            if (listenerSet.isEmpty()) {
                remove(entityId);
            }
            if (ret == null)
                return Collections.emptySet();
            return ret;
        }
    }

    public void remove(EntityId entityId) {
        synchronized (listeners) {
            Set<RangeListener> listenerSet = listeners.remove(entityId);
            if (listenerSet == null) return;
            for (RangeListener listener : listenerSet) {
                HashSet<RangeListener> listenerSet2 = listeners.get(listener.entity);
                if (listenerSet2 == null) continue;
                listenerSet2.removeIf(l ->  l.entity.equals(entityId));
                if (listenerSet2.isEmpty()) {
                    listeners.remove(listener.entity);
                }
            }
        }
    }

    public void listenTo(EntityId entity1, EntityId entity2, double range) {
        synchronized (listeners) {
            listeners.computeIfAbsent(entity1, k -> new HashSet<>()).add(new RangeListener(entity2, range));
            listeners.computeIfAbsent(entity2, k -> new HashSet<>()).add(new RangeListener(entity1, range));
        }
    }


    public static final class RangeListener {
        public final EntityId entity;
        public final double range;

        public RangeListener(EntityId entity, double range) {
            this.entity = entity;
            this.range = range;
        }
    }
}
