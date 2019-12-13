package app;

import app.algo.KMeans;
import app.assign.*;
import client.ai.ai2.DeliverAll;
import client.ai.ai2.Move;
import client.ai.ai2.OneTripTransportTo;
import common.DebugGraphics;
import common.algo.ConnectedSet;
import common.state.EntityReader;
import common.state.Occupancy;
import common.util.DPoint;
import common.util.MapUtils;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DropoffManager {

    private final KMeans[] resourceClusters = new KMeans[2];
    private final PlayerAiContext context;

    DropoffManager(PlayerAiContext context) {
        this.context = context;
    }

    public void clear(int numberOfAssignments) {
        if (resourceClusters[0] == null || resourceClusters[0].getK() != numberOfAssignments) {
            resourceClusters[0] = new KMeans(context.random, numberOfAssignments);
            resourceClusters[1] = new KMeans(context.random, numberOfAssignments + 2);
        } else {
            for (KMeans resourceCluster : resourceClusters)
                resourceCluster.reset();
        }
    }

    private void addDesiredDropoffCarts(AiCheckContext context) {
        double cost1 = resourceClusters[0].totalCost();
        double cost2 = resourceClusters[1].totalCost();
        if (cost1 - cost2 <= AiConstants.DISTANCE_DELTA_TO_CREATE_WAGONS /* && cost2 >= 0.25 * cost1 */) {
            context.goals.wantsMoreDropoffWagons = false;
            return;
        }

        /// TODO: remove them when they are not needed anymore...
        System.out.println(cost1 + ", " + cost2 + ", " + cost2 / cost1);

        // Some sync issues here...
        UnitAssignment gatherCart = context.assignments.nextWagon(AiConstants.DROPOFF_CART_PRIORITY, AssignmentType.DropOffWagon);
        if (gatherCart == null) {
            context.goals.wantsMoreDropoffWagons = true;
            return;
        }
        UnitAssignment driver = context.assignments.nextHuman(AiConstants.DROPOFF_CART_PRIORITY, AssignmentType.DropOffDriver);
        if (driver == null)
            return;
        gatherCart.remove();
        driver.remove();

        Assigner.assignToDropOff(context, driver.entity, gatherCart.entity, AiConstants.DROPOFF_CART_PRIORITY);
    }

    public void assignDropoffs(AiCheckContext context) {
        clear(context.assignments.getNumberOfAssignments(AssignmentType.DropOffDriver));

        // Maybe don't include transport wagons as locations?
        // Once the wagon is full, it needs to deliver its contents somewhere...
        for (Map.Entry<EntityReader, TickProcessingState.GatherLocation> entry : context.tickState.gatherLocations.entrySet())
            for (KMeans kmeans : resourceClusters)
                kmeans.setResourceLocation(entry.getKey().entityId, entry.getValue().location, entry.getValue().resource);
        for (Map.Entry<EntityReader, TickProcessingState.StorageLocation> entry : context.tickState.storageLocations.entrySet()) {
            if (context.assignments.isAssigned(entry.getKey(), AssignmentType.DropOffWagon))
                continue;
            for (KMeans kmeans : resourceClusters)
                kmeans.setStorageLocation(entry.getKey().entityId, entry.getValue().location, entry.getValue().resources);
        }

        for (KMeans resourceCluster : resourceClusters)
            resourceCluster.update(AiConstants.NUM_KMEANS_UPDATES);

        addDesiredDropoffCarts(context);
        moveDropOffsToDesiredLocations(context);
    }

    private void moveDropOffsToDesiredLocations(AiCheckContext context) {
        Set<UnitAssignment> wagonAssignments = context.assignments.getAssignments(AssignmentType.DropOffWagon);

        DPoint[] centers = resourceClusters[0].getCenters();
        DPoint[] currentLocations = new DPoint[wagonAssignments.size()];
        DropoffAssignmentArg[] args = new DropoffAssignmentArg[wagonAssignments.size()];
        DPoint[] desiredLocations = new DPoint[wagonAssignments.size()];
        EntityReader[] entities = new EntityReader[wagonAssignments.size()];

        int index = 0;
        for (UnitAssignment assignment : wagonAssignments) {
            if (assignment.entity.getRiding() == null) {
                continue;
            }
            if (index >= centers.length)
                break;
            if (assignment.args == null) {
                assignment.args = new DropoffAssignmentArg();
            } else {
                if (!(assignment.args instanceof DropoffAssignmentArg))
                    throw new IllegalStateException();
            }
            args[index] = (DropoffAssignmentArg) assignment.args;

            boolean isFull = MapUtils.sum(assignment.entity.getAmountOfResourceAbleToAccept()) == 0;
            boolean isEmpty = assignment.entity.getCarrying().getWeight() == 0;
            if (isFull) {
                if (args[index].delivering && context.aiManager().get(assignment.entity) instanceof DeliverAll)
                    continue;
                args[index].delivering = true;
                context.aiManager().set(assignment.entity, new DeliverAll(assignment.entity));
                continue;
            } else if (args[index].delivering && isEmpty) {
                args[index].delivering = false;
            }
            currentLocations[index] = assignment.entity.getCenterLocation();
            desiredLocations[index] = centers[index];
            entities[index] = assignment.entity;
            ++index;
        }

        for (int i = 0; i < index; i++) {
            for (int j = 0; j < index; j++) {
                if (i == j) continue;

                double d1 = currentLocations[i].distanceTo(desiredLocations[i]) + currentLocations[j].distanceTo(desiredLocations[j]);
                double d2 = currentLocations[i].distanceTo(desiredLocations[j]) + currentLocations[j].distanceTo(desiredLocations[i]);
                if (d1 < d2) continue;

                DPoint tmp = desiredLocations[j];
                desiredLocations[j] = desiredLocations[i];
                desiredLocations[i] = tmp;
            }
        }

        for (int i = 0; i < index; i++) {
            Point emptyTile = ConnectedSet.findNearestEmptyTile(
                    context.clientGameState.gameState.gameSpec,
                    desiredLocations[i].toPoint(),
                    Occupancy.createStaticOccupancy(context.clientGameState.gameState, context.clientGameState.currentPlayer)
            );
            if (args[i].desiredLocation != null && args[i].desiredLocation.equals(emptyTile))
                continue;
            if (emptyTile == null)
                continue;
            if (currentLocations[i].distanceTo(emptyTile.x, emptyTile.y) < 5)
                continue;
            // Don't move them quite so close...
            args[i].desiredLocation = emptyTile;
            context.aiManager().set(entities[i], new Move(entities[i], new DPoint(emptyTile)));
        }
    }


//    private void moveDropOffToDesiredLocation(EntityReader entity, PersistentAiState persistentAiState, EntityReader riding) {
//        DPoint desiredLocation = persistentAiState.desiredDropOffLocations.get(riding);
//        if (desiredLocation == null)
//            return;
//        if (desiredLocation.distanceTo(entity.getLocation()) < 5)
//            return;
//        Point nearestEmptyTile = ConnectedSet.findNearestEmptyTile(context.clientGameState.gameState.gameSpec, desiredLocation.toPoint(), context.clientGameState.gameState.getOccupancyView(context.clientGameState.currentPlayer));
//        if (nearestEmptyTile == null)
//            return;
//        AStar.PathSearch points = GridLocationQuerier.findPathImpl(context.clientGameState.gameState, entity.entityId, new DPoint(nearestEmptyTile), context.clientGameState.currentPlayer);
//        if (points == null)
//            return;
//        context.clientGameState.aiManager.set(entity, new Move(entity, new DPoint(nearestEmptyTile)));
////        context.msgQueue.send(new Message.RequestAction(entity.entityId, new Action.MoveSeq(points.points)));
//    }

    public List<DebugGraphics> getDebugGraphics() {
        return resourceClusters[0].getDebugGraphics();
    }

    private static final class DropoffAssignmentArg implements UnitAssignment.AssignmentArgs {
        Point desiredLocation;
        boolean delivering;
    }
}
