package com.pryzmm.splitself.events;

import com.pryzmm.splitself.screen.ScreenOverlayRenderer;
import com.pryzmm.splitself.screen.TheOtherOverlayRenderer;
import com.pryzmm.splitself.sound.ModSounds;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;

public class ScreenOverlay {
    public static void executeBlackScreen(PlayerEntity Player) {
        new Thread(() -> {
            Player.getWorld().playSound(null, Player .getBlockPos(), ModSounds.STATIC, SoundCategory.MASTER, 1.0f, 1.0f);
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
            Player.getWorld().playSound(null, Player .getBlockPos(), ModSounds.SCREECH, SoundCategory.MASTER, 1.0f, 1.0f);
            TheOtherOverlayRenderer.toggleOverlay();
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            TheOtherOverlayRenderer.toggleOverlay();
        }).start();
    }
}
