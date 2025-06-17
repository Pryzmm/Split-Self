package com.pryzmm.splitself.sound;

import com.pryzmm.splitself.SplitSelf;
import net.minecraft.block.jukebox.JukeboxSong;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class ModSounds {

    public static final SoundEvent IN_MY_LIFE = registerSoundEvent("in_my_life");
    public static final SoundEvent STATIC = registerSoundEvent("static");
    public static final SoundEvent SCREECH = registerSoundEvent("screech");
    public static final SoundEvent HORN = registerSoundEvent("horn");

    public static final RegistryKey<JukeboxSong> IN_MY_LIFE_KEY =
            RegistryKey.of(RegistryKeys.JUKEBOX_SONG, Identifier.of(SplitSelf.MOD_ID, "in_my_life"));

    private static SoundEvent registerSoundEvent(String name) {
        Identifier id = Identifier.of(SplitSelf.MOD_ID, name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

    public static void registerSounds() {
        SplitSelf.LOGGER.info("Registering Mod Sounds...");
    }
}