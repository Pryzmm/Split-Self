package com.pryzmm.splitself.forge;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;

/** Server owns the scene completion; no client teleport requests are accepted. */
final class StoryScenes {
    private static final String BROKEN_RETURN = "SplitSelfBrokenReturnAt";
    // Source delays sum to 19681 ms. Round up to the next server tick.
    private static final long BROKEN_TICKS = 394;
    private StoryScenes() {}

    static boolean canStartBroken(MinecraftServer server) {
        return server.getLevel(SplitSelfWorldEvents.EMPTY) != null
                && server.getPlayerList().getPlayers().stream().noneMatch(
                        player -> player.getPersistentData().contains(BROKEN_RETURN));
    }

    static boolean startBroken(MinecraftServer server) {
        if (!canStartBroken(server)) return false;
        long finish = server.overworld().getGameTime() + BROKEN_TICKS;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player.level().dimension().equals(SplitSelfWorldEvents.EMPTY)) continue;
            player.getPersistentData().putLong(BROKEN_RETURN, finish);
            StoryNetwork.broken(player);
        }
        return true;
    }

    static void onClone(net.minecraftforge.event.entity.player.PlayerEvent.Clone event) {
        var old = event.getOriginal().getPersistentData();
        if (old.contains(BROKEN_RETURN)) event.getEntity().getPersistentData()
                .putLong(BROKEN_RETURN, old.getLong(BROKEN_RETURN));
    }

    static void onTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        var server = event.getServer();
        var empty = server.getLevel(SplitSelfWorldEvents.EMPTY);
        if (empty == null) return;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            var state = player.getPersistentData();
            if (!state.contains(BROKEN_RETURN)
                    || server.overworld().getGameTime() < state.getLong(BROKEN_RETURN)) continue;
            var pos = DeadCoralChunkGenerator.findGroundPos(0, 0);
            empty.getChunkAt(pos);
            player.teleportTo(empty, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0, 0);
            state.remove(BROKEN_RETURN);
        }
    }
}
