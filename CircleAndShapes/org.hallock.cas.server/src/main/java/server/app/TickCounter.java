package server.app;

public class TickCounter {
    int count;
    final int freq;

    public TickCounter(int freq) {
        this.freq = freq;
    }

    public boolean ticked() {
        return ++count % freq == 0;
    }
}
