package com.pryzmm.splitself.forge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.TickEvent;

/** Server-side event cadence and weighted selection from the source EventManager. */
final class EventRuntime {
    private static int cooldown;
    private static int guaranteedTicks;
    private static int totalTriggered;
    private static boolean startMessageSent;
    private static final Map<String, Integer> LAST_TRIGGERED = new HashMap<>();
    private static final Set<String> CONSUMED_ONE_TIME = new HashSet<>();

    private EventRuntime() {}

    static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        var server = event.getServer();
        ServerLevel overworld = server.overworld();
        long worldTime = overworld.getDayTime();
        if (worldTime < EventCatalog.startAfter()) return;

        if (!startMessageSent && !server.getPlayerList().getPlayers().isEmpty()) {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                player.sendSystemMessage(Component.translatable("death.attack.outsideBorder", player.getName().getString()));
            }
            startMessageSent = true;
        }
        if (!EventCatalog.enabled()) return;

        if (guaranteedTicks == 0) guaranteedTicks = EventCatalog.guaranteedAfter();
        else if (guaranteedTicks > 0) guaranteedTicks--;
        if (cooldown > 0) {
            cooldown--;
            return;
        }
        int interval = Math.max(1, EventCatalog.tickInterval());
        if (worldTime % interval != 0) return;
        if (overworld.getRandom().nextDouble() >= EventCatalog.chance() && guaranteedTicks != 0) return;

        List<ServerPlayer> players = new ArrayList<>();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (!player.level().dimension().equals(SplitSelfWorldEvents.LIMBO)
                    && !player.level().dimension().equals(SplitSelfWorldEvents.EMPTY)) players.add(player);
        }
        if (players.isEmpty()) return;

        MemoryData data = MemoryData.get(overworld);
        List<String> candidates = EventCatalog.eligible(data.sleepStage(), CONSUMED_ONE_TIME,
                LAST_TRIGGERED, totalTriggered);
        if (candidates.isEmpty()) return;
        int totalWeight = candidates.stream().mapToInt(SplitSelfConfig::weight).sum();
        int roll = overworld.getRandom().nextInt(totalWeight);
        String selected = candidates.get(0);
        for (String name : candidates) {
            roll -= SplitSelfConfig.weight(name);
            if (roll < 0) { selected = name; break; }
        }
        ServerPlayer target = players.get(overworld.getRandom().nextInt(players.size()));
        if (!PortedEvents.run(target, selected)) return;

        totalTriggered++;
        LAST_TRIGGERED.put(selected, totalTriggered);
        // Kept pinned EventManager quirk: its public oneTimeEvents map is never populated automatically.
        cooldown = EventCatalog.cooldown();
        guaranteedTicks = EventCatalog.guaranteedAfter();
    }

    static boolean force(ServerPlayer player,String requested){
        String name=requested.toUpperCase(java.util.Locale.ROOT);
        if(player.level().dimension().equals(SplitSelfWorldEvents.LIMBO)||player.level().dimension().equals(SplitSelfWorldEvents.EMPTY)){totalTriggered++;return false;}
        if(name.equals("BROWSER")){totalTriggered++;LAST_TRIGGERED.put(name,totalTriggered);return true;}
        if(name.equals("RANDOM")){
            var candidates=EventCatalog.eligible(MemoryData.get(player.server.overworld()).sleepStage(),CONSUMED_ONE_TIME,LAST_TRIGGERED,totalTriggered);
            if(candidates.isEmpty())name="SPAWNTHEOTHER";
            else{int total=candidates.stream().mapToInt(SplitSelfConfig::weight).sum(),roll=player.getRandom().nextInt(total);for(String candidate:candidates){roll-=SplitSelfConfig.weight(candidate);if(roll<0){name=candidate;break;}}}
        }
        if(!PortedEvents.run(player,name))return false;totalTriggered++;LAST_TRIGGERED.put(name,totalTriggered);return true;
    }
    static void reset() {
        cooldown = 0;
        guaranteedTicks = 0;
        totalTriggered = 0;
        startMessageSent = false;
        LAST_TRIGGERED.clear();
        CONSUMED_ONE_TIME.clear();
    }
}
