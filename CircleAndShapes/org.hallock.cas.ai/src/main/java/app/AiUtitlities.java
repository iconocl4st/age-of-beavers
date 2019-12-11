package app;

import common.msg.Message;
import common.state.EntityReader;
import common.state.Occupancy;
import common.state.spec.EntitySpec;
import common.util.Util;

import java.awt.*;

//  rename
public class AiUtitlities {

    private final PlayerAiContext context;

    AiUtitlities(PlayerAiContext context) {
        this.context = context;
    }

//    public EntityReader getAWagon() {
//        return context.clientGameState.gameState.locationManager.multiQuery(new NearestEntityQuery(
//                context.clientGameState.gameState,
//                new DPoint(context.clientGameState.startingLocation),
//                entityId -> {
//                    EntitySpec type = context.clientGameState.gameState.typeManager.get(entityId);
//                    if (type == null || !type.name.equals("wagon")) return false;
//                    Player player = context.clientGameState.gameState.playerManager.get(entityId);
//                    if (allocatedCarts.contains(new EntityReader(clientGameState().gameState, entityId))) return false;
//                    return player != null && player.equals(Player.GAIA);
//                },
//                80,
//                context.clientGameState.currentPlayer,
//                numShiftingToTransport
//        ));
//    }


//    EntityReader locateByType(String type) {
//        NearestEntityQueryResults query = context.clientGameState.gameState.locationManager.query(new NearestEntityQuery(
//                context.clientGameState.gameState,
//                new DPoint(context.clientGameState.startingLocation),
//                e -> context.clientGameState.gameState.typeManager.get(e).name.equals(type) && context.clientGameState.gameState.playerManager.get(e).equals(Player.GAIA),
//                80,
//                context.clientGameState.currentPlayer
//        ));
//        if (query.successful()) {
//            return query.getEntity(context.clientGameState.gameState);
//        }
//        return null;
//    }





    public EntityReader findNearestHuntable(EntityReader unit) {
        return null;
    }

    public Point getSpaceForBuilding(Dimension size) {
        return Util.getSpaceForBuilding(
                context.clientGameState.startingLocation,
                size,
                Occupancy.createConstructionOccupancy(context.clientGameState.gameState, context.clientGameState.exploration),
                context.clientGameState.gameState.gameSpec.width,
                AiConstants.BUILDING_PLACEMENT_BUFFER
        );
    }


    public void addFences() {
        EntitySpec fenceType = this.context.clientGameState.gameState.gameSpec.getUnitSpec("fence");
        EntitySpec fenceGateType = this.context.clientGameState.gameState.gameSpec.getUnitSpec("fence-gate");
        EntitySpec feedingTroughType = this.context.clientGameState.gameState.gameSpec.getUnitSpec("feeding-trough");

        Dimension dimension = new Dimension(AiConstants.FENCED_WIDTH, AiConstants.FENCED_WIDTH);
        Point location = getSpaceForBuilding(dimension);

        context.clientGameState.actionRequester.getWriter().send(new Message.PlaceBuilding(fenceGateType, location.x + AiConstants.FENCED_WIDTH / 2, location.y));
        context.clientGameState.actionRequester.getWriter().send(new Message.PlaceBuilding(feedingTroughType, location.x + AiConstants.FENCED_WIDTH / 2, location.y + AiConstants.FENCED_WIDTH / 2));

        for (int i = 0; i < AiConstants.FENCED_WIDTH; i++) {
            if (i != 0 && i != AiConstants.FENCED_WIDTH - 1) {
                context.clientGameState.actionRequester.getWriter().send(new Message.PlaceBuilding(fenceType, location.x, location.y + i));
                context.clientGameState.actionRequester.getWriter().send(new Message.PlaceBuilding(fenceType, location.x + AiConstants.FENCED_WIDTH - 1, location.y + i));
            }

            context.clientGameState.actionRequester.getWriter().send(new Message.PlaceBuilding(fenceType, location.x + i, location.y + AiConstants.FENCED_WIDTH - 1));
            if (i != AiConstants.FENCED_WIDTH / 2)
                context.clientGameState.actionRequester.getWriter().send(new Message.PlaceBuilding(fenceType, location.x + i, location.y));
        }
    }
}
