package com.pryzmm.splitself.block;

import com.pryzmm.splitself.block.functions.EmptyTeleportBlockFunc;
import com.pryzmm.splitself.item.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class EmptyTeleportBlock extends Block {

    public EmptyTeleportBlock(Settings settings) {
        super(settings);
    }

    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return context.isHolding(ModItems.EMPTY_TELEPORT) ? VoxelShapes.fullCube() : VoxelShapes.empty();
    }

    protected boolean isTransparent(BlockState state, BlockView world, BlockPos pos) {
        return true;
    }

    protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (entity instanceof ServerPlayerEntity player) {
            EmptyTeleportBlockFunc.Location location = EmptyTeleportBlockFunc.getLastLocation(player);
            if (location != null) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 20, 1, false, false, false));
                player.teleport(location.world(), location.pos().x, location.pos().y, location.pos().z, 0, 0);
            }
        }
    }

    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.INVISIBLE;
    }

}
