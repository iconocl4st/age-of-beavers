package client.app;

import common.msg.ConnectionWriter;
import common.util.json.JsonReaderWrapperSpec;
import common.util.json.JsonWrapper;
import common.util.json.JsonWriterWrapperSpec;

import java.net.Socket;
import java.util.concurrent.CountDownLatch;

import static common.util.json.JsonWrapper.createJacksonReaderWrapper;

public class ClientDriver {

    public static void runClientContext(UiClientContext context, CountDownLatch latch) throws Exception {
        try (
                // TODO: should come from the config...
                Socket socket = new Socket(ClientConfig.SERVER_ADDRESS, ClientConfig.SERVER_PORT);
                JsonWriterWrapperSpec outputStream = JsonWrapper.initializeStream(JsonWrapper.createJacksonWriterWrapper(socket.getOutputStream()), "requests");
                JsonReaderWrapperSpec inputStream = JsonWrapper.initializeStream(createJacksonReaderWrapper(socket.getInputStream()), "messages");
        ) {
            context.writer = new ConnectionWriter(outputStream);
            context.uiManager.displayLobbyBrowser();

            if (latch != null) {
                latch.countDown();
            }

            while (context.messageHandler.handleNextMessage(inputStream))
                ;

            JsonWrapper.finishStream(inputStream);
            JsonWrapper.finishStream(outputStream);
        }

//        c.uiManager.gameScreenFrame.dispose();
//        c.uiManager.lobbyFrame.dispose();
    }

    public static void main(String[] args) throws Exception {
        runClientContext(new UiClientContext(), null);
    }
}
