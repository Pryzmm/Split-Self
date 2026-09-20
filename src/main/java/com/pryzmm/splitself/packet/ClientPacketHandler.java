package com.pryzmm.splitself.packet;

import com.igrium.videolib.api.VideoHandle;
import com.igrium.videolib.api.VideoHandleFactory;
import com.igrium.videolib.render.VideoScreen;
import com.pryzmm.splitself.SplitSelf;
import com.pryzmm.splitself.block.BrainBlock;
import com.pryzmm.splitself.client.SplitSelfClient;
import com.pryzmm.splitself.events.EventManager;
import com.pryzmm.splitself.events.EventRunner;
import com.pryzmm.splitself.events.ScreenOverlay;
import com.pryzmm.splitself.events.helper.NotepadManager;
import com.pryzmm.splitself.file.DesktopFileUtil;
import com.pryzmm.splitself.file.EntityScreenshotCapture;
import com.pryzmm.splitself.file.ZipFunc;
import com.pryzmm.splitself.packet.packets.*;
import com.pryzmm.splitself.screen.KickScreen;
import com.pryzmm.splitself.screen.MemoryScreen;
import com.pryzmm.splitself.screen.ServerClosedScreen;
import com.pryzmm.splitself.screen.WarningScreen;
import com.pryzmm.splitself.screen.overlay.ColorOverlay;
import com.pryzmm.splitself.sound.ModSounds;
import com.pryzmm.splitself.world.ClientTickScheduler;
import com.pryzmm.splitself.events.FinaleRenderer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import java.io.IOException;

public class  ClientPacketHandler {

    public static void register() {

        PayloadTypeRegistry.playS2C().register(BrokenEffectPacket.ID, BrokenEffectPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(EventPacket.ID, EventPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(GlitchEventPacket.ID, GlitchEventPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(ChatEventPacket.ID, ChatEventPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(WarningScreenPacket.ID, WarningScreenPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(MemoryScreenPacket.ID, MemoryScreenPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(TheOtherOverlayPacket.ID, TheOtherOverlayPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(KickScreenPacket.ID, KickScreenPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(PartyTimePacket.ID, PartyTimePacket.CODEC);
        PayloadTypeRegistry.playS2C().register(TransitionPacket.ID, TransitionPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(ScreenshotPacket.ID, ScreenshotPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(FinalePacket.ID, FinalePacket.CODEC);
        PayloadTypeRegistry.playS2C().register(ClosePacket.ID, ClosePacket.CODEC);

        ClientPlayNetworking.registerGlobalReceiver(BrokenEffectPacket.ID, (packet, context) -> context.client().execute(() -> {
            if (!BrainBlock.brokenEffectActive) {
                BrainBlock.brokenEffectActive = true;
                BrainBlock.playBrokenEffect(context.player(), context.client());
            }
        }));

        ClientPlayNetworking.registerGlobalReceiver(EventPacket.ID, (packet, context) -> context.client().execute(() -> {
            EventRunner.runClientEvent(context.client(), context.player(), EventManager.Events.valueOf(packet.event()));
        }));

        ClientPlayNetworking.registerGlobalReceiver(TransitionPacket.ID, (packet, context) -> context.client().execute(() -> {
            try {
                VideoHandleFactory factory = SplitSelfClient.videoManager.getVideoHandleFactory();
                VideoHandle idHandle = factory.getVideoHandle(ZipFunc.getVideo("transition").toURI().toURL());
                VideoScreen screen = new VideoScreen(SplitSelfClient.videoPlayer);
                context.client().getSoundManager().stopAll();
                context.client().setScreen(screen);
                SplitSelfClient.videoPlayer.getEvents().onFinished(v -> {
                    context.client().setScreen(new ServerClosedScreen());
                    ClientTickScheduler.schedule(1, () -> context.client().setScreen(new ServerClosedScreen()));
                });
                SplitSelfClient.videoPlayer.getMediaInterface().play(idHandle);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }));

        ClientPlayNetworking.registerGlobalReceiver(PartyTimePacket.ID, (packet, context) -> context.client().execute(() -> {
            // Removed
        }));

        ClientPlayNetworking.registerGlobalReceiver(TheOtherOverlayPacket.ID, (packet, context) -> context.client().execute(() -> {
            ScreenOverlay.executeTheOtherScreen(context.player());
        }));

        ClientPlayNetworking.registerGlobalReceiver(MemoryScreenPacket.ID, (packet, context) -> context.client().execute(() -> {
            MinecraftClient.getInstance().setScreen(new MemoryScreen(packet.memoryBitMask()));
        }));

        ClientPlayNetworking.registerGlobalReceiver(KickScreenPacket.ID, (packet, context) -> context.client().execute(() -> {
            MinecraftClient.getInstance().setScreen(new KickScreen());
        }));

        ClientPlayNetworking.registerGlobalReceiver(GlitchEventPacket.ID, (packet, context) -> context.client().execute(() -> {
            ScreenOverlay.toggleGlitchScreen(context.client());
        }));

        ClientPlayNetworking.registerGlobalReceiver(WarningScreenPacket.ID, (packet, context) -> context.client().execute(() -> {
            context.client().setScreen(new WarningScreen());
        }));

        ClientPlayNetworking.registerGlobalReceiver(ChatEventPacket.ID, (packet, context) -> context.client().execute(() -> {
            EventManager.receiveChatEventPacket(context.player(), packet.message(), packet.talkingToTheForgotten());
        }));

        ClientPlayNetworking.registerGlobalReceiver(ClosePacket.ID, (packet, context) -> context.client().execute(() -> {
            ColorOverlay.startBlackout();
            ColorOverlay.setColor(0xFF000000);
            ColorOverlay.toggleOverlay(true);
            ClientTickScheduler.schedule(100, () -> context.client().stop(), true);
        }));

        ClientPlayNetworking.registerGlobalReceiver(ScreenshotPacket.ID, (packet, context) -> context.client().execute(() -> {
            new Thread(() -> {
                try { Thread.sleep(3000); }
                catch (Exception e) { SplitSelf.LOGGER.error(e.getMessage(), e); }
                EntityScreenshotCapture capture = new EntityScreenshotCapture();
                capture.capture((file) -> {
                    if (file != null) {
                        try {
                            Text[] screenshotMessages = {
                                Text.translatable("events.splitself.theOtherScreenshot.line1"),
                                Text.translatable("events.splitself.theOtherScreenshot.line2")
                            };
                            NotepadManager.execute(screenshotMessages);
                            Thread.sleep(8000);
                            Util.getOperatingSystem().open(file);
                        } catch (Exception e) { SplitSelf.LOGGER.error(e.getMessage(), e); }
                    }
                });
            }).start();
        }));

        ClientPlayNetworking.registerGlobalReceiver(FinalePacket.ID, (packet, context) -> context.client().execute(() -> {
            if (packet.isIntro()) {
                ClientTickScheduler.schedule(100, () -> context.player().sendMessage(Text.translatable("chat.splitself.final.intro1", context.player().getName().getString())));
                ClientTickScheduler.schedule(190, () -> context.player().sendMessage(Text.translatable("chat.splitself.final.intro2", context.player().getName().getString())));
                ClientTickScheduler.schedule(280, () -> context.player().sendMessage(Text.translatable("chat.splitself.final.intro3", context.player().getName().getString())));
                ClientTickScheduler.schedule(370, () -> context.player().sendMessage(Text.translatable("chat.splitself.final.intro4", context.player().getName().getString())));
                ClientTickScheduler.schedule(460, () -> context.player().sendMessage(Text.translatable("chat.splitself.final.intro5", context.player().getName().getString())));
            } else if (packet.deleteWorld()) {
                DesktopFileUtil.createFileOnDesktop(Text.translatable("files.splitself.bad_end.title").getString() + ".txt", Text.translatable("files.splitself.bad_end.message").getString());
                context.player().playSound(ModSounds.DELETE, 1f, 1f);
                ClientTickScheduler.schedule(30, () -> context.player().sendMessage(Text.translatable("chat.splitself.final.delete1", context.player().getName().getString())));
                ClientTickScheduler.schedule(120, () -> context.player().sendMessage(Text.translatable("chat.splitself.final.delete2", context.player().getName().getString())));
                ClientTickScheduler.schedule(270, () -> context.player().playSound(ModSounds.DELETE_HUM, 1f, 1f));
                ClientTickScheduler.schedule(1800, () -> context.client().stop());
                FinaleRenderer.startClientEffect();
            } else {
                DesktopFileUtil.createFileOnDesktop(Text.translatable("files.splitself.good_end.title").getString() + ".txt", Text.translatable("files.splitself.good_end.message").getString());
                ClientTickScheduler.schedule(30, () -> context.player().sendMessage(Text.translatable("chat.splitself.final.cancel1", context.player().getName().getString())));
                ClientTickScheduler.schedule(110, () -> context.player().sendMessage(Text.translatable("chat.splitself.final.cancel2", context.player().getName().getString())));
            }
        }));

    }

}