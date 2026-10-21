package com.pryzmm.splitself.forge;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

/** Source millisecond timing on the render thread; captures stay in GPU/RAM. */
@Mod.EventBusSubscriber(modid = SplitSelfForge.MOD_ID, value = Dist.CLIENT)
public final class ClientStoryEffects {
    private static final long[] STARTS = {3912, 5447, 8536, 10440, 14381};
    private static final long[] LENGTHS = {168, 225, 280, 493, 500};
    private static long started;
    private static RenderTarget tunnel;
    private static ResourceLocation frozen;
    private static BrokenScreen screen;
    private static boolean loading,pulse;private static long loadingDeadline;
    private ClientStoryEffects() {}

    static void startBroken() {
        reset();
        started = System.nanoTime();loading=true;loadingDeadline=started+60_000_000_000L;
    }

    @SubscribeEvent public static void onTick(TickEvent.ClientTickEvent event) {
        if(event.phase!=TickEvent.Phase.END)return;
        if(loading&&System.nanoTime()>=loadingDeadline){reset();return;}
        if(started==0)return;
        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.player == null) { reset(); return; }
        long elapsed = elapsed();
        boolean activePulse=false;for(int i=0;i<STARTS.length;i++)if(elapsed>=STARTS[i]&&elapsed<STARTS[i]+LENGTHS[i])activePulse=true;
        if(activePulse!=pulse){pulse=activePulse;ClientActivity.set(pulse);}
        if (elapsed >= 19681) { ClientSkyEffects.clearSky(); started=0;if(screen!=null&&client.screen==screen)client.setScreen(null);screen=null;releaseTunnel();return; }
        if (elapsed >= 14681 && screen == null) {
            var image = Screenshot.takeScreenshot(client.getMainRenderTarget());
            frozen = client.getTextureManager().register("splitself_broken", new DynamicTexture(image));
            screen = new BrokenScreen();
            client.setScreen(screen);
        }
    }

    @SubscribeEvent public static void onGui(RenderGuiEvent.Post event) {
        if (started == 0 || screen != null) return;
        long elapsed = elapsed();
        boolean visible = false;
        for (int i = 0; i < STARTS.length; i++) {
            if (elapsed >= STARTS[i] && elapsed < STARTS[i] + LENGTHS[i]) visible = true;
        }
        if (visible) renderTunnel(event.getGuiGraphics());
        else releaseTunnel();
    }

    @SubscribeEvent public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) { reset(); }

    private static long elapsed() { return (System.nanoTime() - started) / 1_000_000L; }

    static void renderTunnel(GuiGraphics graphics) {
        Minecraft client = Minecraft.getInstance();
        RenderTarget main = client.getMainRenderTarget();
        boolean created = tunnel == null || tunnel.width != main.width || tunnel.height != main.height;
        if (created) {
            releaseTunnel();
            tunnel = new TextureTarget(main.width, main.height, false, Minecraft.ON_OSX);
            copy(main, tunnel);
        }
        graphics.flush();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, tunnel.getColorTextureId());
        var pose = graphics.pose();
        pose.pushPose();
        pose.translate(0, 0, 500);
        var matrix = pose.last().pose();
        float right = graphics.guiWidth() - 6, bottom = graphics.guiHeight() - 6;
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buffer.vertex(matrix, 6, bottom, 0).uv(0, 0).endVertex();
        buffer.vertex(matrix, right, bottom, 0).uv(1, 0).endVertex();
        buffer.vertex(matrix, right, 6, 0).uv(1, 1).endVertex();
        buffer.vertex(matrix, 6, 6, 0).uv(0, 1).endVertex();
        BufferUploader.drawWithShader(buffer.end());
        pose.popPose();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        copy(main, tunnel);
    }

    private static void copy(RenderTarget source, RenderTarget destination) {
        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, source.frameBufferId);
        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, destination.frameBufferId);
        GL30.glBlitFramebuffer(0, 0, source.width, source.height, 0, 0,
                destination.width, destination.height, GL11.GL_COLOR_BUFFER_BIT, GL11.GL_LINEAR);
        source.bindWrite(false);
    }

    static void releaseTunnel() {
        if (tunnel != null) { tunnel.destroyBuffers(); tunnel = null; }
    }

    @SubscribeEvent public static void loading(net.minecraftforge.client.event.ScreenEvent.Render.Pre event){
        if(!loading||!(event.getScreen() instanceof net.minecraft.client.gui.screens.ReceivingLevelScreen))return;
        var g=event.getGuiGraphics();com.mojang.blaze3d.systems.RenderSystem.enableBlend();com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();
        if(frozen!=null)g.blit(frozen,0,0,0,0,event.getScreen().width,event.getScreen().height,event.getScreen().width,event.getScreen().height);
        g.blit(new ResourceLocation("splitself","textures/screen/broken.png"),0,0,0,0,event.getScreen().width,event.getScreen().height,event.getScreen().width,event.getScreen().height);
        com.mojang.blaze3d.systems.RenderSystem.disableBlend();event.setCanceled(true);
    }
    @SubscribeEvent public static void loadingClosed(net.minecraftforge.client.event.ScreenEvent.Opening event){if(loading&&Minecraft.getInstance().screen instanceof net.minecraft.client.gui.screens.ReceivingLevelScreen&&!(event.getNewScreen() instanceof net.minecraft.client.gui.screens.ReceivingLevelScreen))reset();}
    private static void reset() {
        Minecraft client = Minecraft.getInstance();
        started = 0;loading=false;if(pulse)ClientActivity.set(false);pulse=false;
        if (screen != null && client.screen == screen) client.setScreen(null);
        screen = null;
        releaseTunnel();
        if (frozen != null) { client.getTextureManager().release(frozen); frozen = null; }
    }

    private static final class BrokenScreen extends Screen {
        private static final ResourceLocation BROKEN = new ResourceLocation("splitself", "textures/screen/broken.png");
        BrokenScreen() { super(Component.empty()); }
        @Override public boolean isPauseScreen() { return false; }
        @Override public boolean shouldCloseOnEsc() { return false; }
        @Override public void render(GuiGraphics graphics, int x, int y, float delta) {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            if (frozen != null) graphics.blit(frozen, 0, 0, 0, 0, width, height, width, height);
            graphics.blit(BROKEN, 0, 0, 0, 0, width, height, width, height);
            RenderSystem.disableBlend();
        }
    }
}
