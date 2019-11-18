package client.gui;

import client.app.UiClientContext;
import common.app.LobbyInfo;
import common.msg.Message;

import javax.swing.*;
import java.io.IOException;
import java.util.List;

public class LobbyBrowser {
    private final UiClientContext context;

    private JPanel panel1;
    private JList list1;
    private JButton refreshButton;
    private JButton joinButton;
    private JButton launchButton;
    private JButton leaveButton;
    private JLabel lobbyDisplay;

    private LobbyInfo currentLobby;

    LobbyBrowser(final UiClientContext context) {
        this.context = context;

        refreshButton.addActionListener(actionEvent -> context.executorService.submit(() -> {
            try {
                context.writer.send(new Message.ListLobbies());
                context.writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));
        joinButton.addActionListener(actionEvent ->
            context.executorService.submit(() -> {
                try {
                    context.writer.send(new Message.Join((LobbyInfo) list1.getSelectedValue()));
                    context.writer.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            })
        );
        launchButton.addActionListener(actionEvent -> context.executorService.submit(() -> {
            try {
                context.writer.send(new Message.Launch());
                context.writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));
        updateDisables();
        list1.addListSelectionListener(listSelectionEvent -> updateDisables());
        leaveButton.addActionListener(actionEvent -> context.executorService.submit(() -> {
            try {
                context.writer.send(new Message.Leave());
                context.writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));
    }

    public void setCurrentLobby(LobbyInfo currentLobby) {
        this.currentLobby = currentLobby;
        updateDisables();
    }

    JPanel getMainPanel() {
        return panel1;
    }

    public void setLobbies(List<LobbyInfo> infos) {
        DefaultListModel<LobbyInfo> model = new DefaultListModel<>();
        for (LobbyInfo info : infos) {
            model.addElement(info);
        }
        list1.setModel(model);
    }


    void updateDisables() {
        lobbyDisplay.setText(currentLobby == null ? "No lobby" : currentLobby.name);
        joinButton.setEnabled(currentLobby == null && list1.getSelectedValue() != null);
        launchButton.setEnabled(currentLobby != null);
        leaveButton.setEnabled(currentLobby != null);
    }
}
