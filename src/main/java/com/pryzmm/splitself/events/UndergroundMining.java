package com.pryzmm.splitself.events;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class UndergroundMining {
    public static void Execute(PlayerEntity player, ServerWorld world) {
        playMiningSound(player, world, 0);
    }

    private static void playMiningSound(PlayerEntity player, ServerWorld world, int step) {
        if (step >= 10) return;

        float volume = (step + 1) * 0.1f;

        world.playSound(
                null,
                player.getBlockPos(),
                SoundEvents.BLOCK_STONE_BREAK,
                SoundCategory.BLOCKS,
                volume,
                1.0f
        );

        // Schedule next sound using CompletableFuture
        CompletableFuture.delayedExecutor(1, TimeUnit.SECONDS)
                .execute(() -> {
                    // Execute on server thread
                    world.getServer().execute(() -> {
                        // Check if player is still valid
                        if (!player.isRemoved()) {
                            playMiningSound(player, world, step + 1);
                        }
                    });
                });
    }
}