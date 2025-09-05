package com.pryzmm.splitself;

import com.pryzmm.splitself.block.ModBlocks;
import com.pryzmm.splitself.command.SplitSelfCommands;
import com.pryzmm.splitself.events.*;
import com.pryzmm.splitself.file.JsonReader;
import com.pryzmm.splitself.dimension.LimboLevitation;
import com.pryzmm.splitself.entity.ModEntities;
import com.pryzmm.splitself.entity.custom.TheOtherEntity;
import com.pryzmm.splitself.file.BackgroundManager;
import com.pryzmm.splitself.item.ModItemGroups;
import com.pryzmm.splitself.item.ModItems;
import com.pryzmm.splitself.screen.WarningScreen;
import com.pryzmm.splitself.sound.ModSounds;
import com.pryzmm.splitself.world.DataTracker;
import com.pryzmm.splitself.world.DimensionRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.modogthedev.client.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Random;

public class SplitSelf implements ModInitializer {
	public static final String MOD_ID = "splitself";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private void onServerStarted(MinecraftServer server) {
		ServerWorld limboWorld = server.getWorld(DimensionRegistry.LIMBO_DIMENSION_KEY);
		if (limboWorld != null) {
			StructureManager.placeStructureRandomRotation(
					limboWorld,
					new BlockPos(0, 0, 0),
					"house_empty",
					0,
					0,
					true,
                    1f,
                    false
			);
            StructureManager.placeStructureRandomRotation(
                    limboWorld,
                    new BlockPos(1000, 0, 0),
                    "memory",
                    0,
                    0,
                    true,
                    1f,
                    false
            );
            StructureManager.placeStructureRandomRotation(
                    limboWorld,
                    new BlockPos(2000, 0, 0),
                    "broken_memory",
                    0,
                    0,
                    true,
                    1f,
                    false
            );
		}
	}

	public static Text translate(String translateKey, Object... args) { // makes it easier on me
		return Text.translatable(translateKey, args);
	}

	@Override
	public void onInitialize() {

        MinecraftClient client = MinecraftClient.getInstance();

        JsonReader config = new JsonReader("splitself.json5");

		ModEntities.registerModEntities();
		ModSounds.registerSounds();
        ModBlocks.registerModBlocks();
		ModItems.registerModItems();
		ModItemGroups.registerItemGroups();
		DimensionRegistry.register();
		ServerLifecycleEvents.SERVER_STARTED.register(this::onServerStarted);

		ServerTickEvents.END_SERVER_TICK.register(EventManager::onTick);
        ServerTickEvents.END_SERVER_TICK.register(LimboLevitation::onTick);
        if (FabricLoader.getInstance().isModLoaded("voicelib") && FabricLoader.getInstance().isModLoaded("architectury")) {
            EventHandler.loadVoskModel(JsonReader.getString("voskModel"));
            MicrophoneReader.register();
            System.out.println("Registering microphone reader");
        } else {
            System.out.println("Cannot register microphone reader, missing voicelib or architectury");
        }

		FabricDefaultAttributeRegistry.register(ModEntities.TheOther, TheOtherEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.TheForgotten, TheOtherEntity.createAttributes());

		CommandRegistrationCallback.EVENT.register(SplitSelfCommands::register);

        ClientLifecycleEvents.CLIENT_STOPPING.register(minecraftClient -> {
            if (BackgroundManager.getUserBackground() != null &&
                    Objects.equals(BackgroundManager.getCurrentBackground(), BackgroundManager.getModBackground())) {
                BackgroundManager.restoreUserBackground();
            }
        });

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (BackgroundManager.getUserBackground() != null &&
                    Objects.equals(BackgroundManager.getCurrentBackground(), BackgroundManager.getModBackground())) {
                BackgroundManager.restoreUserBackground();
            }
        }));

		ServerMessageEvents.CHAT_MESSAGE.register((message, messageSender, params) -> {
			EventManager.runChatEvent(messageSender, message.getContent().getString(), false);
		});


		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			ServerPlayerEntity player = handler.getPlayer();
			DataTracker joinTracker = DataTracker.getServerState(server);
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
            if (player.getWorld() == player.getServer().getWorld(DimensionRegistry.LIMBO_DIMENSION_KEY)) {
                player.teleport(player.getServer().getWorld(player.getSpawnPointDimension()), player.getSpawnPointPosition().getX(), player.getSpawnPointPosition().getY() + 0.5625, player.getSpawnPointPosition().getZ(), null, 0, 0);
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

    public static boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");
    }

	private static String getString(SignedMessage message) {
		return message.getContent().getString();
	}
}