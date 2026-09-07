package com.pryzmm.splitself.screen.overlay;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;

public class ColorOverlay {

    public static boolean overlayVisible = false;
    private static int color = 0x00000000;

    public static void toggleOverlay(boolean toggled) {
        overlayVisible = toggled;
    }
    public static void setColor(int color) { ColorOverlay.color = color; }

    public static void renderTopLayerOverlay(DrawContext drawContext) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, calculateVisibility());
        drawContext.fill(0, 0, drawContext.getScaledWindowWidth(), drawContext.getScaledWindowHeight(), color);

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();

    }

    private static Long startTime = null;
    public static void startBlackout() {
        startTime = System.nanoTime();
    }

    private static float calculateVisibility() {
        if (startTime == null) return 1.0f;
        float visibility = (float) (System.nanoTime() - startTime) / 1_000_000_000 / 4;
        return Math.min(visibility, 1.0f);
    }

}
