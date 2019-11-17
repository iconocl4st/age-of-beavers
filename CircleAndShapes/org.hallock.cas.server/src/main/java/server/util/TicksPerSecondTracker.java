package server.util;

public class TicksPerSecondTracker {
    private long currentSecond;

    private int ticksInThisSecond;

    private int ticksPerSecondIndex = 0;
    private int[] ticksPerSecond = new int[10];


    private double getAverageTicksPerSecond() {
        int sum = 0;
        for (int i = 0; i < ticksPerSecond.length; i++) {
            sum += ticksPerSecond[i];
        }
        return sum / (double) ticksPerSecond.length;
    }

    public void receiveTick() {
        long newSecond = System.currentTimeMillis() / 1000;
        if (newSecond == currentSecond) {
            ticksInThisSecond++;
            return;
        }

        if (ticksPerSecondIndex >= ticksPerSecond.length) {
            System.out.println("Average ticks per second = " + getAverageTicksPerSecond());
            ticksPerSecondIndex = 0;
        }
        ticksPerSecond[ticksPerSecondIndex++] = ticksInThisSecond;
        currentSecond = newSecond;
        ticksInThisSecond = 1;
    }
}
