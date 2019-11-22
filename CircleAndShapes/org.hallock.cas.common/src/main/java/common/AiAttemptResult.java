package common;

public enum AiAttemptResult {
    RequestedAction,
    Unsuccessful,
    NothingDone,
    Completed,

    ;

    public boolean didSomething() {
        return !equals(AiAttemptResult.NothingDone);
    }
    public boolean requested() { return equals(RequestedAction); }
    public boolean failed() { return equals(Unsuccessful); }
}
