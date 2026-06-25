package com.pryzmm.splitself.block;

import com.pryzmm.splitself.SplitSelf;
import com.pryzmm.splitself.data.WorldData;
import com.pryzmm.splitself.events.ScreenOverlay;
import com.pryzmm.splitself.item.ModItems;
import com.pryzmm.splitself.packet.packets.BrokenEffectPacket;
import com.pryzmm.splitself.packet.packets.EndBrokenEffectPacket;
import com.pryzmm.splitself.screen.BrokenScreen;
import com.pryzmm.splitself.sound.ModSounds;
import com.pryzmm.splitself.world.DimensionRegistry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.block.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import java.util.Collections;
import java.util.List;

public class BrainBlock extends Block {

    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
    public static final BooleanProperty HAS_BOOK = BooleanProperty.of("has_book");

    public BrainBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH).with(HAS_BOOK, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, HAS_BOOK);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (stack.isOf(ModItems.MEMORY_BOOK)) {
            if (hand == Hand.MAIN_HAND) {
                if (WorldData.getUnlockedMemories().size() < 5) {
                    player.sendMessage(SplitSelf.translate("block.splitself.brain.not_enough_memories"), true);
                } else if (!state.get(HAS_BOOK)) {
                    world.setBlockState(pos, state.with(HAS_BOOK, true));
                    stack.decrement(1);
                    world.playSound(null, pos, ModSounds.SQUISH, SoundCategory.BLOCKS, 1.0f, 1.0f);
                    MinecraftServer server = world.getServer();
                    if (server != null) {
                        for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
                            if (p.getServerWorld().getRegistryKey() != DimensionRegistry.EMPTINESS_DIMENSION_KEY) {
                                world.playSound(null, pos, ModSounds.SCRAPE, SoundCategory.BLOCKS, 1.0f, 1.0f);
                                ClientPlayNetworking.send(new BrokenEffectPacket());
                            }
                        }
                    }
                }
                return ItemActionResult.SUCCESS;
            } else return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        return ItemActionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
    }

    public static boolean brokenEffectActive = false;

    public static void playBrokenEffect(PlayerEntity p, MinecraftClient client) {
        new Thread(() -> {
            try {
                Thread.sleep(3912);
                client.execute(() -> ScreenOverlay.executeRecursiveScreen(p, 168, false));
                Thread.sleep(1535);
                client.execute(() -> ScreenOverlay.executeRecursiveScreen(p, 225, false));
                Thread.sleep(3089);
                client.execute(() -> ScreenOverlay.executeRecursiveScreen(p, 280, false));
                Thread.sleep(1904);
                client.execute(() -> ScreenOverlay.executeRecursiveScreen(p, 493, false));
                Thread.sleep(3941);
                client.execute(() -> ScreenOverlay.executeRecursiveScreen(p, 500, false));
                Thread.sleep(300);
                client.execute(() -> client.setScreen(new BrokenScreen()));
                Thread.sleep(5000);
                client.execute(() -> ClientPlayNetworking.send(new EndBrokenEffectPacket()));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public List<ItemStack> getDroppedStacks(BlockState state, LootContextParameterSet.Builder builder) {
        return Collections.singletonList(new ItemStack(this));
    }

}
