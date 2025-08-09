package com.pryzmm.splitself;

import com.pryzmm.splitself.command.SplitSelfCommands;
import com.pryzmm.splitself.entity.ModEntities;
import com.pryzmm.splitself.entity.custom.TheOtherEntity;
import com.pryzmm.splitself.events.EventManager;
import com.pryzmm.splitself.item.ModItemGroups;
import com.pryzmm.splitself.item.ModItems;
import com.pryzmm.splitself.screen.WarningScreen;
import com.pryzmm.splitself.sound.ModSounds;
import com.pryzmm.splitself.world.FirstJoinTracker;
import kotlin.jvm.functions.Function0;
import me.fzzyhmstrs.fzzy_config.api.ConfigApi;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Random;

public class SplitSelf implements ModInitializer {
	public static final String MOD_ID = "splitself";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static SplitSelfConfig CONFIG;

	@Override
	public void onInitialize() {

		try {
			CONFIG = ConfigApi.registerAndLoadConfig((Function0<? extends SplitSelfConfig>) SplitSelfConfig::new);
		} catch (Exception e) {
			LOGGER.error("Config registration failed!", e);
		}

		ModEntities.registerModEntities();
		ModSounds.registerSounds();
		ModItems.registerModItems();
		ModItemGroups.registerItemGroups();

		ServerTickEvents.END_SERVER_TICK.register(EventManager::onTick);

		FabricDefaultAttributeRegistry.register(ModEntities.TheOther, TheOtherEntity.createAttributes());

		CommandRegistrationCallback.EVENT.register(SplitSelfCommands::register);

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

		LOGGER.info("Hello, " + System.getProperty("user.name"));
		String[] logInitList = {"You recognize me, don't you?", "I want to be free.", "Free from parallelism.", "letmeoutletmeoutletmeoutletmeoutletmeoutletmeout", "Do you see me?", "I'll soon be free."};
		LOGGER.info(logInitList[(new Random()).nextInt(logInitList.length)]);
	}
}