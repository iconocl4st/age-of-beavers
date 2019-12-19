package common.state.spec;

import common.util.Immutable;
import common.util.json.*;

import java.io.IOException;

public class GenerationSpec /* implements Jsonable */ {
    public final Immutable.ImmutableList<ResourceGen> resources;
    public final Immutable.ImmutableList<UnitGen> gaia;
    public final Immutable.ImmutableList<ResourceGen> perPlayerResources;
    public final Immutable.ImmutableList<UnitGen> perPlayerUnits;

    public GenerationSpec(
            Immutable.ImmutableList<ResourceGen> resources,
            Immutable.ImmutableList<UnitGen> gaia,
            Immutable.ImmutableList<ResourceGen> perPlayerResources,
            Immutable.ImmutableList<UnitGen> perPlayerUnits
    ) {
        this.resources = resources;
        this.gaia = gaia;
        this.perPlayerResources = perPlayerResources;
        this.perPlayerUnits = perPlayerUnits;
    }

    public static final class ResourceGen /* implements Jsonable */ {
        public final EntitySpec type;
        public final int numberOfPatches;
        public final int patchSize;

        public ResourceGen(EntitySpec type, int numberOfPatches, int patchSize) {
            this.type = type;
            this.numberOfPatches = numberOfPatches;
            this.patchSize = patchSize;
        }
    }

    public static final class UnitGen {
        public final EntitySpec type;
        public final Immutable.ImmutableMap<ResourceType, Integer> carrying;
        public final int number;

        public UnitGen(EntitySpec spec, int number, Immutable.ImmutableMap<ResourceType, Integer> carrying) {
            type = spec;
            this.number = number;
            this.carrying = carrying;
        }
    }
}