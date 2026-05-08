package com.pryzmm.memory;

import com.pryzmm.memory.data.Card;
import com.pryzmm.memory.data.CardData;
import com.pryzmm.memory.data.StageHandler;
import com.pryzmm.memory.util.ImageUtil;
import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

@SuppressWarnings("DataFlowIssue")
public class Memory extends JPanel implements Runnable {

    public static Memory instance;
    public static JFrame jFrame;
    public static JLabel textLabel = null;
    public static Clip audioClip;

    private static final BufferedImage polkadots;
    private static final BufferedImage polkadotsStage2;
    private static final BufferedImage background;
    private static final BufferedImage logo;
    public static final BufferedImage hoverCard;
    public static final BufferedImage clickCard;
    public static final BufferedImage backCard;
    private static final BufferedImage backgroundHueShifted;
    private static final BufferedImage backgroundBrightnessShifted;
    static {
        try {
            polkadots = readImage("/assets/memory/textures/ui/polka_dots.png");
            polkadotsStage2 = ImageUtil.setBrightness(readImage("/assets/memory/textures/ui/polka_dots_stage_2.png"), 0.1f);
            background = readImage("/assets/memory/textures/ui/polka_dots_background.png");
            logo = readImage("/assets/memory/textures/ui/logo.png");
            hoverCard = toScaled(readImage("/assets/memory/textures/cards/card_flip_small.png"));
            clickCard = toScaled(readImage("/assets/memory/textures/cards/card_flip.png"));
            backCard = toScaled(readImage("/assets/memory/textures/cards/card_back.png"));
            backgroundHueShifted = ImageUtil.shiftHue(background, 0.05f);
            backgroundBrightnessShifted = ImageUtil.setBrightness(background, 0.1f);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private static BufferedImage readImage(String path) throws IOException {
        InputStream stream = Memory.class.getResourceAsStream(path);
        if (stream == null) throw new IOException("Resource not found on classpath: " + path);
        return ImageIO.read(stream);
    }

    private static BufferedImage toScaled(BufferedImage image) {
        BufferedImage scaled = new BufferedImage(76, 108, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = scaled.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.drawImage(image, 0, 0, 76, 108, null);
        g.dispose();
        return scaled;
    }

    private static JButton startButton;
    private static final ArrayList<JButton> cardButtons = new ArrayList<>();

    public Memory() {
        CardData.populateCards();
        setLayout(null);
        startButton = new JButton("Start Game");
        startButton.setBounds(getWidth() / 2, getHeight() / 2, 200, 50);
        startButton.addActionListener(e -> {
            startButton.setVisible(false);
            int i = 0;
            for (Card card : CardData.cards) {
                JButton cardButton = new JButton(new ImageIcon(backCard));
                cardButton.setBorderPainted(false);
                cardButton.setContentAreaFilled(false);
                cardButton.setFocusPainted(false);
                cardButton.setOpaque(false);
                cardButton.setRolloverIcon(new ImageIcon(hoverCard));
                cardButton.setPressedIcon(new ImageIcon(clickCard));
                cardButton.addActionListener(cardClick -> CardData.flipCard(card));
                card.button(cardButton);
                cardButtons.add(cardButton);
                cardButton.setBounds(((getWidth() / 2) - 320) + (80 * (i % 8)), (int) ((double) getHeight() / 2 + (112 * Math.floor((double) i / 8))), 76, 108);
                add(cardButton);
                i++;
            }
        });
        add(startButton);
    }

    public static void main(String[] args) {
        if (StageHandler.stage == -1) return;

        SwingUtilities.invokeLater(() -> {

            if (instance != null) {
                instance.gameLoopThread.interrupt();
            }

            if (audioClip != null && audioClip.isRunning()) {
                audioClip.stop();
                audioClip.close();
            }
            if (jFrame != null) {
                jFrame.dispose();
                jFrame = null;
            }
            cardButtons.clear();
            instance = new Memory();

            JFrame frame = new JFrame("Memory");
            jFrame = frame;
            frame.add(instance);
            frame.setSize(800, 800);
            frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    if (StageHandler.stage != 2) JOptionPane.showMessageDialog(frame, "Finish the game.");
                }
            });
            frame.setVisible(true);

            try {
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(Memory.class.getResourceAsStream("/assets/memory/sounds/lobby_music_stage_" + StageHandler.stage + ".wav"));
                audioClip = AudioSystem.getClip();
                audioClip.open(audioInputStream);
                audioClip.loop(Clip.LOOP_CONTINUOUSLY);
                audioClip.start();
            } catch (LineUnavailableException | IOException | UnsupportedAudioFileException e) {
                throw new RuntimeException(e);
            }

            instance.gameLoopThread.setDaemon(true);
            instance.gameLoopThread.start();
        });
    }

    private int offsetX = 0;
    private int offsetY = 0;

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (StageHandler.stage == 2) g.drawImage(backgroundBrightnessShifted, 0, 0, getWidth(), getHeight(), null);
        else if (StageHandler.stage == 1 && Math.random() * 100 < 2) g.drawImage(backgroundHueShifted, 0, 0, getWidth(), getHeight(), null);
        else g.drawImage(background, 0, 0, getWidth(), getHeight(), null);

        offsetX += 1;
        offsetY += 1;

        if (StageHandler.stage >= 1 && Math.random() * 100 < 2) {
            offsetX += (int) (Math.random() * 40 - 20);
            offsetY += (int) (Math.random() * 40 - 20);
        }

        int imgW = polkadots.getWidth();
        int imgH = polkadots.getHeight();

        int startX = ((offsetX % imgW) - imgW) % imgW;
        int startY = ((offsetY % imgH) - imgH) % imgH;

        if (startButton != null) startButton.setLocation((getWidth() / 2) - 100, (getHeight() / 2) - 25);

        if (!cardButtons.isEmpty()) {
            for (int i = 0; i < cardButtons.size(); i++) {

                int offsetX, offsetY;
                if (StageHandler.stage >= 1 && Math.random() * 100 < 2) {
                    offsetX = (int) (Math.random() * 4 - 2);
                    offsetY = (int) (Math.random() * 4 - 2);
                } else {
                    offsetX = 0;
                    offsetY = 0;
                }

                JButton cardButton = cardButtons.get(i);
                cardButton.setLocation(((getWidth() / 2) - 320) + offsetX + (80 * (i % 8)), (int) (((double) getHeight() / 2) + offsetY + (112 * Math.floor((double) i / 8))));
            }
        }

        for (int x = startX; x < getWidth(); x += imgW) {
            for (int y = startY; y < getHeight(); y += imgH) {
                if (StageHandler.stage == 2) {
                    g.drawImage(polkadotsStage2, x, y, null);
                }
                else g.drawImage(polkadots, x, y, null);
            }
        }

        if (textLabel != null) textLabel.setBounds(0, 0, Memory.instance.getWidth(), Memory.instance.getHeight());

        double rotationOffset;
        if (StageHandler.stage >= 1 && Math.random() * 100 < 2) rotationOffset = Math.random() * 2;
        else rotationOffset = 0;

        double rotation = Math.sin((double) System.currentTimeMillis() / 400 + rotationOffset) * 10;
        BufferedImage cachedRotatedLogo = ImageUtil.rotateImage(logo, rotation);

        g.drawImage(cachedRotatedLogo, (getWidth() / 2) - (323 / 2), 100, 323, 72, null);

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
