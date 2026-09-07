package com.pryzmm.splitself.block;

import com.pryzmm.splitself.data.WorldData;
import com.pryzmm.splitself.packet.packets.ClosePacket;
import com.pryzmm.splitself.sound.ModSounds;
import com.pryzmm.splitself.world.DimensionRegistry;
import com.pryzmm.splitself.world.TickScheduler;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.List;

public class ComputerBlock extends Block {

    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
    public static final BooleanProperty ACTIVATING = BooleanProperty.of("activating");
    public static final BooleanProperty ACTIVATED = BooleanProperty.of("activated");

    public ComputerBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH).with(ACTIVATED, false).with(ACTIVATING, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, ACTIVATED, ACTIVATING);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!state.get(ACTIVATED) && !state.get(ACTIVATING)) {
            if (!world.isClient) {
                WorldData.setIsFull(true);
                world.setBlockState(pos, state.with(ACTIVATING, true));
                world.playSound(null, pos, ModSounds.KEYBOARD, SoundCategory.BLOCKS, 1f, 1f);
                TickScheduler.schedule(47, () -> {
                    if (world.getBlockState(pos).getBlock() == ModBlocks.COMPUTER) world.setBlockState(pos, state.with(ACTIVATED, true).with(ACTIVATING, false));
                });
                TickScheduler.schedule(100, () -> {
                    if (world.getRegistryKey() == DimensionRegistry.LIMBO_DIMENSION_KEY && pos.getX() >= 4000) { // helps to prevent possible abuse
                        for (ServerPlayerEntity p : ((ServerWorld) world).getServer().getPlayerManager().getPlayerList()) {
                            ServerPlayNetworking.send(p, new ClosePacket());
                        }
                    }
                });
                return ActionResult.SUCCESS;
            } return ActionResult.FAIL;
        } return ActionResult.FAIL;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public List<ItemStack> getDroppedStacks(BlockState state, LootContextParameterSet.Builder builder) {
        return Collections.singletonList(new ItemStack(this));
    }

}