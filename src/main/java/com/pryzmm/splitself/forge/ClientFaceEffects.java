package com.pryzmm.splitself.forge;

import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import java.nio.file.Files;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

@Mod.EventBusSubscriber(modid = SplitSelfForge.MOD_ID, value = Dist.CLIENT)
public final class ClientFaceEffects {
    private static final ResourceLocation FACE = new ResourceLocation("splitself", "textures/misc/face.png");
    private static final FaceRules.Sky SKY = new FaceRules.Sky();
    private static int stage;
    private static boolean capturePending, capturing, soundedFlash;
    private static CameraType oldCamera;
    private static ResourceLocation screenshot;
    private static long started, skyStarted;
    private ClientFaceEffects() {}
    static void stage(int value) { stage = value; }
    static void toggle() { SKY.toggle(); skyStarted = System.nanoTime(); }
    static boolean captureBusy() { return capturing; }

    @SubscribeEvent public static void world(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS || !SKY.visible) return;
        var client = Minecraft.getInstance();
        if (client.player == null || client.level == null || capturing) return;
        // Apply world camera rotation; the sky direction must not be glued to the viewport.
        PoseStack pose = new PoseStack(); pose.mulPoseMatrix(event.getPoseStack().last().pose());
        pose.translate(10, 50, -100); pose.scale(SKY.width, SKY.height, 1);
        RenderSystem.enableBlend(); RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, FACE); RenderSystem.setShaderColor(1, 1, 1, 1);
        try {
            var matrix = pose.last().pose();
            BufferBuilder buffer = Tesselator.getInstance().getBuilder();
            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            buffer.vertex(matrix, -1,-1,0).uv(0,1).endVertex();
            buffer.vertex(matrix, 1,-1,0).uv(1,1).endVertex();
            buffer.vertex(matrix, 1,1,0).uv(1,0).endVertex();
            buffer.vertex(matrix, -1,1,0).uv(0,0).endVertex();
            BufferUploader.drawWithShader(buffer.end());
        } finally { RenderSystem.disableBlend(); }
        var look = net.minecraft.world.phys.Vec3.directionFromRotation(event.getCamera().getXRot(),event.getCamera().getYRot());
        if (SKY.frame(FaceRules.looking(look.x, look.y, look.z), stage)) capturePending = true;
    }

    @SubscribeEvent public static void renderTick(TickEvent.RenderTickEvent event) {
        var client = Minecraft.getInstance();
        if (event.phase == TickEvent.Phase.START && capturePending && !capturing && !ClientBatchEvents.captureBusy() && !RemainingClientEvents.captureBusy() && client.player != null && client.level != null) {
            capturePending = false; capturing = true;
            oldCamera = client.options.getCameraType();
            // Adapt reentrant Fabric renderWorld: render one real frame through the normal Forge pipeline.
            client.options.setCameraType(CameraType.THIRD_PERSON_BACK);
        }
        if (event.phase == TickEvent.Phase.END && capturing) restoreCamera();
        update();
    }

    @SubscribeEvent public static void capture(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL || !capturing) return;
        var client = Minecraft.getInstance();
        TextureTarget small = null;
        try {
            var main = client.getMainRenderTarget();
            small = new TextureTarget(client.getWindow().getGuiScaledWidth(), client.getWindow().getGuiScaledHeight(), false, Minecraft.ON_OSX);
            GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, main.frameBufferId);
            GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, small.frameBufferId);
            GL30.glBlitFramebuffer(0,0,main.width,main.height,0,0,small.width,small.height,GL11.GL_COLOR_BUFFER_BIT,GL11.GL_LINEAR);
            var image = Screenshot.takeScreenshot(small);
            try {
                var directory = client.gameDirectory.toPath().resolve("screenshots");
                Files.createDirectories(directory);
                image.writeToFile(directory.resolve("hello_" + client.player.getUUID().toString().substring(0,8) + "_" + System.currentTimeMillis() + ".png"));
            } catch (Exception failure) {
                // Keep the visual event usable even if the source's screenshot directory is unwritable.
                org.slf4j.LoggerFactory.getLogger("splitself").warn("FACE screenshot could not be saved", failure);
            }
            release();
            screenshot = client.getTextureManager().register("splitself_face", new DynamicTexture(image));
            ClientActivity.set(true);started = System.nanoTime(); soundedFlash = false; sound("amstatic");
        } catch (Exception failure) {
            started = 0; ClientActivity.set(false); release();
            org.slf4j.LoggerFactory.getLogger("splitself").warn("FACE capture failed", failure);
        } finally {
            if (small != null) small.destroyBuffers();
            client.getMainRenderTarget().bindWrite(true);
            restoreCamera();
        }
    }

    private static void restoreCamera() {
        if (oldCamera != null) Minecraft.getInstance().options.setCameraType(oldCamera);
        oldCamera = null; capturing = false;
    }
    private static void sound(String id) {
        var client = Minecraft.getInstance();
        if (client.player != null && client.level != null) {
            var pos = client.player.blockPosition();
            client.level.playLocalSound(pos.getX(), pos.getY(), pos.getZ(),
                    SoundEvent.createVariableRangeEvent(new ResourceLocation("splitself", id)),
                    net.minecraft.sounds.SoundSource.MASTER, 1, 1, false);
        }
    }
    private static long elapsed() { return (System.nanoTime() - started) / 1_000_000; }
    private static void update() {
        if (SKY.visible && FaceRules.skyExpired((System.nanoTime()-skyStarted)/1_000_000)) SKY.visible=false;
        if (started == 0) return;
        long ms = elapsed();
        if (ms >= 18000 && !soundedFlash) {
            Minecraft.getInstance().getSoundManager().stop(); sound("hum"); soundedFlash = true;
        }
        if (FaceRules.finished(ms)) { started = 0; release(); }
    }
    @SubscribeEvent public static void gui(RenderGuiEvent.Post event) {
        update();
        if (SKY.visible && FaceRules.skyExpired((System.nanoTime()-skyStarted)/1_000_000)) SKY.visible=false;
        if (started == 0) return;
        var client = Minecraft.getInstance();
        var graphics = event.getGuiGraphics();
        long ms = elapsed();
        boolean flash = FaceRules.flash(ms);
        int w = graphics.guiWidth(), h = graphics.guiHeight(), fw = flash ? 200 : 100, fh = flash ? 320 : 133;
        String text = FaceRules.line(ms) == 0 ? "" : Component.translatable("events.splitself.face.line" + FaceRules.line(ms)).getString();
        graphics.pose().pushPose(); graphics.pose().translate(0,0,1000);
        RenderSystem.enableBlend(); RenderSystem.defaultBlendFunc(); RenderSystem.disableDepthTest();
        try {
            graphics.fill(0,0,w,h,0x80000000);
            graphics.setColor(flash ? 1 : .5f,.5f,.5f,1);
            if (screenshot != null) graphics.blit(screenshot,0,0,0,0,w,h,w,h);
            graphics.setColor(1,1,1,1);
            // Original draws the text twice, once for each renderOverlayContent invocation.
            graphics.drawString(client.font,text,(w-client.font.width(text))/2,h/10,0x33FFFFFF,true);
            graphics.blit(FACE,(w-fw)/2,(h-fh)/2,0,0,fw,fh,fw,fh);
            graphics.drawString(client.font,text,(w-client.font.width(text))/2,h/10,0x33FFFFFF,true);
            graphics.flush();
        } finally {
            graphics.setColor(1,1,1,1); RenderSystem.enableDepthTest(); RenderSystem.disableBlend(); graphics.pose().popPose();
        }
    }
    private static void release() {
        if (screenshot != null) Minecraft.getInstance().getTextureManager().release(screenshot);
        screenshot = null;
    }
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut event) {
        restoreCamera(); capturePending = false; started = 0; stage = 0; SKY.visible=false; release();
        Minecraft.getInstance().getSoundManager().stop(new ResourceLocation("splitself", "amstatic"),null);
        Minecraft.getInstance().getSoundManager().stop(new ResourceLocation("splitself", "hum"),null);
        // Sky toggle/size persist like the source static fields; GPU resources never do.
    }
}
