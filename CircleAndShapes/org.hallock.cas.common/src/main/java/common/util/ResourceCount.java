package common.util;

import common.state.spec.GameSpec;
import common.state.spec.ResourceType;

import java.util.HashMap;
import java.util.Map;


/*
TODO: Use this instead of MapUtils...
 */
public class ResourceCount {

    private static int GUESS_OF_INITIAL_RESOURCES = 5;
    private int[] quantities;

    public ResourceCount() {
        quantities = new int[GUESS_OF_INITIAL_RESOURCES];
    }

    public ResourceCount(HashMap<ResourceType, Integer> values) {
        int max = GUESS_OF_INITIAL_RESOURCES;
        for (ResourceType rType : values.keySet())
            max = Math.max(max, rType.ordinal);
        quantities = new int[max];
        for (Map.Entry<ResourceType, Integer> entry : values.entrySet()) {
            Integer v = entry.getValue();
            if (v == null) continue;
            quantities[entry.getKey().ordinal] = v;
        }
    }

    public ResourceCount(ResourceCount other) {
        quantities = new int[other.quantities.length];
        System.arraycopy(other.quantities, 0, quantities, 0, other.quantities.length);
    }

    public ResourceCount(ResourceType resource, int value) {
        quantities = new int[Math.max(resource.ordinal + 1, GUESS_OF_INITIAL_RESOURCES)];
        quantities[resource.ordinal] = value;
    }

    public HashMap<ResourceType, Integer> toMap(GameSpec spec) {
        HashMap<ResourceType, Integer> ret = new HashMap<>();
        for (int i = 0; i < quantities.length; i++) {
            if (quantities[i] == 0)
                continue;
            ret.put(spec.resourceTypes.get(i), quantities[i]);
        }
        return ret;
    }

    public ResourceCount clone() {
        return new ResourceCount(this);
    }

    public int get(ResourceType resourceType) {
        if (resourceType.ordinal >= quantities.length)
            return 0;
        return quantities[resourceType.ordinal];
    }

    private void ensureSize(int size) {
        if (quantities.length >= size) return;
        int[] newQuantities = new int[size];
        System.arraycopy(quantities, 0, newQuantities, 0, quantities.length);
        quantities = newQuantities;
    }

    public ResourceCount addTo(ResourceType resource, int amount) {
        ensureSize(resource.ordinal + 1);
        quantities[resource.ordinal] += amount;
        return this;
    }

    public ResourceCount increment(ResourceType resource) {
        addTo(resource, 1);
        return this;
    }

    public ResourceCount add(ResourceCount other) {
        ensureSize(other.quantities.length);
        for (int i = 0; i < other.quantities.length; i++)
            quantities[i] += other.quantities[i];
        return this;
    }

    public ResourceCount subtract(ResourceCount other) {
        ensureSize(other.quantities.length);
        for (int i = 0; i < other.quantities.length; i++)
            quantities[i] -= other.quantities[i];
        return this;
    }

    public ResourceCount multiply(int amount) {
        for (int i = 0; i < quantities.length; i++)
            quantities[i] *= amount;
        return this;
    }

    public ResourceCount positivePart() {
        for (int i = 0; i < quantities.length; i++)
            if (quantities[i] < 0) quantities[i] = 0;
        return this;
    }

    public ResourceCount minimum(ResourceCount other) {
        ensureSize(other.quantities.length);
        for (int i = 0; i < other.quantities.length; i++)
            quantities[i] = Math.min(quantities[i], other.quantities[i]);
        return this;
    }

    public boolean isEmpty() {
        for (int i = 0; i < quantities.length; i++)
            if (quantities[i] != 0)
                return false;
        return true;
    }

    public int sum() {
        int sum = 0;
        for (int i= 0; i < quantities.length; i++) {
            sum += quantities[i];
        }
        return sum;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder(3 * quantities.length);
        for (int i = 0; i < quantities.length; i++) {
            builder.append(quantities[i]);
            if (i < quantities.length - 1)
                builder.append(',');
        }
        return builder.toString();
    }

    public String toString(GameSpec spec) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < quantities.length; i++) {
            if (quantities[i] == 0)
                continue;
            builder.append(spec.resourceTypes.get(i).name).append(':').append(quantities[i]).append(' ');
        }
        return builder.toString();
    }

    public boolean equals(Object other) {
        if (!(other instanceof ResourceCount))
            return false;
        ResourceCount r = (ResourceCount) other;
        int l = Math.min(quantities.length, r.quantities.length);
        for (int i = 0; i < l; i++)
            if (quantities[i] != r.quantities[i]) return false;
        if (quantities.length > r.quantities.length) {
            for (int i = r.quantities.length; i < quantities.length; i++)
                if (quantities[i] != 0) return false;
        } else if (quantities.length < r.quantities.length) {
            for (int i = quantities.length; i < r.quantities.length; i++)
                if (r.quantities[i] != 0) return false;
        }
        return true;
    }
}
