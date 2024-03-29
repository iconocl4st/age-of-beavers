//package client.ai;
//
//import client.state.ClientGameState;
//import common.AiAttemptResult;
//import common.state.EntityReader;
//
//import java.util.Collection;
//import java.util.LinkedList;
//
//public class QueueAi extends Ai {
//
//    private final LinkedList<Ai> queue = new LinkedList<>();
//
//    public QueueAi(ClientGameState gameState, EntityReader controlling, Collection<Ai> queue) {
//        super(gameState, controlling);
//        this.queue.addAll(queue);
//    }
//
//    @Override
//    public String toString() {
//        StringBuilder builder = new StringBuilder();
//        for (Ai ai : queue) {
//            builder.append(ai).append(", ");
//        }
//        return builder.toString();
//    }
//
//    @Override
//    public AiAttemptResult setActions(ActionRequester ar) {
//        while (true) {
//            if (queue.isEmpty()) {
//                return AiAttemptResult.Completed;
//            }
//            switch (queue.getFirst().setActions(ar)) {
//                case NothingDone:
//                case Completed:
//                case Unsuccessful:
//                    queue.removeFirst();
//                    continue;
//                case RequestedAction:
//                    return AiAttemptResult.RequestedAction;
//            }
//        }
//    }
//}
