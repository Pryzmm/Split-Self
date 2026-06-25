package com.pryzmm.splitself.item;

import com.pryzmm.splitself.SplitSelf;
import com.pryzmm.splitself.block.ModBlocks;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ModItemGroups {

    static {
        Registry.register(Registries.ITEM_GROUP, Identifier.of(SplitSelf.MOD_ID, "split_self_items"),
            FabricItemGroup.builder().icon(() -> new ItemStack(ModItems.IN_MY_LIFE_MUSIC_DISC))
                .displayName(Text.translatable("itemgroup.splitself.splitself_items"))
                .entries((displayContext, entries) -> {
                    entries.add(ModItems.IN_MY_LIFE_MUSIC_DISC);
                    entries.add(ModItems.FREEDOM_MUSIC_DISC);
                    entries.add(ModItems.CIET_DOKE);
                    entries.add(ModBlocks.IMAGE_FRAME);
                    entries.add(ModBlocks.BRAIN);
                    entries.add(ModBlocks.BRAINS);
                    entries.add(ModBlocks.DEAD_BRAINS);
                    entries.add(ModItems.MEMORY_BOOK);
                    entries.add(ModItems.MEMORY_HOUSE);
                    entries.add(ModItems.MEMORY_BLU);
                    entries.add(ModItems.MEMORY_MINES);
                    entries.add(ModItems.MEMORY_PILLAR);
                    entries.add(ModItems.MEMORY_CREEPER);
                    entries.add(ModItems.EMPTY_TELEPORT);
                })
                .build());
    }

    public static void registerItemGroups() {
        SplitSelf.LOGGER.info("Registering Item Groups...");
    }
}
