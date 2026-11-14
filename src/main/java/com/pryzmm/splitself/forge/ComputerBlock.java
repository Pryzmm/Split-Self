package com.pryzmm.splitself.forge;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;

/** Core activation sequence of the pinned ComputerBlock. */
final class ComputerBlock extends HorizontalDirectionalBlock {
    static final BooleanProperty ACTIVATING = BooleanProperty.create("activating");
    static final BooleanProperty ACTIVATED = BooleanProperty.create("activated");

    ComputerBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH)
                .setValue(ACTIVATING, false).setValue(ACTIVATED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(FACING, ACTIVATING, ACTIVATED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult hit) {
        if (state.getValue(ACTIVATED) || state.getValue(ACTIVATING)) return InteractionResult.FAIL;
        if (level instanceof ServerLevel serverLevel) {
            MemoryData.get(serverLevel).setFull(true);
            level.setBlock(pos, state.setValue(ACTIVATING, true), 3);
            level.playSound(null, pos, SplitSelfForge.KEYBOARD_SOUND.get(), SoundSource.BLOCKS, 1, 1);
            serverLevel.scheduleTick(pos, this, 47);
            TickScheduler.schedule(100, () -> {
                if (serverLevel.dimension().equals(SplitSelfWorldEvents.LIMBO) && pos.getX() >= 4000) {
                    for (var connected : serverLevel.getServer().getPlayerList().getPlayers()) {
                        StoryNetwork.close(connected);
                    }
                }
            });
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.is(this) && state.getValue(ACTIVATING) && !state.getValue(ACTIVATED)) {
            level.setBlock(pos, state.setValue(ACTIVATING, false).setValue(ACTIVATED, true), 3);
        }
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        return List.of(new ItemStack(this));
    }
}
