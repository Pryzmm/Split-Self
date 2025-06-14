package com.pryzmm.splitself.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.pryzmm.splitself.SplitSelf;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class ScreenOverlayRenderer {
    public static KeyBinding toggleOverlayKey;
    public static boolean overlayVisible = false;
    public static long lastShakeUpdate = 0;
    public static long lastShakeUpdate2 = 0;
    static int shakeX;
    static int shakeY;
    static int shakeX2;
    static int shakeY2;

    // Define your image texture - replace "splitself" with your mod id
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
        MinecraftClient client = MinecraftClient.getInstance();
        TextRenderer textRenderer = client.textRenderer;

        // Render the image first (before text so text appears on top)
        renderImageOverlay(drawContext, screenWidth, screenHeight);

        // Example: Center some text on the overlay
        String overlayText = "You did this to me";
        int textWidth = textRenderer.getWidth(overlayText);
        int textX = ((screenWidth - textWidth) / 2) + 10;
        int textY = (screenHeight / 2) + 10;

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastShakeUpdate >= 50) {
            shakeX = -(int) (Math.random() * 20);
            shakeY = -(int) (Math.random() * 20);
        }

        // Draw text with shadow - using white color with full opacity
        drawContext.drawTextWithShadow(textRenderer, overlayText, textX+shakeX, textY+shakeY, 0xFFFFFF);
    }

    public static void renderImageOverlay(DrawContext drawContext, int screenWidth, int screenHeight) {
        try {
            // Test with a simple centered image first
            renderCenteredImage(drawContext, screenWidth, screenHeight);
        } catch (Exception e) {
            // If image fails to load, we'll see this in the console
            System.err.println("Failed to render overlay image: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void renderCenteredImage(DrawContext drawContext, int screenWidth, int screenHeight) {
        // Enable blending for transparency
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor((float) Math.random(), (float) Math.random(), (float) Math.random(), 1.0f);

        // Update shake position every 50ms
        long currentTime2 = System.currentTimeMillis();
        if (currentTime2 - lastShakeUpdate2 >= 50) {
            shakeX2 = -(int) (Math.random() * 20);
            shakeY2 = -(int) (Math.random() * 20);
            lastShakeUpdate2 = currentTime2;
        }

        // Draw with current shake offset
        drawContext.drawTexture(OVERLAY_IMAGE, shakeX2, shakeY2, 0, 0,
                screenWidth + 20, screenHeight + 20,
                screenWidth, screenHeight);

        RenderSystem.disableBlend();
    }
}