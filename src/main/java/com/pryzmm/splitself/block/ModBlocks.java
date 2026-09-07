package com.pryzmm.splitself.block;

import com.pryzmm.splitself.SplitSelf;
import com.pryzmm.splitself.block.entity.ModBlockEntities;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

public class ModBlocks {

    public static final Block IMAGE_FRAME = registerBlock("image_frame",
        new ImageFrameBlock(AbstractBlock.Settings.create().breakInstantly().sounds(BlockSoundGroup.WOOD).nonOpaque().burnable()));

    public static final Block BRAIN = registerBlock("brain",
        new BrainBlock(AbstractBlock.Settings.create().breakInstantly().sounds(BlockSoundGroup.HONEY)));

    public static final Block BRAINS = registerBlock("brains",
        new Block(AbstractBlock.Settings.create().sounds(BlockSoundGroup.HONEY).hardness(3.0f).resistance(3.0f)));

    public static final Block DARKNESS = registerBlock("darkness",
        new Block(AbstractBlock.Settings.create().sounds(BlockSoundGroup.INTENTIONALLY_EMPTY).hardness(10000.0f).resistance(10000.0f)));

    public static final Block DEAD_BRAINS = registerBlock("dead_brains",
        new Block(AbstractBlock.Settings.create().sounds(BlockSoundGroup.HONEY).hardness(3.0f).resistance(3.0f)));

    public static final Block COMPUTER = registerBlock("computer",
        new ComputerBlock(AbstractBlock.Settings.create().sounds(BlockSoundGroup.METAL).nonOpaque().hardness(3.0f).resistance(3.0f)));

    public static final Block KEYBOARD = registerBlock("keyboard",
        new KeyboardBlock(AbstractBlock.Settings.create().sounds(BlockSoundGroup.METAL).nonOpaque().hardness(3.0f).resistance(3.0f)));

    public static final Block TABLE = registerBlock("table",
        new TableBlock(AbstractBlock.Settings.create().sounds(BlockSoundGroup.WOOD).nonOpaque().hardness(3.0f).resistance(3.0f)));

    public static final Block EXIT_DOOR = registerBlock("exit_door",
        new ExitDoorBlock(AbstractBlock.Settings.create().sounds(BlockSoundGroup.METAL).nonOpaque().hardness(3.0f).resistance(3.0f)));

    public static final Block TRIGGER_TRANSITION = registerBlock("dev_trigger_transition",
        new TriggerTransitionBlock(AbstractBlock.Settings.create().nonOpaque().noCollision().hardness(10000.0f).resistance(10000.0f)));

    public static final Block BROKEN_FLOWER = registerBlock("broken_flower",
        new BrokenFlowerBlock(AbstractBlock.Settings.create().nonOpaque().noCollision().hardness(10000.0f).resistance(10000.0f)));

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
    }
}
