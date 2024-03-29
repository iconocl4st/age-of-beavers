package app;

import client.ai.ai2.AiTask;
import client.ai.ai2.Produce;
import client.state.ClientGameState;
import common.msg.Message;
import common.state.EntityReader;
import common.state.Player;
import common.state.spec.CreationMethod;
import common.state.spec.CreationSpec;
import common.state.spec.EntitySpec;
import common.state.spec.ResourceType;
import common.state.sst.GameState;
import common.util.DPoint;
import common.util.query.NearestEntityQuery;
import common.util.query.NearestEntityQueryResults;

import java.util.*;

public class PersistentAiState {

    private final PlayerAiContext context;

    UnitAssignment hunters;
    UnitAssignment berryGatherers;
    UnitAssignment lumberJacks;
    UnitAssignment stoneGatherers;

    UnitAssignment dropOffs;
    UnitAssignment transporters;
    UnitAssignment builders;
    UnitAssignment garrisoners;

    HashMap<EntityReader, DPoint> desiredDropOffLocations = new HashMap<>();

    HashMap<EntityReader, EntityReader> garrisonServicers;

    HashSet<EntityReader> constructionZones = new HashSet<>();
    HashSet<EntityReader> constructionWorkers = new HashSet<>();

    HashSet<EntityReader> allocatedCarts = new HashSet<>();


    int desiredNumTransportWagons = 0;
    boolean desiresMoreDropoffWagons;

    PersistentAiState(PlayerAiContext context) {
        this.context = context;

        hunters = new UnitAssignment(context, state().gameSpec.getResourceType("food"), UnitAssignment.minimal(clientGameState()));
        berryGatherers = new UnitAssignment(context, state().gameSpec.getResourceType("food"), UnitAssignment.minimal(clientGameState()));
        lumberJacks = new UnitAssignment(context, state().gameSpec.getResourceType("wood"), UnitAssignment.minimal(clientGameState()));
        stoneGatherers = new UnitAssignment(context, state().gameSpec.getResourceType("stone"), UnitAssignment.minimal(clientGameState()));

        transporters = new UnitAssignment(context, null, null);
        dropOffs = new UnitAssignment(context, null, null);
        builders = new UnitAssignment(context, null, null);
        garrisoners = new UnitAssignment(context, null, null);

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

    void assignDropOffCarts(DPoint[] centers) {
        LinkedList<EntityReader> clone;
        synchronized (dropOffs.entities) { // sync clone probably not needed
            clone = new LinkedList<>(dropOffs.entities);
        }
        DPoint[] currentLocations = new DPoint[clone.size()];
        EntityReader[] riders = new EntityReader[clone.size()];
        int index = 0;
        for (EntityReader reader : clone) {
            EntityReader rider = reader.getRider();
            if (rider == null) {
                continue;
            }
            riders[index] = rider;
            currentLocations[index++] = rider.getLocation();
        }
        if (index == 0 || clone.isEmpty() || centers[0] == null) {
            desiredDropOffLocations.clear();
            return;
        }

        if (index > centers.length) throw new IllegalStateException();

        int[] map = new int[index];
        for (int i = 0; i < index; i++) {
            map[i] = i;
        }

        for (int i = 0; i < index; i++) {
            for (int j = 0; j < index; j++) {
                if (i == j) continue;

                double d1 = currentLocations[i].distanceTo(centers[map[i]]) + currentLocations[j].distanceTo(centers[map[j]]);
                double d2 = currentLocations[i].distanceTo(centers[map[j]]) + currentLocations[j].distanceTo(centers[map[i]]);
                if (d1 < d2) continue;

                int tmp = map[j];
                map[j] = map[i];
                map[i] = tmp;
            }
        }

        desiredDropOffLocations.clear();
        for (int i = 0; i < index; i++) {
            desiredDropOffLocations.put(riders[i], centers[map[i]]);
        }
    }

    void verifyCounts() {
        hunters.verify();
        berryGatherers.verify();
        lumberJacks.verify();
        stoneGatherers.verify();
        transporters.verify();
        dropOffs.verify();
        builders.verify();
        garrisoners.verify();

        UnitAssignment.Verifier mini = UnitAssignment.minimal(context.clientGameState);
        garrisonServicers.entrySet().removeIf(e -> mini.notAsAssigned(e.getValue()) || e.getKey().noLongerExists());
        constructionZones.removeIf(EntityReader::noLongerExists);
        constructionWorkers.removeIf(mini::notAsAssigned);
        allocatedCarts.removeIf(mini::notAsAssigned);
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

    void update(HashMap<ResourceType, Integer> peopleOnResource) {
        hunters.addNumContributing(peopleOnResource);
        berryGatherers.addNumContributing(peopleOnResource);
        lumberJacks.addNumContributing(peopleOnResource);
        stoneGatherers.addNumContributing(peopleOnResource);
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

    void addTo(EntityReader entity, ResourceType resource, PlayerAiImplementation ai) {
        switch (resource.name) {
            case "food":
                if (Math.random() < 0.5 && false) {
                    ai.hunt(entity);
                    hunters.assigned(entity);
                } else {
                    ai.gather(entity, "berry");
                    berryGatherers.assigned(entity);
                }
                break;
            case "stone":
                ai.gather(entity, "rocks");
                stoneGatherers.assigned(entity);
                break;
            case "wood":
                ai.gather(entity, "tree");
                lumberJacks.assigned(entity);
                break;
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

    CreationSpec getRecommendedCreation(Set<CreationSpec> canCreate) {
        for (CreationSpec spec : canCreate) {
            if (spec.createdType.name.equals("human")) {
                return spec;
            }
            if (spec.createdType.name.equals("wagon") && (desiredNumTransportWagons > transporters.size() || desiresMoreDropoffWagons)) {
                return spec;
            }
        }
        return null;
    }

    int getDesiredNumGarrisons(EntityReader entity) {
        AiTask currentAi = context.clientGameState.aiManager.get(entity);
        if (!(currentAi instanceof Produce)) {
            return 0;
        }
        Produce ai = (Produce) currentAi;
        if (!ai.getCreating().method.equals(CreationMethod.Garrison))
            return 0;
        return 1; // could be more...
    }

    Set<EntityReader> getShiftsToTransport(int population, TickProcessingState tickState) {
        int numShiftingToTransport = (int) (Math.max(1, 0.1 * population) - transporters.size());
        if (numShiftingToTransport == 0) return Collections.emptySet();
        if (numShiftingToTransport < 0) {
            EntityReader pop = transporters.pop();
            EntityReader rider = pop.getRider();
            if (rider != null) {
                context.clientGameState.actionRequester.getWriter().send(new Message.StopRiding(rider.entityId));
                allocatedCarts.remove(rider.entityId);
            }
            tickState.peoplePuller.addIdle(pop);
            return Collections.emptySet();
        }

        // (EntityQueryFilter)
        List<NearestEntityQueryResults> wagons = getWagons(numShiftingToTransport);
        if (wagons.isEmpty()) {
            desiredNumTransportWagons = numShiftingToTransport;
            return Collections.emptySet();
        }
        HashSet<EntityReader> readers = new HashSet<>();
        for (NearestEntityQueryResults results : wagons) {
            readers.add(results.getEntity(context.clientGameState.gameState));
        }
        return readers;
    }

    private List<NearestEntityQueryResults> getWagons(int numShiftingToTransport) {
        return context.clientGameState.gameState.locationManager.multiQuery(new NearestEntityQuery(
                context.clientGameState.gameState,
                new DPoint(context.clientGameState.startingLocation),
                entityId -> {
                    EntitySpec type = context.clientGameState.gameState.typeManager.get(entityId);
                    if (type == null || !type.name.equals("wagon")) return false;
                    Player player = context.clientGameState.gameState.playerManager.get(entityId);
                    if (allocatedCarts.contains(new EntityReader(clientGameState().gameState, entityId))) return false;
                    return player != null && player.equals(Player.GAIA);
                },
                80,
                context.clientGameState.currentPlayer,
                numShiftingToTransport
        ));
    }

    int getRequiredToShiftToConstruction(int size, PeoplePuller puller) {
        int currentNumber = constructionWorkers.size();
        int numberOfConstructions = constructionZones.size() + size;
        if (numberOfConstructions == 0) {
            for (EntityReader constructionWorker : constructionWorkers) {
                puller.addIdle(constructionWorker);
            }
            constructionWorkers.clear();
            return 0;
        }
        int required = Math.max(1, numberOfConstructions / 10);
        return Math.max(currentNumber - required, 0);
    }

    EntityReader getNextConstructionZone() {
        LinkedList<EntityReader> list = new LinkedList<>(constructionZones);
        Collections.shuffle(list, context.random);
        return list.get(0);
    }

    EntityReader getShiftToGatherCart(double cost1, double cost2) {
        if (cost1 - cost2 <= 10 /* && cost2 >= 0.25 * cost1 */) {
            desiresMoreDropoffWagons = false;
            return null;
        }

        /// TODO: remove them when they are not needed anymore...
        System.out.println(cost1 + ", " + cost2 + ", " + cost2 / cost1);
        List<NearestEntityQueryResults> wagons = getWagons(1);
        if (wagons.isEmpty()) {
            desiresMoreDropoffWagons = true;
            return null;
        }
        return wagons.iterator().next().getEntity(context.clientGameState.gameState);
    }
}
