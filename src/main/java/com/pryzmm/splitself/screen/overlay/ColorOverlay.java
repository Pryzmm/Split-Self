package com.pryzmm.splitself.screen.overlay;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

public class ColorOverlay {

    public static boolean overlayVisible = false;
    private static int color = 0x00000000;

    static {
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            if (overlayVisible) renderTopLayerOverlay(drawContext);
        });
    }

    public static void toggleOverlay(boolean toggled) {
        overlayVisible = toggled;
    }
    public static void setColor(int color) { ColorOverlay.color = color; }

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
        drawContext.fill(0, 0, screenWidth, screenHeight, color);

        RenderSystem.disablePolygonOffset();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();

        matrices.pop();
    }

}
