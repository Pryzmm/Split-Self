package com.pryzmm.splitself.packet;

import com.pryzmm.splitself.SplitSelf;
import com.pryzmm.splitself.block.BrainBlock;
import com.pryzmm.splitself.events.EventManager;
import com.pryzmm.splitself.events.EventRunner;
import com.pryzmm.splitself.events.ScreenOverlay;
import com.pryzmm.splitself.file.EntityScreenshotCapture;
import com.pryzmm.splitself.file.FrameFileManager;
import com.pryzmm.splitself.packet.packets.*;
import com.pryzmm.splitself.screen.KickScreen;
import com.pryzmm.splitself.screen.MemoryScreen;
import com.pryzmm.splitself.screen.WarningScreen;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.MinecraftClient;
import java.io.File;
import java.nio.file.Path;
import java.util.Random;

public class ClientPacketHandler {

    public static void register() {
        PayloadTypeRegistry.playS2C().register(BrokenEffectPacket.ID, BrokenEffectPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(EventPacket.ID, EventPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(UpdateFrameItemPacket.ID, UpdateFrameItemPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(GlitchEventPacket.ID, GlitchEventPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(ChatEventPacket.ID, ChatEventPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(WarningScreenPacket.ID, WarningScreenPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(MemoryScreenPacket.ID, MemoryScreenPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(TheOtherOverlayPacket.ID, TheOtherOverlayPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(KickScreenPacket.ID, KickScreenPacket.CODEC);

        ClientPlayNetworking.registerGlobalReceiver(BrokenEffectPacket.ID, (packet, context) -> context.client().execute(() -> {
            if (!BrainBlock.brokenEffectActive) {
                BrainBlock.brokenEffectActive = true;
                BrainBlock.playBrokenEffect(context.player(), context.client());
            }
        }));

        ClientPlayNetworking.registerGlobalReceiver(EventPacket.ID, (packet, context) -> context.client().execute(() -> {
            EventRunner.runClientEvent(context.client(), context.player(), EventManager.Events.valueOf(packet.event()));
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
            ScreenOverlay.executeGlitchScreen(context.client());
        }));

        ClientPlayNetworking.registerGlobalReceiver(WarningScreenPacket.ID, (packet, context) -> context.client().execute(() -> {
            context.client().setScreen(new WarningScreen());
        }));

        ClientPlayNetworking.registerGlobalReceiver(ChatEventPacket.ID, (packet, context) -> context.client().execute(() -> {
            EventManager.receiveChatEventPacket(context.player(), packet.message(), packet.talkingToTheForgotten());
        }));

        ClientPlayNetworking.registerGlobalReceiver(UpdateFrameItemPacket.ID, (packet, context) -> context.client().execute(() -> {
            boolean takeScreenshot = false;
            Path defaultScreenshotsFolder = Path.of(System.getenv("APPDATA") + "\\.minecraft\\screenshots");
            try {
                Random random = new Random();
                File[] screenshotFiles = defaultScreenshotsFolder.toFile().listFiles((dir, name) ->
                        name.toLowerCase().endsWith(".png") || name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".jpeg"));
                if (screenshotFiles != null && screenshotFiles.length > 0) {
                    File randomScreenshot = screenshotFiles[random.nextInt(screenshotFiles.length)];
                    FrameFileManager.loadImageToFrame(randomScreenshot);
                } else {
                    throw new RuntimeException("Screenshot folder is null or empty! " + defaultScreenshotsFolder.toAbsolutePath());
                }
            } catch (Exception e) {
                SplitSelf.LOGGER.warn("Failed to access screenshot folder of default Minecraft directory: {}", e.getMessage());
                File screenshotsDir = new File(MinecraftClient.getInstance().runDirectory, "screenshots");
                if (screenshotsDir.exists() && screenshotsDir.isDirectory()) {
                    File[] screenshotFiles = screenshotsDir.listFiles((dir, name) ->
                            name.toLowerCase().endsWith(".png") || name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".jpeg"));
                    if (screenshotFiles != null && screenshotFiles.length > 0) {
                        Random random = new Random();
                        File randomScreenshot = screenshotFiles[random.nextInt(screenshotFiles.length)];
                        try {
                            FrameFileManager.loadImageToFrame(randomScreenshot);
                            SplitSelf.LOGGER.info("Loaded random screenshot to frame: {}", randomScreenshot.getName());
                        } catch (Exception e2) {
                            SplitSelf.LOGGER.error("Failed to load random screenshot to frame: {} {}", e2.getMessage(), e2);
                        }
                    } else {
                        takeScreenshot = true;
                    }
                } else {
                    takeScreenshot = true;
                } if (takeScreenshot) {
                    SplitSelf.LOGGER.warn("Screenshots directory does not exist, taking a new screenshot instead");
                    new Thread(() -> context.client().execute(() -> {
                        EntityScreenshotCapture capture = new EntityScreenshotCapture();
                        capture.capture((file) -> {
                            if (file != null) {
                                try {
                                    FrameFileManager.loadImageToFrame(file);
                                } catch (Exception e2) {
                                    SplitSelf.LOGGER.error("Failed to load image to frame: {} {}", e2.getMessage(), e2);
                                }
                            } else {
                                SplitSelf.LOGGER.error("Could not get file!");
                            }
                        });
                    })).start();
                }
            }
        }));
    }

}