package server.engine;

import server.app.ServerConfig;
import server.state.Game;
import server.state.TimeInfo;
import common.util.TicksPerSecondTracker;

import java.util.Timer;
import java.util.TimerTask;

public class Engine {
    private final Timer timer = new Timer();

    //  * ServerConfig.UPDATE_FREQUENCY / 1000.0;

    public void schedule(Game game) {
        timer.schedule(new GameUpdater(game), ServerConfig.UPDATE_FREQUENCY, ServerConfig.UPDATE_FREQUENCY);
    }

    private final class GameUpdater extends TimerTask {
        private double gameSpeed;
        private double previousTime = -1d;
        private long previousTick;
        final Game game;
        final TicksPerSecondTracker tracker = new TicksPerSecondTracker(10);

        private GameUpdater(Game game) {
            this.game = game;
            this.gameSpeed = game.serverState.state.gameSpec.gameSpeed;
        }

        @Override
        public void run() {
            long now = System.nanoTime();
            double dt;
            double currentTime;

            if (previousTime < 0) {
                previousTick = now;
                previousTime = 0;
                dt = 0;
                currentTime = 0;
            } else {
                dt = gameSpeed * (now - previousTick) / 1e9;
                currentTime = previousTime + dt;
                previousTime = currentTime;
                previousTick = now;
            }

            String s = tracker.receiveTick();
            if (s != null) {
                System.out.println("Average ticks per second = " + s);
            }

            currentTime += gameSpeed;
            if (game.tick(game.lobby.createMasterStateManipulator(), new TimeInfo(previousTime, currentTime, dt, now))) {
                cancel();
            }
        }
    }
}
