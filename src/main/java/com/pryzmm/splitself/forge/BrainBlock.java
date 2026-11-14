package com.pryzmm.splitself.forge;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
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

/** Placement and memory-book gate from the pinned BrainBlock source. */
final class BrainBlock extends HorizontalDirectionalBlock {
    static final BooleanProperty HAS_BOOK = BooleanProperty.create("has_book");

    BrainBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH)
                .setValue(HAS_BOOK, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(FACING, HAS_BOOK);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND
                || !player.getItemInHand(hand).is(SplitSelfForge.MEMORY_BOOK.get())) return InteractionResult.PASS;
        if (level instanceof ServerLevel serverLevel) {
            if (MemoryData.get(serverLevel).count() < 5) {
                player.displayClientMessage(Component.translatable("block.splitself.brain.not_enough_memories"), true);
            } else if (!state.getValue(HAS_BOOK)) {
                if (!StoryScenes.startBroken(serverLevel.getServer())) return InteractionResult.FAIL;
                player.getItemInHand(hand).shrink(1);
                level.setBlock(pos, state.setValue(HAS_BOOK, true), 3);
                level.playSound(null, pos, SplitSelfForge.SQUISH_SOUND.get(), net.minecraft.sounds.SoundSource.BLOCKS, 1, 1);
                level.playSound(null, pos, SplitSelfForge.SCRAPE_SOUND.get(), net.minecraft.sounds.SoundSource.BLOCKS, 1, 1);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        return List.of(new ItemStack(this));
    }
}
