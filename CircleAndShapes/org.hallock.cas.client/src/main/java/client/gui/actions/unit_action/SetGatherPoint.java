package client.gui.actions.unit_action;

import client.app.ClientContext;
import common.msg.Message;
import common.state.EntityReader;
import common.util.DPoint;

import java.io.IOException;

public class SetGatherPoint extends UnitToLocationAction {
    public SetGatherPoint(ClientContext context) {
        super(context, "Set gather point");
    }

    @Override
    public boolean isEnabled(EntityReader entity) {
        return defaultGuardStatement(entity) && entity.getType().canHaveGatherPoint();
    }

    @Override
    public void run(EntityReader entity, DPoint location) {
        c.executorService.submit(() -> {
            try {
                c.writer.send(new Message.SetGatherPoint(entity.entityId, location));
                c.writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
