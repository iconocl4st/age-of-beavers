package app.assign;

import app.assignments.Verifier;
import client.ai.ai2.*;
import common.state.EntityReader;
import common.state.spec.CreationSpec;
import common.state.spec.EntitySpec;
import common.state.spec.ResourceType;
import common.util.DPoint;

import java.util.HashSet;

public class Assigner {
    private static OnRemove createSimpleRemover(AiCheckContext c, EntityReader entity) {
        return () -> {
            c.assignments.clear(entity);
            c.aiManager().set(entity, null);
            if (entity.noLongerExists()) return;
            c.requester().setUnitActionToIdle(entity);
        };
    }

    public static void assignToConstruction(AiCheckContext context, EntityReader entity, int priority) {
        context.assignments.setAssignment(new UnitAssignment(
            context.currentTime(),
            entity,
            priority,
            AssignmentType.Constructer,
            Verifier.minimal(context.clientGameState),
            createSimpleRemover(context, entity),
            () -> {
//                AiTask aiTask = context.aiManager().get(entity);
//                if (aiTask instanceof ConstructAll)
//                    return;
                DPoint location = entity.getLocation();
                if (location == null) return;
                EntityReader closest = null;
                double distance = Double.MAX_VALUE;
                for (EntityReader reader : context.tickState.constructionZones) {
                    DPoint cLocation = reader.getLocation();
                    if (cLocation == null)
                        continue;
                    double d = cLocation.distanceTo(location);
                    if (d < distance) {
                        distance = d;
                        closest = reader;
                    }
                }
                if (closest == null) return;
                context.aiManager().set(entity, new ConstructAll(entity, closest));
            }
        ));
        context.aiManager().set(entity, new ConstructAll(entity));
    }


    public static void assignToNothing(AiCheckContext context, EntityReader entity, int priority) {
        context.assignments.setAssignment(new UnitAssignment(
                context.currentTime(),
                entity,
                priority,
                AssignmentType.Idle,
                Verifier.minimal(context.clientGameState),
                createSimpleRemover(context, entity),
                OnIdle.StayIdle
        ));
        context.aiManager().set(entity, new ConstructAll(entity));
    }

    public static void assignToHunt(AiCheckContext context, EntityReader entity, EntitySpec huntedType, int priority) {
        HashSet<ResourceType> collecting = new HashSet<>(huntedType.carrying.keySet());
        context.assignments.setAssignment(new ResourceAssignment(
                context.currentTime(),
                entity,
                priority,
                AssignmentType.Hunt,
                Verifier.minimal(context.clientGameState),
                createSimpleRemover(context, entity),
                OnIdle.StayIdle,
                collecting
        ));
        context.aiManager().set(entity, new Hunt(entity, null, huntedType));
    }

    static void assignToGather(AiCheckContext context, EntityReader entity, EntitySpec resourceType, int priority) {
        HashSet<ResourceType> collecting = new HashSet<>(resourceType.carrying.keySet());
        context.assignments.setAssignment(new ResourceAssignment(
                context.currentTime(),
                entity,
                priority,
                getAssignmentTypeFor(resourceType),
                Verifier.minimal(context.clientGameState),
                createSimpleRemover(context, entity),
                OnIdle.StayIdle,
                collecting
        ));
        context.aiManager().set(entity, new Gather(entity, null, resourceType));
    }

    public static void assignToGarrison(AiCheckContext context, EntityReader entity, EntityReader toGarrisonIn, int priority) {
        context.assignments.setAssignment(new ToEntityAssignment(
                context.currentTime(),
                entity,
                priority,
                AssignmentType.Garrisoner,
                Verifier.minimal(context.clientGameState),
                () -> {
                    context.assignments.clear(entity);
                    context.requester().setUnitActionToExit(entity);
                    context.aiManager().set(entity, null);
                },
                OnIdle.StayIdle,
                toGarrisonIn
        ));
        context.aiManager().set(entity, WhileWithinProximity.createGarrison(entity, toGarrisonIn));
    }

    private static OnRemove createOnRemoveWithRider(AiCheckContext c, EntityReader entity, EntityReader wagon) {
        return () -> {
            c.assignments.clear(entity);
            c.assignments.clear(wagon);
            c.requester().setUnitActionToDismount(wagon);
            c.aiManager().set(entity, null);
            c.aiManager().set(wagon, null);
            c.requester().setUnitActionToIdle(entity);
            c.requester().setUnitActionToIdle(wagon);
        };
    }

    public static void assignToTransport(AiCheckContext context, EntityReader entity, EntityReader wagon, int priority) {
        OnRemove onRemove = createOnRemoveWithRider(context, entity, wagon);
        context.assignments.setAssignment(new ToEntityAssignment(
                context.currentTime(),
                entity,
                priority,
                AssignmentType.TransportDriver,
                Verifier.minimal(context.clientGameState),
                onRemove,
                OnIdle.StayIdle,
                wagon
        ));
        context.assignments.setAssignment(new ToEntityAssignment(
                context.currentTime(),
                wagon,
                priority,
                AssignmentType.TransportWagon,
                Verifier.minimal(context.clientGameState),
                onRemove,
                () -> {
                    if (wagon.getRiding() == null) return;
                    context.clientGameState.aiManager.set(wagon, new TransportAi(wagon));
                },
                entity
        ));
        context.aiManager().set(entity, WhileWithinProximity.createBeRidden(entity, wagon));
    }

    public static void assignToDropOff(AiCheckContext context, EntityReader entity, EntityReader wagon, int priority) {
        OnRemove onRemove = createOnRemoveWithRider(context, entity, wagon);
        context.assignments.setAssignment(new ToEntityAssignment(
                context.currentTime(),
                entity,
                priority,
                AssignmentType.DropOffDriver,
                Verifier.minimal(context.clientGameState),
                onRemove,
                OnIdle.StayIdle,
                wagon
        ));
        context.assignments.setAssignment(new ToEntityAssignment(
                context.currentTime(),
                wagon,
                priority,
                AssignmentType.DropOffWagon,
                Verifier.minimal(context.clientGameState),
                onRemove,
                OnIdle.StayIdle,
                entity
        ));
        context.aiManager().set(entity, WhileWithinProximity.createBeRidden(entity, wagon));
    }

    public static void assignToProduce(AiCheckContext context, EntityReader entity, CreationSpec spec, int priority) {
        context.assignments.setAssignment(new UnitAssignment(
                context.currentTime(),
                entity,
                priority,
                AssignmentType.Producer,
                Verifier.minimal(context.clientGameState),
                () -> {
                    context.assignments.clear(entity);
                    context.aiManager().set(entity, null);
                    context.requester().setUnitActionToIdle(entity);
                },
                OnIdle.StayIdle
        ));
        context.clientGameState.aiManager.set(entity, new Produce(entity, spec));
    }

    public static void assignTo(AiCheckContext context, EntityReader reader, ResourceType toResource, int priority) {
        assignToGather(context, reader, getNaturalResourceFrom(context, toResource), priority);
    }

    static AssignmentType getAssignmentTypeFor(EntitySpec resourceType) {
        switch (resourceType.name) {
            case "gold stone":
                return AssignmentType.GoldMiner;
            case "tree":
                return AssignmentType.Lumberjack;
            case "rocks":
                return AssignmentType.RockMiner;
            case "berry":
                return AssignmentType.GatherBerries;
            case "marble stone":
            case "meat":
            default:
                throw new IllegalArgumentException();
        }
    }

    static EntitySpec getNaturalResourceFrom(AiCheckContext context, ResourceType resource) {
        switch (resource.name) {
            case "food":
                return context.gameSpec().getUnitSpec("berry");
            case "wood":
                return context.gameSpec().getUnitSpec("tree");
            case "gold":
                return context.gameSpec().getUnitSpec("gold stone");
            case "stone":
                return context.gameSpec().getUnitSpec("rocks");
            default:
                throw new UnsupportedOperationException(resource.name);
        }
    }
}
