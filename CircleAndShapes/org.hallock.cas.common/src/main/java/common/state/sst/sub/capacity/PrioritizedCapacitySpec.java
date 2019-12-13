package common.state.sst.sub.capacity;

import common.state.spec.ResourceType;
import common.state.sst.sub.Load;
import common.util.json.*;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class PrioritizedCapacitySpec implements Jsonable {


    // TODO: Do not store the values that it cannot accept....

    private int totalWeight = 0;
    private boolean defaultToAccept;
    private final HashMap<ResourceType, Prioritization> prioritizedCapacities = new HashMap<>();

    public PrioritizedCapacitySpec(int maxWeight, boolean defaultToAccept) {
        this.totalWeight = maxWeight;
        this.defaultToAccept = defaultToAccept;
    }

    public PrioritizedCapacitySpec(PrioritizedCapacitySpec other, boolean defaultToAccept) {
        this.totalWeight = other.totalWeight;
        this.defaultToAccept = defaultToAccept;
        for (Map.Entry<ResourceType, Prioritization> rt : other.prioritizedCapacities.entrySet()) {
            prioritizedCapacities.put(rt.getKey(), new Prioritization(rt.getValue()));
        }
    }

    public HashMap<ResourceType, Integer> getMaximumAmounts() {
        HashMap<ResourceType, Integer> maximum = new HashMap<>();
        for (Map.Entry<ResourceType, Prioritization> entry : prioritizedCapacities.entrySet())
            maximum.put(entry.getKey(), entry.getValue().maximumAmount);
        return maximum;
    }

    public Prioritization getPrioritization(ResourceType resourceType) {
        Prioritization prioritization = prioritizedCapacities.get(resourceType);
        if (prioritization != null)
            return prioritization;
        if (defaultToAccept) {
            return new Prioritization(0, Integer.MAX_VALUE, Integer.MAX_VALUE);
        } else {
            return Prioritization.createNotAccepted();
        }
    }

    public void setTotalWeight(int totalWeight) {
        this.totalWeight = totalWeight; // TODO: what if the total weight increased?
        for (Map.Entry<ResourceType, Prioritization> entry : prioritizedCapacities.entrySet()) {
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
                getPrioritization(resource).desiredMaximum - load.quantities.getOrDefault(resource, 0)
        );
    }

    public String getDisplayString() {
        StringBuilder builder = new StringBuilder().append("Maximum weight: ").append(ResourceType.formatWeight(totalWeight)).append(" [");
        for (Map.Entry<ResourceType, Prioritization> entry : prioritizedCapacities.entrySet()) {
            builder.append(entry.getKey().name).append(':').append(entry.getValue());
        }
        return builder.append(']').toString();
    }

    private int sumAllowedResources() {
        int ret = 0;
        for (Map.Entry<ResourceType, Prioritization> entry : prioritizedCapacities.entrySet()) {
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
        writer.write("default-to-accept", defaultToAccept);
        writer.write("by-resource", prioritizedCapacities, ResourceType.Serializer, Prioritization.Serializer, options);
        writer.writeEndDocument();
    }

    public static final DataSerializer<PrioritizedCapacitySpec> Serializer = new DataSerializer.JsonableSerializer<PrioritizedCapacitySpec>() {
        @Override
        public PrioritizedCapacitySpec parse(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
            reader.readBeginDocument();
            int totalWeight = reader.readInt32("total-weight");
            boolean defaultToAccept = reader.readBoolean("default-to-accept");
            PrioritizedCapacitySpec ret = new PrioritizedCapacitySpec(totalWeight,  defaultToAccept);
            reader.read("by-resource", ret.prioritizedCapacities, ResourceType.Serializer, Prioritization.Serializer, spec);
            reader.readEndDocument();
            return ret;
        }
    };

    public static PrioritizedCapacitySpec createCapacitySpec(Map<ResourceType, Integer> carryLimits, boolean makeDesired, boolean defaultToAccept) {
        PrioritizedCapacitySpec prioritizedCapacitySpec = new PrioritizedCapacitySpec(Integer.MAX_VALUE, defaultToAccept);
        for (Map.Entry<ResourceType, Integer> entry : carryLimits.entrySet()) {
            Prioritization prioritization = new Prioritization();
            prioritization.desiredMaximum = prioritization.maximumAmount = entry.getValue();
            if (makeDesired)
                prioritization.desiredAmount = entry.getValue();
            prioritizedCapacitySpec.prioritizedCapacities.put(entry.getKey(), prioritization);
        }
        return prioritizedCapacitySpec;
    }

    public int getMaximumWeightHoldable() {
        return Math.min(totalWeight, sumAllowedResources());
    }
}
