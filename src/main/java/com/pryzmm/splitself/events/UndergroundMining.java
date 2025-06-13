package com.pryzmm.splitself.events;

import net.minecraft.entity.Entity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

public class UndergroundMining {
    public static void Execute(Entity Player, World World) {
        new Thread(() -> {
            for (float i = 0.1f; i < 1.1f; i += 0.1f) {
                // Direct client-side sound playing
                World.playSound(
                        Player.getX(),
                        Player.getY(),
                        Player.getZ(),
                        SoundEvents.BLOCK_STONE_BREAK,
                        SoundCategory.BLOCKS,
                        i,
                        1f,
                        false
                );

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }).start();
    }
}