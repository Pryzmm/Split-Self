package com.pryzmm.splitself.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.pryzmm.splitself.SplitSelf;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class TheOtherWhiteOverlay {
    public static boolean overlayVisible = false;
    public static long lastShakeUpdate = 0;
    static int shakeX;
    static int shakeY;

    public static final Identifier OVERLAY_IMAGE = Identifier.of(SplitSelf.MOD_ID, "textures/screen/overlay_white.png");
    public static final Identifier OVERLAY_PLAYER_IMAGE = Identifier.of(SplitSelf.MOD_ID, "textures/screen/player.png");

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
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastShakeUpdate >= 10) {
            shakeX = -(int) (Math.random() * 200);
            shakeY = -(int) (Math.random() * 200);
            lastShakeUpdate = currentTime;
        }

        renderOverlayImage(drawContext, screenWidth + 200, screenHeight + 200, OVERLAY_IMAGE, shakeX, shakeY, true);

        renderCenteredPlayerImage(drawContext, screenWidth, screenHeight, OVERLAY_PLAYER_IMAGE, 102, 153);
    }

    public static void renderOverlayImage(DrawContext drawContext, int imageWidth, int imageHeight, Identifier image, int offsetX, int offsetY, boolean randomColor) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        if (randomColor) {
            RenderSystem.setShaderColor((float) (Math.random()/5 + 0.8), (float) (Math.random()/5 + 0.8), (float) (Math.random()/5 + 0.8), 1.0f);
        } else {
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        }

        drawContext.drawTexture(image, offsetX, offsetY, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();
    }

    public static void renderCenteredPlayerImage(DrawContext drawContext, int screenWidth, int screenHeight, Identifier image, int imageWidth, int imageHeight) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        int centerX = (screenWidth - imageWidth) / 2;
        int centerY = (screenHeight - imageHeight) / 2;

        drawContext.drawTexture(image, centerX, centerY, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);

        RenderSystem.disableBlend();
    }
}