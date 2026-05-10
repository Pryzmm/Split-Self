package com.pryzmm.minemessage.ui;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DateDivider extends JComponent {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MMMM d, yyyy");
    private final String label;

    public DateDivider(LocalDate date) {
        this.label = date.format(FORMATTER);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(getParent() != null ? getParent().getWidth() : 471, 30);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int y = getHeight() / 2;
        int textPadding = 10;

        g2.setFont(new Font("Arial", Font.BOLD, 11));
        FontMetrics fm = g2.getFontMetrics();
        int textWidth = fm.stringWidth(label);
        int textX = (getWidth() - textWidth) / 2;

        // Lines on either side
        g2.setColor(new Color(255, 255, 255, 60));
        g2.drawLine(10, y, textX - textPadding, y);
        g2.drawLine(textX + textWidth + textPadding, y, getWidth() - 10, y);

        // Label
        g2.setColor(new Color(255, 255, 255, 140));
        g2.drawString(label, textX, y + fm.getAscent() / 2);
    }
}