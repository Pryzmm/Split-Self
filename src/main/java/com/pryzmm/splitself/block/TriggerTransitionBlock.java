package com.pryzmm.splitself.block;

import com.pryzmm.splitself.block.functions.TransitionBlockFunc;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class TriggerTransitionBlock extends Block {

    public TriggerTransitionBlock(Settings settings) {
        super(settings);
    }

    protected boolean isTransparent(BlockState state, BlockView world, BlockPos pos) {
        return true;
    }

    protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (entity instanceof ServerPlayerEntity player) TransitionBlockFunc.transition(player);
    }

    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.INVISIBLE;
    }

}