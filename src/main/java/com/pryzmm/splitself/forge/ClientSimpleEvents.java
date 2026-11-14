package com.pryzmm.splitself.forge;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Client-thread effects. No worker threads modify options, GUI state or OpenGL. */
@Mod.EventBusSubscriber(modid = SplitSelfForge.MOD_ID, value = Dist.CLIENT)
public final class ClientSimpleEvents {
    private static long blackStart, whiteStart, scaleStart, blackShakeAt, whiteShakeAt;
    private static int blackX, blackY, whiteX, whiteY;
    private static Double oldChatScale;
    private static SimpleEventRules.Scale scale;
    private static String lockedTitle;
    private ClientSimpleEvents() {}

    static void start(String name) {
        var client = Minecraft.getInstance();
        if (client.player == null) return;
        long now = System.nanoTime();
        switch (name) {
            case "FACE" -> ClientFaceEffects.toggle();
            case "REDSKY", "INVERTCOLOR" -> ClientSkyEffects.start(name);
            case "PAUSE" -> PauseShake.enable();
            case "POEMSCREEN" -> client.setScreen(new PoemScreen());
            case "SCREENOVERLAY" -> {
                if (blackStart != 0) return;
                ClientActivity.set(true); blackStart = now; sound("static");
            }
            case "WHITESCREENOVERLAY" -> {
                if (whiteStart != 0) return;
                whiteStart = now; sound("screech");
            }
            case "SCALE" -> {
                if (scale != null) return;
                oldChatScale = client.options.chatScale().get();
                ClientActivity.set(true); scaleStart = now; scale = new SimpleEventRules.Scale(); sound("buzz");
                update(now);
            }
            case "RENAME" -> {
                if(!ExternalConsent.computerAllowed())return;
                lockedTitle = Component.translatable("events.splitself.rename").getString();
                client.getWindow().setTitle(lockedTitle);
            }
            default -> { }
        }
    }

    private static void sound(String name) {
        Minecraft.getInstance().player.playSound(
                SoundEvent.createVariableRangeEvent(new ResourceLocation("splitself", name)), 1, 1);
    }

    private static long elapsed(long now, long start) { return (now - start) / 1_000_000; }

    private static void update(long now) {
        var client = Minecraft.getInstance();
        if (client.player == null) { reset(); return; }
        if (blackStart != 0 && elapsed(now, blackStart) >= SimpleEventRules.BLACK_MS) {blackStart = 0;ClientActivity.set(false);}
        if (whiteStart != 0 && elapsed(now, whiteStart) >= SimpleEventRules.WHITE_MS) whiteStart = 0;
        if (scale == null) return;
        long ms = elapsed(now, scaleStart);
        scale.advance(ms, step -> {
            if (step % 5 == 0) client.player.sendSystemMessage(Component.literal("<"
                    + client.player.getName().getString() + "> "
                    + Component.translatable("events.splitself.scale.message").getString()));
            client.options.chatScale().set(Math.random());
        });
        if (ms >= SimpleEventRules.SCALE_MS) {
            client.gui.getChat().clearMessages(true);
            client.options.chatScale().set(oldChatScale);
            oldChatScale = null; scale = null;ClientActivity.set(false);
        }
    }

    @SubscribeEvent public static void renderTick(TickEvent.RenderTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        // Adapt WindowMixin: reapply before every frame; lock persists through logout as in source.
        if (lockedTitle != null) Minecraft.getInstance().getWindow().setTitle(lockedTitle);
        if (blackStart != 0 || whiteStart != 0 || scale != null) update(System.nanoTime());
    }

    @SubscribeEvent public static void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && (blackStart != 0 || whiteStart != 0 || scale != null)) update(System.nanoTime());
    }

    @SubscribeEvent public static void render(RenderGuiEvent.Post event) {
        if (blackStart == 0 && whiteStart == 0) return;
        long now = System.nanoTime();
        update(now);
        GuiGraphics graphics = event.getGuiGraphics();
        var client = Minecraft.getInstance();
        int w = graphics.guiWidth(), h = graphics.guiHeight();
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 1000);
        RenderSystem.enableBlend(); RenderSystem.defaultBlendFunc(); RenderSystem.disableDepthTest();
        try {
            if (blackStart != 0) {
                graphics.fill(0, 0, w, h, 0x80000000);
                if (elapsed(now, blackShakeAt) >= 10) {
                    blackX = -(int) (Math.random() * 20); blackY = -(int) (Math.random() * 20); blackShakeAt = now;
                }
                graphics.setColor((float) Math.random(), (float) Math.random(), (float) Math.random(), 1);
                graphics.blit(texture("overlay"), blackX, blackY, 0, 0, w + 20, h + 20, w, h);
                graphics.setColor(1, 1, 1, 1);
                Component message = Component.translatable("events.splitself.screenOverlay.message");
                // Source never updates lastShakeUpdate for this text, so it shakes every rendered frame.
                graphics.drawString(client.font, message, (w - client.font.width(message)) / 2 + 100
                        - (int) (Math.random() * 200), h / 2 + 100 - (int) (Math.random() * 200), 0xFFFFFF);
            }
            if (whiteStart != 0) {
                graphics.fill(0, 0, w, h, 0x80000000);
                if (elapsed(now, whiteShakeAt) >= 10) {
                    whiteX = -(int) (Math.random() * 200); whiteY = -(int) (Math.random() * 200); whiteShakeAt = now;
                }
                graphics.setColor((float) (Math.random() / 5 + .8), (float) (Math.random() / 5 + .8),
                        (float) (Math.random() / 5 + .8), 1);
                graphics.blit(texture("overlay_white"), whiteX, whiteY, 0, 0, w + 200, h + 200, w + 200, h + 200);
                graphics.setColor(1, 1, 1, 1);
                graphics.blit(texture("player"), (w - 102) / 2, (h - 153) / 2, 0, 0, 102, 153, 102, 153);
            }
        } finally {
            graphics.setColor(1, 1, 1, 1);
            RenderSystem.enableDepthTest(); RenderSystem.disableBlend(); graphics.pose().popPose();
        }
    }

    private static ResourceLocation texture(String name) {
        return new ResourceLocation("splitself", "textures/screen/" + name + ".png");
    }

    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut event) { reset(); }

    private static void reset() {
        var client = Minecraft.getInstance();
        if (oldChatScale != null) client.options.chatScale().set(oldChatScale);
        for (String sound : new String[]{"static", "screech", "buzz"})
            client.getSoundManager().stop(new ResourceLocation("splitself", sound), null);
        oldChatScale = null; scale = null; blackStart = whiteStart = 0;
    }
}
