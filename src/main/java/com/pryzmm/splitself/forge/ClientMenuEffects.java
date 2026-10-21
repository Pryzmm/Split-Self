package com.pryzmm.splitself.forge;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import java.io.IOException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.joml.Matrix4f;

/** ClientTickScheduler + ShaderRenderer.MELT, using Forge's post-render hook. */
@Mod.EventBusSubscriber(modid = SplitSelfForge.MOD_ID, value = Dist.CLIENT)
public final class ClientMenuEffects {
    private static final TransitionMenuTimeline TIMELINE = new TransitionMenuTimeline();
    private static SoundInstance scratch;
    private static RenderTarget swap, source;
    private static PostPass meltPass, blitPass;
    private static boolean melting, failed;
    // Quirk kept: original ShaderRenderer retains Amount between activations.
    private static float meltAmount;
    private ClientMenuEffects() {}

    static boolean startMelt() {
        if (!ClientTransitionEffects.active(ClientTransitionEffects.token())) return false;
        boolean started = TIMELINE.start();
        if (started) failed = false;
        return started;
    }

    @SubscribeEvent public static void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.player == null) { reset(); return; }
        switch (TIMELINE.tick()) {
            case BEGIN_MELT -> {
                melting = true;
                var sound = ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("splitself", "scratch"));
                if (sound != null) {
                    scratch = SimpleSoundInstance.forUI(sound, 1.0F);
                    client.getSoundManager().play(scratch);
                }
            }
            case FINISH -> {
                reset();
                ClientTransitionEffects.requestFinish(false);
            }
            default -> { }
        }
    }

    @SubscribeEvent public static void onRender(TickEvent.RenderTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !melting || failed) return;
        Minecraft client = Minecraft.getInstance();
        RenderTarget main = client.getMainRenderTarget();
        if (main.width <= 0 || main.height <= 0) return;
        try {
            if (swap == null || source != main || swap.width != main.width || swap.height != main.height)
                createPasses(client, main);
            meltAmount = Math.min(1.0F, meltAmount + 0.01F);
            meltPass.getEffect().safeGetUniform("Amount").set(meltAmount);
            // PostPass sets Time from its argument. Keep the original monotonic
            // seconds, rather than PostChain's wrapping 0..1 animation clock.
            float seconds = (float) (System.nanoTime() / 1.0e9);
            RenderSystem.disableBlend();
            RenderSystem.disableDepthTest();
            RenderSystem.resetTextureMatrix();
            meltPass.process(seconds);
            blitPass.process(seconds);
        } catch (IOException | RuntimeException error) {
            failed = true;
            releasePasses();
            org.slf4j.LoggerFactory.getLogger(ClientMenuEffects.class)
                    .error("Cannot render the transition melt effect", error);
        } finally {
            main.bindWrite(true);
            RenderSystem.enableDepthTest();
            RenderSystem.disableBlend();
        }
    }

    private static void createPasses(Minecraft client, RenderTarget main) throws IOException {
        releasePasses();
        source = main;
        swap = new TextureTarget(main.width, main.height, false, Minecraft.ON_OSX);
        // Same main -> melt_swap -> main graph as original shaders/post/melt.json.
        meltPass = new PostPass(client.getResourceManager(), "splitself:melt", main, swap);
        blitPass = new PostPass(client.getResourceManager(), "blit", swap, main);
        Matrix4f projection = new Matrix4f().setOrtho(0, main.width, 0, main.height, 0.1F, 1000.0F);
        meltPass.setOrthoMatrix(projection);
        blitPass.setOrthoMatrix(projection);
    }

    private static void releasePasses() {
        if (meltPass != null) { meltPass.close(); meltPass = null; }
        if (blitPass != null) { blitPass.close(); blitPass = null; }
        if (swap != null) { swap.destroyBuffers(); swap = null; }
        source = null;
    }

    static void reset() {
        TIMELINE.cancel();
        melting = false;
        if (scratch != null) { Minecraft.getInstance().getSoundManager().stop(scratch); scratch = null; }
        releasePasses();
    }

    @SubscribeEvent public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) { reset(); }

    @Mod.EventBusSubscriber(modid = SplitSelfForge.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class ReloadListener {
        @SubscribeEvent public static void register(RegisterClientReloadListenersEvent event) {
            event.registerReloadListener((ResourceManagerReloadListener) manager -> Minecraft.getInstance().execute(() -> {
                releasePasses();
                failed = false;
            }));
        }
    }
}
