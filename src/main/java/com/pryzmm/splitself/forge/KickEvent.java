package com.pryzmm.splitself.forge;

import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SplitSelfForge.MOD_ID)
public final class KickEvent {
    private static final KickRules.Permits PERMITS = new KickRules.Permits();
    private KickEvent() {}
    static void start(ServerPlayer player) { StoryNetwork.kick(player, PERMITS.issue(player.getUUID())); }
    static void answer(ServerPlayer player, UUID token) {
        if (!PERMITS.consume(player.getUUID(), token)) return;
        var world = player.serverLevel();
        var bed = player.getRespawnDimension().equals(world.dimension()) ? player.getRespawnPosition() : null;
        var center = WorldManifestationRules.moveVectorFromBase(player.position(), bed, EventCatalog.baseSafeRadius());
        var random = new java.util.Random();
        KickRules.erase(center, pos -> !world.isOutsideBuildHeight(pos) && world.hasChunkAt(pos),
                pos -> world.getBlockState(pos).isAir(), random::nextBoolean,
                pos -> world.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL));
    }
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent event) { PERMITS.forget(event.getEntity().getUUID()); }
    @SubscribeEvent public static void stop(ServerStoppedEvent event) { PERMITS.clear(); }
}
