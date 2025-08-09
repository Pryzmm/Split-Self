package com.pryzmm.splitself.item;

import com.pryzmm.splitself.SplitSelf;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ModItemGroups {
    public static final ItemGroup SplitSelfItemsGroup = Registry.register(Registries.ITEM_GROUP, Identifier.of(SplitSelf.MOD_ID, "split_self_items"),
            FabricItemGroup.builder().icon(() -> new ItemStack(ModItems.IN_MY_LIFE_MUSIC_DISC))
                    .displayName(Text.translatable("itemgroup.splitself.splitself_items"))
                    .entries((displayContext, entries) -> {
                        entries.add(ModItems.IN_MY_LIFE_MUSIC_DISC);
                        entries.add(ModItems.DIET_COKE);
                    })
                    .build());

    public static void registerItemGroups() {
        SplitSelf.LOGGER.info("Registering Item Groups...");
    }
}
