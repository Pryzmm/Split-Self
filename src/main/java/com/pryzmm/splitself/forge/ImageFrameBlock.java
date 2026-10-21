package com.pryzmm.splitself.forge;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** The physical frame from the pinned mod. Dynamic screenshots need a separate client renderer. */
final class ImageFrameBlock extends HorizontalDirectionalBlock implements EntityBlock {
    private static final VoxelShape NORTH = box(1, 4, 14, 15, 12, 16);
    private static final VoxelShape SOUTH = box(1, 4, 0, 15, 12, 2);
    private static final VoxelShape EAST = box(0, 4, 1, 2, 12, 15);
    private static final VoxelShape WEST = box(14, 4, 1, 16, 12, 15);

    ImageFrameBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction side = context.getClickedFace();
        if (side.getAxis().isVertical()) side = context.getHorizontalDirection().getOpposite();
        return defaultBlockState().setValue(FACING, side);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case SOUTH -> SOUTH;
            case EAST -> EAST;
            case WEST -> WEST;
            default -> NORTH;
        };
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ImageFrameBlockEntity(pos, state);
    }
}
