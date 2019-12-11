package app;

import app.assignments.ResourceAssignment;
import app.assignments.StackAssignment;
import app.assignments.UnitAssignment;
import app.assignments.Verifier;
import client.state.ClientGameState;
import common.state.EntityReader;
import common.state.spec.ResourceType;
import common.state.sst.GameState;
import common.util.DPoint;

import java.util.*;

public class PersistentAiState {

    private final PlayerAiContext context;

    ResourceAssignment hunters;
    ResourceAssignment berryGatherers;
    ResourceAssignment lumberJacks;
    ResourceAssignment stoneGatherers;

    StackAssignment dropOffs;
    StackAssignment transporters;
    UnitAssignment builders;
    StackAssignment garrisoners;

    HashMap<EntityReader, EntityReader> garrisonServicers;

    HashSet<EntityReader> constructionZones = new HashSet<>();
    HashSet<EntityReader> constructionWorkers = new HashSet<>();

    HashSet<EntityReader> allocatedCarts = new HashSet<>();

    // Should not be here
    HashMap<EntityReader, DPoint> desiredDropOffLocations = new HashMap<>();


    int desiredNumTransportWagons = 0;
    boolean desiresMoreDropoffWagons;

    PersistentAiState(PlayerAiContext context) {
        this.context = context;

        hunters = new ResourceAssignment("Hunter", context, state().gameSpec.getResourceType("food"), 5, Verifier.minimal(clientGameState()));
        berryGatherers = new ResourceAssignment("Berry Gatherer", context, state().gameSpec.getResourceType("food"), 1, Verifier.minimal(clientGameState()));
        lumberJacks = new ResourceAssignment("Lumber jack", context, state().gameSpec.getResourceType("wood"), 1, Verifier.minimal(clientGameState()));
        stoneGatherers = new ResourceAssignment("Stone Gatherer", context, state().gameSpec.getResourceType("stone"), 1, Verifier.minimal(clientGameState()));

        transporters = new StackAssignment("Transporter", context, 13, null);
        dropOffs = new StackAssignment("Drop off", context, 12, null);
        builders = new StackAssignment("Builder", context,11, null);
        garrisoners = new StackAssignment("Garrisoner", context, 10, null);

        garrisonServicers = new HashMap<>();
    }

    boolean isIdle(EntityReader entity) {
        if (!entity.isIdle() || context.clientGameState.aiManager.isControlling(entity) || entity.isHidden()) {
            return false;
        }

        if (hunters.contains(entity)) return false;
        if (berryGatherers.contains(entity)) return false;
        if (lumberJacks.contains(entity)) return false;
        if (stoneGatherers.contains(entity)) return false;
        if (dropOffs.contains(entity)) return false;
        if (transporters.contains(entity)) return false;
        if (builders.contains(entity)) return false;
        if (garrisoners.contains(entity)) return false;
        if (garrisonServicers.values().contains(entity)) return false;
        if (constructionWorkers.contains(entity)) return false;

        return true;
    }


    public void remove(EntityReader reader) {
        hunters.remove(reader);
        berryGatherers.remove(reader);
        lumberJacks.remove(reader);
        stoneGatherers.remove(reader);
        transporters.remove(reader);
        builders.remove(reader);
        garrisoners.remove(reader);
        garrisonServicers.remove(reader); garrisonServicers.entrySet().removeIf(e -> e.getValue().equals(reader));
        constructionZones.remove(reader);
        constructionWorkers.remove(reader);
    }

    EntityReader getUnitOn(ResourceType key) {
        switch (key.name) {
            case "food":
                if (hunters.size() > 0) {
                    if (berryGatherers.size() > 0) {
                        if (context.random.nextBoolean()) {
                            return hunters.peek();
                        } else  {
                            return berryGatherers.peek();
                        }
                    } else {
                        return hunters.peek();
                    }
                } else {
                    if (berryGatherers.size() > 0) {
                        return berryGatherers.peek();
                    } else {
                        throw new RuntimeException("No units on food");
                    }
                }
            case "stone":
                return stoneGatherers.peek();
            case "wood":
                return lumberJacks.peek();
            default:
                throw new RuntimeException("Uh oh");
        }
    }

    private GameState state() {
        return clientGameState().gameState;
    }
    private ClientGameState clientGameState() {
        return context.clientGameState;
    }

    EntityReader getNextConstructionZone() {
        LinkedList<EntityReader> list = new LinkedList<>(constructionZones);
        Collections.shuffle(list, context.random);
        return list.get(0);
    }

    Map<ResourceType,Integer> getMinimumOnResources(TickProcessingState state) {
        Map<ResourceType, Integer> minimum = new HashMap<>(1);
        minimum.put(context.clientGameState.gameState.gameSpec.getResourceType("food"), 2 * state.getNumberOf("brothel"));
        return minimum;
    }
}
