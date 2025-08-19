package com.pryzmm.splitself.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import java.awt.*;
import java.util.Random;

public class TheOtherOverlay {
    public static boolean overlayVisible = false;
    private static final Random random = new Random();

    public static void toggleOverlay() {
        overlayVisible = !overlayVisible;
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            if (overlayVisible) {
                renderTopLayerOverlay(drawContext);
            }
        });
    }

    public static void renderTopLayerOverlay(DrawContext drawContext) {
        MatrixStack matrices = drawContext.getMatrices();
        matrices.push();

        matrices.translate(0, 0, 1000);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        RenderSystem.disableDepthTest();

        RenderSystem.polygonOffset(-1.0f, -1.0f);
        RenderSystem.enablePolygonOffset();

        renderOverlayContent(drawContext);

        RenderSystem.disablePolygonOffset();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();

        matrices.pop();
    }

    public static void renderOverlayContent(DrawContext drawContext) {
        MinecraftClient client = MinecraftClient.getInstance();
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();
        int numberOfBoxes = 15 + random.nextInt(10);
        for (int i = 0; i < numberOfBoxes; i++) {
            int x = random.nextInt(screenWidth + 100) - 100;
            int y = random.nextInt(screenHeight + 100) - 100;
            int width = 10 + random.nextInt(300);
            int height = 10 + random.nextInt(300);
            int red = random.nextInt(40);
            int green = random.nextInt(40);
            int blue = random.nextInt(40);
            int randomColor = 0xFF000000 | (red << 16) | (green << 8) | blue;
            drawContext.fill(x, y, x + width, y + height, randomColor);
        }
    }
}