//package client.ai;
//
//import client.state.ClientGameState;
//import common.AiAttemptResult;
//import common.Proximity;
//import common.state.EntityReader;
//
//public class GarrisonAi extends Ai {
//
//    private final EntityReader garrisonLocation;
//
//    public GarrisonAi(ClientGameState state, EntityReader toGarrison, EntityReader garrisonLocation)  {
//        super(state, toGarrison);
//        this.garrisonLocation = garrisonLocation;
//    }
//
//    public String toString() {
//        return "garrison";
//    }
//
//    @Override
//    public AiAttemptResult setActions(ActionRequester ar) {
//        if (controlling.isHidden()) {
//            return AiAttemptResult.Completed;
//        }
//        if (Proximity.closeEnoughToInteract(controlling, garrisonLocation)) {
//            ar.setUnitActionToEnter(controlling, garrisonLocation);
//            return AiAttemptResult.RequestedAction;
//        } else {
//            return ar.setUnitActionToMove(controlling, garrisonLocation);
//        }
//    }
//}
