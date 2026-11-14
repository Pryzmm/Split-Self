package com.pryzmm.splitself.forge;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.sounds.SoundSource;

/** One of the five collectible memories from the pinned ModItems source. */
public final class MemoryItem extends Item {
    private final String memoryId;

    public MemoryItem(Properties properties, String memoryId) {
        super(properties);
        this.memoryId = memoryId;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.splitself.memory." + memoryId + ".desc")
                .withStyle(ChatFormatting.GRAY));
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable("item.splitself.memory");
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level instanceof ServerLevel serverLevel) {
            MemoryData data = MemoryData.get(serverLevel);
            if (data.unlock(memoryId)) {
                level.playSound(null, player.blockPosition(),
                        SplitSelfForge.MEMORY_SOUND.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
                stack.shrink(1);
                serverLevel.getServer().getPlayerList().broadcastSystemMessage(
                        Component.translatable("item.splitself.memory.use"), false);
                player.awardStat(Stats.ITEM_USED.get(this));
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
