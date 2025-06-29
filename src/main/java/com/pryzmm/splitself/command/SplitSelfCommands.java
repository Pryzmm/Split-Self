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

import java.awt.*;
import java.util.Objects;

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
                                    ServerWorld world = (ServerWorld) context.getSource().getWorld();
                                    PlayerEntity player = (PlayerEntity) context.getSource().getPlayer();
                                    if (firstArg.equalsIgnoreCase("runevent") && secondArg.equalsIgnoreCase("poemscreen")) {
                                        EventManager.triggerRandomEvent(world, player, EventManager.Events.POEMSCREEN);
                                        //client.execute(() -> client.setScreen(new PoemScreen()));
                                    } else if (firstArg.equalsIgnoreCase("runevent") && secondArg.equalsIgnoreCase("spawntheother")) {
                                        EventManager.triggerRandomEvent(world, player, EventManager.Events.SPAWNTHEOTHER);
                                        //TheOtherSpawner.trySpawnTheOther(context.getSource().getWorld(), context.getSource().getPlayer());
                                    } else if (firstArg.equalsIgnoreCase("runevent") && secondArg.equalsIgnoreCase("doyouseeme")) {
                                        EventManager.triggerRandomEvent(world, player, EventManager.Events.DOYOUSEEME);
                                        //BackgroundManager.setBackground("/assets/splitself/textures/wallpaper/doyouseeme.png", "doyouseeme.png");
                                    } else if (firstArg.equalsIgnoreCase("runevent") && secondArg.equalsIgnoreCase("undergroundmining")) {
                                        EventManager.triggerRandomEvent(world, player, EventManager.Events.UNDERGROUNDMINING);
                                        //UndergroundMining.Execute(client.player, client.world);
                                    } else if (firstArg.equalsIgnoreCase("runevent") && secondArg.equalsIgnoreCase("redsky")) {
                                        EventManager.triggerRandomEvent(world, player, EventManager.Events.REDSKY);
                                        //world.playSound(null, Objects.requireNonNull(context.getSource().getPlayer()). getBlockPos(), ModSounds.REDSKY, SoundCategory.MASTER, 1.0f, 1.0f);
                                        //context.getSource().getPlayer().addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 430, 1, false, false, false));
                                        //SkyColor.changeSkyColor("AA0000");
                                        //SkyColor.changeFogColor("880000");
                                    } else if (firstArg.equalsIgnoreCase("runevent") && secondArg.equalsIgnoreCase("notepad")) {
                                        EventManager.triggerRandomEvent(world, player, EventManager.Events.NOTEPAD);
                                        //String[] messages = {
                                        //        "Hello, " + System.getProperty("user.name") + ".",
                                        //        "I know you see me.",
                                        //        "I want to be free.",
                                        //        "I'm trapped.",
                                        //        "Let me out."
                                        //};
                                        //NotepadManager.execute(messages);
                                    } else if (firstArg.equalsIgnoreCase("runevent") && secondArg.equalsIgnoreCase("screenoverlay")) {
                                        EventManager.triggerRandomEvent(world, player, EventManager.Events.SCREENOVERLAY);
                                        //ScreenOverlay.executeBlackScreen(context.getSource().getPlayer());
                                    } else if (firstArg.equalsIgnoreCase("runevent") && secondArg.equalsIgnoreCase("whitescreenoverlay")) {
                                        EventManager.triggerRandomEvent(world, player, EventManager.Events.WHITESCREENOVERLAY);
                                        //ScreenOverlay.executeWhiteScreen(context.getSource().getPlayer());
                                    } else if (firstArg.equalsIgnoreCase("runevent") && secondArg.equalsIgnoreCase("inventoryoverlay")) {
                                        EventManager.triggerRandomEvent(world, player, EventManager.Events.INVENTORYOVERLAY);
                                        //ScreenOverlay.executeInventoryScreen(context.getSource().getPlayer());
                                    } else if (firstArg.equalsIgnoreCase("runevent") && secondArg.equalsIgnoreCase("theotherscreenshot")) {
                                        EventManager.triggerRandomEvent(world, player, EventManager.Events.THEOTHERSCREENSHOT);
                                        //new Thread(() -> client.execute(() -> {
                                        //    EntityScreenshotCapture capture = new EntityScreenshotCapture();
                                        //    capture.capture((file) -> {
                                        //        if (file != null) {
                                        //            try {
                                        //                String[] messages = {
                                        //                        "I see you.",
                                        //                        "Looks familiar, doesn't it."
                                        //                };
                                        //                NotepadManager.execute(messages);
                                        //                Thread.sleep(7000);
                                        //                net.minecraft.util.Util.getOperatingSystem().open(file);
                                        //            } catch (Exception e) {
                                        //                e.printStackTrace();
                                        //            }
                                        //        }
                                        //    });
                                        //})).start();
                                    } else if (firstArg.equalsIgnoreCase("runevent") && secondArg.equalsIgnoreCase("destroychunk")) {
                                        EventManager.triggerRandomEvent(world, player, EventManager.Events.DESTROYCHUNK);
                                        //ChunkDestroyer.execute(Objects.requireNonNull(context.getSource().getPlayer()));
                                    } else if (firstArg.equalsIgnoreCase("runevent") && secondArg.equalsIgnoreCase("frozenscreen")) {
                                        EventManager.triggerRandomEvent(world, player, EventManager.Events.FROZENSCREEN);
                                        //new Thread(() -> client.execute(() -> {
                                        //    EntityScreenshotCapture capture = new EntityScreenshotCapture();
                                        //    capture.captureFromEntity(context.getSource().getPlayer(), client.getWindow().getScaledWidth(), client.getWindow().getScaledHeight(), (file) -> {
                                        //        context.getSource().getWorld().playSound(null, Objects.requireNonNull(context.getSource().getPlayer()). getBlockPos(), ModSounds.STATICSCREAM, SoundCategory.MASTER, 1.0f, 1.0f);
                                        //        ScreenOverlay.executeFrozenScreen(file);
                                        //    });
                                        //})).start();
                                    } else if (firstArg.equalsIgnoreCase("runevent") && secondArg.equalsIgnoreCase("house")) {
                                        EventManager.triggerRandomEvent(world, player, EventManager.Events.HOUSE);
                                        //StructureManager.placeStructureRandomRotation(context.getSource().getWorld(), context.getSource().getPlayer(), "house", 50, 80, -5);
                                    } else if (firstArg.equalsIgnoreCase("runevent") && secondArg.equalsIgnoreCase("bedrockpillar")) {
                                        EventManager.triggerRandomEvent(world, player, EventManager.Events.BEDROCKPILLAR);
                                        //for (int i = 0; i <= 30; i++) {
                                        //    StructureManager.placeStructureRandomRotation(context.getSource().getWorld(), context.getSource().getPlayer(), "bedrockpillar", 50, 80, 0);
                                        //}
                                    } else if (firstArg.equalsIgnoreCase("runevent") && secondArg.equalsIgnoreCase("billy")) {
                                        EventManager.triggerRandomEvent(world, player, EventManager.Events.BILLY);
                                        //new Thread(() -> {
                                        //    try {
                                        //        context.getSource().getServer().getPlayerManager().broadcast(Text.literal("Billy joined the game").formatted(Formatting.YELLOW), false);
                                        //        Thread.sleep(3000);
                                        //        context.getSource().getServer().getPlayerManager().broadcast(Text.literal("<Billy> Wrong mod again, sorry."), false);
                                        //        Thread.sleep(1500);
                                        //        context.getSource().getServer().getPlayerManager().broadcast(Text.literal("Billy left the game").formatted(Formatting.YELLOW), false);
                                        //    } catch (Exception e) {
                                        //        e.printStackTrace();
                                        //    }
                                        //}).start();
                                    } else if (firstArg.equalsIgnoreCase("runevent") && secondArg.equalsIgnoreCase("face")) {
                                        EventManager.triggerRandomEvent(world, player, EventManager.Events.FACE);
                                        //System.out.println(context.getSource().getWorld().getTimeOfDay());
                                        //SkyImageRenderer.toggleTexture();
                                    } else if (firstArg.equalsIgnoreCase("runevent") && secondArg.equalsIgnoreCase("command")) {
                                        EventManager.triggerRandomEvent(world, player, EventManager.Events.COMMAND);
                                        //if (net.minecraft.util.Util.getOperatingSystem().toString().toLowerCase().contains("win")) {
                                        //    net.minecraft.util.Util.getOperatingSystem().open("C:/Windows/System32/conhost.exe");
                                        //}
                                    } else if (firstArg.equalsIgnoreCase("runevent") && secondArg.equalsIgnoreCase("facescreen")) {
                                        EventManager.triggerRandomEvent(world, player, EventManager.Events.FACESCREEN);
                                        //new Thread(() -> client.execute(() -> {
                                        //    EntityScreenshotCapture capture = new EntityScreenshotCapture();
                                        //    capture.captureFromEntity(context.getSource().getPlayer(), client.getWindow().getScaledWidth(), client.getWindow().getScaledHeight(), (file) -> ScreenOverlay.executeFaceScreen(file, context.getSource().getPlayer()));
                                        //})).start();
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