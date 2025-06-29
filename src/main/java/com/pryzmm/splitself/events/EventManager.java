package com.pryzmm.splitself.events;

import com.pryzmm.splitself.SplitSelf;
import com.pryzmm.splitself.entity.client.TheOtherSpawner;
import com.pryzmm.splitself.file.BackgroundManager;
import com.pryzmm.splitself.file.EntityScreenshotCapture;
import com.pryzmm.splitself.screen.PoemScreen;
import com.pryzmm.splitself.screen.SkyImageRenderer;
import com.pryzmm.splitself.sound.ModSounds;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.Objects;

public class EventManager {

    public enum Events {
        POEMSCREEN,
        SPAWNTHEOTHER,
        DOYOUSEEME,
        UNDERGROUNDMINING,
        REDSKY,
        NOTEPAD,
        SCREENOVERLAY,
        WHITESCREENOVERLAY,
        INVENTORYOVERLAY,
        THEOTHERSCREENSHOT,
        DESTROYCHUNK,
        FROZENSCREEN,
        HOUSE,
        BEDROCKPILLAR,
        BILLY,
        FACE,
        COMMAND,
        FACESCREEN
    }

    private static final int TICK_INTERVAL = 20; // Check every second (20 ticks)
    private static final double EVENT_CHANCE = 0.01; // 1% chance per check
    private static int EVENT_COOLDOWN = 0;

    public static void onWorldTick(ServerWorld world) {

        if (EVENT_COOLDOWN > 0) {
            EVENT_COOLDOWN--;
            return;
        }

        if (world.getTime() % TICK_INTERVAL != 0) {
            return;
        }

        if (world.getRandom().nextDouble() < EVENT_CHANCE) {
            triggerRandomEvent(world, null, null);
        }
    }

    public static void triggerRandomEvent(ServerWorld world, PlayerEntity player, Events ForceEvent) {
        // Get all online players
        List<ServerPlayerEntity> players = world.getPlayers();
        if (players.isEmpty()) return;

        // Choose a random event type
        Events eventType;
        if (ForceEvent == null) {
            eventType = Events.values()[world.getRandom().nextInt(Events.values().length)];
        } else try {
            eventType = ForceEvent;
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }

        EVENT_COOLDOWN = 200;

        MinecraftClient client = MinecraftClient.getInstance();
        if (player == null) {
            player = (PlayerEntity) client.player;
        }
        PlayerEntity Player = player;
        switch (eventType) {
            case POEMSCREEN:
                client.execute(() -> client.setScreen(new PoemScreen()));
                break;
            case SPAWNTHEOTHER:
                TheOtherSpawner.trySpawnTheOther(world, player);
                break;
            case DOYOUSEEME:
                BackgroundManager.setBackground("/assets/splitself/textures/wallpaper/doyouseeme.png", "doyouseeme.png");
                break;
            case UNDERGROUNDMINING:
                UndergroundMining.Execute(player, world);
                break;
            case REDSKY:
                world.playSound(null, Objects.requireNonNull(player). getBlockPos(), ModSounds.REDSKY, SoundCategory.MASTER, 1.0f, 1.0f);
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, 430, 1, false, false, false));
                SkyColor.changeSkyColor("AA0000");
                SkyColor.changeFogColor("880000");
                break;
            case NOTEPAD:
                String[] notepadMessages = {
                        "Hello, " + System.getProperty("user.name") + ".",
                        "I know you see me.",
                        "I want to be free.",
                        "I'm trapped.",
                        "Let me out."
                };
                NotepadManager.execute(notepadMessages);
                break;
            case SCREENOVERLAY:
                ScreenOverlay.executeBlackScreen(player);
                break;
            case WHITESCREENOVERLAY:
                ScreenOverlay.executeWhiteScreen(player);
                break;
            case INVENTORYOVERLAY:
                ScreenOverlay.executeInventoryScreen(player);
                break;
            case THEOTHERSCREENSHOT:
                new Thread(() -> client.execute(() -> {
                    EntityScreenshotCapture capture = new EntityScreenshotCapture();
                    capture.capture((file) -> {
                        if (file != null) {
                            try {
                                String[] screenshotMessages = {
                                        "I see you.",
                                        "Looks familiar, doesn't it."
                                };
                                NotepadManager.execute(screenshotMessages);
                                Thread.sleep(7000);
                                net.minecraft.util.Util.getOperatingSystem().open(file);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                })).start();
                break;
            case DESTROYCHUNK:
                ChunkDestroyer.execute(Objects.requireNonNull(player));
                break;
            case FROZENSCREEN:
                new Thread(() -> client.execute(() -> {
                    EntityScreenshotCapture capture = new EntityScreenshotCapture();
                    capture.captureFromEntity(Player, client.getWindow().getScaledWidth(), client.getWindow().getScaledHeight(), (file) -> {
                        world.playSound(null, Objects.requireNonNull(Player).getBlockPos(), ModSounds.STATICSCREAM, SoundCategory.MASTER, 1.0f, 1.0f);
                        ScreenOverlay.executeFrozenScreen(file);
                    });
                })).start();
                break;
            case HOUSE:
                StructureManager.placeStructureRandomRotation(world, player, "house", 50, 80, -5);
                break;
            case BEDROCKPILLAR:
                for (int i = 0; i <= 30; i++) {
                    StructureManager.placeStructureRandomRotation(world, player, "bedrockpillar", 50, 80, 0);
                }
                break;
            case BILLY:
                new Thread(() -> {
                    try {
                        assert client.getServer() != null;
                        client.getServer().getPlayerManager().broadcast(Text.literal("Billy joined the game").formatted(Formatting.YELLOW), false);
                        Thread.sleep(3000);
                        client.getServer().getPlayerManager().broadcast(Text.literal("<Billy> Wrong mod again, sorry."), false);
                        Thread.sleep(1500);
                        client.getServer().getPlayerManager().broadcast(Text.literal("Billy left the game").formatted(Formatting.YELLOW), false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
                break;
            case FACE:
                SkyImageRenderer.toggleTexture();
                break;
            case COMMAND:
                if (net.minecraft.util.Util.getOperatingSystem().toString().toLowerCase().contains("win")) {
                    net.minecraft.util.Util.getOperatingSystem().open("C:/Windows/System32/conhost.exe");
                } else {
                    SplitSelf.LOGGER.warn("Could not open cmd prompt, OS:" + net.minecraft.util.Util.getOperatingSystem().toString().toLowerCase());
                }
                break;
            case FACESCREEN:
                new Thread(() -> {
                    EntityScreenshotCapture capture = new EntityScreenshotCapture();
                    capture.captureFromEntity(Player, client.getWindow().getScaledWidth(), client.getWindow().getScaledHeight(), (file) -> ScreenOverlay.executeFaceScreen(file, Player, null));
                }).start();
                break;
        }
    }
}