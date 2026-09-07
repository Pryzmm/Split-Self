package com.pryzmm.splitself.world;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TickScheduler {

    private record ScheduledTask(long runAtTick, Runnable task, boolean ignoreClear) {}

    private static final PriorityQueue<ScheduledTask> tasks = new PriorityQueue<>(Comparator.comparingLong(a -> a.runAtTick));

    private static final ConcurrentLinkedQueue<ScheduledTask> pending = new ConcurrentLinkedQueue<>();

    private static long currentTick = 0;

    public static void init() {
        ServerTickEvents.END_SERVER_TICK.register(TickScheduler::tick);
    }

    public static void schedule(long delayTicks, Runnable task) { schedule(delayTicks, task, false); }
    public static void schedule(long delayTicks, Runnable task, boolean ignoreClear) {
        pending.add(new ScheduledTask(currentTick + delayTicks, task, ignoreClear));
    }

    private static void tick(MinecraftServer server) {
        currentTick++;

        ScheduledTask t;
        while ((t = pending.poll()) != null) {
            tasks.add(t);
        }

        while (!tasks.isEmpty() && tasks.peek().runAtTick <= currentTick) {
            tasks.poll().task().run();
        }
    }

    public static void clearAllTasks() {
        tasks.removeIf(t -> !t.ignoreClear);
        pending.removeIf(p -> !p.ignoreClear);
    }

}