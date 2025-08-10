package com.pryzmm.splitself;

import com.pryzmm.splitself.command.SplitSelfCommands;
import com.pryzmm.splitself.config.SplitSelfConfig;
import com.pryzmm.splitself.entity.ModEntities;
import com.pryzmm.splitself.entity.custom.TheOtherEntity;
import com.pryzmm.splitself.events.EventManager;
import com.pryzmm.splitself.file.BackgroundManager;
import com.pryzmm.splitself.item.ModItemGroups;
import com.pryzmm.splitself.item.ModItems;
import com.pryzmm.splitself.screen.WarningScreen;
import com.pryzmm.splitself.sound.ModSounds;
import com.pryzmm.splitself.world.FirstJoinTracker;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Random;

public class SplitSelf implements ModInitializer {
	public static final String MOD_ID = "splitself";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

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

		ServerTickEvents.END_SERVER_TICK.register(EventManager::onTick);

		FabricDefaultAttributeRegistry.register(ModEntities.TheOther, TheOtherEntity.createAttributes());

		CommandRegistrationCallback.EVENT.register(SplitSelfCommands::register);

        ClientLifecycleEvents.CLIENT_STOPPING.register(minecraftClient -> {
            if (BackgroundManager.getUserBackground() != null &&
                    Objects.equals(BackgroundManager.getCurrentBackground(), BackgroundManager.getModBackground())) {
                BackgroundManager.restoreUserBackground();
            }
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
			System.out.println("Events:" + SplitSelfConfig.getInstance().isEventsEnabled());
		});

		LOGGER.info("Hello, " + System.getProperty("user.name"));
		String[] logInitList = {"You recognize me, don't you?", "I want to be free.", "Free from parallelism.", "letmeoutletmeoutletmeoutletmeoutletmeoutletmeout", "Do you see me?", "I'll soon be free."};
		LOGGER.info(logInitList[(new Random()).nextInt(logInitList.length)]);
	}
}