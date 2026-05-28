package com.pryzmm.splitself.screen.misc;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.pryzmm.splitself.sound.ModSounds;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.Window;
import net.minecraft.sound.SoundCategory;
import java.util.List;

public class BlendManager {

    public static boolean modifyBlend = false;
    public static boolean invertBlend = false;

    private record Blend(GlStateManager.SrcFactor src, GlStateManager.DstFactor dst) {}
    private static final List<Blend> blends = List.of(
        //new Blend(GlStateManager.SrcFactor.ONE_MINUS_DST_COLOR, GlStateManager.DstFactor.ONE_MINUS_SRC_COLOR),    // Invert
        //new Blend(GlStateManager.SrcFactor.ONE_MINUS_DST_COLOR, GlStateManager.DstFactor.ZERO),                   // Black sky, white blocks
        //new Blend(GlStateManager.SrcFactor.DST_ALPHA,           GlStateManager.DstFactor.ZERO),                   // White sky, black blocks
        new Blend(GlStateManager.SrcFactor.ZERO,                GlStateManager.DstFactor.ONE_MINUS_DST_COLOR),      // Darkened
        new Blend(GlStateManager.SrcFactor.DST_COLOR,           GlStateManager.DstFactor.SRC_COLOR)                 // Brightened
    );

    private static Blend activeBlend = null;
    public static void render(DrawContext context, RenderTickCounter tickDelta) {
        if (modifyBlend && activeBlend == null && timePassed(lastDisableBlend) >= 30 && Math.random() * 20 < 1) {
            activeBlend = blends.get((int) (Math.random() * blends.size()));
            lastChangeBlend = System.nanoTime();
            MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(ModSounds.TONE, (float) ((Math.random() / 2) + 0.5f)));
        }
        else if (modifyBlend && activeBlend != null && timePassed(lastChangeBlend) >= 4 && Math.random() * 20 < 1) {
            activeBlend = null;
            lastDisableBlend = System.nanoTime();
            MinecraftClient.getInstance().getSoundManager().stopSounds(ModSounds.TONE.getId(), SoundCategory.MASTER);
        }
        if (invertBlend || (modifyBlend && activeBlend != null)) {
            RenderSystem.enableBlend();
            if (modifyBlend && activeBlend != null) {
                RenderSystem.blendFunc(
                    activeBlend.src,
                    activeBlend.dst
                );
            } else if (invertBlend) {
                RenderSystem.blendFunc(
                    GlStateManager.SrcFactor.ONE_MINUS_DST_COLOR,
                    GlStateManager.DstFactor.ONE_MINUS_SRC_COLOR
                );
            }
            RenderSystem.setShader(GameRenderer::getPositionColorProgram);
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

            Window window = MinecraftClient.getInstance().getWindow();
            int w = window.getScaledWidth();
            int h = window.getScaledHeight();

            Tessellator tess = Tessellator.getInstance();
            BufferBuilder buf = tess.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            if (modifyBlend && activeBlend != null) {
                buf.vertex(0, 0, 0).color(1f, 1f, 1f, 1f);
                buf.vertex(0, h, 0).color(1f, 1f, 1f, 1f);
                buf.vertex(w, h, 0).color(1f, 1f, 1f, 1f);
                buf.vertex(w, 0, 0).color(1f, 1f, 1f, 1f);
            } else if (invertBlend) {
                buf.vertex(0, 0, 0).color(1f, 1f, 1f, 1f);
                buf.vertex(0, (float) (((double) h / 2) + (Math.random() * 20 - 10)), 0).color(1f, 1f, 1f, 1f);
                buf.vertex(w, (float) (((double) h / 2) + (Math.random() * 40 - 20)), 0).color(1f, 1f, 1f, 1f);
                buf.vertex(w, 0, 0).color(1f, 1f, 1f, 1f);
            }
            BufferRenderer.drawWithGlobalProgram(buf.end());

            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
        }
    }

    private static long lastChangeBlend = System.nanoTime();
    private static long lastDisableBlend = System.nanoTime();
    private static float timePassed(long pastTime) {
        return (System.nanoTime() - pastTime) / 1_000_000_000F;
    }

}
