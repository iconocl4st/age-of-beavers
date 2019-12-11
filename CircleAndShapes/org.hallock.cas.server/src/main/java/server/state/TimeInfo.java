package server.state;

public class TimeInfo {
    public final double prevTime;
    public final double currentTime;
    public final double timeDelta;
    public final long timeOfGameTime;

    public TimeInfo(double prevTime, double currentTime, double timeDelta, long timeOfGameTime) {
        this.prevTime = prevTime;
        this.currentTime = currentTime;
        this.timeDelta = timeDelta;
        this.timeOfGameTime = timeOfGameTime;
    }
}
