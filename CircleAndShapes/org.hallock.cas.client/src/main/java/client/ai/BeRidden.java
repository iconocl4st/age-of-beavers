//package client.ai;
//
//import client.state.ClientGameState;
//import common.AiAttemptResult;
//import common.state.EntityReader;
//
//public class BeRidden extends Ai {
//
//    private final EntityReader rider;
//
//    public BeRidden(ClientGameState state, EntityReader controlling, EntityReader rider)  {
//        super(state, controlling);
//        this.rider = rider;
//    }
//
//    public String toString() {
//        return "be ridden by";
//    }
//
//    @Override
//    public AiAttemptResult setActions(ActionRequester ar) {
//        return beRidden(ar, controlling, rider);
//    }
//
//    static AiAttemptResult beRidden(ActionRequester requester, EntityReader entity, EntityReader rider) {
//        AiAttemptResult result = OldAiUtils.moveToProximity(entity, rider);
//        if (!result.equals(AiAttemptResult.NothingDone)) return result;
//        requester.setUnitActionToMount(rider, entity);
//        return AiAttemptResult.RequestedAction;
//    }
//}
