package app.assign;

import common.msg.Message;
import common.state.spec.EntitySpec;

import java.awt.*;

public interface PlayerAction {
    void perform(AiCheckContext context);

    static PlayerAction createBuildBuilding(final String buildingName) {
        return context -> {
            EntitySpec buildingType = context.gameSpec().getUnitSpec(buildingName);
            Point location = context.utils.getSpaceForBuilding(buildingType.size);
            if (location == null)
                return;
            context.requester().getWriter().send(new Message.PlaceBuilding(buildingType, location.x, location.y));
        };
    }
}
