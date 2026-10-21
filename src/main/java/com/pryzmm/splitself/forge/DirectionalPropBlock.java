package com.pryzmm.splitself.forge;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.storage.loot.LootParams;

/** Shapes and placement copied from the pinned KeyboardBlock and TableBlock. */
final class DirectionalPropBlock extends HorizontalDirectionalBlock {
    private final VoxelShape northSouth;
    private final VoxelShape eastWest;
    private final VoxelShape keyboardSouth;
    private final VoxelShape keyboardWest;

    private DirectionalPropBlock(Properties properties, VoxelShape northSouth,
            VoxelShape eastWest, VoxelShape keyboardSouth, VoxelShape keyboardWest) {
        super(properties);
        this.northSouth = northSouth;
        this.eastWest = eastWest;
        this.keyboardSouth = keyboardSouth;
        this.keyboardWest = keyboardWest;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    static DirectionalPropBlock keyboard(Properties properties) {
        return new DirectionalPropBlock(properties,
                box(1.25, 0, 5, 14.75, 1, 12),
                box(4, 0, 1.25, 11, 1, 14.75),
                box(1.25, 0, 4, 14.75, 1, 11),
                box(5, 0, 1.25, 12, 1, 14.75));
    }

    static DirectionalPropBlock table(Properties properties) {
        return new DirectionalPropBlock(properties,
                box(-16, 0, -2.5, 32, 16, 18.5),
                box(-2.5, 0, -16, 18.5, 16, 32), null, null);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    private VoxelShape selectedShape(BlockState state) {
        return switch (state.getValue(FACING)) {
            case SOUTH -> keyboardSouth == null ? northSouth : keyboardSouth;
            case EAST -> eastWest;
            case WEST -> keyboardWest == null ? eastWest : keyboardWest;
            default -> northSouth;
        };
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return selectedShape(state);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return selectedShape(state);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        return List.of(new ItemStack(this));
    }
}
