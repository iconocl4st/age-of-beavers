package client.ai.ai2;

import client.event.AiEventListener;
import common.AiAttemptResult;
import common.event.AiEvent;
import common.event.InitializeAi;
import common.state.EntityId;

import java.util.LinkedList;

public class AiStack implements AiEventListener {

    private final AiManager manager;
    private final LinkedList<AiTask> stack = new LinkedList<>();
    private boolean removing;

    AiStack(AiManager manager) {
        this.manager = manager;
    }

    void removeAllListeners(AiContext aiContext) {
        aiContext = aiContext.stack(this);
        synchronized (stack) {
            for (AiTask task : stack) {
                task.removeListeners(aiContext);
            }
        }
    }

    private AiAttemptResult sendTopInit(AiContext aiContext, AiEvent reason) {
        AiAttemptResult initializationResult = stack.getLast().receiveEvent(aiContext, reason);
        switch (initializationResult) {
            case NothingDone:
            case RequestedAction:
                break;
            case Unsuccessful:
            case Completed:
                pop(aiContext, reason);
                break;
            default:
                throw new IllegalStateException();
        }
        return initializationResult;
    }

    AiAttemptResult push(AiContext aiContext, AiTask task) {
        synchronized (stack) {
            aiContext = aiContext.stack(this);
            if (!stack.isEmpty()) {
                stack.getLast().removeListeners(aiContext);
            }
            stack.addLast(task);
            task.registerListeners(aiContext);
            return sendTopInit(aiContext, new InitializeAi(task.entity.entityId));
        }
    }

    private void pop(AiContext aiContext, AiEvent reason) {
        aiContext = aiContext.stack(this);
        AiTask last = stack.removeLast();
        last.removeListeners(aiContext);
        if (stack.isEmpty()) {
            removeAllListeners(aiContext);
            if (!removing) {
                removing = true;
                aiContext.clientGameState.executor.submit(() -> manager.remove(this));
            }
        } else {
            stack.getLast().registerListeners(aiContext);
            sendTopInit(aiContext, reason);
        }
    }

    @Override
    public void receiveEvent(AiContext aiContext, AiEvent event) {
        synchronized (stack) {
            if (stack.isEmpty()) {
                // message after we are done. Suspicious...
                if (removing) return;
                throw new IllegalStateException();
            }
            AiTask last = stack.getLast();
            AiAttemptResult aiAttemptResult = last.receiveEvent(aiContext.stack(this), event);
            switch (aiAttemptResult) {
                case Unsuccessful:
                case Completed:
                    pop(aiContext, event);
                    break;
                case RequestedAction:
                case NothingDone:
            }
        }
    }

    AiTask getLast() {
        synchronized (stack) {
            if (stack.isEmpty()) return null;
            return stack.getLast();
        }
    }

    String getDisplayString() {
        StringBuilder builder = new StringBuilder();
        synchronized (stack) {
            for (AiTask task : stack) {
                builder.append(task).append(',');
            }
        }
        return builder.toString();
    }
}
