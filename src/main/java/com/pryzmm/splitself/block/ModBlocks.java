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

    public static final Block EMPTY_TELEPORT = registerBlock("empty_teleport",
        new EmptyTeleportBlock(AbstractBlock.Settings.create().nonOpaque().noCollision()));

    public static final Block BRAIN = registerBlock("brain",
        new BrainBlock(AbstractBlock.Settings.create().breakInstantly().sounds(BlockSoundGroup.HONEY)));

    public static final Block BRAINS = registerBlock("brains",
            new BrainsBlock(AbstractBlock.Settings.create().sounds(BlockSoundGroup.HONEY).hardness(3.0f).resistance(3.0f)));

    public static final Block DEAD_BRAINS = registerBlock("dead_brains",
            new DeadBrainsBlock(AbstractBlock.Settings.create().sounds(BlockSoundGroup.HONEY).hardness(3.0f).resistance(3.0f)));

    private static Block registerBlock(String name, Block block) {
        registerBlockItem(name, block);
        return Registry.register(Registries.BLOCK, Identifier.of(SplitSelf.MOD_ID, name), block);
    }

    private static void registerBlockItem(String name, Block block) {
        Registry.register(Registries.ITEM, Identifier.of(SplitSelf.MOD_ID, name), new BlockItem(block, new Item.Settings()));
    }

    public static void registerModBlocks() {
        SplitSelf.LOGGER.info("Registering blocks...");

        ModBlockEntities.registerBlockEntities();

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries -> entries.add(IMAGE_FRAME));
    }
}
