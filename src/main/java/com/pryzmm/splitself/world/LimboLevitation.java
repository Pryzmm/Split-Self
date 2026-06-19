package com.pryzmm.splitself.world;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;

public class LimboLevitation {
    public static void onTick(MinecraftServer server) {
        ServerWorld world = server.getWorld(DimensionRegistry.LIMBO_DIMENSION_KEY);
        if (world == null || world.getPlayers().isEmpty()) return;
        for (PlayerEntity player : world.getPlayers()) {
            if (player.getY() < -2) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION, 40, 10, false, false, false));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 140, 3, false, false, false));
            }
        }
    }
}
