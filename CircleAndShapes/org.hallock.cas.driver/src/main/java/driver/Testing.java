package driver;

import client.app.ClientContext;
import client.app.ClientDriver;
import common.msg.Message;
import org.json.simple.parser.ParseException;
import server.app.ServerContext;
import server.app.ServerDriver;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class Testing {
    public static void main(String[] args) throws InterruptedException, IOException, ParseException {
        // TODO

        // TODO: stance

        // repair
        // create export to tensor
        // make a testing framework
        // update the spec on the fly
        // create service deliveries ai
        // create consumer class with priorities with threshholds
//          make none of the managers have to access to other managers
//        (move methods into a helper class)

        // make the action have a timestamp of when it was set by the server (to enable multiple client renders  for one server tick)
        // make the game time also depend on the current time
        // implement an obstacle search

        // draw orientations
//        use the base initialBaseHealth class
//                        use weapon manager
//                        use armor manager
//                        use projectile manager
        // use entity reader


        // transport ai
        // implement blocked counter
        // make units occupy
        // add color to players
        // add a spectate
        // control click pathing indicator

        // add sounds
        // common actions when multiple are selected
        // using bson

        // BUGS
        // sometimes stuck when walking somewhere
        // make all of the units go back to the town center when the run out of resources to gather...
        // elaborate the selection priority
        // makes sure resources are preserved somehow after construction. (don't let extra resources be there.)
        // players can move to a spot, but still not be close enough to interact...
        // still see a white box over a resource...
        //  To finish astar:
        //          make the path start/stop at the floor of the first and last points
        //          make sure that +1, +2 is also feasible unless the point is integer
        //          this is still slightly too conservative
        //          could also do intersections of all corners of the unit
        // implement evolution params in the brothel/feeding trough
        // change things to attributes
        // image manager
        // create a while within proximity ai
        // create an WithNoOtherResources sub - ai
        // in range listener
        // intercept/chase ai
        // refactor out a move to ai
        // write the first player ai

        // create natural resource for dropped resources...

        int numClients = 2;
        ServerContext serverContext = ServerDriver.createServerContext();
        ClientContext[] clientContexts = new ClientContext[numClients];
        for (int i = 0; i < numClients; i++) {
            clientContexts[i] = new ClientContext();
        }

        System.out.println("Starting server");
        new Thread(() -> {
            try {
                ServerDriver.runServerContext(serverContext);
            } catch (Throwable e) {
                e.printStackTrace();
                System.exit(1);
            }
        }).start();

        Thread.sleep(1000);

        CountDownLatch clientsRunningLatch = new CountDownLatch(numClients);

        System.out.println("Starting clients");
        for (final ClientContext clientContext : clientContexts)
            new Thread(() -> {
                try {
                    ClientDriver.runClientContext(clientContext, clientsRunningLatch);
                } catch (Throwable e) {
                    e.printStackTrace();
                    System.exit(1);
                }
            }).start();


        clientsRunningLatch.await();

        System.out.println("Clients started");

        for (int i = 0; i < 2; i++) {
            clientContexts[i].writer.send(new Message.Join(serverContext.lobbies[0].getInfo()));
            clientContexts[i].writer.flush();
        }

        clientContexts[0].writer.send(new Message.Launch());
        clientContexts[0].writer.flush();

        clientContexts[1].uiManager.mainWindowFrame.setLocation(50, 1920);

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
