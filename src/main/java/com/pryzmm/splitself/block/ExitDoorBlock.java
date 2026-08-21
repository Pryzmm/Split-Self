package com.pryzmm.splitself.block;

import com.pryzmm.splitself.packet.packets.FinalePacket;
import com.pryzmm.splitself.world.DimensionRegistry;
import com.pryzmm.splitself.world.TickScheduler;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.*;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;
import java.util.Collections;
import java.util.List;

public class ExitDoorBlock extends Block {

    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
    public static final EnumProperty<DoubleBlockHalf> HALF = Properties.DOUBLE_BLOCK_HALF;

    protected static final VoxelShape NORTH_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 16.0, 3.0);
    protected static final VoxelShape SOUTH_SHAPE = Block.createCuboidShape(0.0, 0.0, 13.0, 16.0, 16.0, 16.0);
    protected static final VoxelShape EAST_SHAPE  = Block.createCuboidShape(13.0, 0.0, 0.0, 16.0, 16.0, 16.0);
    protected static final VoxelShape WEST_SHAPE  = Block.createCuboidShape(0.0, 0.0, 0.0, 3.0, 16.0, 16.0);

    public ExitDoorBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState()
                .with(FACING, Direction.NORTH)
                .with(HALF, DoubleBlockHalf.LOWER));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, HALF);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockPos pos = ctx.getBlockPos();
        World world = ctx.getWorld();
        if (pos.getY() < world.getTopY() - 1 && world.getBlockState(pos.up()).canReplace(ctx)) {
            return this.getDefaultState()
                    .with(FACING, ctx.getHorizontalPlayerFacing().getOpposite())
                    .with(HALF, DoubleBlockHalf.LOWER);
        }
        return null;
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        world.setBlockState(pos.up(), state.with(HALF, DoubleBlockHalf.UPPER), 3);
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        DoubleBlockHalf half = state.get(HALF);

        if (direction.getAxis() == Direction.Axis.Y && (half == DoubleBlockHalf.LOWER) == (direction == Direction.UP)) {
            if (neighborState.isOf(this) && neighborState.get(HALF) != half) return state.with(FACING, neighborState.get(FACING));
            return Blocks.AIR.getDefaultState();
        }

        if (half == DoubleBlockHalf.LOWER && direction == Direction.DOWN && !state.canPlaceAt(world, pos)) {
            return Blocks.AIR.getDefaultState();
        }

        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        if (state.get(HALF) == DoubleBlockHalf.LOWER) {
            BlockPos belowPos = pos.down();
            return world.getBlockState(belowPos).isSideSolidFullSquare(world, belowPos, Direction.UP);
        } else {
            BlockState belowState = world.getBlockState(pos.down());
            return belowState.isOf(this) && belowState.get(HALF) == DoubleBlockHalf.LOWER;
        }
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        Direction direction = state.get(FACING);
        VoxelShape shape;
        switch (direction) {
            case NORTH -> shape = NORTH_SHAPE;
            case SOUTH -> shape = SOUTH_SHAPE;
            case EAST -> shape = EAST_SHAPE;
            default -> shape = WEST_SHAPE;
        }
        return shape;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        Direction direction = state.get(FACING);
        VoxelShape shape;
        switch (direction) {
            case NORTH -> shape = NORTH_SHAPE;
            case SOUTH -> shape = SOUTH_SHAPE;
            case EAST -> shape = EAST_SHAPE;
            default -> shape = WEST_SHAPE;
        }
        return shape;
    }

    @Override
    public List<ItemStack> getDroppedStacks(BlockState state, LootContextParameterSet.Builder builder) {
        return state.get(HALF) == DoubleBlockHalf.LOWER
                ? Collections.singletonList(new ItemStack(this))
                : Collections.emptyList();
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (player instanceof ServerPlayerEntity p) {
            MinecraftServer server = p.getServer();
            assert server != null;
            if (p.getServerWorld().getRegistryKey() == DimensionRegistry.GRASS_EMPTINESS_DIMENSION_KEY) {
                ServerWorld limboWorld = server.getWorld(DimensionRegistry.LIMBO_DIMENSION_KEY);
                assert limboWorld != null;
                player.teleport(limboWorld, 4009.5, 7.063, 44.0, null, -90, 45);
                TickScheduler.schedule(100, () -> {
                    try {
                        DisplayEntity.TextDisplayEntity display = limboWorld.getEntitiesByClass(DisplayEntity.TextDisplayEntity.class, new Box(new Vec3d(3999, 13, 13), new Vec3d(4019, 0, 0)), (e) -> true).getFirst();
                        display.setText(Text.translatable("selectWorld.deleteQuestion").append("\n")
                            .append(Text.translatable("selectWorld.deleteWarning", server.getSaveProperties().getLevelName())));
                    } catch (Exception ignored) {}
                });
            }
            server.getPlayerManager().getPlayerList().forEach(pl -> ServerPlayNetworking.send(pl, new FinalePacket(true, false)));
        }
        return ActionResult.SUCCESS;
    }

}