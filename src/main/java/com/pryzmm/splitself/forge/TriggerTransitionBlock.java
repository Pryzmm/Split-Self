package com.pryzmm.splitself.forge;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SplitSelfForge.MOD_ID)
final class TriggerTransitionBlock extends Block {
    private static final TransitionRecovery RECOVERY = new TransitionRecovery();
    private static long ticks;
    TriggerTransitionBlock(Properties properties) { super(properties); }
    private static TransitionRecovery.Player adapt(ServerPlayer player) {
        return new TransitionRecovery.Player() {
            public UUID playerId() { return player.getUUID(); }
            public UUID savedId() { return TransitionIntent.read(player.getPersistentData()); }
            public void save(UUID id) { TransitionIntent.write(player.getPersistentData(), id); }
            public void removeSaved() { TransitionIntent.remove(player.getPersistentData()); }
            public boolean alive() { return player.isAlive(); }
            public boolean teleport() { return TriggerTransitionBlock.teleport(player); }
            public void started(UUID id) { StoryNetwork.transition(player, id); }
            public void completed(UUID id) { StoryNetwork.transitionResult(player, id); }
        };
    }
    @Override public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide && entity instanceof ServerPlayer player) start(player);
    }
    static boolean start(ServerPlayer player) { return RECOVERY.start(adapt(player), ticks); }
    static void heartbeat(ServerPlayer player, UUID id) { RECOVERY.heartbeat(adapt(player), id, ticks); }
    static boolean finish(ServerPlayer player, UUID id) { return RECOVERY.finish(adapt(player), id, ticks); }
    private static boolean teleport(ServerPlayer player) {
        try {
            var empty = player.server.getLevel(SplitSelfWorldEvents.GRASS_EMPTY);
            if (empty == null) throw new IllegalStateException("Grass Empty unavailable");
            BlockPos pos = GrassEmptyChunkGenerator.findGroundPos(0, 0);
            empty.getChunkAt(pos);
            player.teleportTo(empty, pos.getX() + 0.5, 700, pos.getZ() + 0.5, 0, 0);
            if (player.serverLevel() != empty || Math.abs(player.getY() - 700) >= 0.01)
                throw new IllegalStateException("Teleport did not reach Grass Empty at Y=700");
            return true;
        } catch (RuntimeException error) {
            org.slf4j.LoggerFactory.getLogger(TriggerTransitionBlock.class)
                .warn("Transition recovery failed for {}; retry in 100 server ticks", player.getUUID(), error);
            return false;
        }
    }
    @SubscribeEvent public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) { recover(event); }
    @SubscribeEvent public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) { recover(event); }
    private static void recover(PlayerEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            RECOVERY.tick(adapt(player), ticks);
        }
    }
    @SubscribeEvent public static void onClone(PlayerEvent.Clone event) {
        TransitionIntent.copy(event.getOriginal().getPersistentData(), event.getEntity().getPersistentData());
        RECOVERY.forget(event.getEntity().getUUID());
    }
    @SubscribeEvent public static void onTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        ticks++;
        for (var player : event.getServer().getPlayerList().getPlayers()) {
            RECOVERY.tick(adapt(player), ticks);
        }
    }
    @SubscribeEvent public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) { RECOVERY.forget(event.getEntity().getUUID()); }
    @SubscribeEvent public static void onServerStopped(ServerStoppedEvent event) { RECOVERY.clearTemporary(); ticks = 0; }
    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.INVISIBLE; }
}
