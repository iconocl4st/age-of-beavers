package app;

import client.state.ClientGameState;
import common.state.EntityReader;
import common.state.Player;
import common.state.spec.EntitySpec;
import common.util.DPoint;
import common.util.query.NearestEntityQuery;
import common.util.query.NearestEntityQueryResults;

import java.awt.*;

public class AiUtitlities {

    private final PlayerAiContext context;

    AiUtitlities(PlayerAiContext context) {
        this.context = context;
    }


    EntityReader locateByType(String type) {
        NearestEntityQueryResults query = context.clientGameState.gameState.locationManager.query(new NearestEntityQuery(
                context.clientGameState.gameState,
                new DPoint(context.clientGameState.startingLocation),
                e -> context.clientGameState.gameState.typeManager.get(e).name.equals(type) && context.clientGameState.gameState.playerManager.get(e).equals(Player.GAIA),
                80,
                context.clientGameState.currentPlayer
        ));
        if (query.successful()) {
            return query.getEntity(context.clientGameState.gameState);
        }
        return null;
    }


    public EntityReader findNearestHuntable(EntityReader unit) {
        return null;
    }

    private final int SS = 30;
    DPoint getSpaceForBuilding(EntitySpec brothelType) {
        for (int i = 0; i < 1000; i++) {
            DPoint randomLocation = new DPoint(
                context.clientGameState.startingLocation.x + context.random.nextInt(2 * SS) - SS,
                context.clientGameState.startingLocation.y + context.random.nextInt(2 * SS) - SS
            );
            if (context.clientGameState.gameState.hasSpaceFor(randomLocation, brothelType.size)) {
                return randomLocation;
            }
        }
        return null;
    }
}
