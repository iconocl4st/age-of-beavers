package client.state;

import client.app.ClientContext;
import common.state.spec.EntitySpec;
import common.state.EntityId;
import common.state.EntityReader;

import java.util.*;

public class SelectionManager {

    final ClientContext context;

    final LinkedList<EntityId> selectedUnits = new LinkedList<>();
    final List<SelectionListener> listeners = new LinkedList<>();

    final HashMap<Integer, Set<EntityId>> controlGroups = new HashMap<>();

    public SelectionManager(ClientContext gameState) {
        this.context = gameState;
    }

    public int getSelectionPriority(EntityId id) {
        EntityReader entity = new EntityReader(context.gameState, id);
        EntitySpec type = entity.getType();
        if (type == null) {
            return -1;
        }
        boolean owned = entity.isOwnedBy(context.currentPlayer);
        if (owned) {
            if (type.containsClass("unit")) {
                return 5;
            }
            if (type.containsClass("natural-resource")) {
                return 4;
            }
            return 3;
        } else {
            if (type.containsClass("unit")) {
                return 2;
            }
            if (type.containsClass("natural-resource")) {
                return 1;
            }
            return 0;
        }
    }

    public Set<Integer> getControlGroups(EntityId entity) {
        TreeSet<Integer> ret = new TreeSet<>();
        for (Map.Entry<Integer, Set<EntityId>> entry : controlGroups.entrySet()) {
            if (entry.getValue().contains(entity)) {
                ret.add(entry.getKey());
            }
        }
        return ret;
    }

    public void registerControlGroup(int i)  {
        synchronized (selectedUnits) {
            controlGroups.put(i, new HashSet<>(selectedUnits));
        }
    }

    public Set<EntityId> recallControlGroup(int i) {
        synchronized (selectedUnits) {
            Set<EntityId> entityIds = controlGroups.get(i);
            if (entityIds == null) return Collections.emptySet();

            Set<EntityId> newSelectedUnits = new HashSet<>();
            for (EntityId id : entityIds) {
                if (context.gameState.entityManager.get(id) == null)
                    continue;
                newSelectedUnits.add(id);
            }
            select(newSelectedUnits);
            return entityIds;
        }
    }

    public boolean isSelected(EntityId entityId) {
        return selectedUnits.contains(entityId);
    }


    public void select(Set<EntityId> entityIds) {
        synchronized (selectedUnits) {
            selectedUnits.clear();
            selectedUnits.addAll(entityIds);
        }
        notifyListeners();
    }

    public void select(EntityId value) {
        synchronized (selectedUnits) {
            selectedUnits.clear();
            if (value != null) {
                selectedUnits.add(value);
            }
        }
        notifyListeners();
    }

    public void select(double gr1x, double gr1y, double gr2x, double gr2y) {
        select(gr1x, gr1y, gr2x, gr2y, null);
    }

    public void select(double gr1x, double gr1y, double gr2x, double gr2y, EntitySpec type) {
        Set<EntityId> possibleSelections = context.gameState.locationManager.getEntitiesWithin(gr1x, gr1y, gr2x, gr2y, entity -> {
            if (context.gameState.hiddenManager.get(entity)) return false;
            if (type == null) return true;
            EntitySpec t = context.gameState.typeManager.get(entity);
            return t != null && t.name.equals(type.name);
        });

        synchronized (selectedUnits) {
            selectedUnits.clear();
            int currentSelectionPriority = -1;
            for (EntityId id : possibleSelections) {
                int selectionPriority = getSelectionPriority(id);
                if (selectionPriority > currentSelectionPriority) {
                    selectedUnits.clear();
                    currentSelectionPriority = selectionPriority;
                }
                if (selectionPriority == currentSelectionPriority) {
                    selectedUnits.add(id);
                }
            }
        }

        notifyListeners();
    }

    public void notifyListeners() {
        context.uiManager.log("Selected " + selectedUnits.size() + " units");
        context.executorService.submit(new Runnable() {
            @Override
            public void run() {
                synchronized (listeners) {
                    for (SelectionListener listener : listeners) {
                        listener.selectionChanged(selectedUnits);
                    }
                }
            }
        });
    }

    public List<EntityId> getSelectedUnits() {
        synchronized (selectedUnits) {
            return (List<EntityId>) selectedUnits.clone();
        }
    }

    // no remove listener?
    public void addListener(SelectionListener ret) {
        synchronized (listeners) {
            listeners.add(ret);
        }
    }

    public interface SelectionListener {
        // TODO: Should be a set instead of a list
        void selectionChanged(List<EntityId> newSelectedUnits);
    }
}
