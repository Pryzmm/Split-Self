package com.pryzmm.splitself.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.pryzmm.splitself.events.*;
import com.pryzmm.splitself.screen.WarningScreen;
import com.pryzmm.splitself.world.FirstJoinTracker;
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
                    context.getSource().sendFeedback(() -> Text.literal("<" + context.getSource().getName() + "> You don't know yourself."), false);
                    return 1;
                })
                .then(CommandManager.argument("text", StringArgumentType.word())
                        .executes(context -> {
                            String argument = StringArgumentType.getString(context, "text").toLowerCase();
                            if (argument.equalsIgnoreCase("information")) {
                                client.execute(() -> client.setScreen(new WarningScreen()));
                            } else if (argument.equalsIgnoreCase("debugToggleEvents")) {
                                FirstJoinTracker tracker = FirstJoinTracker.getServerState(client.getServer());
                                tracker.setPlayerReadWarning(client.player.getUuid(), !tracker.getPlayerReadWarning(client.player.getUuid()));
                                context.getSource().sendFeedback(() -> Text.literal("<SplitSelfDebug> Set playerReadWarning to " + tracker.getPlayerReadWarning(client.player.getUuid())), false);
                            } else if (argument.equalsIgnoreCase("runevent")) {
                                context.getSource().sendFeedback(() -> Text.literal("<" + context.getSource().getName() + "> No."), false);
                            } else if (argument.equalsIgnoreCase("control")) {
                                context.getSource().sendFeedback(() -> Text.literal("<" + context.getSource().getName() + "> I want my own life."), false);
                            } else if (argument.equalsIgnoreCase(context.getSource().getName().toLowerCase())) {
                                context.getSource().sendFeedback(() -> Text.literal("<" + context.getSource().getName() + "> You don't deserve that name."), false);
                            } else if (argument.equalsIgnoreCase("tethered")) {
                                context.getSource().sendFeedback(() -> Text.literal("<" + context.getSource().getName() + "> I will soon be free."), false);
                            } else {
                                context.getSource().sendFeedback(() -> Text.literal("<" + context.getSource().getName() + "> ..."), false);
                            }
                            return 1;
                        })
                        .then(CommandManager.argument("event", StringArgumentType.greedyString())
                                .executes(context -> {
                                    String firstArg = StringArgumentType.getString(context, "text").toLowerCase();
                                    String secondArg = StringArgumentType.getString(context, "event").toLowerCase();
                                    ServerWorld world = (ServerWorld) context.getSource().getWorld();
                                    PlayerEntity player = (PlayerEntity) context.getSource().getPlayer();
                                    if (firstArg.equalsIgnoreCase("runevent") && secondArg.equalsIgnoreCase("random")) {
                                        EventManager.triggerRandomEvent(world, player, null, true);
                                    } try {
                                        EventManager.Events event = EventManager.Events.valueOf(secondArg.toUpperCase());
                                        EventManager.triggerRandomEvent(world, player, event, true);
                                    } catch (IllegalArgumentException e) {
                                        context.getSource().sendFeedback(() -> Text.literal("<" + context.getSource().getName() + "> No."), false);
                                    }
                                    return 1;
                                })
                        )
                )
        );
    }
}