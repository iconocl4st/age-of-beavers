package client.ai.ai2;

import common.AiAttemptResult;
import common.event.AiEvent;
import common.state.EntityReader;

import java.util.Collection;
import java.util.LinkedList;

public class QueueAi extends AiTask {

    private final LinkedList<AiTask> queue = new LinkedList<>();

    public QueueAi(EntityReader entity, Collection<AiTask> queue) {
        super(entity);
        this.queue.addAll(queue);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (AiTask ai : queue) {
            builder.append(ai).append(", ");
        }
        return builder.toString();
    }

    @Override
    public AiAttemptResult receiveEvent(AiContext aiContext, AiEvent event) {
        while (true) {
            if (queue.isEmpty()) {
                return AiAttemptResult.Completed;
            }
            switch (queue.getFirst().receiveEvent(aiContext, event)) {
                case NothingDone:
                case Completed:
                case Unsuccessful:
                    queue.removeFirst();
                    continue;
                case RequestedAction:
                    return AiAttemptResult.RequestedAction;
            }
        }
    }
}
