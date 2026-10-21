package com.pryzmm.splitself.forge;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;

/** Forge server-tick adaptation of the original TickScheduler. */
public final class TickScheduler {
    private record ScheduledTask(long runAtTick, Runnable task, boolean ignoreClear) {}

    private static final PriorityQueue<ScheduledTask> TASKS = new PriorityQueue<>(
            Comparator.comparingLong(ScheduledTask::runAtTick));
    private static final ConcurrentLinkedQueue<ScheduledTask> PENDING = new ConcurrentLinkedQueue<>();
    private static long currentTick;

    private TickScheduler() {}

    public static void schedule(long delayTicks, Runnable task) {
        schedule(delayTicks, task, false);
    }

    public static void schedule(long delayTicks, Runnable task, boolean ignoreClear) {
        PENDING.add(new ScheduledTask(currentTick + delayTicks, task, ignoreClear));
    }

    static void onTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        currentTick++;
        ScheduledTask next;
        while ((next = PENDING.poll()) != null) TASKS.add(next);
        while (!TASKS.isEmpty() && TASKS.peek().runAtTick <= currentTick) {
            TASKS.poll().task().run();
        }
    }

    public static void clearAllTasks() {
        TASKS.removeIf(task -> !task.ignoreClear());
        PENDING.removeIf(task -> !task.ignoreClear());
    }

    static void onServerStopping(ServerStoppingEvent event) {
        TASKS.clear();
        PENDING.clear();
        currentTick = 0;
    }
}
