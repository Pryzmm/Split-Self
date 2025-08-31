package com.pryzmm.splitself.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.pryzmm.splitself.SplitSelf;
import com.pryzmm.splitself.events.*;
import com.pryzmm.splitself.screen.WarningScreen;
import com.pryzmm.splitself.world.DataTracker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

import java.util.concurrent.CompletableFuture;
import java.util.Arrays;

public class SplitSelfCommands {

    private static final SuggestionProvider<ServerCommandSource> MAIN_SUGGESTIONS =
            (context, builder) -> {
                String[] suggestions = {
                        "information",
                        "debugFullscreen",
                        "debugToggleEvents",
                        "debugSleepStage",
                        "runEvent"
                };

                for (String suggestion : suggestions) {
                    if (suggestion.toLowerCase().startsWith(builder.getRemaining().toLowerCase())) {
                        builder.suggest(suggestion);
                    }
                }
                return builder.buildFuture();
            };

    private static final SuggestionProvider<ServerCommandSource> EVENT_SUGGESTIONS =
            (context, builder) -> {
                if ("random".startsWith(builder.getRemaining().toLowerCase())) {
                    builder.suggest("random");
                }

                for (EventManager.Events event : EventManager.Events.values()) {
                    String eventName = event.name().toLowerCase();
                    if (eventName.startsWith(builder.getRemaining().toLowerCase())) {
                        builder.suggest(eventName);
                    }
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
                .then(CommandManager.literal("debugToggleEvents")
                        .requires(source -> source.hasPermissionLevel(2))
                        .executes(context -> {
                            DataTracker tracker = DataTracker.getServerState(client.getServer());
                            tracker.setPlayerReadWarning(client.player.getUuid(), !tracker.getPlayerReadWarning(client.player.getUuid()));
                            context.getSource().sendFeedback(() -> Text.literal(SplitSelf.translate("command.splitself.debug_toggle_warning", tracker.getPlayerReadWarning(client.player.getUuid())).getString()), false);
                            return 1;
                        })
                )
                .then(CommandManager.literal("debugSleepStage")
                        .requires(source -> source.hasPermissionLevel(2))
                        .executes(context -> {
                            DataTracker tracker = DataTracker.getServerState(client.getServer());
                            context.getSource().sendFeedback(() -> Text.literal(String.valueOf(tracker.getPlayerSleepStage(client.player.getUuid()))), false);
                            return 1;
                        })
                        .then(CommandManager.argument("stage", IntegerArgumentType.integer(0))
                                .executes(context -> {
                                    int stage = IntegerArgumentType.getInteger(context, "stage");
                                    ServerWorld world = context.getSource().getWorld();
                                    PlayerEntity player = context.getSource().getPlayer();
                                    DataTracker tracker = DataTracker.getServerState(world.getServer());
                                    tracker.setPlayerSleepStage(player.getUuid(), stage);
                                    context.getSource().sendFeedback(() -> Text.literal(String.valueOf(tracker.getPlayerSleepStage(client.player.getUuid()))), false);
                                    return 1;
                                })
                        )
                )
                .then(CommandManager.literal("runEvent")
                        .requires(source -> source.hasPermissionLevel(2))
                        .then(CommandManager.argument("event", StringArgumentType.word())
                                .suggests(EVENT_SUGGESTIONS)
                                .executes(context -> {
                                    String eventArg = StringArgumentType.getString(context, "event").toLowerCase();
                                    ServerWorld world = context.getSource().getWorld();
                                    PlayerEntity player = context.getSource().getPlayer();

                                    if (eventArg.equalsIgnoreCase("random")) {
                                        EventManager.triggerRandomEvent(world, player, null, true);
                                    } else {
                                        try {
                                            EventManager.Events event = EventManager.Events.valueOf(eventArg.toUpperCase());
                                            EventManager.triggerRandomEvent(world, player, event, true);
                                        } catch (IllegalArgumentException e) {
                                            context.getSource().sendFeedback(() -> Text.literal("<" + context.getSource().getName() + "> " + SplitSelf.translate("command.splitself.invalid_value").getString()), false);
                                        }
                                    }
                                    return 1;
                                })
                        )
                )
        );
    }
}
