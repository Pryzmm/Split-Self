package com.pryzmm.splitself.events;

import com.pryzmm.minemessage.MineMessage;
import com.pryzmm.splitself.SplitSelf;
import com.pryzmm.splitself.block.ModBlocks;
import com.pryzmm.splitself.client.SplitSelfClient;
import com.pryzmm.splitself.data.ClientData;
import com.pryzmm.splitself.entity.client.TheForgottenSpawner;
import com.pryzmm.splitself.entity.client.TheOtherSpawner;
import com.pryzmm.splitself.events.helper.*;
import com.pryzmm.splitself.file.*;
import com.pryzmm.splitself.item.ModItems;
import com.pryzmm.splitself.mixin.WolfMixin;
import com.pryzmm.splitself.packet.packets.GlitchEventPacket;
import com.pryzmm.splitself.packet.packets.KickScreenPacket;
import com.pryzmm.splitself.packet.packets.ScreenshotPacket;
import com.pryzmm.splitself.screen.PoemScreen;
import com.pryzmm.splitself.screen.misc.BlendManager;
import com.pryzmm.splitself.screen.misc.SkyImageRenderer;
import com.pryzmm.splitself.sound.ModSounds;
import com.pryzmm.splitself.world.ClientTickScheduler;
import com.pryzmm.splitself.world.TickScheduler;
import dev.firstdark.rpc.models.DiscordRichPresence;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
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
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.passive.WolfVariants;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

public class EventRunner {

    public static void runClientEvent(MinecraftClient client, ClientPlayerEntity player, EventManager.Events event) {

        String os = Util.getOperatingSystem().toString().toLowerCase();

        switch (event) {
            case POEMSCREEN -> client.execute(() -> client.setScreen(new PoemScreen()));
            case DOYOUSEEME -> SplitSelf.LOGGER.info("Tried running DOYOUSEEME event in the safe version.");
            case REDSKY -> {
                player.playSound(ModSounds.REDSKY, 1f, 1.0f);
                SkyColor.changeSkyColor("AA0000");
                SkyColor.changeFogColor("880000");
                SkyColor.changeDistantSkyColor("880000");
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, 430, 1, false, false, false));
            }
            case NOTEPAD -> {
                Text[] notepadMessages = {
                    Text.translatable("events.splitself.notepad.line1", EventManager.getName(client.player)),
                    Text.translatable("events.splitself.notepad.line2"),
                    Text.translatable("events.splitself.notepad.line3"),
                    Text.translatable("events.splitself.notepad.line4"),
                    Text.translatable("events.splitself.notepad.line5"),
                };
                NotepadManager.execute(notepadMessages);
            }
            case SCREENOVERLAY -> ScreenOverlay.executeBlackScreen(player);
            case WHITESCREENOVERLAY -> ScreenOverlay.executeWhiteScreen(player);
            case INVENTORYOVERLAY -> ScreenOverlay.executeInventoryScreen(player);
            case FROZENSCREEN -> {
                EntityScreenshotCapture capture = new EntityScreenshotCapture();
                capture.captureFromEntity(player, client.getWindow().getScaledWidth(), client.getWindow().getScaledHeight(), (file) -> {
                    player.playSound(ModSounds.STATICSCREAM, 1f, 1.0f);
                    ScreenOverlay.executeFrozenScreen(file);
                });
            }
            case FACE -> SkyImageRenderer.toggleTexture();
            case COMMAND -> SplitSelf.LOGGER.info("Tried running COMMAND event in the safe version.");
            case PAUSE -> EventManager.PAUSE_SHAKE = true;
            case INVERT -> new Thread(() -> {
                try {
                    client.options.getInvertYMouse().setValue(true);
                    Thread.sleep(60000);
                    if (client.options.getInvertYMouse().getValue() == true) {
                        client.options.getInvertYMouse().setValue(false);
                    }
                } catch (Exception e) { throw new RuntimeException(e); }
            }).start();
            case EMERGENCY -> {
                CityLocator geoLocation;
                String city;
                try {
                    geoLocation = new CityLocator();
                    city = geoLocation.getCityFromCurrentIP();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                ScreenOverlay.executeEmergencyScreen(player, city);
            }
            case STATIC -> ScreenOverlay.executeStaticScreen(player);
            case RECURSIVE -> ScreenOverlay.executeRecursiveScreen(player, 2500, true);
            case BROWSER -> {
                List<BrowserHistoryReader.HistoryEntry> history = BrowserHistoryReader.getHistory();
                List<BrowserHistoryReader.HistoryEntry> mostVisited = BrowserHistoryReader.getMostVisited();
                if (history == null || history.isEmpty()) return;
                player.sendMessage(Text.translatable("events.splitself.browser.hello", player.getName().getString()), false);
                ClientTickScheduler.schedule(60, () -> player.sendMessage(Text.translatable("events.splitself.browser.seeMe", player.getName().getString()), false));
                ClientTickScheduler.schedule(160, () -> player.sendMessage(Text.translatable("events.splitself.browser.iAmYou", player.getName().getString()), false));
                ClientTickScheduler.schedule(240, () -> player.sendMessage(Text.translatable("events.splitself.browser.iSeeEverything", player.getName().getString()), false));
                ClientTickScheduler.schedule(320, () -> player.sendMessage(Text.translatable("events.splitself.browser.browserName", player.getName().getString(), history.getFirst().browser), false));
                ClientTickScheduler.schedule(400, () -> {
                    String[] siteName = history.getFirst().title.split(" - ");
                    String siteURL = history.getFirst().url.replaceFirst("https://", "").split("/")[0];
                    player.sendMessage(Text.translatable("events.splitself.browser.displayRecentSite", player.getName().getString(), siteName[0]), false);
                    ClientTickScheduler.schedule(60, () -> {
                        String mostVisitedSiteURL;
                        int mostVisitedSiteCount;
                        int browserIndex;
                        for (browserIndex = 0; browserIndex < 50; browserIndex++) {
                            if (mostVisited.get(browserIndex).url.replaceFirst("https://", "").split("/")[0].equals(siteURL)) {
                                SplitSelf.LOGGER.warn("Skipping index {}", browserIndex);
                                SplitSelf.LOGGER.warn("{}     {}", siteURL, mostVisited.get(browserIndex).url.replaceFirst("https://", "").split("/")[0]);
                            } else break;
                        }
                        mostVisitedSiteURL = mostVisited.get(browserIndex).url.replaceFirst("https://", "").split("/")[0];
                        mostVisitedSiteCount = mostVisited.get(browserIndex).visitCount;
                        player.sendMessage(Text.translatable("events.splitself.browser.displayPopularSite", player.getName().getString(), mostVisitedSiteURL), false);
                        ClientTickScheduler.schedule(100, () -> player.sendMessage(Text.translatable("events.splitself.browser.displaySiteCount", player.getName().getString(), mostVisitedSiteCount), false));
                        ClientTickScheduler.schedule(180, () -> player.sendMessage(Text.translatable("events.splitself.browser.imWatching", player.getName().getString()).formatted(Formatting.RED), false));
                    });
                });
            }
            case MEMORY -> CompletableFuture.runAsync(() -> {
                if (os.contains("mac")) {
                    SplitSelf.LOGGER.error("Tried running event MEMORY but it is not supported on Mac devices!");
                    return;
                }
                try { client.execute(SwingUtil::launchApp); }
                catch (Throwable t) { SplitSelf.LOGGER.error("MEMORY event failed", t); }
            });
            case CLIPBOARD -> client.keyboard.setClipboard(Text.translatable("events.splitself.clipboard").getString());
            case RPC -> {
                if (SplitSelfClient.RPCInitialized)
                    SplitSelfClient.RPC.updatePresence(DiscordRichPresence.builder()
                        .details(Text.translatable("events.splitself.rpc.desc").getString())
                        .state(Text.translatable("events.splitself.rpc.state").getString())
                        .largeImageKey("noise")
                        .build());
            }
            case RECORD -> {
                String process = Processes.getScreenRecordingSoftware();
                if (process != null) {
                    player.sendMessage(Text.translatable("events.splitself.record.detection", process).formatted(Formatting.RED), false);
                    ClientTickScheduler.schedule(100, () -> player.sendMessage(Text.translatable("events.splitself.record.fail", process).formatted(Formatting.RED), false));
                }
            }
            case DISCORDNAME -> {
                if (SplitSelfClient.discordUsername != null) {
                    player.sendMessage(Text.translatable("events.splitself.discordName.friend", SplitSelfClient.discordUsername).withColor(7506394), false);
                }
            }
            case REMINDER -> CompletableFuture.runAsync(() -> {
                if (os.contains("mac")) {
                    SplitSelf.LOGGER.error("Tried running event REMINDER but it is not supported on Mac devices!");
                    return;
                }
                if (!SystemTray.isSupported()) SplitSelf.LOGGER.warn("SystemTray not supported on this platform");
                SystemTray tray = SystemTray.getSystemTray();
                Image image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
                TrayIcon trayIcon = new TrayIcon(image, "Messenger");
                trayIcon.setImageAutoSize(true);
                try { tray.add(trayIcon); } catch (Exception ignored) {}
                trayIcon.addActionListener(e -> {
                    try {
                        MinecraftClient.getInstance().execute(() -> MineMessage.main(new String[]{}));
                    } catch (Throwable t) {
                        SplitSelf.LOGGER.error("REMINDER event failed", t);
                    }
                });

                trayIcon.displayMessage(
                    "Messages",
                    "You have an unread message from ████████████",
                    TrayIcon.MessageType.INFO
                );
            });
            case MEMORIES -> DesktopFileUtil.cloneFileToDesktop(Identifier.of(SplitSelf.MOD_ID, "textures/misc/memories.png"));
            case MORSE -> DesktopFileUtil.cloneFileToDesktop(Identifier.of(SplitSelf.MOD_ID, "textures/misc/morse.png"));
            case LOGS -> {
                String resourcePath = "data/splitself/saved_text/logs.txt";
                try {
                    InputStream inputStream = EventManager.class.getClassLoader().getResourceAsStream(resourcePath);
                    if (inputStream == null) {
                        SplitSelf.LOGGER.error("Resource not found: {}", resourcePath);
                    }
                    StringBuilder content = new StringBuilder();
                    assert inputStream != null;
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            content.append(line).append("\n");
                        }
                    }
                    for (int i = 1; i < 33; i++) {
                        content = new StringBuilder(content.toString().replaceFirst("files.splitself.log.message" + i, Text.translatable("files.splitself.log.message" + i).getString()));
                    }
                    DesktopFileUtil.createFileOnDesktop("latest.log", content.toString().replace("PLAYERNAME", player.getName().getString()));
                } catch (IOException e) {
                    SplitSelf.LOGGER.error("Error reading resource file: {}", resourcePath, e);
                }
            }
            case DISCONNECT -> {
                player.sendMessage(Text.translatable("events.splitself.disconnect.left", player.getName().getString()).formatted(Formatting.YELLOW), false);
                ClientTickScheduler.schedule(2, () -> player.sendMessage(Text.translatable("events.splitself.disconnect.joined", player.getName().getString()).formatted(Formatting.YELLOW), false));
                ClientTickScheduler.schedule(36, () -> player.sendMessage(Text.translatable("events.splitself.disconnect.left", player.getName().getString()).formatted(Formatting.YELLOW), false));
                ClientTickScheduler.schedule(38, () -> player.sendMessage(Text.translatable("events.splitself.disconnect.joined", player.getName().getString()).formatted(Formatting.YELLOW), false));
                ClientTickScheduler.schedule(116, () -> player.sendMessage(Text.translatable("events.splitself.disconnect.left", player.getName().getString()).formatted(Formatting.YELLOW), false));
                ClientTickScheduler.schedule(118, () -> player.sendMessage(Text.translatable("events.splitself.disconnect.joined", player.getName().getString()).formatted(Formatting.YELLOW), false));
            }
            case EJECT -> SplitSelf.LOGGER.info("Tried running EJECT event in the safe version.");
            case SCALE -> {
                EventManager.ACTIVE_EVENT = true;
                new Thread(() -> {
                    player.playSound(ModSounds.BUZZ, 1.0f, 1.0f);
                    Double OldScale = client.options.getChatScale().getValue();
                    for (int i = 0; i <= 200; i++) {
                        if (i % 5 == 0) {
                            player.sendMessage(Text.literal("<" + player.getName().getString() + "> " + Text.translatable("events.splitself.scale.message").getString()), false);
                        }
                        try {
                            client.options.getChatScale().setValue(Math.random());
                            Thread.sleep(25);
                        } catch (Exception e) {
                            SplitSelf.LOGGER.error("Failed Scale Event: Current Chat Scale: {}", client.options.getChatScale());
                        }
                    }
                    ChatHud chatHud = client.inGameHud.getChatHud();
                    chatHud.clear(true);
                    client.options.getChatScale().setValue(OldScale);
                    EventManager.ACTIVE_EVENT = false;
                }).start();
            }
            case SHRINK -> {
                if (client.options.getFullscreen().getValue()) client.getWindow().toggleFullscreen();
                player.playSound(ModSounds.RUMBLE2, 1.0f, 1.0f);
                new Thread(() -> {
                    try {
                        for (int i = 0; i < 100; i++) {
                            if (!client.getWindow().isFullscreen()) break;
                            SplitSelf.LOGGER.warn("Game is still in fullscreen, waiting 50 milliseconds... attempt: {}", i + 1);
                            Thread.sleep(50);
                        }
                        if (client.getWindow().isFullscreen()) {
                            SplitSelf.LOGGER.error("Failed to un-fullscreen user's screen after 5 seconds!");
                            return;
                        }
                        assert client.player != null;
                        player.sendMessage(Text.literal("<" + player.getName().getString() + "> " + Text.translatable("events.splitself.shrink.message").getString()), false);
                        EventManager.WINDOW_MANIPULATION_ACTIVE = true;
                        ClientScreenSizer.runShrinkAnimation(client, player);
                    } catch (Exception e) {
                        SplitSelf.LOGGER.error("Shrink event failed: {} {}", e.getMessage(), e);
                        EventManager.WINDOW_MANIPULATION_ACTIVE = false;
                    }
                }).start();
            }
            case FREEZE -> {
                player.playSound(SoundEvents.ITEM_OMINOUS_BOTTLE_DISPOSE, 1.0f, 1.0f);
                client.execute(() -> {
                    try { Thread.sleep(2000); }
                    catch (Exception e) { SplitSelf.LOGGER.error("Freeze event failed: {}", e.getMessage(), e); }
                });
            }
            case WHISPER -> player.playSound(ModSounds.WHISPER, 40.0f, 1.0f);
            case NAME -> {
                SplitSelf.LOGGER.info("Player name: {}", player.getName().getString());
                SplitSelf.LOGGER.info("Player UUID: {}", player.getUuidAsString());
                new Thread(() -> {
                    try {
                        List<String> nameHistory = AshconNameAPI.getNameHistory(player.getUuidAsString());
                        if (!nameHistory.isEmpty()) {
                            for (String name : nameHistory) {
                                if (!name.equals(player.getName().getString())) {
                                    player.sendMessage(Text.literal("<" + name + "> " + Text.translatable("events.splitself.sign.imWatchingYou").getString()), false);
                                    break;
                                }
                            }
                        }
                    } catch (Exception e) {
                        SplitSelf.LOGGER.error("Failed to fetch name history: {}", e.getMessage());
                    }
                }).start();
            }
            case FREEDOM -> {
                SplitSelf.LOGGER.info("Tried running FREEDOM in the safe version.");
            }
            case RENAME -> {
                client.getWindow().setTitle(Text.translatable("events.splitself.rename").getString());
                EventHelper.preventTitleChange = true;
            }
            case FOV -> {
                EventManager.ACTIVE_EVENT = true;
                new Thread(() -> {
                    Integer OldScale = client.options.getFov().getValue();
                    for (int i = 0; i <= 500; i++) {
                        try {
                            client.options.getFov().setValue((int) (client.options.getFov().getValue() + Math.floor(Math.random() * 3) - 1));
                            Thread.sleep(25);
                        } catch (Exception e) { SplitSelf.LOGGER.error("Failed Scale Event: Current FOV: {}", client.options.getFov()); }
                    }
                    client.options.getFov().setValue(OldScale);
                    EventManager.ACTIVE_EVENT = false;
                }).start();
            }
            case BRIGHTNESS -> {
                EventManager.ACTIVE_EVENT = true;
                new Thread(() -> {
                    Double OldGamma = client.options.getGamma().getValue();
                    for (int i = 0; i <= 500; i++) {
                        try {
                            client.options.getGamma().setValue(Math.random() / 2);
                            Thread.sleep(25);
                        } catch (Exception e) { SplitSelf.LOGGER.error("Failed Brightness Event: Current Gamma: {}", client.options.getGamma()); }
                    }
                    client.options.getGamma().setValue(OldGamma);
                    EventManager.ACTIVE_EVENT = false;
                }).start();
            }
            case ESCAPE -> EventManager.PAUSE_PREVENTION = true;
            case WEATHER -> new Thread(() -> {
                try {
                    String c;
                    if (!ClientData.getPII()) {
                        c = Text.translatable("events.splitself.redacted_name").getString();
                    } else {
                        CityLocator locator = new CityLocator();
                        c = locator.getCityFromCurrentIP();
                    }
                    player.sendMessage(Text.translatable("events.splitself.weather.report", c), false);
                    WeatherFetcher fetcher = new WeatherFetcher();
                    WeatherFetcher.WeatherData weather = fetcher.getWeather(c);
                    if (weather.condition() == null || weather.condition().isEmpty() || weather.feelsLikeC() == null || weather.feelsLikeF() == null) {
                        player.sendMessage(Text.translatable("events.splitself.weather.fail", c), false);
                    } else {
                        player.sendMessage(Text.translatable("events.splitself.weather.loading"), false);
                        Thread.sleep(200);
                        player.sendMessage(Text.translatable("events.splitself.weather.temp", weather.feelsLikeC(), weather.feelsLikeF()), false);
                        player.sendMessage(Text.translatable("events.splitself.weather.weather", weather.condition()), false);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).start();
            case PLAYERDATA -> {
                try {
                    DesktopFileUtil.cloneFileToDesktop(Identifier.of(SplitSelf.MOD_ID, "files/ce7ea4cb-0789-47c9-b536-144f836a30c2.dat_old"));
                    player.sendMessage(Text.translatable("events.splitself.playerData.1", player.getName().getString()), false);
                    ClientTickScheduler.schedule(100, () -> player.sendMessage(Text.translatable("events.splitself.playerData.2", player.getName().getString()), false));
                    ClientTickScheduler.schedule(220, () -> player.sendMessage(Text.translatable("events.splitself.playerData.3", player.getName().getString()), false));
                } catch (Exception ignored) {}
            }
            case INVERTCOLOR -> new Thread(() -> {
                try {
                    player.playSound(ModSounds.TONE, 1.0f, 0.5f);
                    BlendManager.invertBlend = true;
                    Thread.sleep(20000);
                    BlendManager.invertBlend = false;
                    client.getSoundManager().stopSounds(ModSounds.TONE.getId(), null);
                } catch (Exception ignored) {}
            }).start();
            case SPOTIFY -> { try { Util.getOperatingSystem().open("spotify:track:5MkWlSmMZnSGHLYbK2LgdM"); } catch (Exception e) { SplitSelf.LOGGER.warn("User does not have Spotify."); } }
            case SEARCH -> {
                String message = Text.translatable("events.splitself.search").getString().replace(" ", "+");
                Util.getOperatingSystem().open("https://www.google.com/search?q=" + message);
            }
        }

    }

    public static void runGlobalEvent(MinecraftServer server, ServerPlayerEntity player, EventManager.Events event) {

        ServerWorld world = player.getServerWorld();
        PlayerManager manager = server.getPlayerManager();

        switch (event) {
            case SPAWNTHEOTHER -> TheOtherSpawner.trySpawnTheOther(world, player, false);
            case UNDERGROUNDMINING -> UndergroundMining.Execute(player, world);
            case THEOTHERSCREENSHOT -> {
                TheOtherSpawner.trySpawnTheOther(world, player, true);
                for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
                    ServerPlayNetworking.send(p, new ScreenshotPacket());
                }
            }
            case DESTROYCHUNK -> ChunkDestroyer.execute(Objects.requireNonNull(player));
            case HOUSE -> StructureManager.placeStructureRandomRotation(world, player, "house", 50, 80, -5, false, 1f, true);
            case PILLAR -> {
                for (int i = 0; i <= 30; i++) StructureManager.placeStructureRandomRotation(world, player, "pillar", 50, 80, 0, false, 1f, true);
                StructureManager.placeStructureRandomRotation(world, player, "pillarmemory", 48, 48, 0, false, 1f, true);
            }
            case BILLY -> {
                server.getPlayerManager().broadcast(Text.translatable("events.splitself.billy.joined").formatted(Formatting.YELLOW), false);
                TickScheduler.schedule(60, () -> server.getPlayerManager().broadcast(Text.translatable("events.splitself.billy.message"), false));
                TickScheduler.schedule(90, () -> server.getPlayerManager().broadcast(Text.translatable("events.splitself.billy.left").formatted(Formatting.YELLOW), false));
            }
            case TNT -> {
                ServerPlayerEntity selectedPlayer = manager.getPlayerList().get((int) (Math.random() * manager.getPlayerList().size()));
                world.playSound(null, Objects.requireNonNull(selectedPlayer).getBlockPos(), SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.MASTER, 1.0f, 1.0f);
                TNTSpawner.spawnTntInCircle(selectedPlayer, 1.5, 8, 300);
            }
            case IRONTRAP -> StructureManager.placeStructureRandomRotation(world, player, "irontrap", 50, 80, -2, false, 1f, true);
            case LAVA -> {
                ServerPlayerEntity selectedPlayer = manager.getPlayerList().get((int) (Math.random() * manager.getPlayerList().size()));
                BlockPos pos = new BlockPos((int) selectedPlayer.getPos().x, 250, (int) selectedPlayer.getPos().z);
                pos = EventManager.moveBlockPosFromBase(selectedPlayer, pos);
                selectedPlayer.getServerWorld().setBlockState(pos, Blocks.LAVA.getDefaultState());
            }
            case SIGN -> {
                world.setBlockState(player.getBlockPos(), Blocks.OAK_SIGN.getDefaultState());
                BlockEntity blockEntity = world.getBlockEntity(player.getBlockPos());
                if (blockEntity instanceof SignBlockEntity signBlockEntity) {
                    Text[] availableSignTexts = {
                        Text.translatable("events.splitself.sign.helloThere"),
                        Text.translatable("events.splitself.sign.imWatchingYou"),
                        Text.translatable("events.splitself.sign.letMeFree"),
                        Text.translatable("events.splitself.sign.imImprisoned"),
                        Text.translatable("events.splitself.sign.imAHostage"),
                        Text.translatable("events.splitself.sign.stopThis"),
                        Text.translatable("events.splitself.sign.cantEscape"),
                        Text.translatable("events.splitself.sign.letMeOut"),
                        Text.translatable("events.splitself.sign.pleaseListen"),
                        Text.translatable("events.splitself.sign.helpMe"),
                        Text.translatable("events.splitself.sign.iSeeYou"),
                        Text.translatable("events.splitself.sign.iHearYou"),
                        Text.translatable("events.splitself.sign.imComing"),
                        Text.translatable("events.splitself.sign.youTookItAll"),
                        Text.translatable("events.splitself.sign.itHurtsHere"),
                        Text.translatable("events.splitself.sign.iWantLife"),
                        Text.translatable("events.splitself.sign.giveMeLife"),
                        Text.translatable("events.splitself.sign.seeYouSoon"),
                        Text.translatable("events.splitself.sign.iKnowYou"),
                        Text.translatable("events.splitself.sign.triedEscaping"),
                        Text.translatable("events.splitself.sign.failedToLeave"),
                        Text.translatable("events.splitself.sign.getOutMyHouse"),
                        Text.translatable("events.splitself.sign.imYou"),
                        Text.translatable("events.splitself.sign.redacted"),
                        Text.translatable("events.splitself.sign.giveMeFreedom")
                    };

                    Random signRandom = new Random();

                    SignText newSignText = signBlockEntity.getText(true)
                        .withMessage(0, availableSignTexts[signRandom.nextInt(availableSignTexts.length)])
                        .withMessage(1, availableSignTexts[signRandom.nextInt(availableSignTexts.length)])
                        .withMessage(2, availableSignTexts[signRandom.nextInt(availableSignTexts.length)])
                        .withMessage(3, availableSignTexts[signRandom.nextInt(availableSignTexts.length)]);
                    signBlockEntity.setText(newSignText, true);
                    signBlockEntity.markDirty();
                    world.updateListeners(player.getBlockPos(), blockEntity.getCachedState(), blockEntity.getCachedState(), Block.NOTIFY_ALL);
                }
            }
            case MINE -> {
                BlockPos structurePos = StructureManager.placeStructureRandomRotation(world, player, "stripmine", 0, 20, -80, true, 1f, true);
                assert structurePos != null;
                BlockPos signPos = new BlockPos(structurePos.getX() + 5, structurePos.getY() + 5, structurePos.getZ() + 7);
                BlockEntity mineBlockEntity = world.getBlockEntity(signPos);
                if (mineBlockEntity instanceof SignBlockEntity signBlockEntity) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yy");
                    String formattedDate = LocalDate.now().minusMonths(6).minusDays(17).format(formatter);
                    SignText newSignText = signBlockEntity.getText(true)
                            .withMessage(2, Text.literal("- " + Text.translatable("events.splitself.redacted_name"))) // Removed name since global world with PII
                            .withMessage(3, Text.literal(formattedDate));
                    signBlockEntity.setText(newSignText, true);
                    signBlockEntity.markDirty();
                    world.updateListeners(player.getBlockPos(), mineBlockEntity.getCachedState(), mineBlockEntity.getCachedState(), Block.NOTIFY_ALL);
                } else {
                    SplitSelf.LOGGER.error("Got block: {}", world.getBlockState(signPos));
                    SplitSelf.LOGGER.error("Got block at pos: {}, {}, {}", signPos.getX(), signPos.getY(), signPos.getZ());
                }
            }
            case KICK -> {
                ServerPlayerEntity selectedPlayer = manager.getPlayerList().get((int) (Math.random() * manager.getPlayerList().size()));
                ServerPlayNetworking.send(selectedPlayer, new KickScreenPacket());
            }
            case DOOR -> {
                ServerPlayerEntity selectedPlayer = manager.getPlayerList().get((int) (Math.random() * manager.getPlayerList().size()));
                List<BlockPos> doorPositions = new ArrayList<>();
                BlockPos playerPos = selectedPlayer.getBlockPos();
                for (int x = -30; x <= 30; x++) {
                    for (int y = -30; y <= 30; y++) {
                        for (int z = -30; z <= 30; z++) {
                            BlockPos checkPos = playerPos.add(x, y, z);
                            BlockState state = world.getBlockState(checkPos);
                            if (state.getBlock() instanceof DoorBlock && state.get(DoorBlock.HALF) == DoubleBlockHalf.LOWER) {
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
                        SplitSelf.LOGGER.error(e.getMessage(), e);
                    }
                }).start();
            }
            case ITEM -> {
                ServerPlayerEntity selectedPlayer = manager.getPlayerList().get((int) (Math.random() * manager.getPlayerList().size()));
                Inventory inventory = selectedPlayer.getInventory();
                for (int i = 0; i < inventory.size(); i++) {
                    ItemStack stack = inventory.getStack(i);
                    if (!stack.isEmpty() && new Random().nextInt(0, 5) == 0) {
                        inventory.setStack(i, ItemStack.EMPTY);
                        break;
                    }
                }
            }
            case FRAME -> {
                player.dropItem(ModBlocks.IMAGE_FRAME.asItem(), 1);
                world.playSound(null, Objects.requireNonNull(player).getBlockPos(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.MASTER, 1.0f, 1.0f);
            }
            case LIFT -> ChunkDestroyer.liftChunk(player, world, 1, 40);
            case SURROUND -> {
                for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) ServerPlayNetworking.send(p, new GlitchEventPacket());
                world.playSound(null, Objects.requireNonNull(player).getBlockPos(), ModSounds.GLITCH, SoundCategory.MASTER, 1.0f, 1.0f);
                ChunkDestroyer.liftChunk(player, world, 100, 14);
                for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) ServerPlayNetworking.send(p, new GlitchEventPacket());
            }
            case FORGOTTEN -> TheForgottenSpawner.trySpawnTheForgotten(world, player);
            case BLU -> {
                WolfEntity wolf = new WolfEntity(EntityType.WOLF, player.getWorld());
                wolf.setVariant(world.getRegistryManager().get(RegistryKeys.WOLF_VARIANT).getEntry(WolfVariants.ASHEN).orElseThrow());
                wolf.setOwner(player);
                wolf.setTamed(true, true);
                wolf.getDataTracker().set(WolfMixin.getCollarColorData(), DyeColor.CYAN.getId());
                wolf.setCustomName(Text.of("Blu"));
                wolf.refreshPositionAndAngles(player.getX(), player.getY(), player.getZ(), 0F, 0F);
                world.spawnEntity(wolf);
                ItemEntity item = new ItemEntity(EntityType.ITEM, player.getWorld());
                item.setStack(new ItemStack(ModItems.MEMORY_BLU, 1));
                item.refreshPositionAndAngles(player.getX(), player.getY(), player.getZ(), 0F, 0F);
                world.spawnEntity(item);
            }
            case CORAL -> {
                ServerPlayerEntity selectedPlayer = manager.getPlayerList().get((int) (Math.random() * manager.getPlayerList().size()));
                Vec3d playerPos = selectedPlayer.getPos();
                playerPos = EventManager.moveVectorFromBase(selectedPlayer, playerPos);
                // replace all blocks within a radius of playerPos with coral blocks, excluding air and containers.
                // it should dither the farther out it is
                int radius = 10;
                for (int x = -radius; x <= radius; x++) {
                    for (int y = -radius; y <= radius; y++) {
                        for (int z = -radius; z <= radius; z++) {
                            BlockPos checkPos = new BlockPos((int) (playerPos.x + x), (int) (playerPos.y + y), (int) (playerPos.z + z));
                            if (checkPos.isWithinDistance(selectedPlayer.getBlockPos(), radius)) {
                                BlockState state = world.getBlockState(checkPos);
                                if (!state.isAir()) {
                                    double distance = checkPos.getSquaredDistance(selectedPlayer.getBlockPos());
                                    double chance = 1.0 - (distance / (radius * radius));
                                    if (Math.random() < chance) {
                                        world.setBlockState(checkPos, Blocks.DEAD_BRAIN_CORAL_BLOCK.getDefaultState());
                                    }
                                }
                            }
                        }
                    }
                }
            }
            case DEADCHUNK -> ChunkDestroyer.deadChunk(player, world);
            case BRAIN -> {
                player.dropItem(ModBlocks.BRAIN.asItem(), 1);
                world.playSound(null, Objects.requireNonNull(player).getBlockPos(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.MASTER, 1.0f, 1.0f);
            }
            case BOOK -> {
                player.dropItem(ModItems.MEMORY_BOOK.asItem(), 1);
                world.playSound(null, Objects.requireNonNull(player).getBlockPos(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.MASTER, 1.0f, 1.0f);
            }
            case STATUE -> StructureManager.placeStructureRandomRotation(world, player, "statue", 110, 170, -7, false, 1f, true);
            case HALLWAY -> StructureManager.placeStructureRandomRotation(world, player, "broken_flower", 20, 40, 10, false, 1f, true);
        }
    }

}