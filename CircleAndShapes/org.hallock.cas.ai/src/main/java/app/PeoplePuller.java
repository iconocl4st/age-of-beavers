package app;

import common.state.EntityReader;
import common.state.spec.ResourceType;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

public class PeoplePuller {
    LinkedList<EntityReader> idles = new LinkedList<>();
    LinkedList<Map.Entry<ResourceType, Integer>> excess = new LinkedList<>();
    PersistentAiState imp;

    PeoplePuller(PersistentAiState imp) {
        this.imp = imp;
    }

    EntityReader next() {
        if (!idles.isEmpty()) {
            return idles.removeFirst();
        }

        while (true) {
            if (excess.isEmpty())
                throw new RuntimeException("Tried to pull too many people");
            if (excess.getFirst().getValue() > 0)
                break;
            excess.removeFirst();
        }
        Map.Entry<ResourceType, Integer> first = excess.getFirst();
        first.setValue(first.getValue() - 1);
        return imp.getUnitOn(first.getKey());
    }

    void addIdle(EntityReader exTransporter) {
        idles.add(exTransporter);
    }

    private static final Comparator<Map.Entry<ResourceType, Integer>> CMP = (a, b) -> -Integer.compare(a.getValue(), b.getValue());
    void addExcess(Set<Map.Entry<ResourceType,Integer>> entries) {
        excess.addAll(entries);
        excess.sort(CMP);
    }

    void clear() {
        idles.clear();
        excess.clear();
    }

    EntityReader getNextIdle() {
        if (idles.isEmpty()) return null;
        return idles.remove();
    }
}
