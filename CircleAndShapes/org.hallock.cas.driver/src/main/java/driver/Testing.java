package driver;

import app.PlayerAiContext;
import client.app.ClientDriver;
import client.app.UiClientContext;
import common.msg.Message;
import org.json.simple.parser.ParseException;
import server.app.AiConnection;
import server.app.ServerContext;
import server.app.ServerDriver;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class Testing {

    public static void main(String[] args) throws InterruptedException, IOException, ParseException {
        int numSpectators = 0;
        int numGames = 1;
        int numAis = 0;

        System.out.println("Starting server");
        ServerContext serverContext = ServerDriver.createServerContext();
        new Thread(() -> {
            try {
                ServerDriver.runServerContext(serverContext);
            } catch (Throwable e) {
                e.printStackTrace();
                System.exit(1);
            }
        }).start();

        Thread.sleep(1000);

        int numClients = numSpectators + numGames;
        UiClientContext[] clientContexts = new UiClientContext[numClients];
        for (int i = 0; i < numClients; i++) {
            clientContexts[i] = new UiClientContext();
        }
        CountDownLatch clientsRunningLatch = new CountDownLatch(numClients);
        System.out.println("Starting clients");
        for (final UiClientContext clientContext : clientContexts)
            new Thread(() -> {
                try {
                    ClientDriver.runClientContext(clientContext, clientsRunningLatch);
                } catch (Throwable e) {
                    for (int i = 0; i < clientContexts.length; i++) {
                        if (clientContext.equals(clientContexts[i])) {
                            System.out.println("Error in " + i);
                        }
                    }
                    e.printStackTrace();
                    System.exit(1);
                }
            }).start();


        clientsRunningLatch.await();

        System.out.println("Clients started");

        for (int i = 0; i < clientContexts.length; i++) {
            clientContexts[i].writer.send(new Message.Join(serverContext.lobbies[0].getInfo()));
            clientContexts[i].writer.flush();
        }

        Thread.sleep(1000);

        for (int i = 0; i < numAis; i++) {
            PlayerAiContext aiContext = new PlayerAiContext(ServerContext.executorService);
            serverContext.lobbies[0].join(new AiConnection(aiContext));
        }

        for (int i = 0; i < numSpectators; i++) {
            clientContexts[i].writer.send(new Message.Spectate(true));
            clientContexts[i].writer.flush();
        }

        Thread.sleep(1000);

        clientContexts[0].writer.send(new Message.Launch());
        clientContexts[0].writer.flush();
    }
}
