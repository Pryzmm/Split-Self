package com.pryzmm.memory.data;

import javax.swing.*;

public class Card {

    private JButton button;
    private final ICardType type;
    private final int posX;
    private final int posY;
    private boolean matched = false;
    private boolean flipped = false;

    public Card(JButton button, ICardType type, int posX, int posY) {
        this.button = button;
        this.type = type;
        this.posX = posX;
        this.posY = posY;
    }

    public void button(JButton button) { this.button = button; }
    public JButton button() { return button; }
    public ICardType type() { return type; }
    public int posX() { return posX; }
    public int posY() { return posY; }
    public boolean matched() { return matched; }
    public void matched(boolean matched) { this.matched = matched; }
    public boolean flipped() { return flipped; }
    public void flipped(boolean flipped) { this.flipped = flipped; }

    public enum CardTypeStage0 implements ICardType {
        CLUB_2,
        CLUB_6,
        CLUB_9,
        DIAMOND_3,
        DIAMOND_JACK,
        DIAMOND_KING,
        HEART_2,
        HEART_6,
        HEART_10,
        SPADE_7,
        SPADE_ACE,
        SPADE_KING
    }

    public enum CardTypeStage1 implements ICardType {
        BONE,
        EMERALD,
        PICKAXE,
        SIGN,
        AXE,
        PAPER,
        DOOR,
        FRAME,
        BOOK,
        LAVA,
        SNOWBALL,
        NAMETAG
    }

    public enum CardTypeStage2 implements ICardType {
        EMPTY
    }

    public interface ICardType {
        String name();
    }

}
