package com.pryzmm.minemessage.ui;

import net.minecraft.text.Text;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;

public class MessageObject extends JComponent {

    private final String content;
    private final boolean sentByUser;
    private final LocalDate timestamp;

    private static final int BUBBLE_WIDTH = 220;
    private static final int PADDING = 10;
    private static final Font MESSAGE_FONT = new Font("Arial", Font.PLAIN, 14);

    public LocalDate getTimestamp() {
        return timestamp;
    }

    public MessageObject(String content, boolean sentByUser, LocalDate timestamp) {
        this.content = Text.translatable(content).getString();
        this.sentByUser = sentByUser;
        this.timestamp = timestamp;
    }

    private int getBubbleHeight(FontMetrics fm) {
        int maxTextWidth = BUBBLE_WIDTH - PADDING * 2;
        String[] words = content.split(" ");
        int lineHeight = fm.getHeight();
        int lines = 1;
        int lineWidth = 0;

        for (String word : words) {
            int wordWidth = fm.stringWidth(word + " ");
            if (lineWidth + wordWidth > maxTextWidth && lineWidth > 0) {
                lines++;
                lineWidth = wordWidth;
            } else {
                lineWidth += wordWidth;
            }
        }
        return lines * lineHeight + PADDING * 2;
    }

    @Override
    public Dimension getPreferredSize() {
        FontMetrics fm = getFontMetrics(MESSAGE_FONT);
        return new Dimension(getParent() != null ? getParent().getWidth() : 471, getBubbleHeight(fm) + 20);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        FontMetrics fm = g2.getFontMetrics(MESSAGE_FONT);
        int bubbleHeight = getBubbleHeight(fm);
        int x = sentByUser ? getWidth() - BUBBLE_WIDTH - 10 : 10;

        g2.setColor(sentByUser ? new Color(0, 180, 180) : new Color(80, 80, 80));
        g2.fillRoundRect(x, 10, BUBBLE_WIDTH, bubbleHeight, 12, 12);

        g2.setColor(Color.WHITE);
        g2.setFont(MESSAGE_FONT);

        int maxTextWidth = BUBBLE_WIDTH - PADDING * 2;
        String[] words = content.split(" ");
        StringBuilder line = new StringBuilder();
        int lineY = 10 + PADDING + fm.getAscent();

        for (String word : words) {
            String candidate = line.isEmpty() ? word : line + " " + word;
            if (fm.stringWidth(candidate) > maxTextWidth && !line.isEmpty()) {
                g2.drawString(line.toString(), x + PADDING, lineY);
                lineY += fm.getHeight();
                line = new StringBuilder(word);
            } else {
                line = new StringBuilder(candidate);
            }
        }
        if (!line.isEmpty()) {
            g2.drawString(line.toString(), x + PADDING, lineY);
        }
    }
}