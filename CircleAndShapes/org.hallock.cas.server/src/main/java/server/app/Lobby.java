package server.app;


import common.app.LobbyInfo;
import common.msg.ConnectionWriter;
import common.msg.Message;
import common.state.Player;
import common.state.spec.GameSpec;
import server.algo.MapGenerator;
import server.state.Game;
import server.state.ServerStateManipulator;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Random;

public class Lobby implements BroadCaster {

    public Random random = new Random();

    private ServerContext context;
    private String name;
    public GameSpec spec;
    public final LinkedList<ServerConnectionContext> connections = new LinkedList<>();

    private final Object statusSync = new Object();
    private LobbyInfo.LobbyStatus status = LobbyInfo.LobbyStatus.Waiting;

    private Game game;


    Lobby(ServerContext context, String lobbyName, GameSpec spec) {
        this.context = context;
        this.name = lobbyName;
        this.spec = spec;
    }

    public Player getPlayer(ServerConnectionContext c) {
        int index = connections.indexOf(c);
        if (index < 0) return Player.NO_PLAYER;
        return new Player(index + 1);
    }

    public int getNumPlayers() {
        return connections.size();
    }

    void join(ServerConnectionContext c) {
        synchronized (statusSync) {
            ensureStatus(LobbyInfo.LobbyStatus.Waiting);
            connections.add(c);
        }
    }

    void leave(ServerConnectionContext c) {
        synchronized (connections) {
            ensureStatus(LobbyInfo.LobbyStatus.Waiting);
            connections.remove(c);
        }
    }

    public void gameOver() {
        synchronized (statusSync) {
            ensureStatus(LobbyInfo.LobbyStatus.InGame);
            status = LobbyInfo.LobbyStatus.Waiting;
            game = null;
        }
    }

    void launch() {
        synchronized (statusSync) {
            ensureStatus(LobbyInfo.LobbyStatus.Waiting);
            this.status = LobbyInfo.LobbyStatus.InGame;
            spec.numPlayers = connections.size() + 1;
            game = Game.createGame(context, this);

            MapGenerator.randomlyGenerateMap(
                    game.serverState,
                    getCurrentSpec(),
                    getNumPlayers(),
                    random,
                    game.idGenerator,
                    createEmptyStateManipulator()
            );

            for (final ServerConnectionContext connection: connections) {
//                c.executorService.submit(
//                        new Runnable() {
//                            @Override
//                            public void run() {
                                try {
                                    connection.writer.send(new Message.Launched(spec, getPlayer(connection)));
                                    game.sendEntireState();
                                    connection.writer.flush();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
//                            }
//                        }
//                );
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

    public void broadCast(Message msg) {
        for (ServerConnectionContext context : connections) {
            try {
                context.writer.send(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void send(Player player, Message msg) {
        if (player.equals(Player.GAIA)) {
            return;
        }
        try {
            connections.get(player.number - 1).writer.send(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private BroadCaster getBroadCaster() {
        switch (spec.visibility) {
            case FOG:
            case EXPLORED:
                return new SelectiveBroadcaster(game.serverState, getNumPlayers() ,this);
            case ALL_VISIBLE:
                return this;
            default:
                throw new IllegalStateException("Unknown visibility selection: " + spec.visibility);
        }
    }

    public ServerStateManipulator createStateManipulator(ServerConnectionContext context) {
        return new ServerStateManipulator(game, getPlayer(context), getBroadCaster());
    }

    public ServerStateManipulator createMasterStateManipulator() {
        return new ServerStateManipulator(game, Player.GOD, getBroadCaster());
    }

    public ServerStateManipulator createEmptyStateManipulator() {
        return new ServerStateManipulator(game, Player.GOD, new BroadCaster() {
            @Override
            public void broadCast(Message msg) {}
            @Override
            public void send(Player losPlayer, Message unitRemoved) {}
        });
    }

    public void flushAll() {
        for (ServerConnectionContext c : connections) {
            try {
                c.writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
