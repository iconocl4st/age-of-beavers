package common.util;

import common.state.spec.ResourceType;

import java.util.*;

public class MapUtils {
    public static Map<ResourceType, Integer> add(Map<ResourceType, Integer> map, Map<ResourceType, Integer> map1) {
        for (Map.Entry<ResourceType, Integer> entry : map1.entrySet()) {
            if (entry.getValue() == null || entry.getValue() == 0)
                continue;
            int currentAmount = map.getOrDefault(entry.getKey(), 0);
            map.put(entry.getKey(), currentAmount + entry.getValue());
        }
        return map;
    }

    public static Map<ResourceType, Integer> multiply(Map<ResourceType, Integer> map, int amount) {
        for (Map.Entry<ResourceType, Integer> entry : map.entrySet()) {
            if (entry.getValue() == null || entry.getValue() == 0)
                continue;
            entry.setValue(amount * entry.getValue());
        }
        return map;
    }

    public static Map<ResourceType, Integer> subtract(Map<ResourceType, Integer> map, Map<ResourceType, Integer> map1) {
        for (Map.Entry<ResourceType, Integer> entry : map1.entrySet()) {
            if (entry.getValue() == null || entry.getValue() == 0)
                continue;
            int currentAmount = map.getOrDefault(entry.getKey(), 0);
            map.put(entry.getKey(), currentAmount - entry.getValue());
        }
        return map;
    }

    public static Map<ResourceType, Integer> copy(Map<ResourceType, Integer> map) {
        Map<ResourceType, Integer> ret = new HashMap<>(map.size());
        for (Map.Entry<ResourceType, Integer> entry : map.entrySet()) {
            if (entry.getValue() == null || entry.getValue() == 0)
                continue;
            ret.put(entry.getKey(), entry.getValue());
        }
        return ret;
    }

    public static Map<ResourceType, Integer> negate(Map<ResourceType, Integer> map) {
        for (Map.Entry<ResourceType, Integer> entry : map.entrySet()) {
            if (entry.getValue() == null || entry.getValue() == 0)
                continue;
            entry.setValue(-entry.getValue());
        }
        return map;
    }
    public static Map<ResourceType, Integer> positivePart(Map<ResourceType, Integer> map) {
        map.entrySet().removeIf(e -> e.getValue() == null || e.getValue() <= 0);
        return map;
    }

    public static int sum(Map<ResourceType, Integer> map) {
        int sum = 0;
        for (Integer i : map.values())
            sum += i;
        return sum;
    }

    public static Map<ResourceType, Integer> getDesired(int total, Map<ResourceType, Integer> requirements) {
        double sum = sum(requirements);
        Map<ResourceType, Integer> ret = new HashMap<>();
        for (Map.Entry<ResourceType, Integer> entry :  requirements.entrySet())
            ret.put(entry.getKey(), (int) (total * entry.getValue() / sum));
        return ret;
    }

    public static boolean isEmpty(Map<ResourceType, Integer> resources) {
        return sum(resources) == 0;
    }

    public static HashMap<ResourceType, Integer> from(ResourceType resourceType, Integer value) {
        HashMap<ResourceType, Integer> ret = new HashMap<>();
        ret.put(resourceType, value);
        return ret;
    }


//    public static final class Reassignment {
//        public final ResourceType from;
//        public final ResourceType to;
//        public final int amount;
//
//        private Reassignment(ResourceType from, ResourceType to, int amount) {
//            this.from = from;
//            this.to = to;
//            this.amount = amount;
//        }
//    }

//    public static List<Reassignment> getReassignments(Map<ResourceType, Integer> current, Map<ResourceType, Integer> desired) {
//        LinkedList<Map.Entry<ResourceType, Integer>> missing = new LinkedList<>(positivePart(subtract(copy(desired), current)).entrySet());
//
//        excess.sort(CMP);
//        missing.sort(CMP);
//
//        List<Reassignment> ret = new LinkedList<>();
//        int toRemove;
//        rem:
//        while (!excess.isEmpty() && (toRemove = excess.getFirst().getValue()) > 1) {
//            ResourceType from = excess.removeFirst().getKey();
//            while (toRemove > 0) {
//                while (true) {
//                    if (missing.isEmpty()) break rem;
//                    if (missing.getFirst().getValue() > 0) break;
//                    missing.removeFirst();
//                }
//                Map.Entry<ResourceType, Integer> entry = missing.getFirst();
//                int toTransfer = Math.min(toRemove, entry.getValue());
//                entry.setValue(entry.getValue() - toTransfer);
//                toRemove -= toTransfer;
//                ret.add(new Reassignment(from, entry.getKey(), toTransfer));
//            }
//        }
//        return ret;
//    }
}
