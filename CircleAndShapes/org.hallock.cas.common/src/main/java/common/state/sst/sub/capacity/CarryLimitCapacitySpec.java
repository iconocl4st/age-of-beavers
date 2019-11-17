//package common.state.sst.sub.capacity;
//
//import common.state.spec.GameSpec;
//import common.state.spec.ResourceType;
//import common.state.sst.sub.Load;
//import common.util.json.*;
//
//import java.io.IOException;
//import java.util.HashMap;
//import java.util.Map;
//
//public class CarryLimitCapacitySpec extends CapacitySpec {
//    private final Map<ResourceType, Integer> carryLimits = new HashMap<>();
//
//    public CarryLimitCapacitySpec(Map<ResourceType, Integer> limits) {
//        super(CapacitySpecType.CarryLimit);
//        carryLimits.putAll(limits);
//    }
//
//    public int amountPossibleToAccept(Load load, ResourceType resource) {
//        Integer canAccept = carryLimits.get(resource);
//        if (canAccept == null || canAccept == 0) return 0;
//        Integer currentlyCarrying = load.quantities.get(resource);
//        if (currentlyCarrying == null) return canAccept;
//        return canAccept - currentlyCarrying;
//    }
//
//    @Override
//    public boolean hasRoomFor(Load load, ResourceType resource) {
//        return amountPossibleToAccept(load, resource) > 0;
//    }
//
//    public String toString() {
//        StringBuilder builder = new StringBuilder();
//        for (Map.Entry<ResourceType, Integer> entry : carryLimits.entrySet()) {
//            builder.append(entry.getKey() + ":" + entry.getValue());
//            builder.append(',');
//        }
//        return builder.toString();
//    }
//
//    @Override
//    public void writeInnards(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
//        writer.write("carry-limits", carryLimits, ResourceType.Serializer, DataSerializer.IntegerSerializer, options);
//    }
//
//    public static CapacitySpec finishReading(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
//        HashMap<ResourceType, Integer> carryLimits = new HashMap<>();
//        reader.read("carry-limits", carryLimits, ResourceType.Serializer, DataSerializer.IntegerSerializer, spec);
//        return new CarryLimitCapacitySpec(carryLimits);
//    }
//}