//package app;
//
//import app.assign.Assignments;
//import common.state.EntityReader;
//import common.state.spec.EntitySpec;
//import common.state.spec.ResourceType;
//
//import java.util.Comparator;
//import java.util.LinkedList;
//import java.util.Map;
//import java.util.Set;
//
//public class PeoplePuller {
////    private final TickProcessingState tickState;
//    private final Assignments assignments;
//
////    LinkedList<Map.Entry<ResourceType, Integer>> excess = new LinkedList<>();
//
//    PeoplePuller(Assignments assignments, TickProcessingState tickState) {
//        this.assignments = assignments;
////        this.tickState = tickState;
//    }
//
////    public EntitySpec nextWagon() {
////        if (!tickState.idleWagons.isEmpty()) {
////            return tickState.idleWagons.remove();
////        }
////        return null;
////    }
//
//    public EntityReader next(int priority) {
////        if (!tickState.idleVillagers.isEmpty()) {
////            EntityReader ret = tickState.idleVillagers.iterator().next();
////            tickState.idleVillagers.remove(ret);
////            return ret;
////        }
//
//        return assignments.pullPerson(priority);
////        while (true) {
////            if (excess.isEmpty())
////                throw new RuntimeException("Tried to pull too many people");
////            if (excess.getFirst().getValue() > 0)
////                break;
////            excess.removeFirst();
////        }
////        Map.Entry<ResourceType, Integer> first = excess.getFirst();
////        first.setValue(first.getValue() - 1);
////        return imp.getUnitOn(first.getKey());
//    }
//
////    private static final Comparator<Map.Entry<ResourceType, Integer>> CMP = (a, b) -> -Integer.compare(a.getValue(), b.getValue());
////    void addExcess(Set<Map.Entry<ResourceType,Integer>> entries) {
////        excess.addAll(entries);
////        excess.sort(CMP);
////    }
////
////    void clear() {
////        idles.clear();
////        excess.clear();
////    }
//}
