package com.pryzmm.splitself.item;

import com.pryzmm.splitself.SplitSelf;
import com.pryzmm.splitself.sound.ModSounds;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.client.sound.Sound;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvents;
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

    private static Item registerItem(String name, Item item) { 
        return Registry.register(Registries.ITEM, Identifier.of(SplitSelf.MOD_ID, name), item);
    }

    public static void registerModItems() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> {
            entries.add(IN_MY_LIFE_MUSIC_DISC);
            entries.add(FREEDOM_MUSIC_DISC);
        });
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FOOD_AND_DRINK).register(entries -> {
            entries.add(CIET_DOKE);
        });
    }
}