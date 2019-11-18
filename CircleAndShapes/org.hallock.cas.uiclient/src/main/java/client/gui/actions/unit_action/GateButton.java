package client.gui.actions.unit_action;

import client.app.UiClientContext;
import common.msg.Message;
import common.state.EntityReader;
import common.state.sst.sub.GateInfo;

import java.io.IOException;

public class GateButton extends UnitAction {

    private final GateInfo.GateState gateState;

    public GateButton(UiClientContext context, GateInfo.GateState state) {
        super(context, "Set to " + state.name());
        this.gateState = state;
    }

    @Override
    public boolean isEnabled(EntityReader entity) {
        return defaultGuardStatement(entity) && entity.getGateState() != null && !entity.getGateState().state.equals(gateState);
    }

    @Override
    public void run(EntityReader entity) {
        c.executorService.submit(() -> {
            try {
                c.writer.send(new Message.ChangeOccupancy(entity.entityId, gateState));
                c.writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
