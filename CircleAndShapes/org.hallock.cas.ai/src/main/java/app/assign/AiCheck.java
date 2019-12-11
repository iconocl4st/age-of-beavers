package app.assign;

import app.AiConstants;
import client.ai.ai2.AiTask;
import client.ai.ai2.Produce;
import common.state.EntityReader;
import common.state.spec.CreationMethod;
import common.state.spec.CreationSpec;
import common.state.spec.ResourceType;
import common.util.MapUtils;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public interface AiCheck {
    void check(AiCheckContext context);

    // TODO: Minimum number of allocations....





    AiCheck TRANSPORT_CHECK = context -> {
        int population = context.tickState.population;
        int numberOfTransporters = context.assignments.getNumberOfAssignments(AssignmentType.TransportDriver);
        int numShiftingToTransport = (int) (Math.max(1, AiConstants.PERCENTAGE_TO_TRANSPORT * population) - numberOfTransporters);
        if (numShiftingToTransport == 0) return;
        while (numShiftingToTransport < 0) {
            context.goals.wantsMoreTransportWagons = false;
            context.assignments.pullByType(AssignmentType.TransportDriver, AiConstants.TRANSPORTATION_PRIORITY);
            numShiftingToTransport += 1;
        }
        while (numShiftingToTransport > 0) {
            UnitAssignment wagon = context.assignments.nextWagon(AiConstants.TRANSPORTATION_PRIORITY, AssignmentType.TransportWagon);
            if (wagon == null) {
                context.goals.wantsMoreTransportWagons = true;
                break;
            }
            UnitAssignment driver = context.assignments.nextHuman(AiConstants.TRANSPORTATION_PRIORITY, AssignmentType.TransportDriver);
            if (driver == null) return;

            Assigner.assignToTransport(context, driver.entity, wagon.entity, AiConstants.TRANSPORTATION_PRIORITY);
            numShiftingToTransport -= 1;
        }
    };

    AiCheck CARPENTER_SHOP_CHECK = new SimpleAiCheck(
            context -> context.goals.wantsMoreWagons() && context.tickState.getNumberOf("carpenter-shop") == 0,
            PlayerAction.createBuildBuilding("carpenter-shop")
    );

    AiCheck BUILD_CORRALS = new SimpleAiCheck(
            context -> context.tickState.population > 7 && context.tickState.getNumberOf("feeding-trough") == 0,
            context -> {
                context.utils.addFences();
                context.utils.addFences();
            }
    );

    AiCheck BUILD_MORE_BROTHELS = new SimpleAiCheck(
            (Condition) context -> {
                Map<ResourceType, Integer> excessResources = MapUtils.subtract(MapUtils.copy(context.tickState.desiredResources), context.tickState.collectedResources);
                return excessResources.getOrDefault(context.gameSpec().getResourceType("food"), 0) > AiConstants.MAXIMUM_FOOD_BEFORE_MORE_BROTHELS
                        && context.tickState.getNumberBuilding("brothel") == 0
                        && context.tickState.population > AiConstants.MINIMUM_POPULATION_PER_BROTHEL_FOR_NEW_BROTHEL * context.tickState.getNumberOf("brothel");
            },
            PlayerAction.createBuildBuilding("brothel")
    );

    AiCheck NO_IDLE_HUMANS = new SimpleAiCheck(
            Condition.TRUE,
            context -> {
                EntityReader entity;
                while ((entity = context.assignments.pullHuman(AiConstants.STAY_BUSY_PRIORITY - 1, null)) != null)
                    Assigner.assignToGather(context, entity, context.gameSpec().getUnitSpec("berry"), AiConstants.STAY_BUSY_PRIORITY);
            }
    );

    AiCheck BUILD_MORE_STORAGE = new SimpleAiCheck(
            context -> context.tickState.determineStoragePercentage() > AiConstants.MAXIMUM_STORAGE_PERCENTAGE
                    && context.tickState.getNumberBuilding("storage-yard") == 0,
            PlayerAction.createBuildBuilding("storage-yard")
    );

    AiCheck CHECK_NUM_CONSTRUCTION_WORKERS = context -> {
        int numberOfConstructionZones = context.tickState.constructionZones.size();
        int numberOfConstructionWorkers = context.assignments.getNumberOfAssignments(AssignmentType.Constructer);
        if (numberOfConstructionZones == 0) {
            context.assignments.clearType(AssignmentType.Constructer);
            return;
        }

        Map<ResourceType, Integer> missingConstructionMaterials = new HashMap<>();
        Map<ResourceType, Integer> constructionMaterials = new HashMap<>();
        boolean hasBuildable = false;
        for (EntityReader entity : context.tickState.constructionZones) {
            Map<ResourceType, Integer> missing = entity.getMissingConstructionResources();
            hasBuildable |= MapUtils.sum(missing) == 0;
            MapUtils.add(missingConstructionMaterials, missing);
            MapUtils.add(constructionMaterials, entity.getCarrying().quantities);
        }
        if (!hasBuildable && !MapUtils.containsAny(
                MapUtils.subtract(MapUtils.copy(context.tickState.collectedResources), constructionMaterials),
                constructionMaterials.keySet()
        )) {
            context.assignments.clearType(AssignmentType.Constructer);
            return;
        }

        int desiredNumberOfConstructionWorkers = Math.max(
                1,
                Math.min(
                    numberOfConstructionZones / 10,
                        (int)(0.1 * context.tickState.population)
                )
        );

        int numToShift = desiredNumberOfConstructionWorkers - numberOfConstructionWorkers;
        while (numToShift < 0) {
            context.assignments.pullByType(AssignmentType.Constructer, AiConstants.CONSTRUCTION_PRIORITY);
            ++numToShift;
        }
        while (numToShift > 0) {
            EntityReader entityReader = context.assignments.pullHuman(AiConstants.CONSTRUCTION_PRIORITY, AssignmentType.Constructer);
            if (entityReader == null)
                break;
            Assigner.assignToConstruction(context, entityReader, AiConstants.CONSTRUCTION_PRIORITY);
            --numToShift;
        }
    };


    // TODO: Clean this up...
    static CreationSpec getRecommendedCreationSpec(AiCheckContext context, EntityReader entity) {
        for (CreationSpec spec : entity.getType().canCreate.collect()) {
            if (spec.createdType.name.equals("human")) {
                return spec;
            }
            if (spec.createdType.name.equals("wagon") && context.goals.wantsMoreWagons()) {
                return spec;
            }
        }
        return null;
    }

    AiCheck CHECK_NUM_GARRISONERS = context -> {
        outer_loop:
        for (EntityReader entity : context.tickState.garrisoners) {
            if (entity.getType().name.equals("fence-gate") || entity.getType().name.equals("feeding-trough")) {

            } else {
                AiTask currentAi = context.aiManager().get(entity);
                if (currentAi instanceof Produce) {
                    // Is this really needed, or handled elsewhere...
                    Produce ai = (Produce) currentAi;
                    if (!ai.getCreating().method.equals(CreationMethod.Garrison))
                        continue;
                } else if (getRecommendedCreationSpec(context, entity) == null) {
                    continue;
                }
            }

            // Need to make sure they deliver their resources first

            int numDesiredToProduce = 1; // could be more...

            int numToShift = numDesiredToProduce - context.assignments.getNumberAssociated(entity, AssignmentType.Garrisoner);
            while (numToShift < 0)
                throw new UnsupportedOperationException("Please implement me");
            while (numToShift > 0) {
                EntityReader next = context.assignments.pullHuman(AiConstants.GARRISON_PRIORITY,  AssignmentType.Garrisoner);
                if (next == null)
                    break outer_loop;
                Assigner.assignToGarrison(context, next, entity, AiConstants.GARRISON_PRIORITY);
                --numToShift;
            }
        }
    };

    AiCheck CHECK_PRODUCING = context -> {
        for (EntityReader entity : context.assignments.collectIdle(entity -> {
            CreationSpec cSpec = getRecommendedCreationSpec(context, entity);
            if (cSpec == null)
                return false;
            if (cSpec.method.equals(CreationMethod.Garrison) && entity.getNumGarrisonedUnits() == 0)
                return false;
            return true;
        })) {

            CreationSpec cSpec = getRecommendedCreationSpec(context, entity);
            if (cSpec == null)
                continue;
            Assigner.assignToProduce(context, entity, cSpec, AiConstants.PRODUCE_PRIORITY);
        }
    };
    AiCheck MINIMUM_NUMBER_OF_ALLOCATIONS = context -> {
        Map<ResourceType, Integer> minimumAllocations = new HashMap<>();
        minimumAllocations.put(context.gameSpec().getResourceType("food"), 3 * context.tickState.getNumberOf("brothel"));

        Map<ResourceType, Integer> peopleOnResource = new HashMap<>();
        context.assignments.collectResourceAssignments(peopleOnResource, Integer.MAX_VALUE);

        Map<ResourceType, Integer> missingAllocations = MapUtils.positivePart(MapUtils.subtract(MapUtils.copy(minimumAllocations), peopleOnResource));
        for (Map.Entry<ResourceType, Integer> entry : missingAllocations.entrySet()) {
            for (int i = 0; i< entry.getValue();i++) {
                AssignmentType assignmentType = Assigner.getAssignmentTypeFor(Assigner.getNaturalResourceFrom(context, entry.getKey()));
                EntityReader reader = context.assignments.pullHuman(AiConstants.MINIMUM_NUMBER_OF_ALLOCATIONS_PRIORITY, assignmentType);
                if (reader == null)
                    return;
                Assigner.assignTo(context, reader, entry.getKey(), AiConstants.MINIMUM_NUMBER_OF_ALLOCATIONS_PRIORITY);
            }
        }
    };

    Comparator<Map.Entry<ResourceType, Integer>> ENTRY_CMP = Comparator.comparingInt(Map.Entry::getValue);
    AiCheck CHECK_MISSING_RESOURCES = context -> {
        HashMap<ResourceType, Integer> peopleOnResource = new HashMap<>();
        context.assignments.collectResourceAssignments(peopleOnResource, Integer.MAX_VALUE);
        HashMap<ResourceType, Integer> shiftablePeopleOnResource = new HashMap<>();
        context.assignments.collectResourceAssignments(shiftablePeopleOnResource, AiConstants.COLLECT_MISSING_RESOURCES_PRIORITY);
        Map<ResourceType, Integer> missingResources = MapUtils.positivePart(MapUtils.subtract(MapUtils.copy(context.tickState.desiredResources), context.tickState.collectedResources));
        Map<ResourceType, Integer> desiredAllocations = MapUtils.getDesired(MapUtils.sum(peopleOnResource), missingResources);
        Map<ResourceType, Integer> deficits = MapUtils.positivePart(MapUtils.subtract(MapUtils.copy(desiredAllocations), peopleOnResource));
        Map<ResourceType, Integer> excess = MapUtils.minimum(
                shiftablePeopleOnResource,
                MapUtils.positivePart(MapUtils.subtract(MapUtils.copy(peopleOnResource), desiredAllocations))
        );

        LinkedList<Map.Entry<ResourceType, Integer>> deficitEntries = new LinkedList<>(deficits.entrySet());
        LinkedList<Map.Entry<ResourceType, Integer>> excesssEntries = new LinkedList<>(excess.entrySet());
        deficitEntries.sort(ENTRY_CMP.reversed());
        excesssEntries.sort(ENTRY_CMP.reversed());
        int numToAdd;
        rem:
        while (!deficitEntries.isEmpty() && (numToAdd = deficitEntries.getFirst().getValue()) > 1) {
            ResourceType toResource = deficitEntries.removeFirst().getKey();
            while (numToAdd > 0) {
                while (true) {
                    if (excesssEntries.isEmpty()) break rem;
                    if (excesssEntries.getFirst().getValue() > 0) break;
                    excesssEntries.removeFirst();
                }
                Map.Entry<ResourceType, Integer> entry = excesssEntries.getFirst();
                int toTransfer = Math.min(numToAdd, entry.getValue());
                entry.setValue(entry.getValue() - toTransfer);
                numToAdd -= toTransfer;

                for (int i = 0; i < toTransfer; i++) {
                    AssignmentType assignmentType = Assigner.getAssignmentTypeFor(Assigner.getNaturalResourceFrom(context, entry.getKey()));
                    EntityReader reader = context.assignments.pullByType(assignmentType, AiConstants.COLLECT_MISSING_RESOURCES_PRIORITY);
                    if (reader == null)
                        throw new IllegalStateException();
                    Assigner.assignTo(context, reader, toResource, AiConstants.COLLECT_MISSING_RESOURCES_PRIORITY);
                }
            }
        }
    };
}
