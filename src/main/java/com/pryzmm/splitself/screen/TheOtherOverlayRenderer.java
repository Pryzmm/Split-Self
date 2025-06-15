package com.pryzmm.splitself.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.pryzmm.splitself.SplitSelf;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class TheOtherOverlayRenderer {
    public static boolean overlayVisible = false;
    public static long lastShakeUpdate = 0;
    static int shakeX;
    static int shakeY;

    // Define your image texture - replace "splitself" with your mod id
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

        // Push a new matrix to ensure we're rendering at the top level
        MatrixStack matrices = drawContext.getMatrices();
        matrices.push();

        // Translate to ensure we're at the front-most z-level
        matrices.translate(0, 0, 1000);

        // Enable blending for transparency
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // Disable depth testing to ensure it renders over everything
        RenderSystem.disableDepthTest();

        // Set a high z-offset to render above everything
        RenderSystem.polygonOffset(-1.0f, -1.0f);
        RenderSystem.enablePolygonOffset();

        // IMPORTANT: Comment out or remove the black fill to see the image
        // drawContext.fill(0, 0, screenWidth, screenHeight, 0xFF000000);

        // Instead, use a semi-transparent fill if you want a background
        drawContext.fill(0, 0, screenWidth, screenHeight, 0x80000000);

        // Add overlay content (this includes the image)
        renderOverlayContent(drawContext, screenWidth, screenHeight);

        // Restore render states
        RenderSystem.disablePolygonOffset();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();

        matrices.pop();
    }

    public static void renderOverlayContent(DrawContext drawContext, int screenWidth, int screenHeight) {
        // Update shake position every 50ms for the overlay image
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastShakeUpdate >= 10) {
            shakeX = -(int) (Math.random() * 200);
            shakeY = -(int) (Math.random() * 200);
            lastShakeUpdate = currentTime;
        }

        // Render the main overlay image with shake and color effect
        renderOverlayImage(drawContext, screenWidth + 200, screenHeight + 200, OVERLAY_IMAGE, shakeX, shakeY, true);

        // Render the player image centered without shake and without color effect
        renderCenteredPlayerImage(drawContext, screenWidth, screenHeight, OVERLAY_PLAYER_IMAGE, 102, 153);
    }

    public static void renderOverlayImage(DrawContext drawContext, int imageWidth, int imageHeight, Identifier image, int offsetX, int offsetY, boolean randomColor) {
        // Enable blending for transparency
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        if (randomColor) {
            RenderSystem.setShaderColor((float) (Math.random()/5 + 0.8), (float) (Math.random()/5 + 0.8), (float) (Math.random()/5 + 0.8), 1.0f);
        } else {
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        }

        // Draw with shake offset
        drawContext.drawTexture(image, offsetX, offsetY, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);

        // Reset shader color
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();
    }

    public static void renderCenteredPlayerImage(DrawContext drawContext, int screenWidth, int screenHeight, Identifier image, int imageWidth, int imageHeight) {
        // Enable blending for transparency
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // Keep normal white color (no random color)
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        // Calculate centered position
        int centerX = (screenWidth - imageWidth) / 2;
        int centerY = (screenHeight - imageHeight) / 2;

        // Draw centered without shake
        drawContext.drawTexture(image, centerX, centerY, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);

        RenderSystem.disableBlend();
    }
}