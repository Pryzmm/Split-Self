package com.pryzmm.splitself.command;

import com.igrium.videolib.api.VideoHandle;
import com.igrium.videolib.api.VideoHandleFactory;
import com.igrium.videolib.render.VideoScreen;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.pryzmm.splitself.SplitSelf;
import com.pryzmm.splitself.client.SplitSelfClient;
import com.pryzmm.splitself.block.functions.EmptyTeleportBlockFunc;
import com.pryzmm.splitself.data.PersistentData;
import com.pryzmm.splitself.data.WorldData;
import com.pryzmm.splitself.entity.ModEntities;
import com.pryzmm.splitself.entity.custom.TheForgottenEntity;
import com.pryzmm.splitself.events.*;
import com.pryzmm.splitself.file.ZipFunc;
import com.pryzmm.splitself.func.StripMine;
import com.pryzmm.splitself.screen.WarningScreen;
import com.pryzmm.splitself.screen.misc.BlendManager;
import com.pryzmm.splitself.world.DeadCoralChunkGenerator;
import com.pryzmm.splitself.world.DimensionRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import java.io.IOException;

public class SplitSelfCommands {

    private static final SuggestionProvider<ServerCommandSource> EVENT_SUGGESTIONS =
        (context, builder) -> {
            if ("random".startsWith(builder.getRemaining().toLowerCase())) builder.suggest("random");
            for (EventManager.Events event : EventManager.Events.values()) {
                String eventName = event.name().toLowerCase();
                if (eventName.startsWith(builder.getRemaining().toLowerCase())) builder.suggest(eventName);
            }
            return builder.buildFuture();
        };

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        MinecraftClient client = MinecraftClient.getInstance();

        dispatcher.register(CommandManager.literal("splitself")
            .executes(context -> {
                context.getSource().sendFeedback(() -> Text.literal("<" + context.getSource().getName() + "> " + SplitSelf.translate("command.splitself.empty_command").getString()), false);
                return 1;
            })
            .then(CommandManager.literal("information")
                .executes(context -> {
                    client.execute(() -> client.setScreen(new WarningScreen()));
                    return 1;
                })
            )
            .then(CommandManager.literal("runEvent")
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.argument("event", StringArgumentType.word())
                    .suggests(EVENT_SUGGESTIONS)
                    .executes(context -> {
                        String eventArg = StringArgumentType.getString(context, "event").toLowerCase();
                        ServerWorld world = context.getSource().getWorld();
                        ServerPlayerEntity player = context.getSource().getPlayer();

                        if (eventArg.equalsIgnoreCase("random")) {
                            EventManager.triggerRandomEvent(world, player, null);
                        } else {
                            try {
                                EventManager.Events event = EventManager.Events.valueOf(eventArg.toUpperCase());
                                EventManager.triggerRandomEvent(world, player, event);
                            } catch (IllegalArgumentException e) {
                                context.getSource().sendFeedback(() -> Text.literal("<" + context.getSource().getName() + "> " + SplitSelf.translate("command.splitself.invalid_value").getString()), false);
                            }
                        }
                        return 1;
                    })
                )
            )
            .then(CommandManager.literal("debug")
                .then(CommandManager.literal("debugFullscreen")
                    .requires(source -> source.hasPermissionLevel(2))
                    .executes(context -> {
                        if (client.options.getFullscreen().getValue()) {
                            context.getSource().sendFeedback(() -> Text.literal("In fullscreen"), false);
                        } else {
                            context.getSource().sendFeedback(() -> Text.literal("NOT in fullscreen"), false);
                        }
                        return 1;
                    })
                )
                .then(CommandManager.literal("debugSendVec3d")
                    .requires(source -> source.hasPermissionLevel(2))
                    .executes(context -> {
                        ServerPlayerEntity player = context.getSource().getPlayer();
                        if (player != null) {
                            Vec3d vec3d = new Vec3d(player.getPos().x, 319, player.getPos().z);
                            player.sendMessageToClient(Text.literal(EventManager.moveVectorFromBase(player, vec3d).toString()), false);
                        }
                        return 1;
                    })
                )
                .then(CommandManager.literal("debugInvert")
                    .requires(source -> source.hasPermissionLevel(2))
                    .executes(context -> {
                        ServerPlayerEntity player = context.getSource().getPlayer();
                        if (player != null) BlendManager.modifyBlend = !BlendManager.modifyBlend;
                        return 1;
                    })
                )
                .then(CommandManager.literal("debugVideo")
                    .then(CommandManager.argument("video", StringArgumentType.greedyString())
                        .requires(source -> source.hasPermissionLevel(2))
                        .executes(context -> {
                            MinecraftClient mc = MinecraftClient.getInstance();
                            mc.execute(() -> {
                                try {
                                    VideoHandleFactory factory = SplitSelfClient.videoManager.getVideoHandleFactory();
                                    VideoHandle idHandle = factory.getVideoHandle(ZipFunc.getVideo(StringArgumentType.getString(context, "video")).toURI().toURL());
                                    VideoScreen screen = new VideoScreen(SplitSelfClient.videoPlayer);
                                    mc.setScreen(screen);
                                    SplitSelfClient.videoPlayer.getMediaInterface().play(idHandle);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                            return 1;
                        })
                    )
                )
                .then(CommandManager.literal("debugEmpty")
                    .requires(source -> source.hasPermissionLevel(2))
                    .executes(context -> {
                        ServerPlayerEntity player = context.getSource().getPlayer();
                        if (player != null) {
                            ServerWorld emptyWorld = player.getServer().getWorld(DimensionRegistry.EMPTINESS_DIMENSION_KEY);
                            BlockPos pos = DeadCoralChunkGenerator.findGroundPos(0, 0);
                            EmptyTeleportBlockFunc.updateLastLocation(player);
                            PersistentData.setPanoramaStage("empty");
                            player.teleport(emptyWorld, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, null, 0, 0);
                        }
                        return 1;
                    })
                )
                .then(CommandManager.literal("debugStripMine")
                    .requires(source -> source.hasPermissionLevel(2))
                    .executes(context -> {
                        ServerPlayerEntity player = context.getSource().getPlayer();
                        if (player != null) {
                            ServerWorld world = player.getServerWorld();
                            BlockPos pos = StripMine.getEntitySpawnLocation(player);
                            TheForgottenEntity theForgotten = new TheForgottenEntity(ModEntities.TheForgotten, world, TheForgottenEntity.Type.DISAPPEAR);
                            theForgotten.refreshPositionAndAngles(pos, 360.0F, 0.0F);
                            world.spawnEntity(theForgotten);
                        }
                        return 1;
                    })
                )
                .then(CommandManager.literal("debugMemories")
                    .requires(source -> source.hasPermissionLevel(2))
                    .executes(context -> {
                        context.getSource().sendFeedback(() -> Text.literal(String.valueOf(WorldData.getUnlockedMemories())), false);
                        return 1;
                    })
                    .then(CommandManager.argument("memory", StringArgumentType.string())
                        .executes(context -> {
                            String memory = StringArgumentType.getString(context, "memory");
                            WorldData.addUnlockedMemory(memory);
                            context.getSource().sendFeedback(() -> Text.literal("Updated Memory List"), false);
                            return 1;
                        })
                    )
                )
                .then(CommandManager.literal("debugSleepStage")
                    .requires(source -> source.hasPermissionLevel(2))
                    .executes(context -> {
                        context.getSource().sendFeedback(() -> Text.literal(String.valueOf(WorldData.getSleepStage())), false);
                        return 1;
                    })
                    .then(CommandManager.argument("stage", IntegerArgumentType.integer(0))
                        .executes(context -> {
                            int stage = IntegerArgumentType.getInteger(context, "stage");
                            WorldData.setSleepStage(stage);
                            context.getSource().sendFeedback(() -> Text.literal(String.valueOf(WorldData.getSleepStage())), false);
                            return 1;
                        })
                    )
                )
            )
        );
    }
}
