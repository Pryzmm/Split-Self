package com.pryzmm.splitself;

import com.pryzmm.splitself.command.SplitSelfCommands;
import com.pryzmm.splitself.config.SplitSelfConfig;
import com.pryzmm.splitself.entity.ModEntities;
import com.pryzmm.splitself.entity.custom.TheOtherEntity;
import com.pryzmm.splitself.events.EventManager;
import com.pryzmm.splitself.events.SleepTracker;
import com.pryzmm.splitself.events.StructureManager;
import com.pryzmm.splitself.file.BackgroundManager;
import com.pryzmm.splitself.file.DesktopFileUtil;
import com.pryzmm.splitself.item.ModItemGroups;
import com.pryzmm.splitself.item.ModItems;
import com.pryzmm.splitself.screen.WarningScreen;
import com.pryzmm.splitself.sound.ModSounds;
import com.pryzmm.splitself.world.DimensionRegistry;
import com.pryzmm.splitself.world.FirstJoinTracker;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Random;

public class SplitSelf implements ModInitializer {
	public static final String MOD_ID = "splitself";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private void onServerStarted(MinecraftServer server) {
		// Debug: List all available dimensions
		LOGGER.info("Available dimensions:");
		server.getWorldRegistryKeys().forEach(key -> LOGGER.info("  - " + key));

		// Debug: Check if our dimension type exists
		var dimensionTypeRegistry = server.getRegistryManager().get(RegistryKeys.DIMENSION_TYPE);
		boolean dimensionTypeExists = dimensionTypeRegistry.contains(DimensionRegistry.LIMBO_DIMENSION_TYPE_KEY);
		LOGGER.info("Dimension type exists: " + dimensionTypeExists);

		// Debug: List all chunk generators
		var chunkGeneratorRegistry = server.getRegistryManager().get(RegistryKeys.CHUNK_GENERATOR);
		LOGGER.info("Available chunk generators:");
		chunkGeneratorRegistry.getIds().forEach(id -> LOGGER.info("  - " + id));

		ServerWorld limboWorld = server.getWorld(DimensionRegistry.LIMBO_DIMENSION_KEY);
		if (limboWorld != null) {
			LOGGER.info("Limbo dimension loaded successfully!");
			StructureManager.placeStructureRandomRotation(
					limboWorld,
					new BlockPos(0, 0, 0),
					"house_empty",
					0,
					0,
					true
			);
		} else {
			LOGGER.warn("Limbo dimension not found - check datapack files!");
			LOGGER.info("Expected dimension key: " + DimensionRegistry.LIMBO_DIMENSION_KEY);
		}
	}

	public static Text translate(String translateKey, Object... args) { // makes it easier on me
		return Text.translatable(translateKey, args);
	}

	@Override
	public void onInitialize() {

		// Initialize configuration - this will automatically load YACL config if available
		SplitSelfConfig.reload(); // Force a fresh load
		SplitSelfConfig config = SplitSelfConfig.getInstance();

		LOGGER.info("Configuration loaded. Values: eventsEnabled={}, eventTickInterval={}, eventChance={}, eventCooldown={}, startEventsAfter={}",
				config.isEventsEnabled(), config.getEventTickInterval(), config.getEventChance(),
				config.getEventCooldown(), config.getStartEventsAfter());

		ModEntities.registerModEntities();
		ModSounds.registerSounds();
		ModItems.registerModItems();
		ModItemGroups.registerItemGroups();
		DimensionRegistry.register();
		ServerLifecycleEvents.SERVER_STARTED.register(this::onServerStarted);

		ServerTickEvents.END_SERVER_TICK.register(EventManager::onTick);

		FabricDefaultAttributeRegistry.register(ModEntities.TheOther, TheOtherEntity.createAttributes());

		CommandRegistrationCallback.EVENT.register(SplitSelfCommands::register);

        ClientLifecycleEvents.CLIENT_STOPPING.register(minecraftClient -> {
            if (BackgroundManager.getUserBackground() != null &&
                    Objects.equals(BackgroundManager.getCurrentBackground(), BackgroundManager.getModBackground())) {
                BackgroundManager.restoreUserBackground();
            }
        });

		ServerMessageEvents.CHAT_MESSAGE.register((message, messageSender, params) -> {
			EventManager.runChatEvent(messageSender, message.getContent().getString());
		});


		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			ServerPlayerEntity player = handler.getPlayer();
			FirstJoinTracker joinTracker = FirstJoinTracker.getServerState(server);
			MinecraftClient client = MinecraftClient.getInstance();
			if (!joinTracker.hasJoinedBefore(player.getUuid())) {
				joinTracker.markAsJoined(player.getUuid());
				new Thread(() -> {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
					client.execute(() -> client.setScreen(new WarningScreen()));
				}).start();
			}
		});

		EntitySleepEvents.START_SLEEPING.register((entity, sleepingPos) -> {
			if (entity instanceof ServerPlayerEntity serverPlayer) {
				SleepTracker.startSleep(serverPlayer);
			}
		});

		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
				if (player.isSleeping()) {
					SleepTracker.updateSleep(player);
				}
			}
		});

		LOGGER.info("Hello, " + System.getProperty("user.name"));
		String[] logInitList = {"You recognize me, don't you?", "I want to be free.", "Free from parallelism.", "letmeoutletmeoutletmeoutletmeoutletmeoutletmeout", "Do you see me?", "I'll soon be free."};
		LOGGER.info(logInitList[(new Random()).nextInt(logInitList.length)]);
	}

	private static String getString(SignedMessage message) {
		return message.getContent().getString();
	}
}