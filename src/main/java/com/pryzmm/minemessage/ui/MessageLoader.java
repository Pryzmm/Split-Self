package com.pryzmm.minemessage.ui;

import com.pryzmm.minemessage.MineMessage;
import net.minecraft.text.Text;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;

public class MessageLoader {

    public static boolean isConnecting = false;

    private static final ArrayList<MessageObject> messageList = new ArrayList<>();
    public static void setupUnknownMessageList() {
        messageList.clear();
        messageList.add(new MessageObject("game.splitself.minemessage.messages.1",  true,  LocalDate.now().minusMonths(6).minusDays(19)));
        messageList.add(new MessageObject("game.splitself.minemessage.messages.2",  true,  LocalDate.now().minusMonths(6).minusDays(19)));
        messageList.add(new MessageObject("game.splitself.minemessage.messages.3",  false, LocalDate.now().minusMonths(6).minusDays(19)));
        messageList.add(new MessageObject("game.splitself.minemessage.messages.4",  true,  LocalDate.now().minusMonths(6).minusDays(19)));
        messageList.add(new MessageObject("game.splitself.minemessage.messages.5",  false, LocalDate.now().minusMonths(6).minusDays(19)));
        messageList.add(new MessageObject("game.splitself.minemessage.messages.6",  true,  LocalDate.now().minusMonths(6).minusDays(19)));
        messageList.add(new MessageObject("game.splitself.minemessage.messages.7",  false, LocalDate.now().minusMonths(6).minusDays(19)));
        messageList.add(new MessageObject("game.splitself.minemessage.messages.8",  false, LocalDate.now().minusMonths(6).minusDays(17)));
        messageList.add(new MessageObject("game.splitself.minemessage.messages.9",  false, LocalDate.now().minusMonths(6).minusDays(14)));
        messageList.add(new MessageObject("game.splitself.minemessage.messages.10", false, LocalDate.now().minusMonths(6).minusDays(14)));
        messageList.add(new MessageObject("game.splitself.minemessage.messages.11", false, LocalDate.now().minusMonths(6).minusDays(12)));
        messageList.add(new MessageObject("game.splitself.minemessage.messages.12", false, LocalDate.now().minusMonths(6).minusDays(8)));
        messageList.add(new MessageObject("game.splitself.minemessage.messages.13", false, LocalDate.now().minusMonths(5).minusDays(24)));
        messageList.add(new MessageObject("game.splitself.minemessage.messages.14", false, LocalDate.now().minusMonths(5).minusDays(24)));
        messageList.add(new MessageObject("game.splitself.minemessage.messages.15", false, LocalDate.now().minusMonths(5).minusDays(2)));
        messageList.add(new MessageObject("game.splitself.minemessage.messages.16", false, LocalDate.now().minusMonths(4).minusDays(12)));
        messageList.add(new MessageObject("game.splitself.minemessage.messages.17", false, LocalDate.now().minusMonths(2).minusDays(17)));
        messageList.add(new MessageObject("game.splitself.minemessage.messages.18", false, LocalDate.now().minusMonths(2).minusDays(17)));
        messageList.add(new MessageObject("game.splitself.minemessage.messages.19", false, LocalDate.now().minusMonths(2).minusDays(17)));
        messageList.add(new MessageObject("game.splitself.minemessage.messages.20", false, LocalDate.now().minusMonths(1).minusDays(3)));
    }

    public static void loadMessages(UserObject userObject) {
        if (isConnecting) return;
        new Thread(() -> {
            try {
                JLabel label = new JLabel(Text.translatable("game.splitself.minemeassage.connecting").getString());
                label.setForeground(Color.WHITE);
                label.setFont(new Font("Arial", Font.BOLD, 30));

                isConnecting = true;

                MineMessage.messageListPanel.removeAll();
                MineMessage.messageListPanel.add(label);
                MineMessage.messageListPanel.revalidate();
                MineMessage.messageListPanel.repaint();

                Thread.sleep((long) ((Math.random() * 1000) + 500));

                MineMessage.messageListPanel.removeAll();

                if (userObject.getUsername().contains("█")) {
                    LocalDate lastDate = null;
                    for (MessageObject message : messageList) {
                        if (!message.getTimestamp().equals(lastDate)) {
                            MineMessage.messageListPanel.add(new DateDivider(message.getTimestamp()));
                            lastDate = message.getTimestamp();
                        }
                        MineMessage.messageListPanel.add(message);
                    }
                } else {
                    label = new JLabel(Text.translatable("game.splitself.minemeassage.connectfail").getString());
                    label.setForeground(Color.WHITE);
                    label.setFont(new Font("Arial", Font.BOLD, 30));
                    MineMessage.messageListPanel.add(label);
                }
                MineMessage.messageListPanel.revalidate();
                MineMessage.messageListPanel.repaint();

                SwingUtilities.invokeLater(() -> {
                    JScrollPane scrollPane = (JScrollPane) MineMessage.messageListPanel.getParent().getParent();
                    JScrollBar bar = scrollPane.getVerticalScrollBar();
                    bar.setValue(bar.getMaximum());
                });

                isConnecting = false;

            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

}
