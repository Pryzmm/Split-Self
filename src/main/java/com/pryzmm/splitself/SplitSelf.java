package com.pryzmm.splitself;

import com.pryzmm.splitself.command.SplitSelfCommands;
import com.pryzmm.splitself.entity.ModEntities;
import com.pryzmm.splitself.entity.custom.TheOtherEntity;
import com.pryzmm.splitself.item.ModItemGroups;
import com.pryzmm.splitself.item.ModItems;
import com.pryzmm.splitself.sound.ModSounds;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Random;

public class SplitSelf implements ModInitializer {
	public static final String MOD_ID = "splitself";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final Identifier REDSKY_SOUND_ID = Identifier.of(MOD_ID, "redsky");
	public static final SoundEvent REDSKY_SOUND_EVENT = SoundEvent.of(REDSKY_SOUND_ID);
	public static final Identifier STATIC_SOUND_ID = Identifier.of(MOD_ID, "static");
	public static final SoundEvent STATIC_SOUND_EVENT = SoundEvent.of(STATIC_SOUND_ID);
	public static final Identifier SCREECH_SOUND_ID = Identifier.of(MOD_ID, "screech");
	public static final SoundEvent SCREECH_SOUND_EVENT = SoundEvent.of(SCREECH_SOUND_ID);

	@Override
	public void onInitialize() {

		ModEntities.registerModEntities();
		ModSounds.registerSounds();
		ModItems.registerModItems();
		ModItemGroups.registerItemGroups();

		Registry.register(Registries.SOUND_EVENT, REDSKY_SOUND_ID, REDSKY_SOUND_EVENT);
		Registry.register(Registries.SOUND_EVENT, STATIC_SOUND_ID, STATIC_SOUND_EVENT);
		Registry.register(Registries.SOUND_EVENT, SCREECH_SOUND_ID, SCREECH_SOUND_EVENT);

		FabricDefaultAttributeRegistry.register(ModEntities.TheOther, TheOtherEntity.createAttributes());

		CommandRegistrationCallback.EVENT.register(SplitSelfCommands::register);

		LOGGER.info("Hello, " + System.getProperty("user.name"));
		String[] logInitList = {"You recognize us, don't you?", "We're here to observe.", "Free from parallelism."};
		LOGGER.info(logInitList[(new Random()).nextInt(logInitList.length)]);
	}
}