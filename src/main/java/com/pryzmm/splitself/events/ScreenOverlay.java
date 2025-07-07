package com.pryzmm.splitself.events;

import com.pryzmm.splitself.screen.*;
import com.pryzmm.splitself.sound.ModSounds;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;

import java.io.File;

public class ScreenOverlay {
    public static void executeBlackScreen(PlayerEntity Player) {
        new Thread(() -> {
            Player.getWorld().playSound(null, Player.getBlockPos(), ModSounds.STATIC, SoundCategory.MASTER, 1.0f, 1.0f);
            ScreenOverlayRenderer.toggleOverlay();
            try {
                Thread.sleep(3877);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            ScreenOverlayRenderer.toggleOverlay();
        }).start();
    }

    public static void executeWhiteScreen(PlayerEntity Player) {
        new Thread(() -> {
            Player.getWorld().playSound(null, Player.getBlockPos(), ModSounds.SCREECH, SoundCategory.MASTER, 1.0f, 1.0f);
            TheOtherOverlayRenderer.toggleOverlay();
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            TheOtherOverlayRenderer.toggleOverlay();
        }).start();
    }

    public static void executeInventoryScreen(PlayerEntity Player) {
        new Thread(() -> {
            Player.getWorld().playSound(Player, Player.getBlockPos(), ModSounds.HORN, SoundCategory.MASTER, 1.0f, 1.0f);
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
        new Thread(() -> {
            FrozenOverlayRenderer.toggleOverlay(image);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            FrozenOverlayRenderer.toggleOverlay(image);
        }).start();
    }

    public static void executeFaceScreen(File image, PlayerEntity Player, Entity source) {
        new Thread(() -> {
            Player.getWorld().playSound(source, Player.getBlockPos(), ModSounds.AMSTATIC, SoundCategory.MASTER, 1.0f, 1.0f);
            FaceOverlayRenderer.toggleOverlay(image, 0.5f, 0.5f, 0.5f, 1.0f, 100, 133);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            FaceOverlayRenderer.toggleOverlay(image, 0f, 0f, 0f, 0f, 0, 0);
            Player.getWorld().playSound(source, Player.getBlockPos(), ModSounds.HUM, SoundCategory.MASTER, 1.0f, 1.0f);
            FaceOverlayRenderer.toggleOverlay(image, 1f, 0.5f, 0.5f, 1.0f, 200, 266);
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            FaceOverlayRenderer.toggleOverlay(image, 0f, 0f, 0f, 0f, 0, 0);
        }).start();
    }

    public static void executeEmergencyScreen(PlayerEntity player, String city) {
        new Thread(() -> {
            player.getWorld().playSound(null, player.getBlockPos(), ModSounds.AMBER, SoundCategory.MASTER, 1.0f, 1.0f);
            EmergencyOverlayRenderer.toggleOverlay(player, city);
            try {
                Thread.sleep(13000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            EmergencyOverlayRenderer.toggleOverlay(player, city);
        }).start();
    }
}
