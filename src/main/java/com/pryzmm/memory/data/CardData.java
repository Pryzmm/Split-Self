package com.pryzmm.memory.data;

import com.pryzmm.memory.Memory;
import com.pryzmm.memory.util.ImageUtil;
import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CardData {

    public static final List<Card> cards = new ArrayList<>();

    public static void populateCards() {
        cards.clear();
        flippedCards.clear();

        Card.ICardType[] cardTypes;
        if (StageHandler.stage == 0) cardTypes = Card.CardTypeStage0.values();
        else if (StageHandler.stage == 1) cardTypes = Card.CardTypeStage1.values();
        else cardTypes = Card.CardTypeStage2.values();
        for (Card.ICardType type : cardTypes) {
            int count = 2;
            if (StageHandler.stage == 2) count = 24;
            for (int i = 0; i < count; i++) {
                int posX, posY;
                do {
                    posX = (int) (Math.random() * 8);
                    posY = (int) (Math.random() * 4);
                } while (isOccupiedPosition(posX, posY));
                cards.add(new Card(null, type, posX, posY));
            }
        }
        for (int i = 0; i < cards.size(); i++) {
            int j = (int) (Math.random() * cards.size());
            Card temp = cards.get(i);
            cards.set(i, cards.get(j));
            cards.set(j, temp);
        }

    }

    private static boolean isOccupiedPosition(int x, int y) {
        return CardData.cards.stream().anyMatch(card -> card.posX() == x && card.posY() == y);
    }

    public static final ArrayList<Card> flippedCards = new ArrayList<>();
    public static void flipCard(Card card) {
        Thread thread = new Thread(() -> {
            if (canFlipCard(card)) {
                flippedCards.add(card);
                card.flipped(true);
                ImageIcon icon = new ImageIcon(ImageUtil.getCardImage(card.type()).getScaledInstance(76, 108, Image.SCALE_SMOOTH));
                card.button().setIcon(icon);
                card.button().setPressedIcon(icon);
                card.button().setRolloverIcon(icon);
                try {
                    if (flippedCards.size() == 2) {
                        if (!flippedCardsMatch()) {
                            Thread.sleep(1000);
                            unflipCards();
                        }
                        else {
                            for (Card flippedCard : flippedCards) {
                                Card finalCard = getCardFromFlippedCard(flippedCard);
                                finalCard.matched(true);
                            }
                            flippedCards.clear();
                            if (allCardsMatched()) {
                                JOptionPane.showMessageDialog(Memory.jFrame, "Congratulations! You've matched all the cards!");
                                StageHandler.advanceStage();
                                Memory.audioClip.stop();
                                Memory.audioClip.close();
                                Memory.jFrame.removeAll();
                                Memory.jFrame.invalidate();
                                Memory.jFrame.dispose();
                            }
                        }
                    }
                    if (StageHandler.stage == 2 && allFlippedCardsCount() == 7) {
                        Memory.instance.removeAll();
                        Memory.audioClip.stop();
                        Memory.audioClip.close();
                        try {
                            @SuppressWarnings("DataFlowIssue") AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(Memory.class.getResourceAsStream("/assets/memory/sounds/static.wav"));
                            Memory.audioClip.open(audioInputStream);
                            Memory.audioClip.start();
                        } catch (UnsupportedAudioFileException | LineUnavailableException | IOException ignored) {}
                        Thread.sleep(5000);
                        SwingUtilities.invokeLater(() -> {
                            Memory.instance.removeAll();
                            Memory.textLabel = new JLabel("You can remember all these cards, but you don't even remember their name?");
                            Memory.textLabel.setHorizontalAlignment(SwingConstants.CENTER);
                            Memory.textLabel.setBounds(0, 0, Memory.instance.getWidth(), Memory.instance.getHeight());
                            Memory.instance.add(Memory.textLabel);
                            Memory.instance.revalidate();
                            Memory.instance.repaint();
                        });
                        Thread.sleep(6000);
                        SwingUtilities.invokeLater(() -> Memory.textLabel.setText("There's no point in you remembering them anyway, is there?"));
                        Thread.sleep(6000);
                        SwingUtilities.invokeLater(() -> Memory.textLabel.setText("A part of you wont forget them though."));
                        double s = 6000;
                        StringBuilder text = new StringBuilder();
                        for (int i = 0; i <= 300; i++) {
                            Thread.sleep((int) s);
                            if (!text.isEmpty()) text.delete(text.length() - 7, text.length());
                            text.append("<html> I won't forget them.</html>");
                            Memory.textLabel.setText(text.toString());
                            if (s > 10) s /= 1.5f;
                            if (i == 300) {
                                StageHandler.advanceStage();
                                Memory.audioClip.stop();
                                Memory.audioClip.close();
                                Memory.jFrame.dispose();
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    public static void unflipCards() {
        for (Card card : flippedCards) {
            card.button().setIcon(new ImageIcon(Memory.backCard));
            card.button().setPressedIcon(new ImageIcon(Memory.clickCard));
            card.button().setRolloverIcon(new ImageIcon(Memory.hoverCard));
            card.flipped(false);
        }
        flippedCards.clear();
    }

    public static boolean canFlipCard(Card card) {
        return flippedCards.size() < 2 && !flippedCards.contains(card) && !card.flipped();
    }

    public static boolean flippedCardsMatch() {
        if (flippedCards.size() != 2) return false;
        return flippedCards.get(0).type() == flippedCards.get(1).type();
    }

    public static boolean allCardsMatched() {
        return cards.stream().allMatch(Card::matched);
    }

    public static int allFlippedCardsCount() {
        return (int) cards.stream().filter(Card::flipped).count();
    }

    private static Card getCardFromFlippedCard(Card flippedCard) {
        return cards.stream().filter(card -> card.posX() == flippedCard.posX() && card.posY() == flippedCard.posY()).findFirst().orElse(null);
    }


}
