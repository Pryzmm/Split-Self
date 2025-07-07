package com.pryzmm.splitself.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.pryzmm.splitself.SplitSelf;
import com.pryzmm.splitself.world.FirstJoinTracker;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

import java.text.DateFormat;
import java.util.Date;

public class EmergencyOverlayRenderer {
    public static boolean overlayVisible = false;
    private static String currentCity = "";
    private static boolean callbackRegistered = false;
    private static long startTime = 0;
    private static final double SCROLL_SPEED = 150.0; // Pixels per second

    public static long lastShakeUpdate = 0;
    static int shakeX;
    static int shakeY;
    static FirstJoinTracker tracker;

    public static final Identifier OVERLAY_IMAGE = Identifier.of(SplitSelf.MOD_ID, "textures/screen/overlay.png");

    public static void toggleOverlay(PlayerEntity player, String city) {
        overlayVisible = !overlayVisible;
        currentCity = city;

        // Reset start time when toggling on
        if (overlayVisible) {
            startTime = System.currentTimeMillis();
        }

        tracker = FirstJoinTracker.getServerState(player.getServer());

        // Only register the callback once
        if (!callbackRegistered) {
            HudRenderCallback.EVENT.register(EmergencyOverlayRenderer::renderHud);
            callbackRegistered = true;
        }
    }

    private static void renderHud(DrawContext drawContext, RenderTickCounter tickCounter) {
        if (overlayVisible) {
            renderTopLayerOverlay(drawContext, currentCity);
        }
    }

    public static void renderTopLayerOverlay(DrawContext drawContext, String city) {
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

        // Black background
        drawContext.fill(0, 0, screenWidth, screenHeight, 0xFF000000);

        // Add overlay content
        renderOverlayContent(drawContext, screenWidth, screenHeight, city);

        // Restore render states
        RenderSystem.disablePolygonOffset();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();

        matrices.pop();
    }

    public static void renderOverlayContent(DrawContext drawContext, int screenWidth, int screenHeight, String city) {
        MinecraftClient client = MinecraftClient.getInstance();
        TextRenderer textRenderer = client.textRenderer;

        renderImageOverlay(drawContext, screenWidth, screenHeight);

        // Main title (static - no scrolling)
        String overlayText = "EMERGENCY NOTICE";
        int titleY = screenHeight / 3;

        // Draw main title with scaling
        MatrixStack matrices = drawContext.getMatrices();
        matrices.push();
        matrices.scale(2.0f, 2.0f, 1.0f);

        int titleWidth = textRenderer.getWidth(overlayText);
        int titleX = (screenWidth - titleWidth * 2) / 4; // Divide by 4 because we're in scaled space
        int scaledTitleY = titleY / 2; // Divide by 2 because we're in scaled space

        drawContext.drawTextWithShadow(textRenderer, overlayText, titleX, scaledTitleY, 0xFFFFFF);
        matrices.pop();

        if (!tracker.getPlayerPII(client.player.getUuid())) {
            city = "[REDACTED]";
        }

        // Scrolling text below
        String smallerText = "A civil authority has issued an alert for: " + city + "; " + (DateFormat.getTimeInstance().format(new Date(System.currentTimeMillis())) + ". Effective until further notice. Sightings report of anomalous entity roaming the streets of " + city + " with malicious intent. Stay inside. Do not interact with anyone not already present. This is not a drill.");
        int smallerTextY = titleY + 100;
        int smallerTextWidth = textRenderer.getWidth(smallerText);

        // Calculate scroll position based on elapsed time
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - startTime;
        double scrollOffset = (elapsedTime / 1000.0) * SCROLL_SPEED; // Convert ms to seconds

        // Calculate text position (start from right edge)
        int scrollX = (int)(screenWidth - scrollOffset);

        // Reset when text completely leaves screen
        if (scrollX + smallerTextWidth < 0) {
            startTime = currentTime; // Reset the timer
        }

        // Draw scrolling text
        drawContext.drawTextWithShadow(textRenderer, smallerText, scrollX, smallerTextY, 0xFFFFFF);
    }

    public static void renderImageOverlay(DrawContext drawContext, int screenWidth, int screenHeight) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor((float) ((Math.random() / 10) + 0.9), (float) ((Math.random() / 10) + 0.9), (float) ((Math.random() / 10) + 0.9), 1.0f);
        long currentTime2 = System.currentTimeMillis();
        if (currentTime2 - lastShakeUpdate >= 10) {
            shakeX = -(int) (Math.random() * 50);
            shakeY = -(int) (Math.random() * 50);
            lastShakeUpdate = currentTime2;
        }

        drawContext.drawTexture(OVERLAY_IMAGE, shakeX, shakeY, 0, 0,
                screenWidth + 50, screenHeight + 50,
                screenWidth, screenHeight);

        RenderSystem.disableBlend();
    }
}