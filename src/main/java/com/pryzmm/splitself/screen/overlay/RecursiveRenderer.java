package com.pryzmm.splitself.screen.overlay;

import com.mojang.blaze3d.systems.RenderSystem;
import com.pryzmm.splitself.SplitSelf;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.BufferUtils;
import java.nio.ByteBuffer;

public class RecursiveRenderer {
    public static boolean overlayVisible = false;

    private static final Identifier CAPTURE_TEXTURE_ID = Identifier.of("splitself", "recursive_capture");

    private static int previewTexId = 0;
    private static int lastWidth = -1, lastHeight = -1;
    private static ByteBuffer previewPixelBuffer = null;
    private static ByteBuffer lastPreviewPixelBuffer = null;
    private static ByteBuffer fullPixelBuffer = null;

    public static void init() {
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            if (overlayVisible) renderTopLayerOverlay(drawContext);
        });
    }

    public static void toggleOverlay() {
        overlayVisible = !overlayVisible;
        if (!overlayVisible) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client != null) client.execute(RecursiveRenderer::cleanup);
            else cleanup();
        }
    }

    private static void cleanup() {
        try {
            try {
                MinecraftClient client = MinecraftClient.getInstance();
                if (client != null && client.getTextureManager() != null) {
                    client.getTextureManager().destroyTexture(CAPTURE_TEXTURE_ID);
                }
            } catch (Throwable ignored) {}

            if (previewTexId != 0) {
                try {
                    GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
                    GL11.glDeleteTextures(previewTexId);
                } catch (Throwable ignored) {}
                previewTexId = 0;
            }
            if (previewPixelBuffer != null) {
                previewPixelBuffer.clear();
                previewPixelBuffer = null;
            }
            if (lastPreviewPixelBuffer != null) {
                lastPreviewPixelBuffer.clear();
                lastPreviewPixelBuffer = null;
            }
            if (fullPixelBuffer != null) {
                fullPixelBuffer.clear();
                fullPixelBuffer = null;
            }
            lastWidth = -1;
            lastHeight = -1;
            SplitSelf.LOGGER.info("[SplitSelf] Recursive overlay cleanup complete");
        } catch (Throwable t) {
            SplitSelf.LOGGER.error("[SplitSelf] Error during recursive overlay cleanup: {}", String.valueOf(t));
        }
    }

    private static void ensureBuffer(int width, int height) {
        if (previewTexId == 0 || width != lastWidth || height != lastHeight) {
            try {
                if (previewTexId != 0) {
                    try {
                        MinecraftClient client = MinecraftClient.getInstance();
                        if (client != null && client.getTextureManager() != null) {
                            client.getTextureManager().destroyTexture(CAPTURE_TEXTURE_ID);
                        }
                    } catch (Throwable ignored) {}
                    try {
                        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
                        GL11.glDeleteTextures(previewTexId);
                    } catch (Throwable ignored) {}
                    previewTexId = 0;
                }

                previewTexId = GL11.glGenTextures();
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, previewTexId);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
                GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (java.nio.ByteBuffer) null);
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

                ExternalGlTexture previewTextureWrapper = new ExternalGlTexture(previewTexId);
                previewTextureWrapper.setFilter(false, false);
                MinecraftClient.getInstance().getTextureManager().registerTexture(CAPTURE_TEXTURE_ID, previewTextureWrapper);
                lastWidth = width;
                lastHeight = height;
            } catch (Throwable t) {
                SplitSelf.LOGGER.error("[SplitSelf] ensureBuffer (preview texture) failed: {}", String.valueOf(t));
            }
        }
    }

    public static void renderTopLayerOverlay(DrawContext drawContext) {
        int windowW = drawContext.getScaledWindowWidth();
        int windowH = drawContext.getScaledWindowHeight();

        int boxW = windowW - 4;
        int boxH = windowH - 4;
        int x = 2;
        int y = 2;

        MinecraftClient client = MinecraftClient.getInstance();

        boolean previewUploadFailed = false;
        try {
            ensureBuffer(boxW, boxH);
            
            if (previewTexId == 0) {
                SplitSelf.LOGGER.info("[SplitSelf] preview texture still missing after ensureBuffer — aborting CPU readback");
                try {
                    drawContext.fill(x - 1, y - 1, x + boxW + 1, y + boxH + 1, 0xFFFF00FF);
                } catch (Throwable ignored) {}
                return;
            }

            int fbWidth = client.getWindow().getFramebufferWidth();
            int fbHeight = client.getWindow().getFramebufferHeight();

            int pixelCount = boxW * boxH;
            int required = pixelCount * 4;
            if (previewPixelBuffer == null || previewPixelBuffer.capacity() < required) previewPixelBuffer = BufferUtils.createByteBuffer(required);
            else previewPixelBuffer.clear();

            int fbPixelCount = fbWidth * fbHeight;
            long fullRequiredLong = (long) fbPixelCount * 4L;
            if (fullRequiredLong <= Integer.MAX_VALUE) {
                int fullRequired = (int) fullRequiredLong;
                if (fullPixelBuffer == null || fullPixelBuffer.capacity() < fullRequired) {
                    fullPixelBuffer = BufferUtils.createByteBuffer(fullRequired);
                } else {
                    fullPixelBuffer.clear();
                }

                GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, client.getFramebuffer().fbo);
                GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT, 1);
                GL11.glReadPixels(0, 0, fbWidth, fbHeight, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, fullPixelBuffer);
                GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, 0);

                try {
                    if (lastPreviewPixelBuffer != null && lastPreviewPixelBuffer.capacity() >= boxW * boxH * 4) {
                        float sx = (float) fbWidth / windowW;
                        float sy = (float) fbHeight / windowH;

                        float previewPixelScaleX = (float) fbWidth / boxW;
                        float previewPixelScaleY = (float) fbHeight / boxH;
                        
                        int pixelX = Math.round(x * sx);
                        int pixelYTop = Math.round(y * sy);
                        
                        lastPreviewPixelBuffer.rewind();
                        for (int py = 0; py < boxH; py++) {
                            for (int px = 0; px < boxW; px++) {
                                int previewIndex = (py * boxW + px) * 4;
                                byte r = lastPreviewPixelBuffer.get(previewIndex);
                                byte g = lastPreviewPixelBuffer.get(previewIndex + 1);
                                byte b = lastPreviewPixelBuffer.get(previewIndex + 2);
                                byte a = lastPreviewPixelBuffer.get(previewIndex + 3);

                                int destXStart = (int) (pixelX + px * previewPixelScaleX);
                                int destXEnd = (int) (pixelX + (px + 1) * previewPixelScaleX);
                                int destYTopStart = (int) (pixelYTop + py * previewPixelScaleY);
                                int destYTopEnd = (int) (pixelYTop + (py + 1) * previewPixelScaleY);
                                
                                // Fill the rectangle with this preview pixel's color
                                for (int fy = destYTopStart; fy < destYTopEnd; fy++) {
                                    int destY = fbHeight - 1 - fy;
                                    if (destY < 0 || destY >= fbHeight) continue;
                                    for (int fx = destXStart; fx < destXEnd; fx++) {
                                        if (fx < 0 || fx >= fbWidth) continue;
                                        int destIndex = (destY * fbWidth + fx) * 4;
                                        fullPixelBuffer.put(destIndex, r);
                                        fullPixelBuffer.put(destIndex + 1, g);
                                        fullPixelBuffer.put(destIndex + 2, b);
                                        fullPixelBuffer.put(destIndex + 3, a);
                                    }
                                }
                            }
                        }
                    }
                } catch (Throwable t) { SplitSelf.LOGGER.error("[SplitSelf] preview composite failed: {}", String.valueOf(t)); }

                for (int py = 0; py < boxH; py++) {
                    int srcY = fbHeight - 1 - (int) (((long) py * fbHeight) / boxH);
                    for (int px = 0; px < boxW; px++) {
                        int srcX = (int) (((long) px * fbWidth) / boxW);
                        int srcIndex = (srcY * fbWidth + srcX) * 4;
                        byte r = fullPixelBuffer.get(srcIndex);
                        byte g = fullPixelBuffer.get(srcIndex + 1);
                        byte b = fullPixelBuffer.get(srcIndex + 2);
                        byte a = fullPixelBuffer.get(srcIndex + 3);
                        previewPixelBuffer.put(r).put(g).put(b).put(a);
                    }
                }

                GL11.glBindTexture(GL11.GL_TEXTURE_2D, previewTexId);
                GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
                previewPixelBuffer.rewind();
                GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, boxW, boxH, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, previewPixelBuffer);
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

                try {
                    int previewRequired = boxW * boxH * 4;
                    if (lastPreviewPixelBuffer == null || lastPreviewPixelBuffer.capacity() < previewRequired) lastPreviewPixelBuffer = BufferUtils.createByteBuffer(previewRequired);
                    else lastPreviewPixelBuffer.clear();
                    previewPixelBuffer.rewind();
                    lastPreviewPixelBuffer.put(previewPixelBuffer);
                    lastPreviewPixelBuffer.rewind();
                } catch (Throwable t) { SplitSelf.LOGGER.error("[SplitSelf] failed to copy preview to lastPreview buffer: {}", String.valueOf(t)); }
            } else SplitSelf.LOGGER.info("[SplitSelf] framebuffer too large to read for downsample fallback");
        } catch (Throwable t) {
            SplitSelf.LOGGER.error("[SplitSelf] CPU readback->texture upload failed: {}", String.valueOf(t));
            previewUploadFailed = true;
        }

        MatrixStack matrices = drawContext.getMatrices();
        matrices.push();
        matrices.translate(0.0D, 0.0D, 1000.0D);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();

        if (!previewUploadFailed && previewTexId != 0) {
            RenderSystem.setShader(GameRenderer::getPositionTexProgram);
            RenderSystem.setShaderTexture(0, CAPTURE_TEXTURE_ID);
            drawContext.drawTexture(
                CAPTURE_TEXTURE_ID,
                x, y,
                0, 0,
                boxW, boxH,
                lastWidth, lastHeight
            );
        }

        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        matrices.pop();
    }

    private static class ExternalGlTexture extends AbstractTexture {
        ExternalGlTexture(int externalGlId) { this.glId = externalGlId; }

        @Override
        public void load(ResourceManager manager) {}

        @Override
        public void close() {}
    }

}
