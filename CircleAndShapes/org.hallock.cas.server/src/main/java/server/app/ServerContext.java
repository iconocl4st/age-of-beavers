package server.app;

import common.app.LobbyInfo;
import common.msg.Message;
import common.msg.NetworkConnectionWriter;
import common.util.ExecutorServiceWrapper;
import common.util.json.JsonReaderWrapperSpec;
import common.util.json.JsonWrapper;
import common.util.json.JsonWriterWrapperSpec;
import server.engine.Engine;

import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;

import static common.util.json.JsonWrapper.createJacksonReaderWrapper;

public class ServerContext {
    public static final ExecutorServiceWrapper executorService = new ExecutorServiceWrapper(Executors.newCachedThreadPool());

    public final Lobby[] lobbies = new Lobby[ServerConfig.NUM_LOBBIES];
    final ServerMessageHandler messageHandler = new ServerMessageHandler(this);
    final Engine engine = new Engine();


    void handleConnection(final Socket socket) {
        executorService.submit(() -> {
            try (
                    JsonReaderWrapperSpec inputStream = JsonWrapper.initializeStream(createJacksonReaderWrapper(socket.getInputStream()), "requests");
                    JsonWriterWrapperSpec outputStream = JsonWrapper.initializeStream(JsonWrapper.createJacksonWriterWrapper(socket.getOutputStream()), "messages");
            ) {
                final ServerConnectionContext connectionContext = new ServerConnectionContext(
                        new NetworkConnectionWriter(outputStream)
                );

                try {
                    ServerMessageReader reader = new ServerMessageReader(connectionContext);
                    while (messageHandler.handleMessage(connectionContext, reader.readMessage(inputStream)))
                        ;
                    connectionContext.getWriter().send(new Message.Quit());

                    JsonWrapper.finishStream(outputStream);
                    JsonWrapper.finishStream(inputStream);
                } finally {
                    socket.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    Lobby getLobby(LobbyInfo info) {
        for (Lobby lobby : lobbies) {
            if (lobby.isDescribedBy(info)) {
                return lobby;
            }
        }
        return null;
    }

    List<LobbyInfo> getLobbyInfos() {
        LinkedList<LobbyInfo> lobbies = new LinkedList<>();
        for (Lobby l : this.lobbies) {
            lobbies.add(l.getInfo());
        }
        return lobbies;
    }
}
