package app;

import common.state.EntityReader;
import common.state.spec.ResourceType;

import java.util.LinkedList;
import java.util.Map;

public class PeoplePuller {
    LinkedList<EntityReader> idles;
    LinkedList<Map.Entry<ResourceType, Integer>> excess;
    PlayerAiImplementation imp;

    PeoplePuller(PlayerAiImplementation imp, LinkedList<EntityReader> idles, LinkedList<Map.Entry<ResourceType, Integer>> excess) {
        this.idles = idles;
        this.excess = excess;
        this.imp = imp;
    }

    public EntityReader next() {
        if (!idles.isEmpty()) {
            return idles.removeFirst();
        }

        while (true) {
            if (excess.isEmpty()) throw new RuntimeException("Tried to pull too many people");
            if (excess.getFirst().getValue() > 0) break;
            excess.removeFirst();
        }
        Map.Entry<ResourceType, Integer> first = excess.getFirst();
        first.setValue(first.getValue() - 1);
        return imp.getUnitOn(first.getKey());
    }

    public void add(EntityReader exTransporter) {
        idles.add(exTransporter);
    }
}
