package com.pryzmm.minemessage;

import com.pryzmm.memory.Memory;
import com.pryzmm.minemessage.ui.MessageLoader;
import com.pryzmm.minemessage.ui.UserObject;
import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class MineMessage extends JPanel implements Runnable {

    public static MineMessage instance;
    public static JFrame jFrame;

    private static final BufferedImage background;

    private static final BufferedImage avatar_Pryzmm;
    private static final BufferedImage avatar_AquaLoco;
    private static final BufferedImage avatar_Vortexify;
    private static final BufferedImage avatar_oTexasHuntero;
    private static final BufferedImage avatar_Billy;
    private static final BufferedImage avatar_QwertyKeys;
    private static final BufferedImage avatar_Reassembly;
    private static final BufferedImage avatar_OpticalGlass;
    private static final BufferedImage avatar_CloudySkies;
    private static final BufferedImage avatar_DuckyBlade_;
    private static final BufferedImage avatar_CqllMeToxic;
    private static final BufferedImage avatar_Unknown;

    static {
        try {
            background = readImage("/assets/minemessage/textures/ui/background.png");
            avatar_Pryzmm = readImage("/assets/minemessage/textures/ui/avatars/pryzmm.png");
            avatar_AquaLoco = readImage("/assets/minemessage/textures/ui/avatars/aqualoco.png");
            avatar_Vortexify = readImage("/assets/minemessage/textures/ui/avatars/vortexify.png");
            avatar_oTexasHuntero = readImage("/assets/minemessage/textures/ui/avatars/otexashuntero.png");
            avatar_Billy = readImage("/assets/minemessage/textures/ui/avatars/billy.png");
            avatar_QwertyKeys = readImage("/assets/minemessage/textures/ui/avatars/qwertykeys.png");
            avatar_Reassembly = readImage("/assets/minemessage/textures/ui/avatars/reassembly.png");
            avatar_OpticalGlass = readImage("/assets/minemessage/textures/ui/avatars/opticalglass.png");
            avatar_CloudySkies = readImage("/assets/minemessage/textures/ui/avatars/cloudyskies.png");
            avatar_DuckyBlade_ = readImage("/assets/minemessage/textures/ui/avatars/duckyblade.png");
            avatar_CqllMeToxic = readImage("/assets/minemessage/textures/ui/avatars/cqllmetoxic.png");
            avatar_Unknown = readImage("/assets/minemessage/textures/ui/avatars/unknown.png");
        } catch (IOException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static BufferedImage readImage(String path) throws IOException {
        InputStream stream = Memory.class.getResourceAsStream(path);
        if (stream == null) throw new IOException("Resource not found on classpath: " + path);
        return ImageIO.read(stream);
    }

    public static JPanel messageListPanel;

    public MineMessage() {
        instance = this;
        setLayout(null);

        MessageLoader.setupUnknownMessageList();

        JPanel userListPanel = new JPanel();
        userListPanel.setLayout(new BoxLayout(userListPanel, BoxLayout.PAGE_AXIS));
        userListPanel.add(new UserObject("Pryzmm",          avatar_Pryzmm,        "game.splitself.minemessage.last.pryzmm"));
        userListPanel.add(new UserObject("AquaLoco",        avatar_AquaLoco,      "game.splitself.minemessage.last.aqualoco"));
        userListPanel.add(new UserObject("Vortexify",       avatar_Vortexify,     "game.splitself.minemessage.last.vortexify"));
        userListPanel.add(new UserObject("oTexasHuntero",   avatar_oTexasHuntero, "game.splitself.minemessage.last.otexashuntero"));
        userListPanel.add(new UserObject("Billy",           avatar_Billy,         "game.splitself.minemessage.last.billy"));
        userListPanel.add(new UserObject("QwertyKeys",      avatar_QwertyKeys,    "game.splitself.minemessage.last.qwertykeys"));
        userListPanel.add(new UserObject("Reassembly",      avatar_Reassembly,    "game.splitself.minemessage.last.reassembly"));
        userListPanel.add(new UserObject("OpticalGlass",    avatar_OpticalGlass,  "game.splitself.minemessage.last.opticalglass"));
        userListPanel.add(new UserObject("CloudySkies",     avatar_CloudySkies,   "game.splitself.minemessage.last.cloudyskies"));
        userListPanel.add(new UserObject("CqllMeToxic",     avatar_CqllMeToxic,   "game.splitself.minemessage.last.cqllmetoxic"));
        userListPanel.add(new UserObject("DuckyBlade_",     avatar_DuckyBlade_,   "game.splitself.minemessage.last.duckyblade"));
        userListPanel.add(new UserObject("████████",        avatar_Unknown,       "game.splitself.minemessage.last.unknown"));
        JScrollPane scrollPane = new JScrollPane(userListPanel, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setOpaque(false);
        userListPanel.setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(12);
        add(scrollPane);
        scrollPane.setBounds(32, 58, 250, 698);

        messageListPanel = new JPanel();
        messageListPanel.setLayout(new BoxLayout(messageListPanel, BoxLayout.PAGE_AXIS));
        JScrollPane messageScrollPane = new JScrollPane(messageListPanel, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        messageScrollPane.setOpaque(false);
        messageScrollPane.setBorder(null);
        messageScrollPane.getViewport().setOpaque(false);
        messageListPanel.setOpaque(false);
        messageScrollPane.getVerticalScrollBar().setUnitIncrement(12);
        add(messageScrollPane);
        messageScrollPane.setBounds(295, 58, 471, 698);

    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            instance = new MineMessage();
            instance.setPreferredSize(new Dimension(800, 800));
            JFrame frame = new JFrame("MineMessage");
            jFrame = frame;
            frame.add(instance);
            frame.pack();
            frame.setResizable(false);
            frame.setVisible(true);
            try {
                frame.setIconImage(readImage("/assets/minemessage/textures/ui/icon.png"));
            } catch (IOException ignored) {}

            instance.gameLoopThread.setDaemon(true);
            instance.gameLoopThread.start();
        });
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(background, 0, 0, 800, 800, null);

    }

    private final Thread gameLoopThread = new Thread(this);
    @Override
    @SuppressWarnings("BusyWait")
    public void run() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice screen = ge.getDefaultScreenDevice();
        int refreshRate = screen.getDisplayMode().getRefreshRate();
        if (refreshRate == DisplayMode.REFRESH_RATE_UNKNOWN) refreshRate = 60;
        long targetFrameTime = 1000000000L / refreshRate;
        while (true) {
            long frameStart = System.nanoTime();
            SwingUtilities.invokeLater(this::repaint);
            long elapsed = System.nanoTime() - frameStart;
            long sleepTime = targetFrameTime - elapsed;
            if (sleepTime > 0) {
                if (jFrame == null || !jFrame.isEnabled()) {
                    gameLoopThread.interrupt();
                    return;
                }
                try { Thread.sleep(sleepTime / 1000000, (int)(sleepTime % 1000000)); } catch (InterruptedException ignored) {
                    gameLoopThread.interrupt();
                    return;
                }
            }
        }
    }
}
