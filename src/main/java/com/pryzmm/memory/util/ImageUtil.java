package com.pryzmm.memory.util;

import com.pryzmm.memory.data.Card;
import com.pryzmm.splitself.data.WorldData;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class ImageUtil {

    private static final Map<String, BufferedImage> imageCache = new HashMap<>();
    public static BufferedImage getCardImage(Card.ICardType cardType) {
        String key = WorldData.getMemoryStage() + "_" + cardType.name().toLowerCase();
        return imageCache.computeIfAbsent(key, k -> {
            try {
                return ImageIO.read(ImageUtil.class.getResourceAsStream("/assets/memory/textures/cards/stage_" + WorldData.getMemoryStage() + "/" + cardType.name().toLowerCase() + ".png"));
            } catch (Exception e) { throw new RuntimeException(e); }
        });
    }

    private static final Map<Long, BufferedImage> rotationBufferCache = new HashMap<>();
    public static BufferedImage rotateImage(BufferedImage original, double degrees) {
        double radians = Math.toRadians(degrees);
        int w = original.getWidth();
        int h = original.getHeight();
        double sin = Math.abs(Math.sin(radians));
        double cos = Math.abs(Math.cos(radians));
        int newW = (int) Math.floor(w * cos + h * sin);
        int newH = (int) Math.floor(h * cos + w * sin);
        long key = ((long) newW << 32) | (newH & 0xFFFFFFFFL);
        BufferedImage rotated = rotationBufferCache.computeIfAbsent(key, k ->
            new BufferedImage(newW, newH, original.getType())
        );

        Graphics2D g2d = rotated.createGraphics();
        g2d.setComposite(AlphaComposite.Clear);
        g2d.fillRect(0, 0, newW, newH);
        g2d.setComposite(AlphaComposite.SrcOver);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        AffineTransform at = new AffineTransform();
        at.translate((newW - w) / 2.0, (newH - h) / 2.0);
        at.rotate(radians, w / 2.0, h / 2.0);
        g2d.setTransform(at);
        g2d.drawImage(original, 0, 0, null);
        g2d.dispose();
        return rotated;
    }

    public static BufferedImage shiftHue(BufferedImage image, float hueShift) {
        int width  = image.getWidth();
        int height = image.getHeight();

        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb  = image.getRGB(x, y);
                int alpha = (argb >> 24) & 0xFF;
                int red   = (argb >> 16) & 0xFF;
                int green = (argb >>  8) & 0xFF;
                int blue  =  argb        & 0xFF;
                float[] hsb = Color.RGBtoHSB(red, green, blue, null);
                hsb[0] = (hsb[0] + hueShift) % 1.0f;
                if (hsb[0] < 0) hsb[0] += 1.0f;
                int rgb = Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
                int newArgb = (alpha << 24) | (rgb & 0x00FFFFFF);
                result.setRGB(x, y, newArgb);
            }
        }

        return result;
    }

    public static BufferedImage setBrightness(BufferedImage image, float brightness) {
        int width  = image.getWidth();
        int height = image.getHeight();

        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb  = image.getRGB(x, y);
                int alpha = (argb >> 24) & 0xFF;
                int red   = (argb >> 16) & 0xFF;
                int green = (argb >>  8) & 0xFF;
                int blue  =  argb        & 0xFF;
                float[] hsb = Color.RGBtoHSB(red, green, blue, null);
                hsb[2] = Math.min(1.0f, Math.max(0.0f, brightness));
                int rgb = Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
                int newArgb = (alpha << 24) | (rgb & 0x00FFFFFF);
                result.setRGB(x, y, newArgb);
            }
        }

        return result;
    }

}
