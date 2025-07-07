package com.pryzmm.splitself.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.pryzmm.splitself.events.*;
import com.pryzmm.splitself.screen.WarningScreen;
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
                                    if (firstArg.equalsIgnoreCase("runevent") && secondArg.equalsIgnoreCase("poemscreen")) {
                                        EventManager.triggerRandomEvent(world, player, EventManager.Events.POEMSCREEN);
                                    } else if (firstArg.equalsIgnoreCase("runevent") && secondArg.equalsIgnoreCase("spawntheother")) {
                                        EventManager.triggerRandomEvent(world, player, EventManager.Events.SPAWNTHEOTHER);
                                    } else if (firstArg.equalsIgnoreCase("runevent") && secondArg.equalsIgnoreCase("doyouseeme")) {
                                        EventManager.triggerRandomEvent(world, player, EventManager.Events.DOYOUSEEME);
                                    } else if (firstArg.equalsIgnoreCase("runevent") && secondArg.equalsIgnoreCase("undergroundmining")) {
                                        EventManager.triggerRandomEvent(world, player, EventManager.Events.UNDERGROUNDMINING);
                                    } else if (firstArg.equalsIgnoreCase("runevent") && secondArg.equalsIgnoreCase("redsky")) {
                                        EventManager.triggerRandomEvent(world, player, EventManager.Events.REDSKY);
                                    } else if (firstArg.equalsIgnoreCase("runevent") && secondArg.equalsIgnoreCase("notepad")) {
                                        EventManager.triggerRandomEvent(world, player, EventManager.Events.NOTEPAD);
                                    } else if (firstArg.equalsIgnoreCase("runevent") && secondArg.equalsIgnoreCase("screenoverlay")) {
                                        EventManager.triggerRandomEvent(world, player, EventManager.Events.SCREENOVERLAY);
                                    } else if (firstArg.equalsIgnoreCase("runevent") && secondArg.equalsIgnoreCase("whitescreenoverlay")) {
                                        EventManager.triggerRandomEvent(world, player, EventManager.Events.WHITESCREENOVERLAY);
                                    } else if (firstArg.equalsIgnoreCase("runevent") && secondArg.equalsIgnoreCase("inventoryoverlay")) {
                                        EventManager.triggerRandomEvent(world, player, EventManager.Events.INVENTORYOVERLAY);
                                    } else if (firstArg.equalsIgnoreCase("runevent") && secondArg.equalsIgnoreCase("theotherscreenshot")) {
                                        EventManager.triggerRandomEvent(world, player, EventManager.Events.THEOTHERSCREENSHOT);
                                    } else if (firstArg.equalsIgnoreCase("runevent") && secondArg.equalsIgnoreCase("destroychunk")) {
                                        EventManager.triggerRandomEvent(world, player, EventManager.Events.DESTROYCHUNK);
                                    } else if (firstArg.equalsIgnoreCase("runevent") && secondArg.equalsIgnoreCase("frozenscreen")) {
                                        EventManager.triggerRandomEvent(world, player, EventManager.Events.FROZENSCREEN);
                                    } else if (firstArg.equalsIgnoreCase("runevent") && secondArg.equalsIgnoreCase("house")) {
                                        EventManager.triggerRandomEvent(world, player, EventManager.Events.HOUSE);
                                    } else if (firstArg.equalsIgnoreCase("runevent") && secondArg.equalsIgnoreCase("bedrockpillar")) {
                                        EventManager.triggerRandomEvent(world, player, EventManager.Events.BEDROCKPILLAR);
                                    } else if (firstArg.equalsIgnoreCase("runevent") && secondArg.equalsIgnoreCase("billy")) {
                                        EventManager.triggerRandomEvent(world, player, EventManager.Events.BILLY);
                                    } else if (firstArg.equalsIgnoreCase("runevent") && secondArg.equalsIgnoreCase("face")) {
                                        EventManager.triggerRandomEvent(world, player, EventManager.Events.FACE);
                                    } else if (firstArg.equalsIgnoreCase("runevent") && secondArg.equalsIgnoreCase("command")) {
                                        EventManager.triggerRandomEvent(world, player, EventManager.Events.COMMAND);
                                    } else if (firstArg.equalsIgnoreCase("runevent") && secondArg.equalsIgnoreCase("invert")) {
                                        EventManager.triggerRandomEvent(world, player, EventManager.Events.INVERT);
                                    } else {
                                        context.getSource().sendFeedback(() -> Text.literal("<" + context.getSource().getName() + "> No."), false);
                                    }
                                    return 1;
                                })
                        )
                )
        );
    }
}