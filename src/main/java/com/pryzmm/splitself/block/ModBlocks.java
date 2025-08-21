package com.pryzmm.splitself.block;

import com.pryzmm.splitself.SplitSelf;
import com.pryzmm.splitself.block.entity.ModBlockEntities;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

public class ModBlocks {

    public static final Block IMAGE_FRAME = registerBlock("image_frame",
            new ImageFrameBlock(AbstractBlock.Settings.create().breakInstantly().sounds(BlockSoundGroup.WOOD).nonOpaque().burnable()));

    private static Block registerBlock(String name, Block block) {
        registerBlockItem(name, block);
        return Registry.register(Registries.BLOCK, Identifier.of(SplitSelf.MOD_ID, name), block);
    }

    private static void registerBlockItem(String name, Block block) {
        Registry.register(Registries.ITEM, Identifier.of(SplitSelf.MOD_ID, name),
                new BlockItem(block, new Item.Settings()));
    }

    public static void registerModBlocks() {
        SplitSelf.LOGGER.info("Registering frame block...");

        ModBlockEntities.registerBlockEntities();

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.BUILDING_BLOCKS).register(entries -> {
            entries.add(IMAGE_FRAME);
        });
    }
}
