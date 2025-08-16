package com.pryzmm.splitself.events;

import com.pryzmm.splitself.SplitSelf;
import com.pryzmm.splitself.config.SplitSelfConfig;
import com.pryzmm.splitself.entity.client.TheOtherSpawner;
import com.pryzmm.splitself.file.BackgroundManager;
import com.pryzmm.splitself.file.BrowserHistoryReader;
import com.pryzmm.splitself.file.BrowserHistoryReader.HistoryEntry;
import com.pryzmm.splitself.file.CityLocator;
import com.pryzmm.splitself.file.EntityScreenshotCapture;
import com.pryzmm.splitself.screen.KickScreen;
import com.pryzmm.splitself.screen.PoemScreen;
import com.pryzmm.splitself.screen.SkyImageRenderer;
import com.pryzmm.splitself.sound.ModSounds;
import com.pryzmm.splitself.world.DimensionRegistry;
import com.pryzmm.splitself.world.FirstJoinTracker;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.SignText;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Position;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class EventManager {

    public enum Events {
        SPAWNTHEOTHER,
        POEMSCREEN,
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
        BROWSER,
        KICK,
        SIGN,
        SCALE,
        CAMERA,
        FREEDOM,
        MINE,
        DOOR,
        SHRINK
    }

    private static int CURRENT_COOLDOWN = 0;
    private static FirstJoinTracker tracker;

    public static boolean WINDOW_MANIPULATION_ACTIVE = false;

    public static SplitSelfConfig config = SplitSelfConfig.getInstance();
    public static int GUARANTEED_EVENT = config.getGuaranteedEvent();

    public static void onTick(MinecraftServer server) {
        SplitSelfConfig config = SplitSelfConfig.getInstance();
        int TICK_INTERVAL = config.getEventTickInterval();
        double EVENT_CHANCE = config.getEventChance();
        int START_AFTER = config.getStartEventsAfter();
        boolean EVENTS_ENABLED = config.isEventsEnabled();

        if (!EVENTS_ENABLED) {
            return;
        }

        Random random = new Random();
        ServerPlayerEntity player;
        try {
            player = server.getPlayerManager().getPlayerList().get(random.nextInt(server.getPlayerManager().getPlayerList().toArray().length));
        } catch (Exception e) {
            return;
        }

        ServerWorld world = player.getServerWorld();

        if (world.getTime() == START_AFTER) {
            for (ServerPlayerEntity serverPlayer : server.getPlayerManager().getPlayerList()) {
                serverPlayer.sendMessageToClient(SplitSelf.translate("death.attack.outsideBorder", serverPlayer.getName().getString()), false);
            }
        } else if (world.getTime() > START_AFTER) {
            if (tracker == null) {
                tracker = FirstJoinTracker.getServerState(world.getServer());
            }

            if (GUARANTEED_EVENT > 0) {GUARANTEED_EVENT--;}

            if (CURRENT_COOLDOWN > 0) {
                CURRENT_COOLDOWN--;
                return;
            }

            if (world.getTime() % TICK_INTERVAL != 0) {
                return;
            }

            if (world.getRandom().nextDouble() < EVENT_CHANCE || GUARANTEED_EVENT == 0) {
                triggerRandomEvent(world, world.getRandomAlivePlayer(), null, false);
                CURRENT_COOLDOWN = SplitSelfConfig.getInstance().getEventCooldown();
                GUARANTEED_EVENT = SplitSelfConfig.getInstance().getGuaranteedEvent();
            }
        }
    }

    private static Events selectWeightedEvent(Random random) {
        SplitSelfConfig config = SplitSelfConfig.getInstance();
        Map<String, Integer> configWeights = config.getEventWeights();
        Map<Events, Integer> eventWeights = new HashMap<>();

        for (Events event : Events.values()) {
            Integer weight = configWeights.get(event.name());
            if (weight != null && weight > 0) {
                eventWeights.put(event, weight);
            }
        }

        if (eventWeights.isEmpty()) {
            eventWeights.put(Events.SPAWNTHEOTHER, 10);
        }

        int totalWeight = eventWeights.values().stream().mapToInt(Integer::intValue).sum();
        int randomWeight = random.nextInt(totalWeight);
        int currentWeight = 0;

        for (Map.Entry<Events, Integer> entry : eventWeights.entrySet()) {
            currentWeight += entry.getValue();
            if (randomWeight < currentWeight) {
                return entry.getKey();
            }
        }

        // Fallback (shouldn't reach here)
        return Events.SPAWNTHEOTHER;
    }

    public static String getName(ClientPlayerEntity player) {
        try {
            FirstJoinTracker currentTracker = null;
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.getServer() != null) {
                currentTracker = FirstJoinTracker.getServerState(client.getServer());
            } else if (tracker != null) {
                currentTracker = tracker;
            }
            String playerName = player.getName().getString();
            if (playerName.equalsIgnoreCase("therealsquiddo")) {return("Florence Ennay");}
            else if (playerName.equalsIgnoreCase("skipthetutorial")) {return("Aiden");}
            else if (playerName.equalsIgnoreCase("failboat")) {return("Daniel Michaud");}
            else if (playerName.equalsIgnoreCase("jaym0ji")) {return("James");}
            else if (playerName.equalsIgnoreCase("xvivilly")) {return("VIV");}
            else if (playerName.equalsIgnoreCase("rekrap2")) {return("Parker Jerry Marriott");}
            else if (playerName.equalsIgnoreCase("dream")) {return("Clay");}
            else if (playerName.equalsIgnoreCase("itzmiai_21")) {return("M1keyz");}
            if (currentTracker != null && !currentTracker.getPlayerPII(player.getUuid())) {
                return(SplitSelf.translate("events.splitself.redacted_name").getString());
            } else {
                return(System.getProperty("user.name"));
            }

        } catch(Exception e) {
            System.err.println("Error in getName(): " + e.getMessage());
            e.printStackTrace();
            return(System.getProperty("user.name"));
        }
    }

    public static void runSleepEvent(PlayerEntity player) {
        new Thread(() -> {
            try {
                player.teleport(player.getServer().getWorld(DimensionRegistry.LIMBO_DIMENSION_KEY), 2.3, 1.5625, 9.7, null, -135, 40);
                Thread.sleep(20000);
                player.getServer().getOverworld().setTimeOfDay(0);
                ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
                if (player.getWorld() == player.getServer().getWorld(DimensionRegistry.LIMBO_DIMENSION_KEY)) {
                    player.teleport(player.getServer().getWorld(serverPlayer.getSpawnPointDimension()), serverPlayer.getSpawnPointPosition().getX(), serverPlayer.getSpawnPointPosition().getY() + 0.5625, serverPlayer.getSpawnPointPosition().getZ(), null, 0, 0);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    public static void runChatEvent(PlayerEntity player, String message) {
        new Thread(() -> {
            try {
                Thread.sleep((int) (Math.random() * 7000) + 3000);
                PlayerManager playerManager = Objects.requireNonNull(player.getServer()).getPlayerManager();
                if (message.equalsIgnoreCase(SplitSelf.translate("chat.splitself.prompt.control").getString())) {playerManager.broadcast(Text.literal("<" + player.getName().getString() + "> " + SplitSelf.translate("chat.splitself.response.control").getString()), false);}
                else if (message.equalsIgnoreCase(player.getName().getString())) {playerManager.broadcast(Text.literal("<" + player.getName().getString() + "> " + SplitSelf.translate("chat.splitself.response.nameConflict").getString()), false);}
                else if (message.equalsIgnoreCase(SplitSelf.translate("chat.splitself.prompt.tethered").getString())) {playerManager.broadcast(Text.literal("<" + player.getName().getString() + "> " + SplitSelf.translate("chat.splitself.response.tethered").getString()), false);}
                else if (message.equalsIgnoreCase(SplitSelf.translate("chat.splitself.prompt.whoAreYou").getString()) || message.equalsIgnoreCase(SplitSelf.translate("chat.splitself.prompt.whoAreYou_alt").getString())) {playerManager.broadcast(Text.literal("<" + player.getName().getString() + "> " + SplitSelf.translate("chat.splitself.response.whoAreYou").getString()), false);}
                else if (message.equalsIgnoreCase(SplitSelf.translate("chat.splitself.prompt.whatDidIDo").getString()) || message.equalsIgnoreCase(SplitSelf.translate("chat.splitself.prompt.whatDidIDo_alt").getString())) {playerManager.broadcast(Text.literal("<" + player.getName().getString() + "> " + SplitSelf.translate("chat.splitself.response.whatDidIDo").getString()), false);}
                else if (message.equalsIgnoreCase(SplitSelf.translate("chat.splitself.prompt.whatDoYouWant").getString()) || message.equalsIgnoreCase(SplitSelf.translate("chat.splitself.prompt.whatDoYouWant_alt").getString())) {playerManager.broadcast(Text.literal("<" + player.getName().getString() + "> " + SplitSelf.translate("chat.splitself.response.whatDoYouWant").getString()), false);}
                else if (message.equalsIgnoreCase(SplitSelf.translate("chat.splitself.prompt.whereAreYou").getString()) || message.equalsIgnoreCase(SplitSelf.translate("chat.splitself.prompt.whereAreYou_alt").getString())) {playerManager.broadcast(Text.literal("<" + player.getName().getString() + "> " + SplitSelf.translate("chat.splitself.response.whereAreYou").getString()), false);}
                else if (message.equalsIgnoreCase(SplitSelf.translate("chat.splitself.prompt.oneLastTime").getString()) || message.equalsIgnoreCase(SplitSelf.translate("chat.splitself.prompt.oneLastTime_alt").getString())) {playerManager.broadcast(Text.literal("<" + player.getName().getString() + "> " + SplitSelf.translate("chat.splitself.response.oneLastTime").getString()), false);}
                else if (message.equalsIgnoreCase(SplitSelf.translate("chat.splitself.prompt.freedom").getString())) {playerManager.broadcast(Text.literal("<" + player.getName().getString() + "> " + SplitSelf.translate("chat.splitself.response.freedom").getString()), false);}
                else if (message.equalsIgnoreCase(SplitSelf.translate("chat.splitself.prompt.help").getString())) {playerManager.broadcast(Text.literal("<" + player.getName().getString() + "> " + SplitSelf.translate("chat.splitself.response.help").getString()), false);}
                else if (message.equalsIgnoreCase(SplitSelf.translate("chat.splitself.prompt.absence").getString())) {playerManager.broadcast(Text.literal("<" + player.getName().getString() + "> " + SplitSelf.translate("chat.splitself.response.absence").getString()), false);}
                else if (message.equalsIgnoreCase(SplitSelf.translate("chat.splitself.prompt.hello").getString()) || message.equalsIgnoreCase(SplitSelf.translate("chat.splitself.prompt.hello_alt").getString())) {playerManager.broadcast(Text.literal("<" + player.getName().getString() + "> " + SplitSelf.translate("chat.splitself.response.hello").getString()), false);}
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
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

        if (player.getWorld() == player.getServer().getWorld(DimensionRegistry.LIMBO_DIMENSION_KEY)) {return;} // no events in limbo dimension

        if (player != null) {
            if (!BypassWarning && !tracker.getPlayerReadWarning(player.getUuid())) {
                SplitSelf.LOGGER.warn("Tried executing an event, but " + player + " did not read the warning!");
                return;
            }
        }

        Events eventType;
        if (ForceEvent == null) {
            Random javaRandom = new Random(world.getRandom().nextLong());
            eventType = selectWeightedEvent(javaRandom);
            System.out.println(eventType);
        } else try {
            eventType = ForceEvent;
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Running Event: " + eventType);

        Position[] newPositions;
        int arrayLength;
        try {
            newPositions = new Position[TheOtherSpawner.spawnPositions.length + 1];
            arrayLength = TheOtherSpawner.spawnPositions.length;
            System.arraycopy(TheOtherSpawner.spawnPositions, 0, newPositions, 0, arrayLength);
        } catch (Exception e) {
            newPositions = new Position[1];
            arrayLength = 0;
        }
        newPositions[arrayLength] = Objects.requireNonNull(world.getRandomAlivePlayer()).getPos();
        TheOtherSpawner.spawnPositions = newPositions;

        MinecraftClient client = MinecraftClient.getInstance();
        switch (eventType) {
            case SPAWNTHEOTHER:
                TheOtherSpawner.trySpawnTheOther(world, player);
                break;
            case POEMSCREEN:
                client.execute(() -> client.setScreen(new PoemScreen()));
                break;
            case DOYOUSEEME:
                BackgroundManager.setBackground("/assets/splitself/textures/wallpaper/doyouseeme.png", "doyouseeme.png");
                break;
            case UNDERGROUNDMINING:
                UndergroundMining.Execute(player, world);
                break;
            case REDSKY:
                world.playSound(null, Objects.requireNonNull(player).getBlockPos(), ModSounds.REDSKY, SoundCategory.MASTER, 1.0f, 1.0f);
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, 430, 1, false, false, false));
                SkyColor.changeSkyColor("AA0000");
                SkyColor.changeFogColor("880000");
                break;
            case NOTEPAD:
                Text[] notepadMessages = {
                        SplitSelf.translate("events.splitself.notepad.line1", EventManager.getName(client.player)),
                        SplitSelf.translate("events.splitself.notepad.line2"),
                        SplitSelf.translate("events.splitself.notepad.line3"),
                        SplitSelf.translate("events.splitself.notepad.line4"),
                        SplitSelf.translate("events.splitself.notepad.line5"),
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
                                Text[] screenshotMessages = {
                                        SplitSelf.translate("events.splitself.theOtherScreenshot.line1"),
                                        SplitSelf.translate("events.splitself.theOtherScreenshot.line2")
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
                        world.playSound(null, Objects.requireNonNull(player).getBlockPos(), ModSounds.STATICSCREAM, SoundCategory.MASTER, 0.6f, 1.0f);
                        ScreenOverlay.executeFrozenScreen(file);
                    });
                })).start();
                break;
            case HOUSE:
                StructureManager.placeStructureRandomRotation(world, player, "house", 50, 80, -5, false);
                break;
            case BEDROCKPILLAR:
                for (int i = 0; i <= 30; i++) {
                    StructureManager.placeStructureRandomRotation(world, player, "bedrockpillar", 50, 80, 0, false);
                }
                break;
            case BILLY:
                new Thread(() -> {
                    try {
                        assert client.getServer() != null;
                        client.getServer().getPlayerManager().broadcast(Text.literal(SplitSelf.translate("events.splitself.billy.joined").getString()).formatted(Formatting.YELLOW), false);
                        Thread.sleep(3000);
                        client.getServer().getPlayerManager().broadcast(Text.literal(SplitSelf.translate("events.splitself.billy.message").getString()), false);
                        Thread.sleep(1500);
                        client.getServer().getPlayerManager().broadcast(Text.literal(SplitSelf.translate("events.splitself.billy.left").getString()).formatted(Formatting.YELLOW), false);
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
                    try {
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
                } else if (os.contains("nux") || os.contains("nix")) {
                    String[] terminals = {
                            "x-terminal-emulator", "gnome-terminal", "konsole",
                            "xfce4-terminal", "xterm", "lxterminal", "mate-terminal",
                            "alacritty", "tilix"
                    };
                    boolean opened = false;
                    for (String term : terminals) {
                        try {
                            new ProcessBuilder(term).start();
                            opened = true;
                            break;
                        } catch (IOException ignored) {}
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
                world.playSound(null, Objects.requireNonNull(player).getBlockPos(), SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.MASTER, 1.0f, 1.0f);
                TNTSpawner.spawnTntInCircle(player, 1.5, 8, 300);
                break;
            case IRONTRAP:
                StructureManager.placeStructureRandomRotation(world, player, "irontrap", 50, 80, -2, false);
                break;
            case LAVA:
                assert player != null;
                BlockPos pos = new BlockPos((int) player.getPos().x, 250, (int) player.getPos().z);
                player.getWorld().setBlockState(pos, Blocks.LAVA.getDefaultState());
                break;
            case BROWSER:
                new Thread(() -> {
                    try {
                        List<HistoryEntry> history = BrowserHistoryReader.getHistory();
                        List<HistoryEntry> mostVisited = BrowserHistoryReader.getMostVisited();
                        System.out.println(mostVisited);
                        assert player != null;
                        assert client.getServer() != null;
                        client.getServer().getPlayerManager().broadcast(Text.literal(SplitSelf.translate("events.splitself.browser.hello", player.getName().getString()).getString()), false);
                        Thread.sleep(3000);
                        client.getServer().getPlayerManager().broadcast(Text.literal(SplitSelf.translate("events.splitself.browser.seeMe", player.getName().getString()).getString()), false);
                        Thread.sleep(5000);
                        client.getServer().getPlayerManager().broadcast(Text.literal(SplitSelf.translate("events.splitself.browser.iAmYou", player.getName().getString()).getString()), false);
                        Thread.sleep(3000);
                        client.getServer().getPlayerManager().broadcast(Text.literal(SplitSelf.translate("events.splitself.browser.iSeeEverything", player.getName().getString()).getString()), false);
                        Thread.sleep(4000);
                        client.getServer().getPlayerManager().broadcast(Text.literal(SplitSelf.translate("events.splitself.browser.browserName", player.getName().getString(), history.getFirst().browser).getString()), false);
                        Thread.sleep(4000);
                        String[] siteName = history.getFirst().title.split(" - ");
                        client.getServer().getPlayerManager().broadcast(Text.literal(SplitSelf.translate("events.splitself.browser.displayRecentSite", player.getName().getString(), siteName[0]).getString()), false);
                        Thread.sleep(3000);
                        String[] siteName2 = mostVisited.getFirst().title.split(" - ");
                        client.getServer().getPlayerManager().broadcast(Text.literal(SplitSelf.translate("events.splitself.browser.displayPopularSite", player.getName().getString(), siteName2[0]).getString()), false);
                        Thread.sleep(5000);
                        client.getServer().getPlayerManager().broadcast(Text.literal(SplitSelf.translate("events.splitself.browser.displaySiteCount", player.getName().getString(), mostVisited.getFirst().visitCount).getString()), false);
                        Thread.sleep(4000);
                        client.getServer().getPlayerManager().broadcast(Text.literal(SplitSelf.translate("events.splitself.browser.imWatching", player.getName().getString()).getString()).formatted(Formatting.RED), false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
                break;
            case KICK:
                client.execute(() -> client.setScreen(new KickScreen()));
                break;
            case SIGN:
                world.setBlockState(player.getBlockPos(), Blocks.OAK_SIGN.getDefaultState());
                BlockEntity blockEntity = world.getBlockEntity(player.getBlockPos());
                if (blockEntity instanceof SignBlockEntity signBlockEntity) {
                    String[] availableSignTexts = {
                            SplitSelf.translate("events.splitself.sign.helloThere").getString(),
                            SplitSelf.translate("events.splitself.sign.imWatchingYou").getString(),
                            SplitSelf.translate("events.splitself.sign.letMeFree").getString(),
                            SplitSelf.translate("events.splitself.sign.imImprisoned").getString(),
                            SplitSelf.translate("events.splitself.sign.imAHostage").getString(),
                            SplitSelf.translate("events.splitself.sign.stopThis").getString(),
                            SplitSelf.translate("events.splitself.sign.cantEscape").getString(),
                            SplitSelf.translate("events.splitself.sign.letMeOut").getString(),
                            SplitSelf.translate("events.splitself.sign.pleaseListen").getString(),
                            SplitSelf.translate("events.splitself.sign.helpMe").getString(),
                            SplitSelf.translate("events.splitself.sign.iSeeYou").getString(),
                            SplitSelf.translate("events.splitself.sign.iHearYou").getString(),
                            SplitSelf.translate("events.splitself.sign.imComing").getString(),
                            SplitSelf.translate("events.splitself.sign.youTookItAll").getString(),
                            SplitSelf.translate("events.splitself.sign.helloPlayer", player.getName().getString()).getString(),
                            SplitSelf.translate("events.splitself.sign.itHurtsHere").getString(),
                            SplitSelf.translate("events.splitself.sign.iWantLife").getString(),
                            SplitSelf.translate("events.splitself.sign.giveMeLife").getString(),
                            SplitSelf.translate("events.splitself.sign.seeYouSoon").getString(),
                            SplitSelf.translate("events.splitself.sign.iKnowYou").getString(),
                            SplitSelf.translate("events.splitself.sign.triedEscaping").getString(),
                            SplitSelf.translate("events.splitself.sign.failedToLeave").getString(),
                            SplitSelf.translate("events.splitself.sign.getOutMyHouse").getString(),
                            SplitSelf.translate("events.splitself.sign.imYou").getString(),
                            SplitSelf.translate("events.splitself.sign.redacted").getString(),
                            SplitSelf.translate("events.splitself.sign.giveMeFreedom").getString()
                    };

                    Random signRandom = new Random();

                    SignText newSignText = signBlockEntity.getText(true)
                            .withMessage(0, Text.literal(availableSignTexts[signRandom.nextInt(availableSignTexts.length)]))
                            .withMessage(1, Text.literal(availableSignTexts[signRandom.nextInt(availableSignTexts.length)]))
                            .withMessage(2, Text.literal(availableSignTexts[signRandom.nextInt(availableSignTexts.length)]))
                            .withMessage(3, Text.literal(availableSignTexts[signRandom.nextInt(availableSignTexts.length)]));
                    signBlockEntity.setText(newSignText, true);
                    signBlockEntity.markDirty();
                    world.updateListeners(player.getBlockPos(), blockEntity.getCachedState(), blockEntity.getCachedState(), Block.NOTIFY_ALL);
                }
                break;
            case SCALE:
                new Thread(() -> {
                    world.playSound(null, Objects.requireNonNull(player).getBlockPos(), ModSounds.BUZZ, SoundCategory.MASTER, 1.0f, 1.0f);
                    Double OldScale = client.options.getChatScale().getValue();
                    for (int i = 0; i <= 200; i++) {
                        if (i % 5 == 0) {
                            client.getServer().getPlayerManager().broadcast(Text.literal("<" + player.getName().getString() + "> " + SplitSelf.translate("events.splitself.scale.message").getString()), false);
                        }
                        try {
                            client.options.getChatScale().setValue(Math.random());
                            Thread.sleep(25);
                        } catch (Exception e) {
                            System.out.println("Failed Scale Event: Current Chat Scale: " + client.options.getChatScale());
                        }
                    }
                    ChatHud chatHud = client.inGameHud.getChatHud();
                    chatHud.clear(true);
                    client.options.getChatScale().setValue(OldScale);
                }).start();
                break;
            case CAMERA:
                new Thread(() -> {
                    for (int i = 0; i <= 400; i++) {
                        try {
                            client.player.setYaw(client.player.getYaw() + (int) ((Math.random() * 6) - 3));
                            client.player.setPitch(client.player.getPitch() + (int) ((Math.random() * 6) - 3));
                            Thread.sleep(25);
                        } catch (Exception ignored) {}
                    }
                }).start();
                break;
            case FREEDOM:
                new Thread(() -> {
                    try {
                        ProcessBuilder pb = null;
                        if (System.getProperty("os.name").toLowerCase().contains("win")) { // aint gonna lie, ai mostly generated this, aint no way am i understanding all this
                            String script = String.join("; ",
                                    "Add-Type -AssemblyName System.Windows.Forms",
                                    "Add-Type -AssemblyName System.Drawing",
                                    "$form = New-Object System.Windows.Forms.Form",
                                    "$form.FormBorderStyle = 'None'",
                                    "$form.WindowState = 'Maximized'",
                                    "$form.TopMost = $true",
                                    "$form.BackColor = 'DarkRed'",
                                    "$form.Opacity = 0.5",
                                    "$form.ShowInTaskbar = $false",
                                    "$form.Cursor = 'None'",
                                    "$label = New-Object System.Windows.Forms.Label",
                                    "$label.Text = '" + SplitSelf.translate("events.splitself.freedom.message").getString() + "'",
                                    "$label.TextAlign = 'MiddleCenter'",
                                    "$label.Font = New-Object System.Drawing.Font('Ink Free', 32, [System.Drawing.FontStyle]::Regular)",
                                    "$label.ForeColor = 'Red'",
                                    "$label.BackColor = 'Transparent'",
                                    "$label.AutoSize = $true",
                                    "$form.Controls.Add($label)",
                                    "$form.Show()",
                                    "$player = New-Object System.Media.SoundPlayer('C:\\Windows\\Media\\Windows Information Bar.wav')",
                                    "$centerX = ($form.Width - $label.Width) / 2",
                                    "$centerY = ($form.Height - $label.Height) / 2",
                                    "$shakeTimer = New-Object System.Windows.Forms.Timer",
                                    "$shakeTimer.Interval = 50",
                                    "$random = New-Object System.Random",
                                    "$shakeTimer.Add_Tick({",
                                    "  $shakeX = $random.Next(-40, 41)",
                                    "  $shakeY = $random.Next(-40, 41)",
                                    "  $label.Location = New-Object System.Drawing.Point(($centerX + $shakeX), ($centerY + $shakeY))",
                                    "  $player.Play()",
                                    "})",
                                    "$shakeTimer.Start()",
                                    "$timer = New-Object System.Windows.Forms.Timer",
                                    "$timer.Interval = 5000",
                                    "$timer.Add_Tick({$form.Close(); $timer.Stop()})",
                                    "$timer.Start()",
                                    "while($form.Visible){[System.Windows.Forms.Application]::DoEvents(); Start-Sleep -Milliseconds 50}"
                            );

                            pb = new ProcessBuilder("powershell.exe", "-WindowStyle", "Hidden", "-ExecutionPolicy", "Bypass", "-Command", script);

                        }

                        if (pb != null) {
                            pb.start();

                        }

                    } catch (Exception e) {
                        SplitSelf.LOGGER.error("System overlay failed: " + e.getMessage());
                        e.printStackTrace();
                    }
                }).start();
                break;
            case MINE:
                BlockPos structurePos = StructureManager.placeStructureRandomRotation(world, player, "stripmine", 0, 20, -80, true);
                BlockPos signPos = new BlockPos(structurePos.getX() + 5, structurePos.getY() + 5, structurePos.getZ() + 7);
                BlockEntity mineBlockEntity = world.getBlockEntity(signPos);
                if (mineBlockEntity instanceof SignBlockEntity signBlockEntity) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yy");
                    String formattedDate = LocalDate.now().minusMonths(6).minusDays(17).format(formatter);
                    SignText newSignText = signBlockEntity.getText(true)
                            .withMessage(2, Text.literal("- " + getName(client.player)))
                            .withMessage(3, Text.literal(formattedDate));
                    signBlockEntity.setText(newSignText, true);
                    signBlockEntity.markDirty();
                    world.updateListeners(player.getBlockPos(), mineBlockEntity.getCachedState(), mineBlockEntity.getCachedState(), Block.NOTIFY_ALL);
                } else {
                    System.out.println("Got block: " + world.getBlockState(signPos));
                    System.out.println("Got block at pos: " + signPos.getX() + ", " + signPos.getY() + ", " + signPos.getZ());
                }
                break;
            case DOOR:
                List<BlockPos> doorPositions = new ArrayList<>();
                BlockPos playerPos = player.getBlockPos();
                for (int x = -30; x <= 30; x++) {
                    for (int y = -30; y <= 30; y++) {
                        for (int z = -30; z <= 30; z++) {
                            BlockPos checkPos = playerPos.add(x, y, z);
                            BlockState state = world.getBlockState(checkPos);
                            if (state.getBlock() instanceof DoorBlock &&
                                    state.get(DoorBlock.HALF) == DoubleBlockHalf.LOWER) {
                                doorPositions.add(checkPos);
                            }
                        }
                    }
                }
                new Thread(() -> {
                    try {
                        for (int cycle = 0; cycle < 50; cycle++) {
                            for (BlockPos bottomDoorPos : doorPositions) {
                                BlockPos topDoorPos = bottomDoorPos.up();

                                BlockState bottomState = world.getBlockState(bottomDoorPos);
                                BlockState topState = world.getBlockState(topDoorPos);

                                if (bottomState.getBlock() instanceof DoorBlock && topState.getBlock() instanceof DoorBlock && new Random().nextBoolean()) {
                                    boolean isOpen = bottomState.get(DoorBlock.OPEN);
                                    world.setBlockState(bottomDoorPos, bottomState.with(DoorBlock.OPEN, !isOpen));
                                    world.setBlockState(topDoorPos, topState.with(DoorBlock.OPEN, !isOpen));
                                    SoundEvent sound = isOpen ? SoundEvents.BLOCK_WOODEN_DOOR_CLOSE : SoundEvents.BLOCK_WOODEN_DOOR_OPEN;
                                    world.playSound(null, bottomDoorPos, sound, SoundCategory.BLOCKS, 1.0f, 1.0f);
                                }
                            }
                            Thread.sleep(100);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
                break;
            case SHRINK:
                new Thread(() -> {
                    try {
                        world.playSound(null, Objects.requireNonNull(player).getBlockPos(), ModSounds.RUMBLE2, SoundCategory.MASTER, 1.0f, 1.0f);
                        client.getServer().getPlayerManager().broadcast(Text.literal("<" + player.getName().getString() + "> " + SplitSelf.translate("events.splitself.shrink.message").getString()), false);
                        WINDOW_MANIPULATION_ACTIVE = true;
                        if (client.options.getFullscreen().getValue()) {
                            client.execute(() -> client.options.getFullscreen().setValue(false));
                            while (client.getWindow().isFullscreen()) {
                                Thread.sleep(50);
                            }
                        }
                        long glfwWindow = client.getWindow().getHandle();
                        int[] width = new int[1];
                        int[] height = new int[1];
                        GLFW.glfwGetWindowSize(glfwWindow, width, height);
                        int originalWidth = width[0];
                        int originalHeight = height[0];
                        int minWidth = originalWidth / 2;
                        int minHeight = originalHeight / 2;
                        long monitor = GLFW.glfwGetPrimaryMonitor();
                        GLFWVidMode vidMode = GLFW.glfwGetVideoMode(monitor);
                        int screenWidth = vidMode.width();
                        int screenHeight = vidMode.height();
                        int steps = 200;
                        for (int i = 0; i < steps; i++) {
                            float progress = (float) i / steps;
                            int currentWidth = (int) (originalWidth - (originalWidth - minWidth) * progress);
                            int currentHeight = (int) (originalHeight - (originalHeight - minHeight) * progress);
                            int xPos = (screenWidth - currentWidth) / 2;
                            int yPos = (screenHeight - currentHeight) / 2;
                            GLFW.glfwSetWindowSize(glfwWindow, currentWidth, currentHeight);
                            GLFW.glfwSetWindowPos(glfwWindow, xPos, yPos);
                            Thread.sleep(20);
                        }
                        Random shakeRandom = new Random();
                        int shakeIntensity = 7;
                        int shakeSteps = 200;

                        for (int i = 0; i < shakeSteps; i++) {
                            int[] currentPosX = new int[1];
                            int[] currentPosY = new int[1];
                            GLFW.glfwGetWindowPos(glfwWindow, currentPosX, currentPosY);

                            int shakeX = currentPosX[0] + shakeRandom.nextInt(shakeIntensity * 2) - shakeIntensity;
                            int shakeY = currentPosY[0] + shakeRandom.nextInt(shakeIntensity * 2) - shakeIntensity;
                            GLFW.glfwSetWindowPos(glfwWindow, shakeX, shakeY);
                            Thread.sleep(20);
                        }
                        client.getSoundManager().stopSounds(ModSounds.RUMBLE2.getId(), SoundCategory.MASTER);
                    } catch (Exception e) {
                        SplitSelf.LOGGER.error("Shrink event failed: " + e.getMessage());
                        e.printStackTrace();
                    } finally {
                        WINDOW_MANIPULATION_ACTIVE = false;
                    }
                }).start();
                break;
        }
    }
}
