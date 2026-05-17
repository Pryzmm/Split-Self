package com.pryzmm.minemessage.ui;

import com.pryzmm.minemessage.MineMessage;
import net.minecraft.text.Text;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;

public class UserObject extends JComponent {

    public static final BufferedImage userPlaceholder;
    static {
        try {
            userPlaceholder = MineMessage.readImage("/assets/minemessage/textures/ui/user.png");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
    private final String username;
    private final BufferedImage avatar;
    private final String lastMessage;
    private final UserObject instance;

    private boolean hovered = false;

    public String getUsername() {
        return username;
    }

    public UserObject(String username, BufferedImage avatar, String lastMessage) {
        this.username = username;
        this.avatar = avatar;
        this.lastMessage = Text.translatable(lastMessage).getString();
        this.instance = this;
        setPreferredSize(new Dimension(250, 75));

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                hovered = true;
                repaint();
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                MessageLoader.loadMessages(instance);
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (hovered && !MessageLoader.isConnecting) {
            g.setColor(new Color(255, 255, 255, 20));
            g.fillRect(0, 0, getWidth(), getHeight());
        }
        g.drawImage(userPlaceholder, 0, 0, 250, 75, null);
        if (avatar != null) g.drawImage(avatar, 11, 11, 53, 53, null);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 24));
        g.drawString(username, 80, 35);
        g.setColor(Color.GRAY);
        g.setFont(new Font("Arial", Font.PLAIN, 12));
        String previewMessage;
        if (lastMessage.length() >= 30) previewMessage = lastMessage.substring(0, 27) + "...";
        else previewMessage = lastMessage;
        g.drawString(previewMessage, 80, 55);
    }

}
