package com.pryzmm.splitself.dimension;

import com.pryzmm.splitself.world.DimensionRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import java.util.Objects;

public class LimboLevitation {
    public static void onTick(MinecraftServer server) {
        for (PlayerEntity player : Objects.requireNonNull(server.getWorld(DimensionRegistry.LIMBO_DIMENSION_KEY)).getPlayers()) {
            if (player.getY() < -2) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION, 40, 10, false, false, false));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 140, 3, false, false, false));
            }
        }
    }
}
