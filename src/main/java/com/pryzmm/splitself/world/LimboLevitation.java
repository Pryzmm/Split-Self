package com.pryzmm.splitself.world;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;

public class LimboLevitation {
    @SuppressWarnings("DataFlowIssue")
    public static void onTick(MinecraftServer server) {
        for (PlayerEntity player : server.getWorld(DimensionRegistry.LIMBO_DIMENSION_KEY).getPlayers()) {
            if (player.getY() < -2) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION, 40, 10, false, false, false));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 140, 3, false, false, false));
            }
        }
    }
}
