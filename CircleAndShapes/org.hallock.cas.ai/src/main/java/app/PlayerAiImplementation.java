package app;

import client.ai.*;
import client.state.ClientGameState;
import common.msg.Message;
import common.state.EntityId;
import common.state.EntityReader;
import common.state.Player;
import common.state.spec.CreationMethod;
import common.state.spec.CreationSpec;
import common.state.spec.EntitySpec;
import common.state.spec.ResourceType;
import common.state.sst.GameState;
import common.state.sst.manager.RevPair;
import common.state.sst.sub.capacity.Prioritization;
import common.util.DPoint;
import common.util.GridLocationQuerier;
import common.util.MapUtils;
import org.junit.rules.Verifier;

import java.util.*;

class PlayerAiImplementation {
    private final PlayerAiContext context;

    UnitAssignment hunters;
    UnitAssignment berryGatherers;
    UnitAssignment lumberJacks;
    UnitAssignment stoneGatherers;

    UnitAssignment transporters;
    UnitAssignment builders;
    UnitAssignment garrisoners;

    HashMap<EntityReader, EntityReader> garrisonServicers;
    HashMap<EntityReader, EntityReader> constructionServicers;

    // need to retrieve building ids from server when requested...
    // this will be an ai event...
    LinkedList<EntityReader> constructionZones = new LinkedList<>();

    LinkedList<EntityReader> startingGaiaUnits = null;

    PlayerAiImplementation(PlayerAiContext playerAiContext) {
        this.context = playerAiContext;

        hunters = new UnitAssignment(context, state().gameSpec.getResourceType("food"), UnitAssignment.aiIsNotOfClass(clientGameState(), HuntAi.class));
        berryGatherers = new UnitAssignment(context, state().gameSpec.getResourceType("food"), UnitAssignment.aiIsNotOfClass(clientGameState(), HuntAi.class));
        lumberJacks = new UnitAssignment(context, state().gameSpec.getResourceType("wood"), UnitAssignment.aiIsNotOfClass(clientGameState(), HuntAi.class));
        stoneGatherers = new UnitAssignment(context, state().gameSpec.getResourceType("stone"), UnitAssignment.aiIsNotOfClass(clientGameState(), HuntAi.class));

        transporters = new UnitAssignment(context, null, UnitAssignment.aiIsNotOfClass(clientGameState(), TransportAi.class));
        builders = new UnitAssignment(context, null, UnitAssignment.aiIsNotOfClass(clientGameState(), ConstructAi.class));
        garrisoners = new UnitAssignment(context, null, e -> e.noLongerExists() || !e.isHidden());

        garrisonServicers = new HashMap<>();
        constructionServicers = new HashMap<>();
    }


    private GameState state() {
        return clientGameState().gameState;
    }
    private ClientGameState clientGameState() {
        return context.clientGameState;
    }

    private static final Comparator<Map.Entry<ResourceType, Integer>> CMP = (a, b) -> -Integer.compare(a.getValue(), b.getValue());


    // could have the game spec send the starting location...
    private EntityReader locateGaiaByType(DPoint point, String type) {
        GridLocationQuerier.NearestEntityQueryResults query = state().locationManager.query(new GridLocationQuerier.NearestEntityQuery(
                state(),
                point,
                e -> state().typeManager.get(e).name.equals(type) && state().playerManager.get(e).equals(Player.GAIA),
                80,
                clientGameState().currentPlayer
        ));
        if (query.successful()) {
            return query.getEntity(state());
        }
        return null;
    }

    // need to add the initial carts...
    synchronized void updateActions(ActionRequester actionRequester) {
        hunters.verify();
        berryGatherers.verify();
        lumberJacks.verify();
        stoneGatherers.verify();
        transporters.verify();
        builders.verify();
        garrisoners.verify();

        // Need to verify the two maps as well...

        HashMap<ResourceType, Integer> peopleOnResource = new HashMap<>();
        hunters.addNumContributing(peopleOnResource);
        berryGatherers.addNumContributing(peopleOnResource);
        lumberJacks.addNumContributing(peopleOnResource);
        stoneGatherers.addNumContributing(peopleOnResource);

        HashMap<ResourceType, Integer> desiredResources = new HashMap<>();
        HashMap<ResourceType, Integer> collectedResources = new HashMap<>();
        LinkedList<EntityReader> idleHumans = new LinkedList<>();

        LinkedList<EntityReader> garrisonsToService = new LinkedList<>();
        LinkedList<EntityReader> constructionToService = new LinkedList<>();

        // this loop needs to include nearby buildings from the start
        int pop = 0;
        for (RevPair<Player> pair : state().playerManager.getByType(clientGameState().currentPlayer)) {
            EntityReader entity = new EntityReader(state(),  pair.entityId);

            if (startingGaiaUnits == null) {
                startingGaiaUnits = new LinkedList<>();
                EntityReader unit;
                unit = locateGaiaByType(entity.getLocation(), "brothel");
                if (unit != null) startingGaiaUnits.add(unit);
                // don't really need to add this...
                unit = locateGaiaByType(entity.getLocation(), "storage-yard");
                if (unit != null) startingGaiaUnits.add(unit);
            }

            boolean isHuman = entity.getType().name.equals("human");
            if (isHuman) pop += 1;

            if (entity.isIdle() && !clientGameState().aiManager.isControlling(entity)) {
                if (isHuman) {
                    idleHumans.add(entity);
                } else if (!entity.getType().canCreate.isEmpty()) {
                    CreationSpec spec = getBestCreation(entity.getType().canCreate);
                    context.clientGameState.aiManager.startAi(entity.entityId, new CreateAi(clientGameState(), entity, spec));
                }
            }

            if (entity.getType().containsClass("storage")) {
                MapUtils.add(collectedResources, entity.getCarrying().quantities);
            }
            // TODO: set demands...
            Ai currentAi = context.clientGameState.aiManager.getCurrentAi(entity.entityId);
            if (currentAi instanceof CreateAi) {
                CreateAi createAi = (CreateAi) currentAi;
                if (createAi.getCreating().method.equals(CreationMethod.Garrison) && getNumGarrisonsDesired(entity) > entity.getNumGarrisonedUnits()) {
                    garrisonsToService.add(entity);
                }
                Map<ResourceType, Integer> creationResources = MapUtils.multiply(MapUtils.copy(createAi.getCreating().createdType.requiredResources), 2);
                for (Map.Entry<ResourceType, Integer> entry : creationResources.entrySet()) {
                    Prioritization prioritization = entity.getCapacity().getPrioritization(entry.getKey());
                    int desiredMinimum = Math.min(prioritization.maximumAmount, entry.getValue());
                    if (prioritization.desiredAmount < desiredMinimum) {
                        context.msgQueue.send(new Message.SetDesiredCapacity(entity.entityId, entry.getKey(), 1, desiredMinimum, prioritization.maximumAmount));
                    }
                }
                MapUtils.add(desiredResources, creationResources);
            }
        }

        for (EntityReader reader : startingGaiaUnits) {
            if (reader.getNumGarrisonedUnits() == 0  && garrisonServicers.get(reader) == null) {
                garrisonsToService.add(reader);
            }
        }

        for (EntityReader constructionZone : constructionZones) {
            MapUtils.add(desiredResources, constructionZone.getMissingConstructionResources());
            if (constructionServicers.get(constructionZone) == null)
                constructionToService.add(constructionZone);
        }

        int numShiftingToTransport = (int) (Math.max(1, 0.2 * pop) - transporters.size());
        int numShiftableHumans = (
                + MapUtils.sum(peopleOnResource)
                + idleHumans.size()
                - garrisonsToService.size()
                - constructionToService.size()
                - numShiftingToTransport
        );


        Map<ResourceType, Integer> missingResources = MapUtils.abs(MapUtils.subtract(MapUtils.copy(desiredResources), collectedResources));

        Map<ResourceType, Integer> desiredAllocations = MapUtils.getDesired(numShiftableHumans, missingResources);
        LinkedList<Map.Entry<ResourceType, Integer>> excessPeople = new LinkedList<>(MapUtils.abs(MapUtils.subtract(MapUtils.copy(peopleOnResource), desiredAllocations)).entrySet());
        excessPeople.sort(CMP);
        PeoplePuller puller = new PeoplePuller(this, idleHumans, excessPeople);

        if (numShiftingToTransport < 0) {
            for (int i = 0; i < -numShiftingToTransport; i++) {
                EntityReader exTransporter = transporters.pop();
                puller.add(exTransporter);
            }
        } else if (numShiftingToTransport > 0) {
            for (int i = 0; i < numShiftingToTransport; i++) {
                EntityReader pull = pull(puller.next());
                clientGameState().aiManager.startAi(pull.entityId, new TransportAi(clientGameState(), pull));
                transporters.assigned(pull);
            }
        }

        for (EntityReader reader : garrisonsToService) {
            EntityReader pull = pull(puller.next());
            garrisoners.assigned(pull);
            clientGameState().aiManager.startAi(pull.entityId, new GarrisonAi(clientGameState(), pull, reader));
            garrisonServicers.put(reader, pull); // TODO: when is this removed?
        }

        for (EntityReader reader : constructionToService) {
            EntityReader pull = pull(puller.next());
            clientGameState().aiManager.startAi(pull.entityId, new ConstructAi(clientGameState(), pull, reader));
            constructionServicers.put(reader, pull); // TODO: when is this removed?
        }

        LinkedList<Map.Entry<ResourceType, Integer>> missingPeople = new LinkedList<>(MapUtils.abs(MapUtils.subtract(MapUtils.copy(desiredAllocations), peopleOnResource)).entrySet());
        for (Map.Entry<ResourceType, Integer> missingEntry : missingPeople) {
            if (missingEntry.getValue() <= 1)
                continue;
            for (int i = 0; i < missingEntry.getValue(); i++) {
                addTo(pull(puller.next()), missingEntry.getKey());
            }
        }

        // if people are still idle...
        for (EntityReader entity : idleHumans) {
            addTo(entity, state().gameSpec.getResourceType("food"));
        }

        // spend extra resources...
        Map<ResourceType, Integer> excessResources = MapUtils.abs(MapUtils.subtract(MapUtils.copy(collectedResources), desiredResources));
        // spend them
    }

    private int getNumGarrisonsDesired(EntityReader entity) {
        return 1;
    }

    private EntityReader pull(EntityReader reader) {
        hunters.remove(reader);
        berryGatherers.remove(reader);
        lumberJacks.remove(reader);
        stoneGatherers.remove(reader);
        transporters.remove(reader);
        builders.remove(reader);
        garrisoners.remove(reader);
        garrisonServicers.remove(reader);
        constructionServicers.remove(reader);
        return reader;
    }


    public EntityReader getUnitOn(ResourceType key) {
        switch (key.name) {
            case "food":
                if (Math.random() < 0.5 && hunters.size() > 0) {
                    return hunters.peek();
                }
                return berryGatherers.peek();
            case "stone":
                return stoneGatherers.peek();
            case "wood":
                return lumberJacks.peek();
            default:
                throw new RuntimeException("Uh oh");
        }
    }

    private void addTo(EntityReader entity, ResourceType resource) {
        switch (resource.name) {
            case "food":
                if (Math.random() < 0.5) {
                    hunt(entity);
                    hunters.assigned(entity);
                } else {
                    gather(entity, "berry");
                    berryGatherers.assigned(entity);
                }
                break;
            case "stone":
                gather(entity, "stone mine");
                stoneGatherers.assigned(entity);
                break;
            case "wood":
                gather(entity, "tree");
                lumberJacks.assigned(entity);
                break;
            default:
                throw new RuntimeException("Uh oh");
        }
    }

    private void hunt(EntityReader entity) {
        if (!entity.getType().containsClass("hunter"))
            throw new RuntimeException("Can't hunt");
        EntitySpec deer = state().gameSpec.getUnitSpec("deer");
        clientGameState().aiManager.startAi(entity.entityId, new HuntAi(clientGameState(), entity, null, deer));
    }
    private void gather(EntityReader entity, String name) {
        if (!entity.getType().containsClass("gatherer"))
            throw new RuntimeException("Can't gather");
        EntitySpec type = state().gameSpec.getNaturalResource(name);
        clientGameState().aiManager.startAi(entity.entityId, new Gather(clientGameState(), entity, null, type));
    }

    private CreationSpec getBestCreation(Set<CreationSpec> canCreate) {
        return canCreate.iterator().next();
    }
}
