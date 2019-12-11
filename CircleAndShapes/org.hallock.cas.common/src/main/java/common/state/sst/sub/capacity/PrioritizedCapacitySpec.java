package common.state.sst.sub.capacity;

import common.state.spec.GameSpec;
import common.state.spec.ResourceType;
import common.state.sst.sub.Load;
import common.util.json.*;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class PrioritizedCapacitySpec implements Jsonable {

    private int totalWeight = 0;
    private final HashMap<ResourceType, Prioritization> prioritizations = new HashMap<>();

    public PrioritizedCapacitySpec(Collection<ResourceType> allResourceTypes) {
        for (ResourceType resourceType : allResourceTypes)
            prioritizations.put(resourceType, new Prioritization());
    }

    public PrioritizedCapacitySpec(int maxWeight, Collection<ResourceType> allResourceTypes) {
        this(allResourceTypes);
        this.totalWeight = maxWeight;
    }

    public PrioritizedCapacitySpec(PrioritizedCapacitySpec other) {
        this.totalWeight = other.totalWeight;
        for (Map.Entry<ResourceType, Prioritization> rt : other.prioritizations.entrySet()) {
            prioritizations.put(rt.getKey(), new Prioritization(rt.getValue()));
        }
    }

    private PrioritizedCapacitySpec() {}

    public HashMap<ResourceType, Integer> getMaximumAmounts() {
        HashMap<ResourceType, Integer> maximum = new HashMap<>();
        for (Map.Entry<ResourceType, Prioritization> entry : prioritizations.entrySet())
            maximum.put(entry.getKey(), entry.getValue().maximumAmount);
        return maximum;
    }

    public Prioritization getPrioritization(ResourceType resourceType) {
        return prioritizations.get(resourceType);
    }

    public void setTotalWeight(int totalWeight) {
        this.totalWeight = totalWeight; // TODO: what if the total weight increased?
        for (Map.Entry<ResourceType, Prioritization> entry : prioritizations.entrySet()) {
            Prioritization value = entry.getValue();
            int max = totalWeight / entry.getKey().weight;
            value.desiredMaximum = Math.min(value.desiredMaximum, max);
            value.desiredAmount = Math.min(value.desiredAmount, max);
            value.maximumAmount = Math.min(value.maximumAmount, max);
        }
    }

    public int amountPossibleToAccept(Load load, ResourceType resource) {
        return Math.min(
                (totalWeight - load.getWeight()) / resource.weight,
                prioritizations.getOrDefault(resource, Prioritization.MISSING).desiredMaximum - load.quantities.getOrDefault(resource, 0)
        );
    }

    public String getDisplayString() {
        StringBuilder builder = new StringBuilder().append("Maximum weight: ").append(ResourceType.formatWeight(totalWeight)).append(" [");
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

    public int getTotalWeight() {
        return totalWeight;
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

    public static PrioritizedCapacitySpec createIncapableCapacity(Collection<ResourceType> allResourceTypes) {
        PrioritizedCapacitySpec prioritizedCapacitySpec = new PrioritizedCapacitySpec();
        prioritizedCapacitySpec.totalWeight = Integer.MAX_VALUE;
        for (ResourceType resourceType : allResourceTypes) {
            prioritizedCapacitySpec.prioritizations.put(resourceType, Prioritization.createNotAccepted());
        }
        return prioritizedCapacitySpec;
    }
    public static PrioritizedCapacitySpec createCapacitySpec(Collection<ResourceType> allResourceTypes, Map<ResourceType, Integer> carryLimits, boolean makeDesired) {
        PrioritizedCapacitySpec prioritizedCapacitySpec = createIncapableCapacity(allResourceTypes);
        for (Map.Entry<ResourceType, Integer> entry : carryLimits.entrySet()) {
            Prioritization prioritization = new Prioritization();
            prioritization.desiredMaximum = prioritization.maximumAmount = entry.getValue();
            if (makeDesired)
                prioritization.desiredAmount = entry.getValue();
            prioritizedCapacitySpec.prioritizations.put(entry.getKey(), prioritization);
        }
        return prioritizedCapacitySpec;
    }

    public int getMaximumWeightHoldable() {
        return Math.min(totalWeight, sumAllowedResources());
    }
}
