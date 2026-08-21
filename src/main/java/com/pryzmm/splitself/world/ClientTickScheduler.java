package com.pryzmm.splitself.world;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ClientTickScheduler {

    private record ScheduledTask(long runAtTick, Runnable task) {}

    private static final PriorityQueue<ScheduledTask> tasks = new PriorityQueue<>(Comparator.comparingLong(a -> a.runAtTick));

    private static final ConcurrentLinkedQueue<ScheduledTask> pending = new ConcurrentLinkedQueue<>();

    private static long currentTick = 0;

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(ClientTickScheduler::tick);
    }

    public static void schedule(long delayTicks, Runnable task) {
        pending.add(new ScheduledTask(currentTick + delayTicks, task));
    }

    private static void tick(MinecraftClient client) {
        currentTick++;

        ScheduledTask t;
        while ((t = pending.poll()) != null) {
            tasks.add(t);
        }

        while (!tasks.isEmpty() && tasks.peek().runAtTick <= currentTick) {
            tasks.poll().task().run();
        }
    }
}