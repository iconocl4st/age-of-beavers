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
        // TODO

        // TODO: stance

        // repair action
        // research action


        // missing buildings
        // amory
        // weapon manufactorer (probably different kinds) / blacksmith
        // refinery
        // track
        // archery range
        // lancing thing
        // stable
        // city hall (what for?)
        // university
        // hospital
        // farm
        // stance
        // mill (by water or slaves)
        // shooting range
        // market
        // outpost
        // tower
        // wall
        // castle
        // dock
        // fishing ship
        // airport
        // bar
        // winery
        // grape vine
        // granary
        // statue
        // portal
        // gunpowder: sulfur, charcoal potassium
        // expeditions
        // mine
        // oil
        // select old



        // create nueral ai
        //      create export to tensor

        // questionable
        //      make a testing framework
        //      update the spec on the fly

        // make the action have a timestamp of when it was setResourceLocation by the server (to enable multiple client renders  for one server tick)
        // image manager
        // make the game time also depend on the current time (as an option)

        // fix pathing:
        //    implement an obstacle search
        //    implement blocked counter
        //    make units occupy

        // small changes
        //      draw orientations
        //      use armor manager
        //      use entity reader
        //      control click pathing indicator
        //      create natural resource for dropped resources...
        //      implement evolution params in the brothel/feeding trough

        // add a spectate

        // add sounds
        // common actions when multiple are selected
        // using bson


        // write the first player client.ai
        //      refactor ai
        //      create a while within proximity client.ai
        //      create an WithNoOtherResources sub - client.ai
        //      in range listener
        //      intercept/chase client.ai
        //      refactor out a move to client.ai



        // BUGS
        // transporting before demand is set?
        //
        // don't let extra resources be in construction zone.
        // players can move to a spot, but still not be close enough to interact...
        //  To finish astar:
        //          make the path start/stop at the floor of the first and last points
        //          make sure that +1, +2 is also feasible unless the point is integer
        //          this is still slightly too conservative
        //          could also do intersections of all corners of the unit


        int numSpectators = 1;
        int numGames = 0;
        int numAis = 2;

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

        // lc, abadon, bb quell, mars, silencer, earthshaker, sven, kunkka ship, magnus rp
        // omniknight, phoenix, spirit breaker, timbersaw, undying, qop
        // bloodseeker, clinkz (2 spells), (ember portal), brood, naga, phantom lancer, sniper
        // spectre, ursa, dazzle, leshrac, lich, natures prophet, rubick, techies, windranger,
        // winter wyvern, witch doctor, mirana

        // ancient

        // divine rapier
    }
}
