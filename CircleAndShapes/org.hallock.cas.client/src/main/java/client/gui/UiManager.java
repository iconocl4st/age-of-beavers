package client.gui;

import client.app.ClientContext;
import client.gui.actions.UnitActions;
import client.gui.game.GameScreen;
import client.gui.keys.GoToListener;
import client.gui.selected.SelectedUnits;
import common.msg.Message;
import common.state.spec.GameSpec;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Collections;

public class UiManager {

    final ClientContext context;

    final JFrame lobbyFrame = new JFrame("Lobbies");
    public final JFrame mainWindowFrame = new JFrame("Game Window");

    MainWindow mainWindow;
    public GameScreen gameScreen;
    public LobbyBrowser lobbyBrowser;
    public SelectedUnits selectedUnitsBrowser;
    BuildingSelector buildingSelector;
    public UnitActions unitActions;
    public Minimap minimap;


    private UiManager(ClientContext context) {
        this.context = context;
    }


    public void displayGame(GameSpec spec) {
        lobbyFrame.setVisible(false);
        buildingSelector.initialize(spec);
        selectedUnitsBrowser.initialize(spec);
        unitActions.initialize(spec);
        mainWindowFrame.setVisible(true);
        minimap.setGameSpec(spec);
        gameScreen.setGameSpec(spec);

        mainWindow.updateSplitPaneDividers();
    }

    public void displayLobbyBrowser() {
        lobbyFrame.setVisible(true);
        context.executorService.submit(() -> {
            try {
                context.writer.send(new Message.ListLobbies());
                context.writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void log(String message) {
        mainWindow.show(message);
    }

    public static UiManager createUiManager(ClientContext context) {
        final UiManager manager = new UiManager(context);

        manager.mainWindowFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        manager.lobbyFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        manager.mainWindowFrame.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, Collections.emptySet());
        manager.mainWindowFrame.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, Collections.emptySet());
        manager.mainWindowFrame.setFocusTraversalKeysEnabled(false);

        manager.minimap = Minimap.createMinimap(context);
        manager.gameScreen = GameScreen.createGameScreen(context);
        manager.lobbyBrowser = new LobbyBrowser(context);
        manager.selectedUnitsBrowser = SelectedUnits.createSelectedUnits(context);
        manager.buildingSelector = new BuildingSelector(manager.gameScreen);
        manager.unitActions = UnitActions.createUnitActions(context);

        manager.mainWindow = new MainWindow(manager);

        manager.lobbyFrame.setBounds(50, 50, 500, 500);
        manager.lobbyFrame.setContentPane(manager.lobbyBrowser.getMainPanel());

        manager.mainWindowFrame.setBounds(50, 50, 5000, 1000);
        manager.mainWindowFrame.setContentPane(manager.mainWindow.panel1);
        manager.mainWindow.addBottom();

        // TODO: when the game is over
        new Timer(100, actionEvent -> {
            manager.gameScreen.repaint();
            manager.minimap.repaint();
            manager.selectedUnitsBrowser.updateInfo();
        }).start();

        return manager;
    }
}
