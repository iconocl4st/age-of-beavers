package client.ai;

import client.app.ClientContext;
import common.state.EntityId;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class QueueAi extends Ai {

    private final LinkedList<Ai> queue = new LinkedList<>();

    public QueueAi(ClientContext context, EntityId controlling, Collection<Ai> queue) {
        super(context, controlling);
        this.queue.addAll(queue);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (Ai ai : queue) {
            builder.append(ai).append(", ");
        }
        return builder.toString();
    }

    @Override
    public AiAttemptResult setActions(ActionRequester ar) {
        while (true) {
            if (queue.isEmpty()) {
                return AiAttemptResult.Completed;
            }
            switch (queue.getFirst().setActions(ar)) {
                case NothingDone:
                case Completed:
                case Unsuccessful:
                    queue.removeFirst();
                    continue;
                case Successful:
                    return AiAttemptResult.Successful;
            }
        }
    }
}
