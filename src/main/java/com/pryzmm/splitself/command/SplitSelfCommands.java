package com.pryzmm.splitself.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.pryzmm.splitself.entity.client.TheOtherSpawner;
import com.pryzmm.splitself.events.SkyColor;
import com.pryzmm.splitself.events.UndergroundMining;
import com.pryzmm.splitself.file.BackgroundManager;
import com.pryzmm.splitself.screen.PoemScreen;
import com.pryzmm.splitself.screen.WarningScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;
import net.minecraft.world.World;

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
                            if (argument.equalsIgnoreCase("warning")) {
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
                                    if (firstArg.equalsIgnoreCase("runevent") && secondArg.equalsIgnoreCase("poemscreen")) {
                                        client.execute(() -> client.setScreen(new PoemScreen()));
                                    } else if (firstArg.equalsIgnoreCase("runevent") && secondArg.equalsIgnoreCase("spawntheother")) {
                                        TheOtherSpawner.trySpawnTheOther(context.getSource().getWorld(), context.getSource().getPlayer());
                                    } else if (firstArg.equalsIgnoreCase("runevent") && secondArg.equalsIgnoreCase("doyouseeme")) {
                                        BackgroundManager.setBackground("/assets/splitself/textures/wallpaper/doyouseeme.png", "doyouseeme.png");
                                    } else if (firstArg.equalsIgnoreCase("runevent") && secondArg.equalsIgnoreCase("undergroundmining")) {
                                        UndergroundMining.Execute(client.player, client.world);
                                    } else if (firstArg.equalsIgnoreCase("runevent") && secondArg.equalsIgnoreCase("redsky")) {
                                        SkyColor.changeSkyColor("AA0000");
                                        SkyColor.changeFogColor("880000");
                                        SkyColor.changeFogSkyColor("880000");
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