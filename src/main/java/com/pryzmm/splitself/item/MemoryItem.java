package com.pryzmm.splitself.item;

import com.pryzmm.splitself.SplitSelf;
import com.pryzmm.splitself.data.WorldData;
import com.pryzmm.splitself.sound.ModSounds;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import java.util.List;

public class MemoryItem extends Item {

    public record MemoryItemData(String image) {}

    private final MemoryItemData data;

    public MemoryItem(Settings settings, MemoryItemData data) {
        super(settings);
        this.data = data;
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("item.splitself.memory." + data.image + ".desc").formatted(Formatting.GRAY));
    }

    @SuppressWarnings("DataFlowIssue")
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        if (!WorldData.getUnlockedMemories().contains(data.image)) {
            user.playSound(ModSounds.MEMORY, 1f, 1f);
            if (!world.isClient) {
                WorldData.addUnlockedMemory(data.image);
                itemStack.decrement(1);
                MinecraftServer server = MinecraftClient.getInstance().getServer();
                server.getPlayerManager().broadcast(Text.translatable("item.splitself.memory.use"), false);
            }
            user.incrementStat(Stats.USED.getOrCreateStat(this));
        }
        return TypedActionResult.success(itemStack, world.isClient());
    }

    @Override
    public Text getName(ItemStack stack) {
        return SplitSelf.translate("item.splitself.memory");
    }

}
