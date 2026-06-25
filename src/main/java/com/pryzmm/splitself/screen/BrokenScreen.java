package com.pryzmm.splitself.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.pryzmm.splitself.SplitSelf;
import com.pryzmm.splitself.screen.overlay.RecursiveRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class BrokenScreen extends Screen {

    public static final Identifier OVERLAY_IMAGE = Identifier.of(SplitSelf.MOD_ID, "textures/screen/broken.png");
    public static Identifier capturedFrameTexture = null;

    public BrokenScreen() {
        super(Text.empty());
        RecursiveRenderer.captureFreezeFrameAsync(id -> {
            if (id == null) SplitSelf.LOGGER.info("[SplitSelf] freeze frame capture failed/unsafe");
            capturedFrameTexture = id;
        });
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        Identifier frameTex;
        try {
            MinecraftClient client = MinecraftClient.getInstance();
            if (capturedFrameTexture != null && client != null && client.getTextureManager().getTexture(capturedFrameTexture) != null) {
                frameTex = capturedFrameTexture;
            } else {
                frameTex = RecursiveRenderer.CAPTURE_TEXTURE_ID;
            }
        } catch (Throwable t) {
            frameTex = RecursiveRenderer.CAPTURE_TEXTURE_ID;
        }

        renderOverlayImage(context, this.width, this.height, frameTex);
        renderOverlayImage(context, this.width, this.height, OVERLAY_IMAGE);
        super.render(context, mouseX, mouseY, delta);
    }

    public void renderOverlayImage(DrawContext drawContext, int screenWidth, int screenHeight, Identifier image) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // Use the 9-parameter overload: (id, x, y, u, v, width, height, texWidth, texHeight)
        drawContext.drawTexture(image, 0, 0, 0, 0, screenWidth, screenHeight, screenWidth, screenHeight);

        RenderSystem.disableBlend();
    }

    @Override
    public boolean shouldPause() {return true;}

    @Override
    public boolean shouldCloseOnEsc() {return false;}

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {}
}