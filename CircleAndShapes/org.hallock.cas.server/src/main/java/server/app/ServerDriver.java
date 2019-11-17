package server.app;

import common.state.spec.GameSpec;
import common.state.spec.GameSpecParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;

public class ServerDriver {
    public static void main(String[] args) throws IOException, ParseException {
        runServerContext(createServerContext());
    }

    public static void runServerContext(ServerContext context) throws IOException, ParseException {
        System.out.println("Waiting on " + ServerConfig.PORT);
        try (ServerSocket serverSocket = new ServerSocket(ServerConfig.PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Found client on " + socket.getInetAddress());
                context.handleConnection(socket);
            }
        }
    }

    public static ServerContext createServerContext() throws IOException, ParseException {
        ServerContext context = new ServerContext();
        for (int i = 0; i < context.lobbies.length; i++) {
            GameSpec spec = GameSpecParser.parseGameSpec(Paths.get("./spec"));
            context.lobbies[i] = new Lobby(context, "Lobby " + String.format("%02d", i + 1), spec);
        }
        return context;
    }
}
