package common.util;

public class TicksPerSecondTracker {
    private long currentSecond;

    private int ticksInThisSecond;

    private int ticksPerSecondIndex = 0;
    private final int[] ticksPerSecond;

    public TicksPerSecondTracker(int size) {
        ticksPerSecond = new int[size];
    }

    private double getAverageTicksPerSecond() {
        int sum = 0;
        for (int i = 0; i < ticksPerSecond.length; i++) {
            sum += ticksPerSecond[i];
        }
        return sum / (double) ticksPerSecond.length;
    }

    public String receiveTick() {
        long now = System.currentTimeMillis();
        long newSecond = now / 1000;
        if (newSecond == currentSecond) {
            ticksInThisSecond++;
            return null;
        }

        ticksPerSecond[ticksPerSecondIndex++] = ticksInThisSecond;
        currentSecond = newSecond;
        ticksInThisSecond = 1;

        String ret = null;
        if (ticksPerSecondIndex >= ticksPerSecond.length) {
            ret = String.valueOf(getAverageTicksPerSecond());
            ticksPerSecondIndex = 0;
        }

        return ret;
    }
}
