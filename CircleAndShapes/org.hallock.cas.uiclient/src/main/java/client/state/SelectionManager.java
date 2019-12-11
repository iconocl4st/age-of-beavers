package client.state;

import client.app.UiClientContext;
import common.state.EntityId;
import common.state.EntityReader;
import common.state.spec.EntitySpec;

import java.util.*;

public class SelectionManager {

    final UiClientContext context;

    final LinkedList<EntityReader> selectedUnits = new LinkedList<>();
    final LinkedList<SelectionListener> listeners = new LinkedList<>();

    final HashMap<Integer, Set<EntityReader>> controlGroups = new HashMap<>();

    public SelectionManager(UiClientContext gameState) {
        this.context = gameState;
    }

    public int getSelectionPriority(EntityReader entity) {
        EntitySpec type = entity.getType();
        if (type == null) {
            return -1;
        }
        boolean owned = entity.isOwnedBy(context.clientGameState.currentPlayer);
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
        for (Map.Entry<Integer, Set<EntityReader>> entry : controlGroups.entrySet()) {
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

    public Set<EntityReader> recallControlGroup(int i) {
        synchronized (selectedUnits) {
            Set<EntityReader> entityIds = controlGroups.get(i);
            if (entityIds == null) return Collections.emptySet();

            Set<EntityReader> newSelectedUnits = new HashSet<>();
            for (EntityReader id : entityIds) {
                if (id.noLongerExists())
                    continue;
                newSelectedUnits.add(id);
            }
            select(newSelectedUnits);
            return entityIds;
        }
    }

    public boolean isSelected(EntityReader entity) {
        return selectedUnits.contains(entity);
    }


    public void select(Set<EntityReader> entityIds) {
        synchronized (selectedUnits) {
            selectedUnits.clear();
            selectedUnits.addAll(entityIds);
        }
        notifyListeners();
    }

    public void select(EntityReader value) {
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
        Set<EntityReader> possibleSelections = context.clientGameState.gameState.locationManager.getEntitiesWithin(gr1x, gr1y, gr2x, gr2y, entity -> {
            if (entity.isHidden()) return false;
            if (type == null) return true;
            EntitySpec t = entity.getType();
            return t != null && t.name.equals(type.name);
        });

        synchronized (selectedUnits) {
            selectedUnits.clear();
            int currentSelectionPriority = -1;
            for (EntityReader entity : possibleSelections) {
                int selectionPriority = getSelectionPriority(entity);
                if (selectionPriority > currentSelectionPriority) {
                    selectedUnits.clear();
                    currentSelectionPriority = selectionPriority;
                }
                if (selectionPriority == currentSelectionPriority) {
                    selectedUnits.add(entity);
                }
            }
        }

        notifyListeners();
    }

    public void notifyListeners() {
        context.uiManager.log("Selected " + selectedUnits.size() + " units");
        context.executorService.submit(() -> {
            Collection<SelectionListener> clone;
            synchronized (listeners) {
                clone = (Collection<SelectionListener>) listeners.clone();
            }
            for (SelectionListener listener : clone) {
                listener.selectionChanged(getSelectedUnits());
            }
        });
    }

    public List<EntityReader> getSelectedUnits() {
        synchronized (selectedUnits) {
            return (List<EntityReader>) selectedUnits.clone();
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
        void selectionChanged(List<EntityReader> newSelectedUnits);
    }
}
