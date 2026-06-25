package com.pryzmm.splitself.screen.overlay;

import com.mojang.blaze3d.systems.RenderSystem;
import com.pryzmm.splitself.SplitSelf;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;
import java.nio.ByteBuffer;
import java.util.function.Consumer;

public class RecursiveRenderer {

    public static boolean overlayVisible = false;

    public static Identifier CAPTURE_TEXTURE_ID = Identifier.of(SplitSelf.MOD_ID, "textures/screen/capture.png");

    private static final int INSET_PIXELS = 6;

    private static Framebuffer tunnelCapture = null;
    private static int tunnelWidth = -1, tunnelHeight = -1;

    private static volatile boolean worldActive = false;

    public static void init() {
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            if (overlayVisible) renderTunnel(drawContext);
        });

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> worldActive = true);
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            worldActive = false;
            client.execute(RecursiveRenderer::releaseGpuResources);
        });
    }

    private static boolean isSafeState() {
        if (!worldActive) return false;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) return false;
        if (client.world == null) return false;
        return client.getWindow() != null;
    }

    public static void toggleOverlay() {
        overlayVisible = !overlayVisible;
        if (!overlayVisible) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client != null) client.execute(RecursiveRenderer::releaseGpuResources);
            else releaseGpuResources();
        }
    }

    private static void releaseGpuResources() {
        try {
            if (tunnelCapture != null) {
                try { tunnelCapture.delete(); } catch (Throwable ignored) {}
                tunnelCapture = null;
            }
            tunnelWidth = -1;
            tunnelHeight = -1;
            SplitSelf.LOGGER.info("[SplitSelf] RecursiveRenderer GPU resources released");
        } catch (Throwable t) {
            SplitSelf.LOGGER.error("[SplitSelf] releaseGpuResources failed: {}", String.valueOf(t));
        }
    }

    private static void ensureTunnelCapture(int width, int height) {
        if (tunnelCapture != null && width == tunnelWidth && height == tunnelHeight) return;

        if (tunnelCapture != null) {
            try { tunnelCapture.delete(); } catch (Throwable ignored) {}
            tunnelCapture = null;
        }

        try {
            SimpleFramebuffer fb = new SimpleFramebuffer(width, height, false, false);
            fb.setTexFilter(GL11.GL_LINEAR);
            tunnelCapture = fb;
            tunnelWidth = width;
            tunnelHeight = height;
        } catch (Throwable t) {
            SplitSelf.LOGGER.error("[SplitSelf] ensureTunnelCapture failed: {}", String.valueOf(t));
            tunnelCapture = null;
            tunnelWidth = -1;
            tunnelHeight = -1;
        }
    }

    private static void renderTunnel(DrawContext drawContext) {
        if (!isSafeState()) return;

        MinecraftClient client = MinecraftClient.getInstance();
        Framebuffer mainFb = client.getFramebuffer();
        if (mainFb == null) return;

        int windowW = drawContext.getScaledWindowWidth();
        int windowH = drawContext.getScaledWindowHeight();
        if (windowW <= 0 || windowH <= 0) return;

        try {
            ensureTunnelCapture(mainFb.textureWidth, mainFb.textureHeight);
            if (tunnelCapture == null) return;

            int insetX = INSET_PIXELS;
            int insetY = INSET_PIXELS;
            int drawW = Math.max(1, windowW - insetX * 2);
            int drawH = Math.max(1, windowH - insetY * 2);

            MatrixStack matrices = drawContext.getMatrices();
            matrices.push();
            matrices.translate(0.0D, 0.0D, 500.0D);

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableDepthTest();
            RenderSystem.setShader(GameRenderer::getPositionTexProgram);
            RenderSystem.setShaderTexture(0, tunnelCapture.getColorAttachment());

            float x1 = insetX + drawW, y1 = insetY + drawH;
            var matrix = matrices.peek().getPositionMatrix();

            BufferBuilder buffer = Tessellator.getInstance().begin(
                    VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
            buffer.vertex(matrix, (float) insetX, y1, 0.0f).texture(0.0f, 0.0f);
            buffer.vertex(matrix, x1, y1, 0.0f).texture(1.0f, 0.0f);
            buffer.vertex(matrix, x1, (float) insetY, 0.0f).texture(1.0f, 1.0f);
            buffer.vertex(matrix, (float) insetX, (float) insetY, 0.0f).texture(0.0f, 1.0f);
            BufferRenderer.drawWithGlobalProgram(buffer.end());

            RenderSystem.enableDepthTest();
            RenderSystem.disableBlend();
            matrices.pop();

            GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, mainFb.fbo);
            GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, tunnelCapture.fbo);
            GL30.glBlitFramebuffer(
                    0, 0, mainFb.textureWidth, mainFb.textureHeight,
                    0, 0, tunnelCapture.textureWidth, tunnelCapture.textureHeight,
                    GL11.GL_COLOR_BUFFER_BIT, GL11.GL_LINEAR
            );
            GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, 0);
            GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, 0);
            mainFb.beginWrite(false);
        } catch (Throwable t) {
            SplitSelf.LOGGER.error("[SplitSelf] renderTunnel failed: {}", String.valueOf(t));
        }
    }

    public static void captureFreezeFrameAsync(Consumer<Identifier> callback) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) {
            callback.accept(null);
            return;
        }

        Runnable doCapture = () -> callback.accept(performFreezeFrameCapture());

        if (client.isOnThread()) {
            doCapture.run();
        } else {
            client.execute(doCapture);
        }
    }

    private static Identifier performFreezeFrameCapture() {
        if (!isSafeState()) {
            SplitSelf.LOGGER.info("[SplitSelf] freeze-frame capture skipped — unsafe client state");
            return null;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        Framebuffer mainFb = client.getFramebuffer();
        if (mainFb == null) return null;

        int width = mainFb.textureWidth;
        int height = mainFb.textureHeight;
        if (width <= 0 || height <= 0) return null;

        ByteBuffer readBuffer = null;
        int texId = 0;
        try {
            int required = width * height * 4;
            readBuffer = MemoryUtil.memAlloc(required);

            GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, mainFb.fbo);
            GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT, 1);
            GL11.glReadPixels(0, 0, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, readBuffer);
            GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, 0);

            int rowBytes = width * 4;
            byte[] rowA = new byte[rowBytes];
            byte[] rowB = new byte[rowBytes];
            for (int y = 0; y < height / 2; y++) {
                int topOffset = y * rowBytes;
                int bottomOffset = (height - 1 - y) * rowBytes;
                readBuffer.position(topOffset);
                readBuffer.get(rowA, 0, rowBytes);
                readBuffer.position(bottomOffset);
                readBuffer.get(rowB, 0, rowBytes);
                readBuffer.position(topOffset);
                readBuffer.put(rowB, 0, rowBytes);
                readBuffer.position(bottomOffset);
                readBuffer.put(rowA, 0, rowBytes);
            }
            readBuffer.rewind();

            texId = GL11.glGenTextures();
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, texId);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height,
                    0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, readBuffer);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

            ExternalGlTexture wrapper = new ExternalGlTexture(texId);
            String uniqueName = "dynamic/freeze_frame_" + System.currentTimeMillis();
            Identifier id = Identifier.of(SplitSelf.MOD_ID, uniqueName);
            client.getTextureManager().registerTexture(id, wrapper);
            SplitSelf.LOGGER.info("[SplitSelf] Freeze frame captured to {} (GL id {})", id, texId);
            CAPTURE_TEXTURE_ID = id;
            return id;
        } catch (Throwable t) {
            SplitSelf.LOGGER.error("[SplitSelf] performFreezeFrameCapture failed: {}", String.valueOf(t));
            if (texId != 0) {
                try { GL11.glDeleteTextures(texId); } catch (Throwable ignored) {}
            }
            return null;
        } finally {
            if (readBuffer != null) {
                try { MemoryUtil.memFree(readBuffer); } catch (Throwable ignored) {}
            }
        }
    }

    private static class ExternalGlTexture extends AbstractTexture {
        ExternalGlTexture(int externalGlId) {
            this.glId = externalGlId;
        }

        @Override
        public void load(ResourceManager manager) {}

        @Override
        public void close() {
            try {
                if (this.glId != 0) {
                    try { GL11.glDeleteTextures(this.glId); } catch (Throwable ignored) {}
                    this.glId = 0;
                }
            } catch (Throwable ignored) {}
        }
    }
}