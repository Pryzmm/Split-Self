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

        if (overlayVisible) {
            startTime = System.currentTimeMillis();
        }

        tracker = FirstJoinTracker.getServerState(player.getServer());

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

        MatrixStack matrices = drawContext.getMatrices();
        matrices.push();

        matrices.translate(0, 0, 1000);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        RenderSystem.disableDepthTest();

        RenderSystem.polygonOffset(-1.0f, -1.0f);
        RenderSystem.enablePolygonOffset();

        drawContext.fill(0, 0, screenWidth, screenHeight, 0xFF000000);

        renderOverlayContent(drawContext, screenWidth, screenHeight, city);

        RenderSystem.disablePolygonOffset();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();

        matrices.pop();
    }

    public static void renderOverlayContent(DrawContext drawContext, int screenWidth, int screenHeight, String city) {
        MinecraftClient client = MinecraftClient.getInstance();
        TextRenderer textRenderer = client.textRenderer;

        renderImageOverlay(drawContext, screenWidth, screenHeight);

        String overlayText = "EMERGENCY NOTICE";
        int titleY = screenHeight / 3;

        MatrixStack matrices = drawContext.getMatrices();
        matrices.push();
        matrices.scale(2.0f, 2.0f, 1.0f);

        int titleWidth = textRenderer.getWidth(overlayText);
        int titleX = (screenWidth - titleWidth * 2) / 4;
        int scaledTitleY = titleY / 2;

        drawContext.drawTextWithShadow(textRenderer, overlayText, titleX, scaledTitleY, 0xFFFFFF);
        matrices.pop();

        if (!tracker.getPlayerPII(client.player.getUuid())) {
            city = String.valueOf(SplitSelf.translate("events.splitself.redacted_name"));
        }
        String smallerText = String.valueOf(SplitSelf.translate("events.splitself.emergency.message", city, (DateFormat.getTimeInstance().format(new Date(System.currentTimeMillis()))), city));
        int smallerTextY = titleY + 100;
        int smallerTextWidth = textRenderer.getWidth(smallerText);

        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - startTime;
        double scrollOffset = (elapsedTime / 1000.0) * SCROLL_SPEED;

        int scrollX = (int)(screenWidth - scrollOffset);

        if (scrollX + smallerTextWidth < 0) {
            startTime = currentTime;
        }

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