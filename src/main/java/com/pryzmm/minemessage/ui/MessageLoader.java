package com.pryzmm.minemessage.ui;

import com.pryzmm.minemessage.MineMessage;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;

public class MessageLoader {

    public static boolean isConnecting = false;

    private static final ArrayList<MessageObject> messageList = new ArrayList<>();
    public static void setupUnknownMessageList() {
        messageList.clear();
        messageList.add(new MessageObject("I'm tired man", true, LocalDate.now().minusMonths(6).minusDays(19)));
        messageList.add(new MessageObject("mom is forcing me to get off for the night, sorry :(", true, LocalDate.now().minusMonths(6).minusDays(19)));
        messageList.add(new MessageObject("that's okay, we can try hanging out again tomorrow!", false, LocalDate.now().minusMonths(6).minusDays(19)));
        messageList.add(new MessageObject("maybe, idk.", true, LocalDate.now().minusMonths(6).minusDays(19)));
        messageList.add(new MessageObject("something wrong?", false, LocalDate.now().minusMonths(6).minusDays(19)));
        messageList.add(new MessageObject("no", true, LocalDate.now().minusMonths(6).minusDays(19)));
        messageList.add(new MessageObject("okay, well gn!", false, LocalDate.now().minusMonths(6).minusDays(19)));
        messageList.add(new MessageObject("don't be too long on your trip, I still got loads of stuff planned on our Minecraft world!", false, LocalDate.now().minusMonths(6).minusDays(17)));
        messageList.add(new MessageObject("I took a couple hours to send our villagers to a better spot, zombies were getting in way to easily at the old village.", false, LocalDate.now().minusMonths(6).minusDays(14)));
        messageList.add(new MessageObject("taking care of Blu while you're gone btw, don't want them feeling lonely.", false, LocalDate.now().minusMonths(6).minusDays(14)));
        messageList.add(new MessageObject("hopefully you're getting my messages, don't know if where you went has good signal.", false, LocalDate.now().minusMonths(6).minusDays(12)));
        messageList.add(new MessageObject("waiting till you get back to enter the end. I found the portal ways off while trying to get some resources.", false, LocalDate.now().minusMonths(6).minusDays(8)));
        messageList.add(new MessageObject("Hey, it's been a bit, you doing okay?", false, LocalDate.now().minusMonths(5).minusDays(24)));
        messageList.add(new MessageObject("I miss being able to hang out with you, hoping nothing bad came up.", false, LocalDate.now().minusMonths(5).minusDays(24)));
        messageList.add(new MessageObject("Blu got blown up by a creeper a couple days ago, I'm sorry.", false, LocalDate.now().minusMonths(5).minusDays(2)));
        messageList.add(new MessageObject("I miss you..", false, LocalDate.now().minusMonths(4).minusDays(12)));
        messageList.add(new MessageObject("saw you appear online on MineMessage, are you seeing my messages?", false, LocalDate.now().minusMonths(2).minusDays(17)));
        messageList.add(new MessageObject("hello?", false, LocalDate.now().minusMonths(2).minusDays(17)));
        messageList.add(new MessageObject("please respond...", false, LocalDate.now().minusMonths(2).minusDays(17)));
        messageList.add(new MessageObject("Umm... hope you're okay.", false, LocalDate.now().minusMonths(1).minusDays(3)));
    }

    public static void loadMessages(UserObject userObject) {
        if (isConnecting) return;
        new Thread(() -> {
            try {
                JLabel label = new JLabel("Connecting To Server....");
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
                    label = new JLabel("Failed To Connect.");
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
