package com.pryzmm.splitself.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.pryzmm.splitself.SplitSelf;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class GlitchOverlay {
    public static boolean overlayVisible = false;

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
        renderOverlayImage(drawContext, screenWidth + 200, screenHeight + 200, OVERLAY_IMAGE, true);
        renderOverlayContent(drawContext);
    }

    public static void renderOverlayImage(DrawContext drawContext, int imageWidth, int imageHeight, Identifier image, boolean randomColor) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        if (randomColor) {
            RenderSystem.setShaderColor((float) (Math.random()/5 + 0.8), (float) (Math.random()/5 + 0.8), (float) (Math.random()/5 + 0.8), 1.0f);
        } else {
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        }

        drawContext.drawTexture(image, 0, 0, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();
    }

    public static void renderOverlayContent(DrawContext drawContext) {
        MinecraftClient client = MinecraftClient.getInstance();
        TextRenderer textRenderer = client.textRenderer;
        String overlayText = Text.translatable("selectWorld.warning.lowDiskSpace.title").getString() + " " + Text.translatable("selectWorld.warning.lowDiskSpace.description").getString().replace("\n", " ");
        drawContext.drawTextWithShadow(textRenderer, overlayText, 200, 100, 0xFFFFFF);
    }
}