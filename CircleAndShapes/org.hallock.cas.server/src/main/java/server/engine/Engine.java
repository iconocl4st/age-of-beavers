package server.engine;

import server.state.Game;
import server.app.ServerConfig;
import server.util.TicksPerSecondTracker;

import java.util.Timer;
import java.util.TimerTask;

public class Engine {
    private final Timer timer = new Timer();
    private double gameSpeed = 3.0 * ServerConfig.UPDATE_FREQUENCY / 1000.0;
    private double currentTime = 0;

    public void schedule(Game game) {
        timer.schedule(new GameUpdater(game), ServerConfig.UPDATE_FREQUENCY, ServerConfig.UPDATE_FREQUENCY);
    }

    private final class GameUpdater extends TimerTask {
        final Game game;
        final TicksPerSecondTracker tracker = new TicksPerSecondTracker();

        private GameUpdater(Game game) {
            this.game = game;
        }

        @Override
        public void run() {
            tracker.receiveTick();

            double prevTime = currentTime;
            currentTime += gameSpeed;
            if (game.tick(game.lobby.createMasterStateManipulator(), prevTime, currentTime, gameSpeed)) {
                cancel();
            }
        }
    }
}
