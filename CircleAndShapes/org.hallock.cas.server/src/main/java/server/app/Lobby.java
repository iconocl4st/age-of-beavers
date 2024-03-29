package server.app;


import common.state.EntityId;
import common.app.LobbyInfo;
import common.msg.ConnectionWriter;
import common.msg.Message;
import common.state.Player;
import common.state.spec.GameSpec;
import server.algo.MapGenerator;
import server.state.Game;
import server.state.ServerGameState;
import server.state.ServerStateManipulator;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;

public class Lobby {

    public Random random = new Random();

    private ServerContext context;
    private String name;
    public GameSpec spec;

    public final LinkedList<PlayerConnection> connections = new LinkedList<>();
    private final ArrayList<LobbyPlayer> players = new ArrayList<>();

    private final Object statusSync = new Object();
    private LobbyInfo.LobbyStatus status = LobbyInfo.LobbyStatus.Waiting;

    private DefaultBroadCaster broadCaster;
    private Game game;

    Lobby(ServerContext context, String lobbyName, GameSpec spec) {
        this.context = context;
        this.name = lobbyName;
        this.spec = spec;
    }

    public Player getPlayer(PlayerConnection c) {
        LobbyPlayer lobbyPlayer = getLobbyPlayer(c);
        if (lobbyPlayer == null) return null;
        return lobbyPlayer.player;
    }

    private LobbyPlayer getLobbyPlayer(PlayerConnection c) {
        for (LobbyPlayer lobbyPlayer : players) {
            if (lobbyPlayer.connection.equals(c))
                return lobbyPlayer;
        }
        return null;
    }

    public int getNumPlayers() {
        return players.size();
    }

    public void join(PlayerConnection c) {
        synchronized (statusSync) {
            ensureStatus(LobbyInfo.LobbyStatus.Waiting);
            connections.add(c);
            addPlayer(c);
        }
    }

    void leave(ServerConnectionContext c) {
        synchronized (connections) {
            ensureStatus(LobbyInfo.LobbyStatus.Waiting);
            removePlayer(c);
            connections.remove(c);
        }
    }

    private void addPlayer(PlayerConnection serverConnectionContext) {
        if (isAPlayer(serverConnectionContext))
            throw new IllegalStateException("Already a player");
        LobbyPlayer player = new LobbyPlayer();
        player.player = new Player(getNumPlayers() + 1);
        player.connection = serverConnectionContext;
        players.add(player);
    }

    private void removePlayer(PlayerConnection serverConnectionContext) {
        players.removeIf(lp -> lp.connection.equals(serverConnectionContext));
        for (int i = 0; i < players.size(); i++) {
            players.get(i).player = new Player(i + 1);
        }
    }

    void setSpectating(boolean spectate, ServerConnectionContext serverConnectionContext) {
        boolean isAPlayer = isAPlayer(serverConnectionContext);
        if (spectate && isAPlayer) {
            removePlayer(serverConnectionContext);
        } else if (!spectate && !isAPlayer) {
            addPlayer(serverConnectionContext);
        }
    }

    private boolean isAPlayer(PlayerConnection serverConnectionContext) {
        for (LobbyPlayer lobbyPlayer : players) {
            if (lobbyPlayer.connection.equals(serverConnectionContext)) {
                return true;
            }
        }
        return false;
    }

    public void gameOver() {
        synchronized (statusSync) {
            ensureStatus(LobbyInfo.LobbyStatus.InGame);
            status = LobbyInfo.LobbyStatus.Waiting;

            try {
                broadCaster.broadCast(new Message.GameOver());
            } catch (Throwable e) {
                e.printStackTrace();
            }

            for (PlayerConnection connection : connections) {
                connection.setMessageHandler(null);
            }
            game = null;
            broadCaster = null;
        }
    }

    public void ticked() {
        broadCaster.flush();

        for (PlayerConnection connection : connections) {
            connection.ticked();
        }
    }

    void launch() {
        synchronized (statusSync) {
            ensureStatus(LobbyInfo.LobbyStatus.Waiting);
            this.status = LobbyInfo.LobbyStatus.InGame;
            spec.numPlayers = getNumPlayers();
            game = Game.createGame(context, this);

            MapGenerator.randomlyGenerateMap(
                    game.serverState,
                    getCurrentSpec(),
                    getNumPlayers(),
                    random,
                    game.idGenerator,
                    createEmptyStateManipulator()
            );

            broadCaster = createBroadCaster(game.serverState);

            CountDownLatch latch = new CountDownLatch(connections.size());
            for (final PlayerConnection connection : connections) {
                try {
                    Player player = getPlayer(connection);
                    Point playerStart = null;
                    Set<EntityId> startingUnits = null;
                    if (player != null) {
                        playerStart = game.serverState.playerStarts[player.number - 1];
                        startingUnits = game.serverState.startingUnits.get(player.number- 1);
                    }
                    connection.setMessageHandler(new GamePlayerMessageHandler(new ServerStateManipulator(game, player, broadCaster)));
                    connection.getWriter().send(new Message.Launched(spec, player, playerStart, startingUnits));
                    connection.getWriter().send(new Message.UpdateEntireGameState(game.serverState.createGameState(player)));
                    connection.getWriter().flush();
                } catch (Throwable e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            }

            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            context.engine.schedule(game);
        }
    }

    private void ensureStatus(LobbyInfo.LobbyStatus status) {
        if (this.status != status) {
            throw new RuntimeException("Expected to be in state " + status + " was in state " + status);
        }
    }

    public LobbyInfo getInfo() {
        return new LobbyInfo(status, name, connections.size());
    }

    public boolean isDescribedBy(LobbyInfo info) {
        return name.equals(info.name);
    }

    public GameSpec getCurrentSpec() {
        return spec;
    }

    private ConnectionWriter getFilter(ServerGameState state, LobbyPlayer player) {
        switch (spec.visibility) {
            case FOG:
            case EXPLORED:
                return new LineOfSightMessageFilter(state, player.connection.getWriter(), player.player);
            case ALL_VISIBLE:
                return player.connection.getWriter();
            default:
                throw new IllegalStateException("Unknown visibility selection: " + spec.visibility);
        }
    }

    private DefaultBroadCaster createBroadCaster(ServerGameState state) {
        ConnectionWriter[] filters = new ConnectionWriter[connections.size()];
        HashMap<Player, ConnectionWriter> byPlayer = new HashMap<>();

        int index = 0;
        for (PlayerConnection context : connections) {
            LobbyPlayer player = getLobbyPlayer(context);
            if (player == null) {
                filters[index++] = context.getWriter();
            } else {
                byPlayer.put(player.player, filters[index++] = getFilter(state, player));
            }
        }
        return new DefaultBroadCaster(filters, byPlayer, (writer, t) -> {
            t.printStackTrace();
            System.exit(1); // TODO: just remove that player...
        });
    }

    public ServerStateManipulator createMasterStateManipulator() {
        return new ServerStateManipulator(game, Player.GOD, broadCaster);
    }

    private ServerStateManipulator createEmptyStateManipulator() {
        return new ServerStateManipulator(game, Player.GOD, new BroadCaster() {
            @Override
            public void broadCast(Message msg) {}
            @Override
            public void flush() {}
            @Override
            public void send(Player losPlayer, Message unitRemoved) {}
        });
    }

    private static final class LobbyPlayer {
        Player player;
        PlayerConnection connection;
    }
}
