package com.pryzmm.splitself.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.pryzmm.splitself.block.BrainBlock;
import com.pryzmm.splitself.screen.BrokenScreen;
import com.pryzmm.splitself.screen.overlay.RecursiveRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DownloadingTerrainScreen.class)
public class LevelLoadMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    public void onRenderLoadLevelScreen(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (BrainBlock.brokenEffectActive) {
            ci.cancel();
            Identifier frameTex;
            try {
                MinecraftClient client = MinecraftClient.getInstance();
                if (BrokenScreen.capturedFrameTexture != null && client != null && client.getTextureManager().getTexture(BrokenScreen.capturedFrameTexture) != null) {
                    frameTex = BrokenScreen.capturedFrameTexture;
                } else {
                    frameTex = RecursiveRenderer.CAPTURE_TEXTURE_ID;
                }
            } catch (Throwable t) {
                frameTex = RecursiveRenderer.CAPTURE_TEXTURE_ID;
            }

            renderOverlayImage(context, context.getScaledWindowWidth(), context.getScaledWindowHeight(), frameTex);
            renderOverlayImage(context, context.getScaledWindowWidth(), context.getScaledWindowHeight(), BrokenScreen.OVERLAY_IMAGE);
        }
    }

    @Unique
    public void renderOverlayImage(DrawContext drawContext, int screenWidth, int screenHeight, Identifier image) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        drawContext.drawTexture(image, 0, 0, 0, 0, screenWidth, screenHeight, screenWidth, screenHeight);

        RenderSystem.disableBlend();
    }

    @Inject(method = "close", at = @At("RETURN"))
    public void onCloseLoadLevelScreen(CallbackInfo ci) {
        BrainBlock.brokenEffectActive = false;
        try {
            MinecraftClient client = MinecraftClient.getInstance();
            if (BrokenScreen.capturedFrameTexture != null && client != null && client.getTextureManager().getTexture(BrokenScreen.capturedFrameTexture) != null) {
                try { client.getTextureManager().destroyTexture(BrokenScreen.capturedFrameTexture); } catch (Throwable ignored) {}
            }
        } catch (Throwable ignored) {}
        BrokenScreen.capturedFrameTexture = null;
    }

}
