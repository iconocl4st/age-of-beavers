package common.state.sst.sub;

import common.state.spec.ResourceType;
import common.util.json.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class Load implements Jsonable {

    public static final Load EMPTY_LOAD = new Load();

    public final HashMap<ResourceType, Integer> quantities = new HashMap<>();

    public int getWeight() {
        int sum = 0;
        for (Map.Entry<ResourceType, Integer> quantity : quantities.entrySet()) {
            sum += quantity.getKey().weight * quantity.getValue();
        }
        return sum;
    }

    public ResourceType getNonzeroResource() {
        for (Map.Entry<ResourceType, Integer> entry : quantities.entrySet()) {
            if (entry.getValue() > 0) {
                return entry.getKey();
            }
        }
        return null;
    }

    public void setQuantity(ResourceType resource, int amount) {
        if (amount < 0) {
            throw new IllegalStateException();
        }
        quantities.put(resource, amount);
    }

    public boolean canAfford(Map<ResourceType, Integer> requiredResources) {
        for (Map.Entry<ResourceType, Integer> entry : requiredResources.entrySet()) {
            if (entry.getValue() <= 0)
                continue;
            Integer quantity = quantities.get(entry.getKey());
            if (quantity == null) {
                return false;
            }
            if (quantity < entry.getValue())
                return false;
        }
        return true;
    }

    public void subtract(Map<ResourceType, Integer> requiredResources) {
        for (Map.Entry<ResourceType, Integer> entry : requiredResources.entrySet()) {
            if (entry.getValue() == null || entry.getValue() <= 0)
                continue;
            Integer currentAmount = quantities.get(entry.getKey());
            if (currentAmount == null) throw new IllegalStateException(entry.getKey() + ", " + entry.getValue());
            int newAmount = currentAmount - entry.getValue();
            if (newAmount < 0) throw new IllegalStateException(entry.getKey() + ", " + entry.getValue() + ", " + newAmount);
            quantities.put(entry.getKey(), Math.max(0, newAmount));
        }
    }

    @Override
    public void writeTo(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
        writer.writeBeginDocument();
        writer.write("quantities", quantities, ResourceType.Serializer, DataSerializer.IntegerSerializer, options);
        writer.writeEndDocument();
    }

    public static DataSerializer<Load> Serializer = new DataSerializer.JsonableSerializer<Load>() {
        @Override
        public Load parse(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
            Load load = new Load();
            reader.readBeginDocument();
            reader.read("quantities", load.quantities, ResourceType.Serializer, DataSerializer.IntegerSerializer, spec);
            reader.readEndDocument();
            return load;
        }
    };
}
