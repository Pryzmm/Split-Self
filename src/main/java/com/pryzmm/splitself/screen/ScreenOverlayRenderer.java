package com.pryzmm.splitself.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.pryzmm.splitself.SplitSelf;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class ScreenOverlayRenderer {
    public static boolean overlayVisible = false;
    public static long lastShakeUpdate = 0;
    public static long lastShakeUpdate2 = 0;
    static int shakeX;
    static int shakeY;
    static int shakeX2;
    static int shakeY2;

    public static final Identifier OVERLAY_IMAGE = Identifier.of(SplitSelf.MOD_ID, "textures/screen/overlay.png");

    public static void toggleOverlay() {
        overlayVisible = !overlayVisible;
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            if (overlayVisible) {
                renderTopLayerOverlay(drawContext);
            }
        });
    }

    public static void renderTopLayerOverlay(DrawContext drawContext) {
        MinecraftClient client = MinecraftClient.getInstance();
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();

        MatrixStack matrices = drawContext.getMatrices();
        matrices.push();

        matrices.translate(0, 0, 1000);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        RenderSystem.disableDepthTest();

        RenderSystem.polygonOffset(-1.0f, -1.0f);
        RenderSystem.enablePolygonOffset();

        drawContext.fill(0, 0, screenWidth, screenHeight, 0x80000000);

        renderOverlayContent(drawContext, screenWidth, screenHeight);

        RenderSystem.disablePolygonOffset();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();

        matrices.pop();
    }

    public static void renderOverlayContent(DrawContext drawContext, int screenWidth, int screenHeight) {
        MinecraftClient client = MinecraftClient.getInstance();
        TextRenderer textRenderer = client.textRenderer;

        renderImageOverlay(drawContext, screenWidth, screenHeight);

        String overlayText = "You did this to me";
        int textWidth = textRenderer.getWidth(overlayText);
        int textX = ((screenWidth - textWidth) / 2) + 100;
        int textY = (screenHeight / 2) + 100;

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastShakeUpdate >= 50) {
            shakeX = -(int) (Math.random() * 200);
            shakeY = -(int) (Math.random() * 200);
        }

        // Draw text with shadow - using white color with full opacity
        drawContext.drawTextWithShadow(textRenderer, overlayText, textX+shakeX, textY+shakeY, 0xFFFFFF);
    }

    public static void renderImageOverlay(DrawContext drawContext, int screenWidth, int screenHeight) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor((float) Math.random(), (float) Math.random(), (float) Math.random(), 1.0f);
        long currentTime2 = System.currentTimeMillis();
        if (currentTime2 - lastShakeUpdate2 >= 10) {
            shakeX2 = -(int) (Math.random() * 20);
            shakeY2 = -(int) (Math.random() * 20);
            lastShakeUpdate2 = currentTime2;
        }

        drawContext.drawTexture(OVERLAY_IMAGE, shakeX2, shakeY2, 0, 0,
                screenWidth + 20, screenHeight + 20,
                screenWidth, screenHeight);

        RenderSystem.disableBlend();
    }
}