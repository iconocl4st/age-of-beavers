package app.assign;

public interface Condition {

    boolean isSatisfied(AiCheckContext context);

    Condition TRUE = context -> true;
}
