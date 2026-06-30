package com.pryzmm.splitself;

import com.pryzmm.splitself.block.ModBlocks;
import com.pryzmm.splitself.command.SplitSelfCommands;
import com.pryzmm.splitself.config.DefaultConfig;
import com.pryzmm.splitself.data.PersistentData;
import com.pryzmm.splitself.data.WorldData;
import com.pryzmm.splitself.entity.TheForgottenFunc;
import com.pryzmm.splitself.events.*;
import com.pryzmm.splitself.events.helper.StructureManager;
import com.pryzmm.splitself.file.JsonReader;
import com.pryzmm.splitself.func.StripMine;
import com.pryzmm.splitself.packet.ServerPacketHandler;
import com.pryzmm.splitself.world.LimboLevitation;
import com.pryzmm.splitself.entity.ModEntities;
import com.pryzmm.splitself.entity.custom.TheOtherEntity;
import com.pryzmm.splitself.file.BackgroundManager;
import com.pryzmm.splitself.item.ModItemGroups;
import com.pryzmm.splitself.item.ModItems;
import com.pryzmm.splitself.screen.WarningScreen;
import com.pryzmm.splitself.sound.ModSounds;
import com.pryzmm.splitself.world.DimensionRegistry;
import com.pryzmm.splitself.world.structure.StructurePieces;
import com.pryzmm.splitself.world.structure.Structures;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import com.pryzmm.client.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Objects;
import java.util.Random;

public class SplitSelf implements ModInitializer {
	public static final String MOD_ID = "splitself";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static JsonReader CONFIG = null;

	private void onServerStarted(MinecraftServer server) {
        WorldData.loadData(server.getOverworld());
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
            StructureManager.placeStructureRandomRotation(
                    limboWorld,
                    new BlockPos(3000, 0, 0),
                    "meadow",
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
    public static boolean ShriekInstalled = false;

    private static int nextForgottenSpawn = 600;

	@Override
	public void onInitialize() {

        MinecraftClient client = MinecraftClient.getInstance();

        DefaultConfig.createDefaultConfigs();
        CONFIG = new JsonReader("splitself.json5", true);

		ModEntities.registerModEntities();
		ModSounds.registerSounds();
        ModBlocks.registerModBlocks();
		ModItems.registerModItems();
		ModItemGroups.registerItemGroups();
		DimensionRegistry.register();
		Structures.register();
		StructurePieces.register();
		ServerLifecycleEvents.SERVER_STARTED.register(this::onServerStarted);
        PersistentData.loadData();

        ServerPacketHandler.register();

		ServerTickEvents.END_SERVER_TICK.register(EventManager::onTick);
        ServerTickEvents.END_SERVER_TICK.register(TheForgottenFunc::removeIfRayCasted);
        ServerTickEvents.END_SERVER_TICK.register(StripMine::trySpawnAttempt);
        ServerTickEvents.END_SERVER_TICK.register(LimboLevitation::onTick);
        if (FabricLoader.getInstance().isModLoaded("shriek") && FabricLoader.getInstance().isModLoaded("architectury")) {
            EventHandler.loadVoskModel(CONFIG.getString("voskModel"));
            MicrophoneReader.register();
            SplitSelf.LOGGER.info("Registered MicrophoneReader...");
            ShriekInstalled = true;
        } else SplitSelf.LOGGER.warn("Cannot register microphone reader, missing shriek or architectury");

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

		ServerMessageEvents.CHAT_MESSAGE.register((message, messageSender, params) -> EventManager.runChatEvent(messageSender, message.getContent().getString(), false));

        ServerTickEvents.END_WORLD_TICK.register(world -> {
            if (world == world.getServer().getWorld(DimensionRegistry.EMPTINESS_DIMENSION_KEY)) {
                nextForgottenSpawn--;
                if (nextForgottenSpawn == 0) {
                    TheForgottenFunc.tryRandomSpawn(world);
                    nextForgottenSpawn = 600;
                }
            }
        });

        ClientPlayConnectionEvents.JOIN.register((handler, sender, mc) -> {
            ClientPlayerEntity player = mc.player;
            assert player != null;
            if (!WorldData.getJoinedPlayers().contains(player.getUuid())) {
                WorldData.updateJoinedPlayers(player.getUuid());
                client.execute(() -> client.setScreen(new WarningScreen()));
            }
        });

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			ServerPlayerEntity player = handler.getPlayer();
            if (player.getWorld() == server.getWorld(DimensionRegistry.LIMBO_DIMENSION_KEY)) {
                if (player.getSpawnPointPosition() == null) {
                    ServerWorld world = server.getOverworld();
                    player.teleport(world, world.getSpawnPos().getX(), world.getSpawnPos().getY(), world.getSpawnPos().getZ(), 0, 0);
                } else {
                    player.teleport(server.getWorld(player.getSpawnPointDimension()), player.getSpawnPointPosition().getX(), player.getSpawnPointPosition().getY() + 0.5625, player.getSpawnPointPosition().getZ(), null, 0, 0);
                }
            }
		});

        LOGGER.info("Hello, {}", System.getProperty("user.name"));
		String[] logInitList = {"You recognize me, don't you?", "I want to be free.", "Free from parallelism.", "letmeoutletmeoutletmeoutletmeoutletmeoutletmeout", "Do you see me?", "I'll soon be free."};
		LOGGER.info(logInitList[(new Random()).nextInt(logInitList.length)]);
	}

}