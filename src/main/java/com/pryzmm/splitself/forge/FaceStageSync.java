package com.pryzmm.splitself.forge;

import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Source WorldData is global; give remote clients the authoritative sleep stage too. */
@Mod.EventBusSubscriber(modid = SplitSelfForge.MOD_ID)
public final class FaceStageSync {
    private static final Map<ServerPlayer, Integer> SENT = new WeakHashMap<>();
    @SubscribeEvent public static void tick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        int stage = MemoryData.get(event.getServer().overworld()).sleepStage();
        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            if (!Integer.valueOf(stage).equals(SENT.get(player))) {
                StoryNetwork.sleepStage(player, stage); SENT.put(player, stage);
            }
        }
    }
    @SubscribeEvent public static void stopped(ServerStoppedEvent event) { SENT.clear(); }
}
