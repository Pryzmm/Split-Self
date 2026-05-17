package com.pryzmm.splitself.item;

import com.pryzmm.splitself.SplitSelf;
import com.pryzmm.splitself.block.ModBlocks;
import com.pryzmm.splitself.sound.ModSounds;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

public class ModItems {

    public static final Item IN_MY_LIFE_MUSIC_DISC = registerItem("in_my_life_music_disc",
        new Item(new Item.Settings()
            .rarity(Rarity.RARE)
            .maxCount(1)
            .jukeboxPlayable(ModSounds.IN_MY_LIFE_KEY))
    );

    public static final Item FREEDOM_MUSIC_DISC = registerItem("freedom_music_disc",
        new Item(new Item.Settings()
            .rarity(Rarity.EPIC)
            .maxCount(1)
            .jukeboxPlayable(ModSounds.FREEDOM_KEY))
    );


    public static final FoodComponent CIET_DOKE_COMPONENT = new FoodComponent.Builder()
        .alwaysEdible()
        .nutrition(10)
        .build();

    public static final Item CIET_DOKE = registerItem(
        "ciet_doke",
        new Item(new Item.Settings()
            .food(CIET_DOKE_COMPONENT))
    );

    public static final Item EMPTY_TELEPORT = registerItem(
        "empty_teleport",
        new EmptyTeleportItem(ModBlocks.EMPTY_TELEPORT, new Item.Settings()
            .maxCount(1)
            .rarity(Rarity.EPIC)
            .fireproof())
    );

    public static final Item MEMORY_BOOK = registerItem(
        "memory_book",
        new MemoryBookItem(new Item.Settings()
            .maxCount(1)
            .rarity(Rarity.EPIC)
            .fireproof())
    );

    public static final Item MEMORY_HOUSE = registerItem(
        "memory_house",
        new MemoryItem(new Item.Settings()
            .maxCount(64)
            .rarity(Rarity.RARE)
            .fireproof(),
            new MemoryItem.MemoryItemData("house"))
    );

    public static final Item MEMORY_BLU = registerItem(
        "memory_blu",
        new MemoryItem(new Item.Settings()
            .maxCount(64)
            .rarity(Rarity.RARE)
            .fireproof(),
            new MemoryItem.MemoryItemData("blu"))
    );

    public static final Item MEMORY_MINES = registerItem(
        "memory_mines",
        new MemoryItem(new Item.Settings()
            .maxCount(64)
            .rarity(Rarity.RARE)
            .fireproof(),
            new MemoryItem.MemoryItemData("mines"))
    );

    public static final Item MEMORY_PILLAR = registerItem(
        "memory_pillar",
        new MemoryItem(new Item.Settings()
            .maxCount(64)
            .rarity(Rarity.RARE)
            .fireproof(),
            new MemoryItem.MemoryItemData("pillar"))
    );

    public static final Item MEMORY_CREEPER = registerItem(
        "memory_creeper",
        new MemoryItem(new Item.Settings()
            .maxCount(64)
            .rarity(Rarity.RARE)
            .fireproof(),
            new MemoryItem.MemoryItemData("creeper"))
    );

    private static Item registerItem(String name, Item item) { 
        return Registry.register(Registries.ITEM, Identifier.of(SplitSelf.MOD_ID, name), item);
    }

    public static void registerModItems() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> {
            entries.add(IN_MY_LIFE_MUSIC_DISC);
            entries.add(FREEDOM_MUSIC_DISC);
            entries.add(MEMORY_BOOK);
            entries.add(MEMORY_HOUSE);
            entries.add(MEMORY_BLU);
            entries.add(MEMORY_MINES);
            entries.add(MEMORY_PILLAR);
            entries.add(MEMORY_CREEPER);
        });
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FOOD_AND_DRINK).register(entries -> {
            entries.add(CIET_DOKE);
        });
    }
}