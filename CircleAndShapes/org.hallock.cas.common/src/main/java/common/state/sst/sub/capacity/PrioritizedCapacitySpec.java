package common.state.sst.sub.capacity;

import common.state.spec.GameSpec;
import common.state.spec.ResourceType;
import common.state.sst.sub.Load;
import common.util.json.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PrioritizedCapacitySpec implements Jsonable {

    public int totalWeight = 0;
    private final HashMap<ResourceType, Prioritization> prioritizations = new HashMap<>();

    public Prioritization getPrioritization(ResourceType resourceType) {
        return prioritizations.get(resourceType);
    }

    public int amountPossibleToAccept(Load load, ResourceType resource) {
        return Math.min(
                (totalWeight - load.getWeight()) / resource.weight,
                prioritizations.getOrDefault(resource, Prioritization.MISSING).desiredMaximum - load.quantities.getOrDefault(resource, 0)
        );
    }

    public String getDisplayString() {
        StringBuilder builder = new StringBuilder().append("Maximum weight: " + totalWeight).append(" [");
        for (Map.Entry<ResourceType, Prioritization> entry : prioritizations.entrySet()) {
            builder.append(entry.getKey().name).append(':').append(entry.getValue());
        }
        return builder.append(']').toString();
    }

    public int sumAllowedResources() {
        int ret = 0;
        for (Map.Entry<ResourceType, Prioritization> entry : prioritizations.entrySet()) {
            if (entry.getValue().maximumAmount == Integer.MAX_VALUE) {
                return Integer.MAX_VALUE;
            }
            ret += entry.getValue().maximumAmount * entry.getKey().weight;
        }
        return ret;
    }

    @Override
    public void writeTo(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
        writer.writeBeginDocument();
        writer.write("total-weight", totalWeight);
        writer.write("by-resource", prioritizations, ResourceType.Serializer, Prioritization.Serializer, options);
        writer.writeEndDocument();
    }

    public static final DataSerializer<PrioritizedCapacitySpec> Serializer = new DataSerializer.JsonableSerializer<PrioritizedCapacitySpec>() {
        @Override
        public PrioritizedCapacitySpec parse(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
            PrioritizedCapacitySpec ret = new PrioritizedCapacitySpec();
            reader.readBeginDocument();
            ret.totalWeight = reader.readInt32("total-weight");
            reader.read("by-resource", ret.prioritizations, ResourceType.Serializer, Prioritization.Serializer, spec);
            reader.readEndDocument();
            return ret;
        }
    };

    public static PrioritizedCapacitySpec createIncapableCapacity(GameSpec spec) {
        PrioritizedCapacitySpec prioritizedCapacitySpec = new PrioritizedCapacitySpec();
        prioritizedCapacitySpec.totalWeight = Integer.MAX_VALUE;
        for (ResourceType resourceType : spec.resourceTypes) {
            prioritizedCapacitySpec.prioritizations.put(resourceType, Prioritization.createNotAccepted());
        }
        return prioritizedCapacitySpec;
    }
    public static PrioritizedCapacitySpec createCapacitySpec(GameSpec spec, HashMap<ResourceType, Integer> carryLimits) {
        PrioritizedCapacitySpec prioritizedCapacitySpec = createIncapableCapacity(spec);
        for (Map.Entry<ResourceType, Integer> entry : carryLimits.entrySet()) {
            Prioritization prioritization = new Prioritization();
            prioritization.desiredMaximum = prioritization.maximumAmount = entry.getValue();
            prioritizedCapacitySpec.prioritizations.put(entry.getKey(), prioritization);
        }
        return prioritizedCapacitySpec;
    }
}
