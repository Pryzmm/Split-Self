package com.pryzmm.splitself.events;

import com.pryzmm.splitself.SplitSelf;
import com.pryzmm.splitself.events.helper.ChunkDestroyer;
import com.pryzmm.splitself.screen.overlay.*;
import com.pryzmm.splitself.sound.ModSounds;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import java.io.File;

public class ScreenOverlay {
    public static void executeBlackScreen(PlayerEntity Player) {
        EventManager.ACTIVE_EVENT = true;
        new Thread(() -> {
            Player.getWorld().playSound(null, Player.getBlockPos(), ModSounds.STATIC, SoundCategory.MASTER, 1.0f, 1.0f);
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

    public static void executeWhiteScreen(PlayerEntity Player) {
        new Thread(() -> {
            Player.getWorld().playSound(null, Player.getBlockPos(), ModSounds.SCREECH, SoundCategory.MASTER, 1.0f, 1.0f);
            TheOtherWhiteOverlay.toggleOverlay();
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            TheOtherWhiteOverlay.toggleOverlay();
        }).start();
    }

    public static void executeTheOtherScreen(PlayerEntity Player) {
        new Thread(() -> {
            Player.getWorld().playSound(null, Player.getBlockPos(), ModSounds.SCREAM, SoundCategory.MASTER, 1.0f, 1.0f);
            TheOtherOverlay.toggleOverlay();
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            TheOtherOverlay.toggleOverlay();
        }).start();
    }

    public static void executeInventoryScreen(PlayerEntity Player) {
        new Thread(() -> {
            Player.getWorld().playSound(null, Player.getBlockPos(), ModSounds.HORN, SoundCategory.MASTER, 1.0f, 1.0f);
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
            player.getWorld().playSound(null, player.getBlockPos(), ModSounds.AMBER, SoundCategory.MASTER, 1.0f, 1.0f);
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
            client.getSoundManager().stopSounds(ModSounds.GLITCH.getId(), SoundCategory.MASTER);
            GlitchOverlay.toggleOverlay();
            EventManager.ACTIVE_EVENT = false;
        }).start();
    }

    public static void executeStaticScreen(ServerPlayerEntity player) {
        EventManager.ACTIVE_EVENT = true;
        new Thread(() -> {
            StaticOverlay.toggleOverlay();
            player.getWorld().playSound(null, player.getBlockPos(), ModSounds.STATIC2, SoundCategory.MASTER, 1.0f, 1.0f);
            try { Thread.sleep(30000); } catch (InterruptedException ignored) {}
            MinecraftClient.getInstance().getSoundManager().stopSounds(ModSounds.STATIC2.getId(), SoundCategory.MASTER);
            StaticOverlay.toggleOverlay();
            EventManager.ACTIVE_EVENT = false;
        }).start();
    }

    public static void executeRecursiveScreen(PlayerEntity player, int milliseconds, boolean sound) {
        EventManager.ACTIVE_EVENT = true;
        new Thread(() -> {
            RecursiveRenderer.toggleOverlay();
            if (sound) player.getWorld().playSound(null, player.getBlockPos(), ModSounds.GLITCH2, SoundCategory.MASTER, 1.0f, 1.0f);
            try { Thread.sleep(milliseconds); } catch (InterruptedException ignored) {}
            if (sound) MinecraftClient.getInstance().getSoundManager().stopSounds(ModSounds.GLITCH2.getId(), SoundCategory.MASTER);
            RecursiveRenderer.toggleOverlay();
            EventManager.ACTIVE_EVENT = false;
        }).start();
    }

}
