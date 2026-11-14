package com.pryzmm.splitself.forge;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;

final class SplitSelfWorldEvents {
    private static volatile long terrainSeed;
    static final ResourceKey<Level> EMPTY = ResourceKey.create(Registries.DIMENSION,
            new ResourceLocation(SplitSelfForge.MOD_ID, "empty_dimension"));
    static final ResourceKey<Level> GRASS_EMPTY = ResourceKey.create(Registries.DIMENSION,
            new ResourceLocation(SplitSelfForge.MOD_ID, "grass_empty_dimension"));
    static final ResourceKey<Level> LIMBO = ResourceKey.create(Registries.DIMENSION,
            new ResourceLocation(SplitSelfForge.MOD_ID, "limbo_dimension"));
    static final ResourceKey<Level> HALLWAY = ResourceKey.create(Registries.DIMENSION,
            new ResourceLocation(SplitSelfForge.MOD_ID, "hallway_dimension"));

    private SplitSelfWorldEvents() {}

    static long terrainSeed() { return terrainSeed; }

    static void onServerStarted(ServerStartedEvent event) {
        terrainSeed = event.getServer().overworld().getSeed();
        LimboStructures.placeForFirstLoad(event.getServer());
    }

    static void onServerStopping(ServerStoppingEvent event) {
        terrainSeed = 0;
        EventRuntime.reset();
        SleepSystem.reset();
        EncounterSpawns.reset();
        BrokenFlowerBlock.resetForServerStop();
    }

    /** First-join warning uses a safe screen with no file or PII actions. */
    static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            var data=MemoryData.get(player.serverLevel());
            if(data.isDeleted()||data.isFull()){player.connection.disconnect(Component.translatable(data.isDeleted()?"events.splitself.bad_end.screen":"events.splitself.good_end.screen"));return;}
            // Original SplitSelf join hook ejects players who reconnect in the hallway
            // or in the early Limbo regions (x <= 3500). The finale house stays reachable.
            boolean leaveLimbo = player.level().dimension().equals(LIMBO) && player.getX() <= 3500.0;
            if (leaveLimbo || player.level().dimension().equals(HALLWAY)) {
                var server = player.getServer();
                BlockPos spawn = player.getRespawnPosition();
                ServerLevel destination = spawn == null ? null : server.getLevel(player.getRespawnDimension());
                if (destination == null) {
                    destination = server.overworld();
                    spawn = destination.getSharedSpawnPos();
                    player.teleportTo(destination, spawn.getX(), spawn.getY(), spawn.getZ(), 0, 0);
                } else {
                    player.teleportTo(destination, spawn.getX(), spawn.getY() + 0.5625,
                            spawn.getZ(), 0, 0);
                }
            }
            if (MemoryData.get(player.serverLevel()).recordJoin(player.getUUID())) {
                MemoryNetwork.openWarning(player);
            }
        }
    }

    /** Original FallDamageMixin cancels every player damage call in these worlds. */
    static void onAttack(LivingAttackEvent event) {
        if (event.getEntity() instanceof ServerPlayer player
                && (player.level().dimension().equals(EMPTY)
                    || player.level().dimension().equals(GRASS_EMPTY))) {
            event.setCanceled(true);
        }
    }

    /** Original LimboLevitation.onTick, applied only below Y=-2. */
    static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        EventRuntime.onServerTick(event);
        ServerLevel limbo = event.getServer().getLevel(LIMBO);
        if (limbo == null) return;
        for (ServerPlayer player : limbo.players()) {
            if (player.getY() < -2) {
                player.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 40, 10, false, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 140, 3, false, false, false));
            }
        }
    }
}
