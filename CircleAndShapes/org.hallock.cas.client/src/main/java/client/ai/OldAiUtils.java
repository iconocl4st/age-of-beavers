//package client.ai;
//
//import common.AiAttemptResult;
//import common.Proximity;
//import common.state.EntityReader;
//
//public class OldAiUtils {
//
//    public static AiAttemptResult moveToProximity(ActionRequester requester, EntityReader move, EntityReader destination) {
//        if (Proximity.closeEnoughToInteract(move, destination)) {
//            return AiAttemptResult.NothingDone;
//        }
//        return requester.setUnitActionToMove(move, destination);
//    }
//}
