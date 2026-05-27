package com.pryzmm.splitself.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.pryzmm.splitself.SplitSelf;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

public class StaticOverlay {
    public static boolean overlayVisible = false;

    private static Long startTime = null;

    public static final Identifier OVERLAY_IMAGE = Identifier.of(SplitSelf.MOD_ID, "textures/screen/static.png");

    private static final float[] degrees = new float[]{0, 90, 180, 270};

    public static void register() {
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            if (overlayVisible) renderTopLayerOverlay(drawContext, degrees[(int) (Math.random() * degrees.length)]);
        });
    }

    public static void toggleOverlay() {
        overlayVisible = !overlayVisible;
        if (overlayVisible) startTime = System.nanoTime();
        else startTime = null;
    }

    private static float calculateVisibility() {
        if (startTime == null) return 0;
        float visibility = (float) (System.nanoTime() - startTime) / 1_000_000_000 / 20;
        return Math.min(visibility, 1.0f);
    }

    public static void renderTopLayerOverlay(DrawContext drawContext, float degrees) {
        MinecraftClient client = MinecraftClient.getInstance();
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();
        MatrixStack matrices = drawContext.getMatrices();
        matrices.push();
        matrices.translate(0, 0, 1000);
        matrices.translate(screenWidth / 2f, screenHeight / 2f, 0);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(degrees));
        boolean sideways = (degrees % 180 != 0);
        if (sideways) {
            float scaleX = (float) screenHeight / screenWidth;
            float scaleY = (float) screenWidth / screenHeight;
            matrices.scale(scaleX, scaleY, 1.0f);
        }
        matrices.translate(-screenWidth / 2f, -screenHeight / 2f, 0);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        renderOverlayImage(drawContext, screenWidth, screenHeight, OVERLAY_IMAGE);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        matrices.pop();
    }

    public static void renderOverlayImage(DrawContext drawContext, int imageWidth, int imageHeight, Identifier image) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, calculateVisibility());

        drawContext.drawTexture(image, 0, 0, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();
    }

}