package com.pryzmm.splitself.events;

import com.pryzmm.splitself.world.FirstJoinTracker;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SleepTracker {

    private static final Map<UUID, SleepData> sleepingPlayers = new HashMap<>();
    private static FirstJoinTracker tracker;

    public static class SleepData {
        public final long sleepStartTime;
        public final long sleepStartWorldTime;
        public SleepData(long gameTime, long worldTime) {
            this.sleepStartTime = gameTime;
            this.sleepStartWorldTime = worldTime;
        }
    }

    public static void startSleep(ServerPlayerEntity player) {
        ServerWorld world = player.getServerWorld();
        sleepingPlayers.put(player.getUuid(),
                new SleepData(world.getTime(), world.getTimeOfDay()));
    }

    public static void updateSleep(ServerPlayerEntity player) {
        SleepData data = sleepingPlayers.get(player.getUuid());
        if (data == null) return;
        tracker = FirstJoinTracker.getServerState(player.getServer());
        ServerWorld world = player.getServerWorld();
        long currentGameTime = world.getTime();
        long sleepDuration = currentGameTime - data.sleepStartTime;
        if (sleepDuration == 100) {
            double num = Math.random();
            System.out.println(num);
            System.out.println(Math.floor((num * 2) + 1));
            if (tracker.getPlayerSleepStage(player.getUuid()) == 0) {
                tracker.setPlayerSleepStage(player.getUuid(), tracker.getPlayerSleepStage(player.getUuid()) + 1);
                EventManager.runSleepEvent(player, 0);
            } else if (Math.floor((num * 2) + 1) == 1) {
                tracker.setPlayerSleepStage(player.getUuid(), tracker.getPlayerSleepStage(player.getUuid()) + 1);
                EventManager.runSleepEvent(player, tracker.getPlayerSleepStage(player.getUuid()));
            }
        }
    }
}