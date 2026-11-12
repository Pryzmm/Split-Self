package com.pryzmm.splitself.forge;

import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import java.nio.file.Files;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

@Mod.EventBusSubscriber(modid=SplitSelfForge.MOD_ID,value=Dist.CLIENT)
public final class ClientBatchEvents {
    private static final BatchEventRules.PauseGate PAUSE = new BatchEventRules.PauseGate();
    private static boolean pending, capturing;
    private static CameraType oldCamera;
    private static ResourceLocation frozen;
    private static long frozenUntil;
    private static FreezeScreen freeze;
    private ClientBatchEvents() {}
    static boolean captureBusy() { return capturing; }
    public static boolean cancelPause() { return PAUSE.cancel(ClientActivity.active() || freeze!=null || frozen!=null || RemainingClientEvents.windowActive() || RemainingClientEvents.emergencyActive()); }
    @SubscribeEvent public static void opening(ScreenEvent.Opening event) {
        if (event.getNewScreen() instanceof net.minecraft.client.gui.screens.PauseScreen && cancelPause()) {
            event.setCanceled(true);
        }
    }
    static void start(String event) {
        var client=Minecraft.getInstance();
        if(client.player==null)return;
        switch(event) {
            case "ESCAPE" -> PAUSE.enable();
            case "FROZENSCREEN" -> { if(!capturing)pending=true; }
            case "FREEZE" -> {
                if(freeze!=null)return;
                // 1.20.1 has no ominous bottle sound. BOTTLE_EMPTY is an explicit adaptation.
                client.player.playSound(SoundEvents.BOTTLE_EMPTY,1,1);
                var image=Screenshot.takeScreenshot(client.getMainRenderTarget());
                var texture=client.getTextureManager().register("splitself_freeze",new DynamicTexture(image));
                freeze=new FreezeScreen(client.screen,texture,now()+BatchEventRules.FREEZE_MS);
                client.setScreen(freeze);
            }
        }
    }
    private static long now() { return System.nanoTime()/1_000_000; }
    private static void update() {
        var client=Minecraft.getInstance();
        if(freeze!=null && now()>=freeze.until) {
            FreezeScreen old=freeze; freeze=null;
            if(client.screen==old)client.setScreen(old.previous);
            client.getTextureManager().release(old.texture);
        }
        if(frozen!=null && now()>=frozenUntil)releaseFrozen();
    }
    @SubscribeEvent public static void renderTick(TickEvent.RenderTickEvent event) {
        var client=Minecraft.getInstance(); update();
        if(event.phase==TickEvent.Phase.START && pending && !ClientFaceEffects.captureBusy() && !RemainingClientEvents.captureBusy() && client.player!=null && client.level!=null) {
            pending=false; capturing=true; oldCamera=client.options.getCameraType();
            client.options.setCameraType(CameraType.THIRD_PERSON_BACK);
        }
        if(event.phase==TickEvent.Phase.END && capturing)restoreCamera();
    }
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event) { if(event.phase==TickEvent.Phase.END)update(); }
    @SubscribeEvent public static void capture(RenderLevelStageEvent event) {
        if(event.getStage()!=RenderLevelStageEvent.Stage.AFTER_LEVEL || !capturing)return;
        var client=Minecraft.getInstance(); TextureTarget small=null;
        try {
            var main=client.getMainRenderTarget();
            small=new TextureTarget(client.getWindow().getGuiScaledWidth(),client.getWindow().getGuiScaledHeight(),false,Minecraft.ON_OSX);
            GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER,main.frameBufferId);
            GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER,small.frameBufferId);
            GL30.glBlitFramebuffer(0,0,main.width,main.height,0,0,small.width,small.height,GL11.GL_COLOR_BUFFER_BIT,GL11.GL_LINEAR);
            var image=Screenshot.takeScreenshot(small);
            try {
                var directory=client.gameDirectory.toPath().resolve("screenshots"); Files.createDirectories(directory);
                image.writeToFile(directory.resolve("hello_"+client.player.getUUID().toString().substring(0,8)+"_"+System.currentTimeMillis()+".png"));
            } catch(Exception failure){org.slf4j.LoggerFactory.getLogger("splitself").warn("Frozen screenshot could not be saved",failure);}
            releaseFrozen(); frozen=client.getTextureManager().register("splitself_frozen",new DynamicTexture(image));
            ClientActivity.set(true);frozenUntil=now()+BatchEventRules.FROZEN_MS;
            client.player.playSound(SoundEvent.createVariableRangeEvent(new ResourceLocation("splitself","staticscream")),1,1);
        } catch(Exception failure){org.slf4j.LoggerFactory.getLogger("splitself").warn("Frozen capture failed",failure);}
        finally {if(small!=null)small.destroyBuffers();client.getMainRenderTarget().bindWrite(true);restoreCamera();}
    }
    private static void restoreCamera() {
        if(oldCamera!=null)Minecraft.getInstance().options.setCameraType(oldCamera);
        oldCamera=null;capturing=false;
    }
    @SubscribeEvent public static void gui(RenderGuiEvent.Post event) {
        update(); if(frozen==null)return;
        var graphics=event.getGuiGraphics(); int w=graphics.guiWidth(),h=graphics.guiHeight();
        graphics.pose().pushPose();graphics.pose().translate(0,0,1000);
        RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();RenderSystem.disableDepthTest();
        try {
            graphics.fill(0,0,w,h,0x80000000);
            graphics.setColor(1,BatchEventRules.frozenTint(Math.random()),BatchEventRules.frozenTint(Math.random()),1);
            graphics.blit(frozen,0,0,0,0,w,h,w,h);graphics.flush();
        } finally {graphics.setColor(1,1,1,1);RenderSystem.enableDepthTest();RenderSystem.disableBlend();graphics.pose().popPose();}
    }
    private static void releaseFrozen() {
        if(frozen!=null){Minecraft.getInstance().getTextureManager().release(frozen);ClientActivity.set(false);}frozen=null;
    }
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut event) {
        pending=false;restoreCamera();releaseFrozen();
        if(freeze!=null){Minecraft.getInstance().getTextureManager().release(freeze.texture);freeze=null;}
        // PauseMixin's flag/attempt counter survive logout in the source too.
    }
    private static final class FreezeScreen extends Screen {
        final Screen previous;
        final ResourceLocation texture;
        final long until;
        FreezeScreen(Screen previous,ResourceLocation texture,long until){super(Component.empty());this.previous=previous;this.texture=texture;this.until=until;}
        @Override public boolean shouldCloseOnEsc(){return false;}
        @Override public boolean isPauseScreen(){return previous!=null && previous.isPauseScreen();}
        @Override public void render(GuiGraphics graphics,int x,int y,float partial){
            graphics.blit(texture,0,0,0,0,width,height,width,height);
        }
    }
}
