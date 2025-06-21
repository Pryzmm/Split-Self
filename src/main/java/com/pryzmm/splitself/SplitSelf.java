package com.pryzmm.splitself;

import com.pryzmm.splitself.command.SplitSelfCommands;
import com.pryzmm.splitself.entity.ModEntities;
import com.pryzmm.splitself.entity.custom.TheOtherEntity;
import com.pryzmm.splitself.events.StructureManager;
import com.pryzmm.splitself.item.ModItemGroups;
import com.pryzmm.splitself.item.ModItems;
import com.pryzmm.splitself.sound.ModSounds;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Random;

public class SplitSelf implements ModInitializer {
	public static final String MOD_ID = "splitself";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final Identifier REDSKY_SOUND_ID = Identifier.of(MOD_ID, "redsky");
	public static final SoundEvent REDSKY_SOUND_EVENT = SoundEvent.of(REDSKY_SOUND_ID);
	public static final Identifier STATIC_SOUND_ID = Identifier.of(MOD_ID, "static");
	public static final SoundEvent STATIC_SOUND_EVENT = SoundEvent.of(STATIC_SOUND_ID);
	public static final Identifier SCREECH_SOUND_ID = Identifier.of(MOD_ID, "screech");
	public static final SoundEvent SCREECH_SOUND_EVENT = SoundEvent.of(SCREECH_SOUND_ID);
	public static final Identifier HORN_SOUND_ID = Identifier.of(MOD_ID, "horn");
	public static final SoundEvent HORN_SOUND_EVENT = SoundEvent.of(HORN_SOUND_ID);
	public static final Identifier STATICSCREAM_SOUND_ID = Identifier.of(MOD_ID, "staticscream");
	public static final SoundEvent STATICSCREAM_SOUND_EVENT = SoundEvent.of(STATICSCREAM_SOUND_ID);

	@Override
	public void onInitialize() {

		ModEntities.registerModEntities();
		ModSounds.registerSounds();
		ModItems.registerModItems();
		ModItemGroups.registerItemGroups();

		FabricDefaultAttributeRegistry.register(ModEntities.TheOther, TheOtherEntity.createAttributes());

		CommandRegistrationCallback.EVENT.register(SplitSelfCommands::register);

		LOGGER.info("Hello, " + System.getProperty("user.name"));
		String[] logInitList = {"You recognize us, don't you?", "We're here to observe.", "Free from parallelism."};
		LOGGER.info(logInitList[(new Random()).nextInt(logInitList.length)]);
	}
}