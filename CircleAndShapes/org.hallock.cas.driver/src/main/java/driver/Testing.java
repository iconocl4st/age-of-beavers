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
        // TODO:

        // Priority 0 (doing right now)
        //      implement farms
        //      implement bee hives
        //      implement houses
        //      replenish natural resources
        //      implements weapons in capacity spec
        //      drop resources into a loot natural resource
        //      make natural resources less special
        //      implement evolution params in the brothel/feeding trough
        //      create a dies-on-empty class
        //      draw orientations
        //      implement mining (add some sort of digging)
        //      create fight ai
        //      plankimplement tools

        // Priority 1 (before i can implement nueral ai)
        //      fix the clustering in the ai
        //      implement repair
        //      research action
        //      implement railroads
        //      make the double progress actions have a a timestamp of when it was started so that we don't need as many messages...
        //      use armor manager

        // Priority 2 (implement nueral ai)
        //      create export to tensor


        // Priority 3 (after that)
        //      implement seasons, day/night cycle
        //      farms grow faster over green
        //      make a testing framework
        //      update the spec on the fly
        //      make the buildings have doors that depend on their orientation
        //      create a spectator window
        //      add sounds
        //      common actions when multiple are selected
        //      use bson
        //      refactor the ai to reuse other ais
        //      use entity reader
        //      control click pathing indicator
        //      implement stances


        // BUGS
        // wagons are too fast
        //  To finish astar:
        //          make the points start/stop at the floor of the first and last points
        //          make sure that +1, +2 is also feasible unless the point is integer
        //          this is still slightly too conservative
        //          could also do intersections of all corners of the unit


        int numSpectators = 0;
        int numGames = 1;
        int numAis = 1;

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

//        clientContexts[0].uiManager.mainWindowFrame.setLocation(1920, 50);

        /////////////////////////
        /// TEMPLE IDEAS


        // ZUES: vision cloud
        // ulti percent of remaining

        // spectre: damage return
        // haunt

        // pudge hook

        // abadon, bb quell, mars, silencer, earthshaker, sven, kunkka ship, magnus rp
        // omniknight, phoenix, spirit breaker, timbersaw, undying, qop
        // bloodseeker, clinkz (2 spells), (ember portal), brood, naga, phantom lancer, sniper
        // spectre, ursa, dazzle, leshrac, lich, natures prophet, rubick, techies, windranger,
        // winter wyvern, witch doctor, mirana

        // ancient

        // divine rapier
    }
}
