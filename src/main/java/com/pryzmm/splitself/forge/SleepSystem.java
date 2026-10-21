package com.pryzmm.splitself.forge;

import java.util.List;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.level.SleepFinishedTimeEvent;

/** Server-thread implementation of the source mod's staged wake-up sequence. */
final class SleepSystem {
    private static boolean running;
    private static final java.util.Set<TheOtherEntity> sceneActors = new java.util.HashSet<>();

    private SleepSystem() {}

    static void onSleepFinished(SleepFinishedTimeEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)
                || level.dimension() != Level.OVERWORLD || running) return;
        List<UUID> sleepers = level.players().stream().filter(ServerPlayer::isSleeping)
                .map(ServerPlayer::getUUID).toList();
        if (sleepers.isEmpty()) return;

        MemoryData data = MemoryData.get(level);
        int current = data.sleepStage();
        int eventStage = selectStage(current, Math.random());
        if (eventStage < 0) return;
        if (current < 4) data.setSleepStage(current + 1);
        running = true;
        start(level.getServer(), sleepers, eventStage);
    }

    /** Returns -1 when the source's random gate does not start an event. */
    static int selectStage(int current, double random) {
        if (current == 0) return 0;
        if (current == 1 && random < 0.25) return 1;
        if (current == 2 && random < (1.0 / 6.0)) return 2;
        if (current == 3 && random < (1.0 / 6.0)) return 3;
        if (current == 4 && random < 0.5) return 4;
        return -1;
    }

    static boolean debugRun(ServerPlayer player, int stage) {
        if (running) {
            player.sendSystemMessage(Component.literal("A sleep sequence is already running"));
            return false;
        }
        if (player.getServer().getLevel(SplitSelfWorldEvents.LIMBO) == null) {
            player.sendSystemMessage(Component.literal("Limbo dimension is unavailable"));
            return false;
        }
        running = true;
        start(player.getServer(), List.of(player.getUUID()), stage);
        return true;
    }

    private static void start(MinecraftServer server, List<UUID> playerIds, int stage) {
        ServerLevel limbo = server.getLevel(SplitSelfWorldEvents.LIMBO);
        if (limbo == null) {
            running = false;
            return;
        }
        forEachOnline(server, playerIds, player -> player.setGameMode(GameType.ADVENTURE));
        switch (stage) {
            case 0 -> {
                teleport(server, playerIds, limbo, 2.3, 1.5625, 9.7, -135, 40);
                finishAfter(server, playerIds, 20 * 20L);
            }
            case 1 -> {
                spawnActor(limbo, 1006.5, 3, 33.5, -160, -40);
                teleport(server, playerIds, limbo, 1015.3, 9.5625, 34.7, -135, 40);
                finishAfter(server, playerIds, 60 * 20L);
            }
            case 2 -> {
                spawnActor(limbo, 2036.5, 4, 20, 49, -14);
                teleport(server, playerIds, limbo, 2015.3, 9.5625, 34.7, -135, 40);
                finishAfter(server, playerIds, 60 * 20L);
            }
            case 3 -> {
                teleport(server, playerIds, limbo, 3009.5, 11.5625, 6.5, -45, 40);
                scheduleDialogue(server, playerIds, 100, 0);
            }
            case 4 -> {
                teleport(server, playerIds, limbo, 3009.5, 11.5625, 6.5, -45, 40);
                finishAfter(server, playerIds, 30 * 20L);
            }
            default -> finishAfter(server, playerIds, 0);
        }
    }

    private static void scheduleDialogue(MinecraftServer server, List<UUID> ids,
                                         long delay, int line) {
        if (line == 4) {
            finishAfter(server, ids, delay);
            return;
        }
        TickScheduler.schedule(delay, () -> {
            forEachOnline(server, ids, player -> player.sendSystemMessage(Component.literal(
                    "<" + player.getName().getString() + "> ").append(
                    Component.translatable("chat.splitself.sleep.talk" + (line + 1)))));
            long nextDelay = switch (line) {
                case 0, 1 -> 100;
                case 2 -> 200;
                default -> 300;
            };
            scheduleDialogue(server, ids, nextDelay, line + 1);
        });
    }

    private static void finishAfter(MinecraftServer server, List<UUID> ids, long delay) {
        TickScheduler.schedule(delay, () -> {
            clearActors();
            server.overworld().setDayTime(0);
            forEachOnline(server, ids, player -> {
                if (player.level().dimension().equals(SplitSelfWorldEvents.LIMBO)) {
                    player.removeEffect(net.minecraft.world.effect.MobEffects.SLOW_FALLING);
                    player.removeEffect(net.minecraft.world.effect.MobEffects.LEVITATION);
                    BlockPosTarget target = respawnTarget(server, player);
                    player.teleportTo(target.level(), target.x(), target.y(), target.z(), 0, 0);
                }
                player.setGameMode(GameType.SURVIVAL);
            });
            running = false;
        });
    }

    private record BlockPosTarget(ServerLevel level, double x, double y, double z) {}

    private static BlockPosTarget respawnTarget(MinecraftServer server, ServerPlayer player) {
        ServerLevel destination = player.getRespawnDimension() == null ? null
                : server.getLevel(player.getRespawnDimension());
        net.minecraft.core.BlockPos spawn = player.getRespawnPosition();
        if (destination == null || spawn == null) {
            destination = server.overworld();
            spawn = destination.getSharedSpawnPos();
        }
        return new BlockPosTarget(destination, spawn.getX() + 0.5,
                spawn.getY() + 0.5625, spawn.getZ() + 0.5);
    }

    private static void teleport(MinecraftServer server, List<UUID> ids, ServerLevel destination,
                                 double x, double y, double z, float yaw, float pitch) {
        forEachOnline(server, ids,
                player -> player.teleportTo(destination, x, y, z, yaw, pitch));
    }

    private static void forEachOnline(MinecraftServer server, List<UUID> ids,
                                      java.util.function.Consumer<ServerPlayer> action) {
        for (UUID id : ids) {
            ServerPlayer player = server.getPlayerList().getPlayer(id);
            if (player != null) action.accept(player);
        }
    }

    private static void spawnActor(ServerLevel level, double x, double y, double z, float yaw, float pitch) {
        TheOtherEntity actor = SplitSelfForge.THE_OTHER.get().create(level);
        if (actor == null) throw new IllegalStateException("Cannot create The Other");
        actor.moveTo(x, y, z, yaw, pitch);
        actor.setYHeadRot(yaw);
        actor.setYBodyRot(yaw);
        // Source runSleepEvent directly constructs DEFAULT in both scenes; no finalizeSpawn.
        actor.setPersistenceRequired();
        actor.expireAfter(1200);
        level.getChunkAt(actor.blockPosition());
        if (level.addFreshEntity(actor)) sceneActors.add(actor);
    }

    private static void clearActors() {
        sceneActors.forEach(TheOtherEntity::discard);
        sceneActors.clear();
    }

    static void reset() { clearActors(); running = false; }
}
