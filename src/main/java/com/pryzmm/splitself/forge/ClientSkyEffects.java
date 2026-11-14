package com.pryzmm.splitself.forge;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Source persistent sky and timed upper-screen inversion, executed only on the client thread. */
@Mod.EventBusSubscriber(modid = SplitSelfForge.MOD_ID, value = Dist.CLIENT)
public final class ClientSkyEffects {
    private static boolean redSky;
    private static final SkyEventRules.Inversion INVERSION = new SkyEventRules.Inversion();
    private ClientSkyEffects() {}
    private static long now() { return System.nanoTime() / 1_000_000L; }
    static void start(String event) {
        var player = Minecraft.getInstance().player;
        if (player == null) return;
        if (event.equals("REDSKY")) {
            redSky = true;
            player.playSound(sound("redsky"), 1, 1);
            player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, SkyEventRules.DARKNESS_TICKS, 1, false, false, false));
        } else {
            player.playSound(sound("tone"), 1, .5f);
            INVERSION.start(now());
        }
    }
    private static SoundEvent sound(String name) { return SoundEvent.createVariableRangeEvent(new ResourceLocation("splitself", name)); }
    public static boolean redSky() { return redSky; }
    static void clearSky() { redSky = false; }
    @SubscribeEvent public static void fog(ViewportEvent.ComputeFogColor event) {
        if (redSky) { event.setRed(136 / 255f); event.setGreen(0); event.setBlue(0); }
    }
    private static void update() {
        if (INVERSION.finish(now())) Minecraft.getInstance().getSoundManager().stop(new ResourceLocation("splitself", "tone"), null);
    }
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event) { if (event.phase == TickEvent.Phase.END) update(); }
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut event) {
        INVERSION.clear();
        Minecraft.getInstance().getSoundManager().stop(new ResourceLocation("splitself", "tone"), null);
        // SkyColor in source is static and persists across worlds until explicitly reset.
    }
    @SubscribeEvent public static void draw(RenderGuiEvent.Post event) {
        update();
        int blend=ClientBlendEffects.active();
        if (!INVERSION.active(now()) && blend==0) return;
        var g = event.getGuiGraphics();
        g.flush();
        var matrix = g.pose().last().pose();
        int w = g.guiWidth(), h = g.guiHeight();
        RenderSystem.enableBlend(); RenderSystem.disableDepthTest();
        RenderSystem.blendFunc(com.mojang.blaze3d.platform.GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR,
                com.mojang.blaze3d.platform.GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR);
        if(blend==1)RenderSystem.blendFunc(com.mojang.blaze3d.platform.GlStateManager.SourceFactor.ZERO,com.mojang.blaze3d.platform.GlStateManager.DestFactor.ONE_MINUS_DST_COLOR);
        else if(blend==2)RenderSystem.blendFunc(com.mojang.blaze3d.platform.GlStateManager.SourceFactor.DST_COLOR,com.mojang.blaze3d.platform.GlStateManager.DestFactor.SRC_COLOR);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.setShaderColor(1,1,1,1);
        try {
            BufferBuilder buffer = Tesselator.getInstance().getBuilder();
            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            buffer.vertex(matrix,0,0,0).color(1f,1f,1f,1f).endVertex();
            buffer.vertex(matrix,0,blend!=0?h:SkyEventRules.leftEdge(h, Math.random()),0).color(1f,1f,1f,1f).endVertex();
            buffer.vertex(matrix,w,blend!=0?h:SkyEventRules.rightEdge(h, Math.random()),0).color(1f,1f,1f,1f).endVertex();
            buffer.vertex(matrix,w,0,0).color(1f,1f,1f,1f).endVertex();
            BufferUploader.drawWithShader(buffer.end());
        } finally {
            RenderSystem.defaultBlendFunc(); RenderSystem.disableBlend(); RenderSystem.enableDepthTest(); RenderSystem.setShaderColor(1,1,1,1);
        }
    }
}
