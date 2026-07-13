package com.pryzmm.splitself.events;

import com.pryzmm.splitself.SplitSelf;
import com.pryzmm.splitself.events.helper.ChunkDestroyer;
import com.pryzmm.splitself.screen.overlay.*;
import com.pryzmm.splitself.sound.ModSounds;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import java.io.File;

public class ScreenOverlay {
    public static void executeBlackScreen(PlayerEntity player) {
        EventManager.ACTIVE_EVENT = true;
        new Thread(() -> {
            player.playSound(ModSounds.STATIC, 1.0f, 1.0f);
            ScreenOverlayRenderer.toggleOverlay();
            try {
                Thread.sleep(3877);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            ScreenOverlayRenderer.toggleOverlay();
            EventManager.ACTIVE_EVENT = false;
        }).start();
    }

    public static void executeWhiteScreen(PlayerEntity player) {
        new Thread(() -> {
            player.playSound(ModSounds.SCREECH, 1.0f, 1.0f);
            TheOtherWhiteOverlay.toggleOverlay();
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            TheOtherWhiteOverlay.toggleOverlay();
        }).start();
    }

    public static void executeTheOtherScreen(PlayerEntity player) {
        new Thread(() -> {
            player.playSound(ModSounds.SCREAM, 1.0f, 1.0f);
            TheOtherOverlay.toggleOverlay();
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            TheOtherOverlay.toggleOverlay();
        }).start();
    }

    public static void executeInventoryScreen(PlayerEntity player) {
        new Thread(() -> {
            player.playSound(ModSounds.HORN, 1.0f, 1.0f);
            InventoryOverlayRenderer.toggleOverlay();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            InventoryOverlayRenderer.toggleOverlay();
        }).start();
    }

    public static void executeFrozenScreen(File image) {
        EventManager.ACTIVE_EVENT = true;
        new Thread(() -> {
            FrozenOverlayRenderer.toggleOverlay(image);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            FrozenOverlayRenderer.toggleOverlay(image);
            EventManager.ACTIVE_EVENT = false;
        }).start();
    }

    public static void executeFaceScreen(File image, PlayerEntity player, Entity source) {
        new Thread(() -> {
            EventManager.ACTIVE_EVENT = true;
            FaceOverlayRenderer.setOverlayText("");
            player.getWorld().playSound(source, player.getBlockPos(), ModSounds.AMSTATIC, SoundCategory.MASTER, 1.0f, 1.0f);
            FaceOverlayRenderer.toggleOverlay(image, 0.5f, 0.5f, 0.5f, 1.0f, 100, 133);
            try { Thread.sleep(3000); } catch (InterruptedException e) { throw new RuntimeException(e); }
            FaceOverlayRenderer.setOverlayText(SplitSelf.translate("events.splitself.face.line1").getString());
            try { Thread.sleep(4500); } catch (InterruptedException e) { throw new RuntimeException(e); }
            FaceOverlayRenderer.setOverlayText(SplitSelf.translate("events.splitself.face.line2").getString());
            try { Thread.sleep(4500); } catch (InterruptedException e) { throw new RuntimeException(e); }
            FaceOverlayRenderer.setOverlayText(SplitSelf.translate("events.splitself.face.line3").getString());
            try { Thread.sleep(6000); } catch (InterruptedException e) { throw new RuntimeException(e); }
            FaceOverlayRenderer.toggleOverlay(image, 0f, 0f, 0f, 0f, 0, 0);
            MinecraftClient.getInstance().getSoundManager().stopAll();
            player.getWorld().playSound(source, player.getBlockPos(), ModSounds.HUM, SoundCategory.MASTER, 1.0f, 1.0f);
            FaceOverlayRenderer.toggleOverlay(image, 1f, 0.5f, 0.5f, 1.0f, 200, 320);
            try { Thread.sleep(150); } catch (InterruptedException e) { throw new RuntimeException(e); }
            FaceOverlayRenderer.toggleOverlay(image, 0f, 0f, 0f, 0f, 0, 0);
            EventManager.ACTIVE_EVENT = false;
        }).start();
    }

    public static void executeEmergencyScreen(PlayerEntity player, String city) {
        new Thread(() -> {
            EventManager.ACTIVE_EVENT = true;
            player.playSound(ModSounds.AMBER, 1.0f, 1.0f);
            EmergencyOverlayRenderer.toggleOverlay(city);
            try {
                Thread.sleep(13000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            EmergencyOverlayRenderer.toggleOverlay(city);
            EventManager.ACTIVE_EVENT = false;
        }).start();
    }

    public static void executeGlitchScreen(MinecraftClient client) {
        EventManager.ACTIVE_EVENT = true;
        new Thread(() -> {
            GlitchOverlay.toggleOverlay();
            try {
                while (ChunkDestroyer.liftChunkActive) {
                    Thread.sleep(50);
                }
                Thread.sleep(200);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            client.getSoundManager().stopSounds(ModSounds.GLITCH.getId(), null);
            GlitchOverlay.toggleOverlay();
            EventManager.ACTIVE_EVENT = false;
        }).start();
    }

    public static void executeStaticScreen(ClientPlayerEntity player) {
        EventManager.ACTIVE_EVENT = true;
        new Thread(() -> {
            StaticOverlay.toggleOverlay();
            player.playSound(ModSounds.STATIC2, 1.0f, 1.0f);
            try { Thread.sleep(30000); } catch (InterruptedException ignored) {}
            MinecraftClient.getInstance().getSoundManager().stopSounds(ModSounds.STATIC2.getId(), null);
            StaticOverlay.toggleOverlay();
            EventManager.ACTIVE_EVENT = false;
        }).start();
    }

    public static void executeRecursiveScreen(PlayerEntity player, int milliseconds, boolean sound) {
        EventManager.ACTIVE_EVENT = true;
        new Thread(() -> {
            RecursiveRenderer.toggleOverlay();
            if (sound) player.playSound(ModSounds.GLITCH2, 1.0f, 1.0f);
            try { Thread.sleep(milliseconds); } catch (InterruptedException ignored) {}
            if (sound) MinecraftClient.getInstance().getSoundManager().stopSounds(ModSounds.GLITCH2.getId(), null);
            RecursiveRenderer.toggleOverlay();
            EventManager.ACTIVE_EVENT = false;
        }).start();
    }

}
