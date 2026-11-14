package com.pryzmm.splitself.forge;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.BlockHitResult;

/** Double-height exit door and safe server-side route from grass-empty to Limbo. */
final class ExitDoorBlock extends HorizontalDirectionalBlock {
    static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    private static final VoxelShape NORTH = box(0, 0, 0, 16, 16, 3);
    private static final VoxelShape SOUTH = box(0, 0, 13, 16, 16, 16);
    private static final VoxelShape EAST = box(13, 0, 0, 16, 16, 16);
    private static final VoxelShape WEST = box(0, 0, 0, 3, 16, 16);

    ExitDoorBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH)
                .setValue(HALF, DoubleBlockHalf.LOWER));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(FACING, HALF);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        if (pos.getY() >= context.getLevel().getMaxBuildHeight() - 1
                || !context.getLevel().getBlockState(pos.above()).canBeReplaced(context)) return null;
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        level.setBlock(pos.above(), state.setValue(HALF, DoubleBlockHalf.UPPER), 3);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbor,
            LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        DoubleBlockHalf half = state.getValue(HALF);
        if (direction.getAxis() == Direction.Axis.Y
                && (half == DoubleBlockHalf.LOWER) == (direction == Direction.UP)) {
            if (neighbor.is(this) && neighbor.getValue(HALF) != half)
                return state.setValue(FACING, neighbor.getValue(FACING));
            return Blocks.AIR.defaultBlockState();
        }
        if (half == DoubleBlockHalf.LOWER && direction == Direction.DOWN && !canSurvive(state, level, pos))
            return Blocks.AIR.defaultBlockState();
        return super.updateShape(state, direction, neighbor, level, pos, neighborPos);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER)
            return below.is(this) && below.getValue(HALF) == DoubleBlockHalf.LOWER;
        return below.isFaceSturdy(level, pos.below(), Direction.UP);
    }

    private VoxelShape shape(BlockState state) {
        return switch (state.getValue(FACING)) {
            case SOUTH -> SOUTH;
            case EAST -> EAST;
            case WEST -> WEST;
            default -> NORTH;
        };
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shape(state);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shape(state);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        return state.getValue(HALF) == DoubleBlockHalf.LOWER
                ? List.of(new ItemStack(this)) : List.of();
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult hit) {
        if (!(player instanceof ServerPlayer serverPlayer) || level.isClientSide
                 ) {
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if(!level.dimension().equals(SplitSelfWorldEvents.GRASS_EMPTY)){for(var listener:serverPlayer.server.getPlayerList().getPlayers())StoryNetwork.clientEvent(listener,"FINALE_INTRO");return InteractionResult.SUCCESS;}
        ServerLevel limbo = serverPlayer.getServer().getLevel(SplitSelfWorldEvents.LIMBO);
        if (limbo == null) return InteractionResult.FAIL;
        serverPlayer.setGameMode(GameType.ADVENTURE);
        serverPlayer.teleportTo(limbo, 4009.5, 7.063, 44.0, -90.0f, 45.0f);
        TickScheduler.schedule(100,()->{var displays=limbo.getEntitiesOfClass(net.minecraft.world.entity.Display.TextDisplay.class,new net.minecraft.world.phys.AABB(3999,0,0,4019,13,13));if(!displays.isEmpty()){var display=displays.get(0);var tag=display.saveWithoutId(new net.minecraft.nbt.CompoundTag());tag.putString("text",net.minecraft.network.chat.Component.Serializer.toJson(net.minecraft.network.chat.Component.translatable("selectWorld.deleteQuestion").append("\n").append(net.minecraft.network.chat.Component.translatable("selectWorld.deleteWarning",serverPlayer.server.getWorldData().getLevelName()))));display.load(tag);};});
        for(var listener:serverPlayer.server.getPlayerList().getPlayers())StoryNetwork.clientEvent(listener,"FINALE_INTRO");
        return InteractionResult.SUCCESS;
    }
}
