package app.assign;

public class SimpleAiCheck implements AiCheck {
    private final Condition condition;
    private final PlayerAction playerAction;

    public SimpleAiCheck(Condition condition, PlayerAction playerAction) {
        this.condition = condition;
        this.playerAction = playerAction;
    }


    @Override
    public void check(AiCheckContext context) {
        if (!condition.isSatisfied(context))
            return;
        playerAction.perform(context);
    }
}
