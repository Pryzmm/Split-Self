package com.pryzmm.splitself.forge;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** Transition and 1200-tick return from pinned BrokenFlowerBlock. */
final class BrokenFlowerBlock extends Block {
    private static final Set<UUID> TRANSITIONING = new HashSet<>();

    BrokenFlowerBlock(Properties properties) { super(properties); }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!(entity instanceof ServerPlayer player) || !(level instanceof ServerLevel oldWorld)) return;
        if (TRANSITIONING.contains(player.getUUID())) return;
        ServerLevel hallway = oldWorld.getServer().getLevel(SplitSelfWorldEvents.HALLWAY);
        if (hallway == null) return;
        // Generate the destination before teleporting and use a section away
        // from the wall torch at z=0. The corridor is one block wide at x=0.
        BlockPos arrival = new BlockPos(0, 101, 1);
        hallway.getChunkAt(arrival);
        if (!hallway.getBlockState(arrival).getCollisionShape(hallway, arrival).isEmpty()
                || !hallway.getBlockState(arrival.above()).getCollisionShape(hallway, arrival.above()).isEmpty()
                || hallway.getBlockState(arrival.below()).getCollisionShape(hallway, arrival.below()).isEmpty()) return;
        TRANSITIONING.add(player.getUUID());
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 0);
        player.teleportTo(hallway, 0.5, 101, 1.5, 0, 0);
        TickScheduler.schedule(1200, () -> {
            try {
                if (!player.isRemoved() && oldWorld.getServer().isRunning()) {
                    player.teleportTo(oldWorld, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0, 0);
                }
            } finally {
                TRANSITIONING.remove(player.getUUID());
            }
        });
    }

    static void resetForServerStop() { TRANSITIONING.clear(); }
}
