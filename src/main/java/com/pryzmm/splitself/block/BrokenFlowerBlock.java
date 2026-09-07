package com.pryzmm.splitself.block;

import com.pryzmm.splitself.world.DimensionRegistry;
import com.pryzmm.splitself.world.TickScheduler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BrokenFlowerBlock extends Block {

    public BrokenFlowerBlock(Settings settings) {
        super(settings);
    }

    protected boolean isTransparent(BlockState state, BlockView world, BlockPos pos) {
        return true;
    }

    protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (entity instanceof ServerPlayerEntity player) transition(player, pos);
    }

    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    public static final List<UUID> playersTransitioning = new ArrayList<>();

    @SuppressWarnings("DataFlowIssue")
    public static void transition(ServerPlayerEntity player, BlockPos pos) {
        if (!playersTransitioning.contains(player.getUuid())) {
            playersTransitioning.add(player.getUuid());
            ServerWorld oldWorld = player.getServerWorld();
            player.getWorld().setBlockState(pos, Blocks.AIR.getDefaultState(), 0);
            ServerWorld world = player.getServer().getWorld(DimensionRegistry.HALLWAY_DIMENSION_KEY);
            player.teleport(world, 0.5f, 101, 0.5f, null, 0, 0);
            TickScheduler.schedule(1200, () -> player.teleport(oldWorld, pos.getX() + 0.5f, pos.getY(), pos.getZ() + 0.5f, null, 0, 0));
        }
    }

}