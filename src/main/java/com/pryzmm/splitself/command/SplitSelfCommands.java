package com.pryzmm.splitself.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
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

public class SplitSelfCommands {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        MinecraftClient client = MinecraftClient.getInstance();
        dispatcher.register(CommandManager.literal("splitself")
                .executes(context -> {
                    context.getSource().sendFeedback(() -> Text.literal("<" + context.getSource().getName() + "> " + SplitSelf.translate("command.splitself.empty_command").getString()), false);
                    return 1;
                })
                .then(CommandManager.argument("text", StringArgumentType.word())
                        .executes(context -> {
                            String argument = StringArgumentType.getString(context, "text").toLowerCase();
                            if (argument.equalsIgnoreCase("information")) {
                                client.execute(() -> client.setScreen(new WarningScreen()));
                            } else if (argument.equalsIgnoreCase("debugFullscreen")) {
                                if (client.options.getFullscreen().getValue()) {
                                    context.getSource().sendFeedback(() -> Text.literal("In fullscreen"), false);
                                } else {
                                    context.getSource().sendFeedback(() -> Text.literal("NOT in fullscreen"), false);
                                }
                            } else if (argument.equalsIgnoreCase("debugToggleEvents")) {
                                DataTracker tracker = DataTracker.getServerState(client.getServer());
                                tracker.setPlayerReadWarning(client.player.getUuid(), !tracker.getPlayerReadWarning(client.player.getUuid()));
                                context.getSource().sendFeedback(() -> Text.literal(SplitSelf.translate("command.splitself.debug_toggle_warning", tracker.getPlayerReadWarning(client.player.getUuid())).getString()), false);
                            } else if (argument.equalsIgnoreCase("debugSleepStage")) {
                                DataTracker tracker = DataTracker.getServerState(client.getServer());
                                context.getSource().sendFeedback(() -> Text.literal(String.valueOf(tracker.getPlayerSleepStage(client.player.getUuid()))), false);
                            } else {
                                context.getSource().sendFeedback(() -> Text.literal("<" + context.getSource().getName() + "> " + SplitSelf.translate("command.splitself.invalid_value").getString()), false);
                            }
                            return 1;
                        })
                        .then(CommandManager.argument("event", StringArgumentType.greedyString())
                                .executes(context -> {
                                    String firstArg = StringArgumentType.getString(context, "text").toLowerCase();
                                    String secondArg = StringArgumentType.getString(context, "event").toLowerCase();
                                    ServerWorld world = context.getSource().getWorld();
                                    PlayerEntity player = context.getSource().getPlayer();
                                    if (firstArg.equalsIgnoreCase("debugSleepStage") && SplitSelf.isNumeric(secondArg)) {
                                        DataTracker tracker = DataTracker.getServerState(world.getServer());
                                        tracker.setPlayerSleepStage(player.getUuid(), Integer.parseInt(secondArg));
                                        context.getSource().sendFeedback(() -> Text.literal(String.valueOf(tracker.getPlayerSleepStage(client.player.getUuid()))), false);
                                    } else if (firstArg.equalsIgnoreCase("runevent") && secondArg.equalsIgnoreCase("random")) {
                                        EventManager.triggerRandomEvent(world, player, null, true);
                                    } else try {
                                        EventManager.Events event = EventManager.Events.valueOf(secondArg.toUpperCase());
                                        EventManager.triggerRandomEvent(world, player, event, true);
                                    } catch (IllegalArgumentException e) {
                                        context.getSource().sendFeedback(() -> Text.literal("<" + context.getSource().getName() + "> " + SplitSelf.translate("command.splitself.invalid_value").getString()), false);
                                    }
                                    return 1;
                                })
                        )
                )
        );
    }
}