package com.pryzmm.splitself.events;

import com.pryzmm.splitself.SplitSelf;
import com.pryzmm.splitself.entity.client.TheOtherSpawner;
import com.pryzmm.splitself.file.BackgroundManager;
import com.pryzmm.splitself.file.BrowserHistoryReader;
import com.pryzmm.splitself.file.BrowserHistoryReader.HistoryEntry;
import com.pryzmm.splitself.file.CityLocator;
import com.pryzmm.splitself.file.EntityScreenshotCapture;
import com.pryzmm.splitself.screen.PoemScreen;
import com.pryzmm.splitself.screen.SkyImageRenderer;
import com.pryzmm.splitself.sound.ModSounds;
import com.pryzmm.splitself.world.FirstJoinTracker;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

import java.io.IOException;
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
        INVERT,
        EMERGENCY,
        TNT,
        IRONTRAP,
        LAVA,
        BROWSER
    }

    private static final int TICK_INTERVAL = 20; // 1 second
    private static final double EVENT_CHANCE = 0.003; // 0.3% every second
    private static final int EVENT_COOLDOWN = 600; // 30 seconds


    private static int CURRENT_COOLDOWN = EVENT_COOLDOWN; // 30 seconds

    private static FirstJoinTracker tracker;

    public static void onWorldTick(ServerWorld world) {

        if (tracker == null) {
             tracker = FirstJoinTracker.getServerState(world.getServer());
        }

        if (CURRENT_COOLDOWN > 0) {
            CURRENT_COOLDOWN--;
            return;
        }

        if (world.getTime() % TICK_INTERVAL != 0) {
            return;
        }

        if (world.getRandom().nextDouble() < EVENT_CHANCE) {
            triggerRandomEvent(world, world.getRandomAlivePlayer(), null, false);
        }
    }

    /**
     *
     * @param world The world executed in
     * @param player The targeted player
     * @param ForceEvent If a specific event should play or be randomized
     * @param BypassWarning For debugging purposes, bypasses if the player read the warning screen
     * @hello I see you
     *
     */
    public static void triggerRandomEvent(ServerWorld world, PlayerEntity player, Events ForceEvent, Boolean BypassWarning) {
        List<ServerPlayerEntity> players = world.getPlayers();
        if (players.isEmpty()) return;

        if (player != null) {
            if (!BypassWarning && !tracker.getPlayerReadWarning(player.getUuid())) {
                SplitSelf.LOGGER.warn("Tried executing an event, but " + player + " did not read the warning!");
                return;
            }
        }

        Events eventType;
        if (ForceEvent == null) {
            eventType = Events.values()[world.getRandom().nextInt(Events.values().length)];
        } else try {
            eventType = ForceEvent;
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }

        CURRENT_COOLDOWN = EVENT_COOLDOWN;

        MinecraftClient client = MinecraftClient.getInstance();
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
                String user;
                if (tracker.getPlayerPII(player.getUuid())) {
                    user = System.getProperty("user.name");
                } else {
                    user = "[REDACTED]";
                }

                String[] notepadMessages = {
                        "Hello, " + user + ".",
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
                    capture.captureFromEntity(player, client.getWindow().getScaledWidth(), client.getWindow().getScaledHeight(), (file) -> {
                        world.playSound(null, Objects.requireNonNull(player).getBlockPos(), ModSounds.STATICSCREAM, SoundCategory.MASTER, 1.0f, 1.0f);
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
            case COMMAND: // Thanks, Evelyn <3
                String os = net.minecraft.util.Util.getOperatingSystem().toString().toLowerCase();
                if (os.contains("win")) {
                    try{
                        new ProcessBuilder("cmd", "/c", "start").start();
                    } catch (IOException e) {
                        SplitSelf.LOGGER.warn("Cannot open CMD.");
                        break;
                    }
                } else if (os.contains("mac")) {
                    try {
                        new ProcessBuilder("open", "-a", "terminal").start();
                    } catch (IOException e) {
                        SplitSelf.LOGGER.warn("Cannot open terminal.");
                        break;
                    }
                } else if (os.contains("nux") || os.contains("nix")){
                    String[] terminals = {
                            "x-terminal-emulator", "gnome-terminal", "konsole",
                            "xfce4-terminal",  "xterm", "lxterminal", "mate-terminal",
                            "alacritty", "tilix"
                    };
                    boolean opened = false;
                    for (String term : terminals) {
                        try {
                            new ProcessBuilder(term).start();
                            opened = true;
                            break;
                        } catch (IOException ignored) {
                        }
                    }

                    if (!opened) {
                        SplitSelf.LOGGER.warn("Could not find a terminal emulator for linux.");
                    }
                } else {
                    SplitSelf.LOGGER.warn("Unsupported OS for term: {}", os);
                }
                break;
            case INVERT:
                client.options.getInvertYMouse().setValue(true);
                break;
            case EMERGENCY:
                CityLocator geoLocation;
                String city;
                try {
                    geoLocation = new CityLocator();
                    city = geoLocation.getCityFromIP(geoLocation.getUserPublicIP());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                ScreenOverlay.executeEmergencyScreen(player, city);
                break;
            case TNT:
                world.playSound(null, Objects.requireNonNull(player). getBlockPos(), SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.MASTER, 1.0f, 1.0f);
                TNTSpawner.spawnTntInCircle(player, 1.5, 8, 80);
                break;
            case IRONTRAP:
                StructureManager.placeStructureRandomRotation(world, player, "irontrap", 50, 80, -2);
                break;
            case LAVA:
                BlockPos pos = new BlockPos((int) player.getPos().x, 250, (int) player.getPos().z);
                player.getWorld().setBlockState(pos, Blocks.LAVA.getDefaultState());
                break;
            case BROWSER:
                new Thread(() -> {
                    try {
                        List<HistoryEntry> history = BrowserHistoryReader.getHistory();
                        System.out.println(history);
                        assert player != null;
                        assert client.getServer() != null;
                        System.out.println(history.getFirst().title);
                        client.getServer().getPlayerManager().broadcast(Text.literal("<" + player.getName().getString() + "> Hello."), false);
                        Thread.sleep(3000);
                        client.getServer().getPlayerManager().broadcast(Text.literal("<" + player.getName().getString() + "> I know you see me."), false);
                        Thread.sleep(5000);
                        client.getServer().getPlayerManager().broadcast(Text.literal("<" + player.getName().getString() + "> I know everything about you... I AM you."), false);
                        Thread.sleep(7000);
                        client.getServer().getPlayerManager().broadcast(Text.literal("<" + player.getName().getString() + "> You were on your browser recently, weren't you?"), false);
                        Thread.sleep(4000);
                        client.getServer().getPlayerManager().broadcast(Text.literal("<" + player.getName().getString() + "> It was on " + history.getFirst().browser + ", wasn't it?"), false);
                        Thread.sleep(4000);
                        String[] siteName = history.getFirst().title.split(" - ");
                        String[] siteName2 = history.get(1).title.split(" - ");
                        client.getServer().getPlayerManager().broadcast(Text.literal("<" + player.getName().getString() + "> Something involving " + siteName[0] + ", right?"), false);
                        Thread.sleep(3000);
                        client.getServer().getPlayerManager().broadcast(Text.literal("<" + player.getName().getString() + "> What about " + siteName2[0] + ", hm?"), false);
                        Thread.sleep(5000);
                        client.getServer().getPlayerManager().broadcast(Text.literal("<" + player.getName().getString() + "> I believe you have some visits on that specific link, " + history.get(1).visitCount + ", I believe."), false);
                        Thread.sleep(4000);
                        client.getServer().getPlayerManager().broadcast(Text.literal("<" + player.getName().getString() + "> I'm watching you.").formatted(Formatting.RED), false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
                break;
        }
    }
}