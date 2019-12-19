package app.assign;

import common.msg.Message;
import common.state.spec.CreationSpec;
import common.state.spec.EntitySpec;

import java.awt.*;

public interface PlayerAction {
    void perform(AiCheckContext context);

    static PlayerAction createBuildBuilding(final String name) {
        return context -> {
            CreationSpec buildingType = context.gameSpec().canPlace.find(cs -> cs.createdType.name.equals(name));
            Point location = context.utils.getSpaceForBuilding(buildingType.createdType.size);
            if (location == null)
                return;
            context.requester().getWriter().send(new Message.PlaceBuilding(buildingType, location.x, location.y));
        };
    }
}
