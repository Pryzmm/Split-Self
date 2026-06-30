package com.pryzmm.splitself.events;

import com.pryzmm.memory.Memory;
import com.pryzmm.minemessage.MineMessage;
import com.pryzmm.splitself.SplitSelf;
import com.pryzmm.splitself.block.ModBlocks;
import com.pryzmm.splitself.client.SplitSelfClient;
import com.pryzmm.splitself.config.DefaultConfig;
import com.pryzmm.splitself.data.WorldData;
import com.pryzmm.splitself.entity.client.TheForgottenSpawner;
import com.pryzmm.splitself.entity.custom.TheForgottenEntity;
import com.pryzmm.splitself.events.helper.*;
import com.pryzmm.splitself.entity.ModEntities;
import com.pryzmm.splitself.entity.client.TheOtherSpawner;
import com.pryzmm.splitself.entity.custom.TheOtherEntity;
import com.pryzmm.splitself.file.*;
import com.pryzmm.splitself.file.BrowserHistoryReader.HistoryEntry;
import com.pryzmm.splitself.item.ModItems;
import com.pryzmm.splitself.mixin.WolfMixin;
import com.pryzmm.splitself.screen.KickScreen;
import com.pryzmm.splitself.screen.PoemScreen;
import com.pryzmm.splitself.screen.misc.BlendManager;
import com.pryzmm.splitself.screen.misc.SkyImageRenderer;
import com.pryzmm.splitself.sound.ModSounds;
import com.pryzmm.splitself.world.DimensionRegistry;
import dev.firstdark.rpc.models.DiscordRichPresence;
import net.minecraft.block.*;
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
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
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
        PILLAR,
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
        FREEDOM,
        MINE,
        DOOR,
        SHRINK,
        PAUSE,
        ITEM,
        FRAME,
        NAME,
        WHISPER,
        ESCAPE,
        LIFT,
        SURROUND,
        LOGS,
        DISCONNECT,
        FORGOTTEN,
        EJECT,
        FREEZE,
        BLU,
        MEMORY,
        REMINDER,
        RENAME,
        FOV,
        WEATHER,
        MEMORIES,
        MORSE,
        CORAL,
        STATIC,
        INVERTCOLOR,
        CLIPBOARD,
        RPC,
        RECORD,
        DISCORDNAME,
        DEADCHUNK,
        RECURSIVE,
        PLAYERDATA,
        BRAIN,
        BOOK,
        SPOTIFY,
        SEARCH
    }

    public static Map<Events, Boolean> oneTimeEvents = new HashMap<>(); // oneLastTime events ong

    private static int CURRENT_COOLDOWN = 0;

    public static boolean PAUSE_PREVENTION = false;
    public static boolean WINDOW_MANIPULATION_ACTIVE = false;
    public static boolean PAUSE_SHAKE = false;
    public static boolean ACTIVE_EVENT = false;

    public static Identifier CURRENT_FRAME_TEXTURE = null;

    public static boolean EVENTS_ENABLED = SplitSelf.CONFIG.getBoolean("eventsEnabled", DefaultConfig.eventsEnabled);
    public static int TICK_INTERVAL = SplitSelf.CONFIG.getInt("eventTickInterval", DefaultConfig.eventTickInterval);
    public static double EVENT_CHANCE = SplitSelf.CONFIG.getDouble("eventChance", DefaultConfig.eventChance);
    public static double START_AFTER = SplitSelf.CONFIG.getInt("startEventsAfter", DefaultConfig.startEventsAfter);
    public static double GUARANTEED_EVENT = SplitSelf.CONFIG.getInt("guaranteedEvent", DefaultConfig.guaranteedEvent);
    public static double REPEAT_EVENTS_AFTER = SplitSelf.CONFIG.getInt("repeatEventsAfter", DefaultConfig.repeatEventsAfter);
    private static final Map<Events, Integer> eventLastTriggered = new HashMap<>();
    private static int totalEventsTriggered = 0;

    public static void onTick(MinecraftServer server) {

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

            if (GUARANTEED_EVENT > 0) {GUARANTEED_EVENT--;}

            if (CURRENT_COOLDOWN > 0) {
                CURRENT_COOLDOWN--;
                return;
            }

            if (world.getTime() % TICK_INTERVAL != 0) {
                return;
            }

            if (world.getRandom().nextDouble() < EVENT_CHANCE || GUARANTEED_EVENT == 0) {
                triggerRandomEvent(world, world.getRandomAlivePlayer(), null);
                CURRENT_COOLDOWN = SplitSelf.CONFIG.getInt("eventCooldown", DefaultConfig.eventCooldown);
                GUARANTEED_EVENT = SplitSelf.CONFIG.getInt("guaranteedEvent", DefaultConfig.guaranteedEvent);
            }
        }
    }

    public static Vec3d moveVectorFromBase(PlayerEntity player, Vec3d vector) {
        if (!(player instanceof ServerPlayerEntity serverPlayer)) return vector;
        RegistryKey<World> spawnDimension = serverPlayer.getSpawnPointDimension();
        if (spawnDimension == null || !spawnDimension.equals(serverPlayer.getWorld().getRegistryKey())) return vector;
        BlockPos bedLocation = serverPlayer.getSpawnPointPosition();
        if (bedLocation == null) return vector;
        Vec3d bedCenter = new Vec3d(bedLocation.getX() + 0.5, bedLocation.getY() + 0.5, bedLocation.getZ() + 0.5);
        double baseSafeRadius = SplitSelf.CONFIG.getDouble("baseSafeRadius", DefaultConfig.baseSafeRadius);
        double dx = vector.x - bedCenter.x;
        double dz = vector.z - bedCenter.z;
        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
        if (horizontalDistance < baseSafeRadius) {
            Vec3d diff = new Vec3d(dx, 0, dz);
            if (diff.lengthSquared() < 1e-10) diff = new Vec3d(1, 0, 0);
            Vec3d direction = diff.normalize();
            return new Vec3d(bedCenter.x + direction.x * (baseSafeRadius + 1.0), vector.y, bedCenter.z + direction.z * (baseSafeRadius + 1.0));
        }
        return vector;
    }

    public static BlockPos moveBlockPosFromBase(PlayerEntity player, BlockPos pos) {
        if (!(player instanceof ServerPlayerEntity serverPlayer)) return pos;
        RegistryKey<World> spawnDimension = serverPlayer.getSpawnPointDimension();
        if (spawnDimension == null || !spawnDimension.equals(serverPlayer.getWorld().getRegistryKey())) return pos;
        BlockPos bedLocation = serverPlayer.getSpawnPointPosition();
        if (bedLocation == null) return pos;
        Vec3d bedCenter = new Vec3d(bedLocation.getX() + 0.5, bedLocation.getY() + 0.5, bedLocation.getZ() + 0.5);
        double baseSafeRadius = SplitSelf.CONFIG.getDouble("baseSafeRadius", DefaultConfig.baseSafeRadius);
        double dx = pos.getX() - bedCenter.x;
        double dz = pos.getZ() - bedCenter.z;
        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
        if (horizontalDistance < baseSafeRadius) {
            Vec3d diff = new Vec3d(dx, 0, dz);
            if (diff.lengthSquared() < 1e-10) diff = new Vec3d(1, 0, 0);
            Vec3d direction = diff.normalize();
            return new BlockPos((int) (bedCenter.x + direction.x * (baseSafeRadius + 1.0)), pos.getX(), (int) (bedCenter.z + direction.z * (baseSafeRadius + 1.0)));
        }
        return pos;
    }

    private static Events selectWeightedEvent(Random random) {
        Map<String, Integer> configWeights = SplitSelf.CONFIG.getMap("eventWeights", Integer.class);
        Map<String, Integer> configStages = SplitSelf.CONFIG.getMap("eventStages", Integer.class);

        Map<Events, Integer> eventWeights = new HashMap<>();

        for (Events event : Events.values()) {
            Integer weight = configWeights.get(event.name());
            Integer stage = configStages.get(event.name());
            Integer lastTriggered = eventLastTriggered.get(event);

            if (weight != null && weight > 0
                    && stage <= WorldData.getSleepStage()
                    && !oneTimeEvents.containsKey(event)
                    && (lastTriggered == null || (totalEventsTriggered - lastTriggered) >= REPEAT_EVENTS_AFTER)) {
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
            if (randomWeight < currentWeight) return entry.getKey();
        }

        // Fallback event, in case something breaks
        return Events.SPAWNTHEOTHER;
    }

    public static String getName(ClientPlayerEntity player) {
        try {
            String playerName = player.getName().getString();
            if      (playerName.equalsIgnoreCase("therealsquiddo"))  { return("Florence Ennay");        }
            else if (playerName.equalsIgnoreCase("skipthetutorial")) { return("Aiden");                 }
            else if (playerName.equalsIgnoreCase("failboat"))        { return("Daniel Michaud");        }
            else if (playerName.equalsIgnoreCase("jaym0ji"))         { return("James");                 }
            else if (playerName.equalsIgnoreCase("xvivilly"))        { return("VIV");                   }
            else if (playerName.equalsIgnoreCase("rekrap2"))         { return("Parker Jerry Marriott"); }
            else if (playerName.equalsIgnoreCase("dream"))           { return("Clay");                  }
            else if (playerName.equalsIgnoreCase("itzmiai_21"))      { return("M1keyz");                }
            else if (playerName.equalsIgnoreCase("zachbealetv"))     { return("Zach Beale");            }
            else if (playerName.equalsIgnoreCase("cxlvxn"))          { return("Calvin9000");            }
            else if (playerName.equalsIgnoreCase("pufferfish81"))    { return("Puff");                  }
            else if (playerName.equalsIgnoreCase("Lord0wnage "))     { return("Swayle");                }
            if (!WorldData.getPII()) {
                return(SplitSelf.translate("events.splitself.redacted_name").getString());
            } else {
                return(System.getProperty("user.name"));
            }

        } catch (Exception e) {
            SplitSelf.LOGGER.error("Error in getName(): {} {}", e.getMessage(), e);
            return(SplitSelf.translate("events.splitself.redacted_name").getString());
        }
    }

    @SuppressWarnings("DataFlowIssue")
    public static void runSleepEvent(Integer stage) {
        MinecraftServer server = MinecraftClient.getInstance().getServer();
        server.execute(() -> new Thread(() -> {
            List<ServerPlayerEntity> players = server.getPlayerManager().getPlayerList();
            try {
                ServerWorld limboWorld = server.getWorld(DimensionRegistry.LIMBO_DIMENSION_KEY);
                players.forEach(p -> p.changeGameMode(GameMode.ADVENTURE));
                assert limboWorld != null;
                if (stage == 0) {
                    players.forEach(p -> p.teleport(limboWorld, 2.3, 1.5625, 9.7, null, -135, 40));
                    Thread.sleep(20000);
                } else if (stage == 1) {
                    TheOtherEntity theOther = new TheOtherEntity(ModEntities.TheOther, limboWorld);
                    theOther.refreshPositionAndAngles(1006.5, 3, 33.5, -160F, -40F);
                    limboWorld.spawnEntity(theOther);
                    players.forEach(p -> p.teleport(limboWorld, 1015.3, 9.5625, 34.7, null, -135, 40));
                    Thread.sleep(60000);
                } else if (stage == 2) {
                    TheOtherEntity theOther = new TheOtherEntity(ModEntities.TheOther, limboWorld);
                    theOther.refreshPositionAndAngles(2036.5, 4, 20.0, 49F, -14F);
                    limboWorld.spawnEntity(theOther);
                    theOther.setupGoals();
                    players.forEach(p -> p.teleport(limboWorld, 2015.3, 9.5625, 34.7, null, -135, 40));
                    Thread.sleep(60000);
                } else if (stage == 3) {
                    players.forEach(p -> p.teleport(limboWorld, 3009.5, 11.5625, 6.5, null, -45, 40));
                    Thread.sleep(5000);
                    players.forEach(p -> p.sendMessageToClient(Text.literal("<" + p.getName().getString() + "> " + SplitSelf.translate("chat.splitself.sleep.talk1").getString()), false));
                    Thread.sleep(5000);
                    players.forEach(p -> p.sendMessageToClient(Text.literal("<" + p.getName().getString() + "> " + SplitSelf.translate("chat.splitself.sleep.talk2").getString()), false));
                    Thread.sleep(5000);
                    players.forEach(p -> p.sendMessageToClient(Text.literal("<" + p.getName().getString() + "> " + SplitSelf.translate("chat.splitself.sleep.talk3").getString()), false));
                    Thread.sleep(10000);
                    players.forEach(p -> p.sendMessageToClient(Text.literal("<" + p.getName().getString() + "> " + SplitSelf.translate("chat.splitself.sleep.talk4").getString()), false));
                    Thread.sleep(15000);
                } else if (stage == 4) {
                    players.forEach(p -> p.teleport(limboWorld, 3009.5, 11.5625, 6.5, null, -45, 40));
                    Thread.sleep(30000);
                }
                server.getOverworld().setTimeOfDay(0);
                players.forEach(p -> {
                    if (p.getWorld() == server.getWorld(DimensionRegistry.LIMBO_DIMENSION_KEY)) {
                        assert p.getSpawnPointPosition() != null;
                        p.removeStatusEffect(StatusEffects.SLOW_FALLING);
                        p.removeStatusEffect(StatusEffects.LEVITATION);
                        p.teleport(server.getWorld(p.getSpawnPointDimension()), p.getSpawnPointPosition().getX(), p.getSpawnPointPosition().getY() + 0.5625, p.getSpawnPointPosition().getZ(), null, 0, 0);
                    }
                    p.changeGameMode(GameMode.SURVIVAL);
                });
            } catch (Exception e) {
                SplitSelf.LOGGER.error(e.getMessage(), e);
            }
        }).start());
    }
    public static void runChatEvent(PlayerEntity player, String rawMessage, boolean SkipWait) {
        if (player.getWorld() == Objects.requireNonNull(player.getServer()).getWorld(DimensionRegistry.LIMBO_DIMENSION_KEY)) {return;}
        if (player.getWorld() == Objects.requireNonNull(player.getServer()).getWorld(DimensionRegistry.EMPTINESS_DIMENSION_KEY)) {return;}
        new Thread(() -> {
            try {
                if (!SkipWait) {
                    Thread.sleep((int) (Math.random() * 7000) + 3000);
                }
                PlayerManager playerManager = Objects.requireNonNull(player.getServer()).getPlayerManager();
                List<TheForgottenEntity> entities = player.getWorld().getEntitiesByType(ModEntities.TheForgotten, player.getBoundingBox().expand(20), entity -> true);
                String message = rawMessage.replace("?", "").replace("!", "").replace(".", "");
                if (!entities.isEmpty()) { // If The Forgotten entity is nearby
                    if (message.equalsIgnoreCase(SplitSelf.translate("chat.splitself.prompt.control").getString())) {playerManager.broadcast(Text.literal("<████████████> " + SplitSelf.translate("chat.splitself.forgottenResponse.control").getString()), false);}
                    else if (message.equalsIgnoreCase(player.getName().getString())) {playerManager.broadcast(Text.literal("<████████████> " + SplitSelf.translate("chat.splitself.forgottenResponse.nameConflict").getString()), false);}
                    else if (message.equalsIgnoreCase(SplitSelf.translate("chat.splitself.prompt.tethered").getString())) {playerManager.broadcast(Text.literal("<████████████> " + SplitSelf.translate("chat.splitself.forgottenResponse.tethered").getString()), false);}
                    else if (message.equalsIgnoreCase(SplitSelf.translate("chat.splitself.prompt.whoAreYou").getString()) || message.equalsIgnoreCase(SplitSelf.translate("chat.splitself.prompt.whoAreYou_alt").getString())) {playerManager.broadcast(Text.literal("<████████████> " + SplitSelf.translate("chat.splitself.forgottenResponse.whoAreYou").getString()), false);}
                    else if (message.equalsIgnoreCase(SplitSelf.translate("chat.splitself.prompt.whatDidIDo").getString()) || message.equalsIgnoreCase(SplitSelf.translate("chat.splitself.prompt.whatDidIDo_alt").getString())) {playerManager.broadcast(Text.literal("<████████████> " + SplitSelf.translate("chat.splitself.forgottenResponse.whatDidIDo").getString()), false);}
                    else if (message.equalsIgnoreCase(SplitSelf.translate("chat.splitself.prompt.whatDoYouWant").getString()) || message.equalsIgnoreCase(SplitSelf.translate("chat.splitself.prompt.whatDoYouWant_alt").getString())) {playerManager.broadcast(Text.literal("<████████████> " + SplitSelf.translate("chat.splitself.forgottenResponse.whatDoYouWant").getString()), false);}
                    else if (message.equalsIgnoreCase(SplitSelf.translate("chat.splitself.prompt.whereAreYou").getString()) || message.equalsIgnoreCase(SplitSelf.translate("chat.splitself.prompt.whereAreYou_alt").getString())) {playerManager.broadcast(Text.literal("<████████████> " + SplitSelf.translate("chat.splitself.forgottenResponse.whereAreYou").getString()), false);}
                    else if (message.equalsIgnoreCase(SplitSelf.translate("chat.splitself.prompt.oneLastTime").getString()) || message.equalsIgnoreCase(SplitSelf.translate("chat.splitself.prompt.oneLastTime_alt").getString())) {playerManager.broadcast(Text.literal("<████████████> " + SplitSelf.translate("chat.splitself.forgottenResponse.oneLastTime").getString()), false);}
                    else if (message.equalsIgnoreCase(SplitSelf.translate("chat.splitself.prompt.freedom").getString())) {
                        playerManager.broadcast(Text.literal("<████████████> " + SplitSelf.translate("chat.splitself.forgottenResponse.freedom").getString()), false);
                        player.getServer().execute(() -> player.dropItem(ModItems.FREEDOM_MUSIC_DISC));
                    }
                    else if (message.equalsIgnoreCase(SplitSelf.translate("chat.splitself.prompt.help").getString())) {playerManager.broadcast(Text.literal("<████████████> " + SplitSelf.translate("chat.splitself.forgottenResponse.help").getString()), false);}
                    else if (message.equalsIgnoreCase(SplitSelf.translate("chat.splitself.prompt.absence").getString())) {playerManager.broadcast(Text.literal("<████████████> " + SplitSelf.translate("chat.splitself.forgottenResponse.absence").getString()), false);}
                    else if (message.equalsIgnoreCase(SplitSelf.translate("chat.splitself.prompt.hello").getString()) || message.equalsIgnoreCase(SplitSelf.translate("chat.splitself.prompt.hello_alt").getString())) {playerManager.broadcast(Text.literal("<████████████> " + SplitSelf.translate("chat.splitself.forgottenResponse.hello", System.getProperty("user.name")).getString()), false);}
                } else { // Default to The Other entity messages
                    if (message.equalsIgnoreCase(SplitSelf.translate("chat.splitself.prompt.control").getString())) {
                        playerManager.broadcast(Text.literal("<" + player.getName().getString() + "> " + SplitSelf.translate("chat.splitself.response.control").getString()), false);
                    } else if (message.equalsIgnoreCase(player.getName().getString())) {
                        playerManager.broadcast(Text.literal("<" + player.getName().getString() + "> " + SplitSelf.translate("chat.splitself.response.nameConflict").getString()), false);
                    } else if (message.equalsIgnoreCase(SplitSelf.translate("chat.splitself.prompt.tethered").getString())) {
                        playerManager.broadcast(Text.literal("<" + player.getName().getString() + "> " + SplitSelf.translate("chat.splitself.response.tethered").getString()), false);
                    } else if (message.equalsIgnoreCase(SplitSelf.translate("chat.splitself.prompt.whoAreYou").getString()) || message.equalsIgnoreCase(SplitSelf.translate("chat.splitself.prompt.whoAreYou_alt").getString())) {
                        playerManager.broadcast(Text.literal("<" + player.getName().getString() + "> " + SplitSelf.translate("chat.splitself.response.whoAreYou").getString()), false);
                    } else if (message.equalsIgnoreCase(SplitSelf.translate("chat.splitself.prompt.whatDidIDo").getString()) || message.equalsIgnoreCase(SplitSelf.translate("chat.splitself.prompt.whatDidIDo_alt").getString())) {
                        playerManager.broadcast(Text.literal("<" + player.getName().getString() + "> " + SplitSelf.translate("chat.splitself.response.whatDidIDo").getString()), false);
                    } else if (message.equalsIgnoreCase(SplitSelf.translate("chat.splitself.prompt.whatDoYouWant").getString()) || message.equalsIgnoreCase(SplitSelf.translate("chat.splitself.prompt.whatDoYouWant_alt").getString())) {
                        playerManager.broadcast(Text.literal("<" + player.getName().getString() + "> " + SplitSelf.translate("chat.splitself.response.whatDoYouWant").getString()), false);
                    } else if (message.equalsIgnoreCase(SplitSelf.translate("chat.splitself.prompt.whereAreYou").getString()) || message.equalsIgnoreCase(SplitSelf.translate("chat.splitself.prompt.whereAreYou_alt").getString())) {
                        playerManager.broadcast(Text.literal("<" + player.getName().getString() + "> " + SplitSelf.translate("chat.splitself.response.whereAreYou").getString()), false);
                    } else if (message.equalsIgnoreCase(SplitSelf.translate("chat.splitself.prompt.oneLastTime").getString()) || message.equalsIgnoreCase(SplitSelf.translate("chat.splitself.prompt.oneLastTime_alt").getString())) {
                        playerManager.broadcast(Text.literal("<" + player.getName().getString() + "> " + SplitSelf.translate("chat.splitself.response.oneLastTime").getString()), false);
                    } else if (message.equalsIgnoreCase(SplitSelf.translate("chat.splitself.prompt.freedom").getString())) {
                        playerManager.broadcast(Text.literal("<" + player.getName().getString() + "> " + SplitSelf.translate("chat.splitself.response.freedom").getString()), false);
                    } else if (message.equalsIgnoreCase(SplitSelf.translate("chat.splitself.prompt.help").getString())) {
                        playerManager.broadcast(Text.literal("<" + player.getName().getString() + "> " + SplitSelf.translate("chat.splitself.response.help").getString()), false);
                    } else if (message.equalsIgnoreCase(SplitSelf.translate("chat.splitself.prompt.absence").getString())) {
                        playerManager.broadcast(Text.literal("<" + player.getName().getString() + "> " + SplitSelf.translate("chat.splitself.response.absence").getString()), false);
                    } else if (message.equalsIgnoreCase(SplitSelf.translate("chat.splitself.prompt.hello").getString()) || message.equalsIgnoreCase(SplitSelf.translate("chat.splitself.prompt.hello_alt").getString())) {
                        playerManager.broadcast(Text.literal("<" + player.getName().getString() + "> " + SplitSelf.translate("chat.splitself.response.hello").getString()), false);
                    }
                }
            } catch (Exception e) {
                SplitSelf.LOGGER.error(e.getMessage(), e);
            }
        }).start();
    }

    /**
     *
     * @param world The world executed in
     * @param player The targeted player
     * @param ForceEvent If a specific event should play or be randomized
     * @hello I see you
     *
     */
    public static void triggerRandomEvent(ServerWorld world, ServerPlayerEntity player, Events ForceEvent) {

        if (player == null || world == null) {
            SplitSelf.LOGGER.error("Tried to run '{}' but player or world was null", ForceEvent != null ? ForceEvent.name() : "random event");
            return;
        }

        MinecraftServer server = world.getServer();

        totalEventsTriggered++;

        List<ServerPlayerEntity> players = world.getPlayers();
        if (players.isEmpty()) return;

        if (world == server.getWorld(DimensionRegistry.LIMBO_DIMENSION_KEY)) { return; }
        if (world == server.getWorld(DimensionRegistry.EMPTINESS_DIMENSION_KEY)) { return; }

        Events eventType;
        if (ForceEvent == null) {
            Random javaRandom = new Random(world.getRandom().nextLong());
            eventType = selectWeightedEvent(javaRandom);
        } else {
            eventType = ForceEvent;
        }

        eventLastTriggered.put(eventType, totalEventsTriggered);

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
        String os = net.minecraft.util.Util.getOperatingSystem().toString().toLowerCase();

        switch (eventType) {
            case SPAWNTHEOTHER -> TheOtherSpawner.trySpawnTheOther(world, player, false);
            case POEMSCREEN -> client.execute(() -> client.setScreen(new PoemScreen()));
            case DOYOUSEEME -> BackgroundManager.setBackground("/assets/splitself/textures/wallpaper/doyouseeme.png", "doyouseeme.png");
            case UNDERGROUNDMINING -> UndergroundMining.Execute(player, world);
            case REDSKY -> {
                world.playSound(null, Objects.requireNonNull(player).getBlockPos(), ModSounds.REDSKY, SoundCategory.MASTER, 1.0f, 1.0f);
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, 430, 1, false, false, false));
                SkyColor.changeSkyColor("AA0000");
                SkyColor.changeFogColor("880000");
            }
            case NOTEPAD -> {
                Text[] notepadMessages = {
                    SplitSelf.translate("events.splitself.notepad.line1", EventManager.getName(client.player)),
                    SplitSelf.translate("events.splitself.notepad.line2"),
                    SplitSelf.translate("events.splitself.notepad.line3"),
                    SplitSelf.translate("events.splitself.notepad.line4"),
                    SplitSelf.translate("events.splitself.notepad.line5"),
                };
                NotepadManager.execute(notepadMessages);
            }
            case SCREENOVERLAY -> ScreenOverlay.executeBlackScreen(player);
            case WHITESCREENOVERLAY -> ScreenOverlay.executeWhiteScreen(player);
            case INVENTORYOVERLAY -> ScreenOverlay.executeInventoryScreen(player);
            case THEOTHERSCREENSHOT -> {
                TheOtherSpawner.trySpawnTheOther(world, player, true);
                new Thread(() -> {
                    try { Thread.sleep(3000); }
                    catch (Exception e) { SplitSelf.LOGGER.error(e.getMessage(), e); }
                    EntityScreenshotCapture capture = new EntityScreenshotCapture();
                    capture.capture((file) -> {
                        if (file != null) {
                            try {
                                Text[] screenshotMessages = {
                                    SplitSelf.translate("events.splitself.theOtherScreenshot.line1"),
                                    SplitSelf.translate("events.splitself.theOtherScreenshot.line2")
                                };
                                NotepadManager.execute(screenshotMessages);
                                Thread.sleep(8000);
                                net.minecraft.util.Util.getOperatingSystem().open(file);
                            } catch (Exception e) { SplitSelf.LOGGER.error(e.getMessage(), e); }
                        }
                    });
                }).start();
            }
            case DESTROYCHUNK -> ChunkDestroyer.execute(Objects.requireNonNull(player));
            case FROZENSCREEN -> new Thread(() -> client.execute(() -> {
                EntityScreenshotCapture capture = new EntityScreenshotCapture();
                capture.captureFromEntity(player, client.getWindow().getScaledWidth(), client.getWindow().getScaledHeight(), (file) -> {
                    world.playSound(null, Objects.requireNonNull(player).getBlockPos(), ModSounds.STATICSCREAM, SoundCategory.MASTER, 1f, 1.0f);
                    ScreenOverlay.executeFrozenScreen(file);
                });
            })).start();
            case HOUSE -> StructureManager.placeStructureRandomRotation(world, player, "house", 50, 80, -5, false, 1f, true);
            case PILLAR -> {
                for (int i = 0; i <= 30; i++) StructureManager.placeStructureRandomRotation(world, player, "pillar", 50, 80, 0, false, 1f, true);
                StructureManager.placeStructureRandomRotation(world, player, "pillarmemory", 48, 48, 0, false, 1f, true);
            }
            case BILLY -> new Thread(() -> {
                try {
                    server.getPlayerManager().broadcast(Text.literal(SplitSelf.translate("events.splitself.billy.joined").getString()).formatted(Formatting.YELLOW), false);
                    Thread.sleep(3000);
                    server.getPlayerManager().broadcast(Text.literal(SplitSelf.translate("events.splitself.billy.message").getString()), false);
                    Thread.sleep(1500);
                    server.getPlayerManager().broadcast(Text.literal(SplitSelf.translate("events.splitself.billy.left").getString()).formatted(Formatting.YELLOW), false);
                } catch (Exception e) { SplitSelf.LOGGER.error(e.getMessage(), e); }
            }).start();
            case FACE -> SkyImageRenderer.toggleTexture();
            case COMMAND -> { // Thanks, Evelyn <3
                if (os.contains("win")) {
                    try { new ProcessBuilder("cmd", "/c", "start").start(); }
                    catch (IOException e) { SplitSelf.LOGGER.warn("Cannot open CMD."); }
                } else if (os.contains("mac")) {
                    try { new ProcessBuilder("open", "-a", "terminal").start(); }
                    catch (IOException e) { SplitSelf.LOGGER.warn("Cannot open terminal."); }
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
                    if (!opened) SplitSelf.LOGGER.warn("Could not find a terminal emulator for linux.");
                } else SplitSelf.LOGGER.warn("Unsupported OS for term: {}", os);
            }
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
            case TNT -> {
                world.playSound(null, Objects.requireNonNull(player).getBlockPos(), SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.MASTER, 1.0f, 1.0f);
                TNTSpawner.spawnTntInCircle(player, 1.5, 8, 300);
            }
            case IRONTRAP -> StructureManager.placeStructureRandomRotation(world, player, "irontrap", 50, 80, -2, false, 1f, true);
            case LAVA -> {
                BlockPos pos = new BlockPos((int) player.getPos().x, 250, (int) player.getPos().z);
                pos = moveBlockPosFromBase(player, pos);
                player.getWorld().setBlockState(pos, Blocks.LAVA.getDefaultState());
            }
            case BROWSER -> new Thread(() -> {
                try {
                    List<HistoryEntry> history = BrowserHistoryReader.getHistory();
                    List<HistoryEntry> mostVisited = BrowserHistoryReader.getMostVisited();
                    if (history == null || history.isEmpty()) return;
                    server.getPlayerManager().broadcast(Text.literal(SplitSelf.translate("events.splitself.browser.hello", player.getName().getString()).getString()), false);
                    Thread.sleep(3000);
                    server.getPlayerManager().broadcast(Text.literal(SplitSelf.translate("events.splitself.browser.seeMe", player.getName().getString()).getString()), false);
                    Thread.sleep(5000);
                    server.getPlayerManager().broadcast(Text.literal(SplitSelf.translate("events.splitself.browser.iAmYou", player.getName().getString()).getString()), false);
                    Thread.sleep(3000);
                    server.getPlayerManager().broadcast(Text.literal(SplitSelf.translate("events.splitself.browser.iSeeEverything", player.getName().getString()).getString()), false);
                    Thread.sleep(4000);
                    server.getPlayerManager().broadcast(Text.literal(SplitSelf.translate("events.splitself.browser.browserName", player.getName().getString(), history.getFirst().browser).getString()), false);
                    Thread.sleep(4000);
                    String[] siteName = history.getFirst().title.split(" - ");
                    String siteURL = history.getFirst().url.replaceFirst("https://", "").split("/")[0];
                    server.getPlayerManager().broadcast(Text.literal(SplitSelf.translate("events.splitself.browser.displayRecentSite", player.getName().getString(), siteName[0]).getString()), false);
                    Thread.sleep(3000);
                    String mostVisitedSiteURL;
                    int mostVisitedSiteCount;
                    System.out.println(mostVisited.getFirst().title);
                    System.out.println(history.getFirst().title);
                    int browserIndex;
                    for (browserIndex = 0; browserIndex < 50; browserIndex++) {
                        if (mostVisited.get(browserIndex).url.replaceFirst("https://", "").split("/")[0].equals(siteURL)) {
                            System.out.println("Skipping index " + browserIndex);
                            System.out.println(siteURL + "     " + mostVisited.get(browserIndex).url.replaceFirst("https://", "").split("/")[0]);
                        } else {
                            break;
                        }
                    }
                    System.out.println("Browser index: " + (browserIndex));
                    mostVisitedSiteURL = mostVisited.get(browserIndex).url.replaceFirst("https://", "").split("/")[0];
                    mostVisitedSiteCount = mostVisited.get(browserIndex).visitCount;
                    server.getPlayerManager().broadcast(Text.literal(SplitSelf.translate("events.splitself.browser.displayPopularSite", player.getName().getString(), mostVisitedSiteURL).getString()), false);
                    Thread.sleep(5000);
                    server.getPlayerManager().broadcast(Text.literal(SplitSelf.translate("events.splitself.browser.displaySiteCount", player.getName().getString(), mostVisitedSiteCount).getString()), false);
                    Thread.sleep(4000);
                    server.getPlayerManager().broadcast(Text.literal(SplitSelf.translate("events.splitself.browser.imWatching", player.getName().getString()).getString()).formatted(Formatting.RED), false);
                } catch (Exception e) {
                    SplitSelf.LOGGER.error(e.getMessage(), e);
                }
            }).start();
            case KICK -> client.execute(() -> client.setScreen(new KickScreen()));
            case SIGN -> {
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
            }
            case SCALE -> {
                ACTIVE_EVENT = true;
                new Thread(() -> {
                    world.playSound(null, Objects.requireNonNull(player).getBlockPos(), ModSounds.BUZZ, SoundCategory.MASTER, 1.0f, 1.0f);
                    Double OldScale = client.options.getChatScale().getValue();
                    for (int i = 0; i <= 200; i++) {
                        if (i % 5 == 0) {
                            server.getPlayerManager().broadcast(Text.literal("<" + player.getName().getString() + "> " + SplitSelf.translate("events.splitself.scale.message").getString()), false);
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
                    ACTIVE_EVENT = false;
                }).start();
            }
            case FREEDOM -> new Thread(() -> {
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
                    if (pb != null) pb.start();
                } catch (Exception e) { SplitSelf.LOGGER.error("System overlay failed: {}", e.getMessage(), e); }
            }).start();
            case MINE -> {
                BlockPos structurePos = StructureManager.placeStructureRandomRotation(world, player, "stripmine", 0, 20, -80, true, 1f, true);
                assert structurePos != null;
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
            }
            case DOOR -> {
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
                        SplitSelf.LOGGER.error(e.getMessage(), e);
                    }
                }).start();
            }
            case SHRINK -> {
                if (client.options.getFullscreen().getValue()) client.getWindow().toggleFullscreen();
                new Thread(() -> {
                    try {
                        for (int i = 0; i < 100; i++) {
                            if (!client.getWindow().isFullscreen()) {
                                break;
                            }
                            System.out.println("Game is still in fullscreen, waiting 50 milliseconds... attempt: " + (i + 1));
                            Thread.sleep(50);
                        }
                        if (!client.getWindow().isFullscreen()) {
                            assert client.player != null;
                            world.playSound(null, Objects.requireNonNull(player).getBlockPos(), ModSounds.RUMBLE2, SoundCategory.MASTER, 1.0f, 1.0f);
                            server.getPlayerManager().broadcast(Text.literal("<" + player.getName().getString() + "> " + SplitSelf.translate("events.splitself.shrink.message").getString()), false);
                            WINDOW_MANIPULATION_ACTIVE = true;
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
                            assert vidMode != null;
                            int screenWidth = vidMode.width();
                            int screenHeight = vidMode.height();
                            int steps = 50;
                            for (int i = 0; i < steps; i++) {
                                float progress = (float) i / steps;
                                int currentWidth = (int) (originalWidth - (originalWidth - minWidth) * progress);
                                int currentHeight = (int) (originalHeight - (originalHeight - minHeight) * progress);
                                int xPos = (screenWidth - currentWidth) / 2;
                                int yPos = (screenHeight - currentHeight) / 2;
                                GLFW.glfwSetWindowSize(glfwWindow, currentWidth, currentHeight);
                                GLFW.glfwSetWindowPos(glfwWindow, xPos, yPos);
                                client.player.setYaw(client.player.getYaw() + (int) ((Math.random() * 6) - 3));
                                client.player.setPitch(client.player.getPitch() + (int) ((Math.random() * 6) - 3));
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
                                client.player.setYaw(client.player.getYaw() + (int) ((Math.random() * 6) - 3));
                                client.player.setPitch(client.player.getPitch() + (int) ((Math.random() * 6) - 3));
                                Thread.sleep(20);
                            }
                            client.getSoundManager().stopSounds(ModSounds.RUMBLE2.getId(), SoundCategory.MASTER);
                        } else {
                            System.err.println("Failed to un-fullscreen user's screen after 5 seconds!");
                        }
                    } catch (Exception e) {
                        SplitSelf.LOGGER.error("Shrink event failed: {} {}", e.getMessage(), e);
                    } finally {
                        WINDOW_MANIPULATION_ACTIVE = false;
                    }
                }).start();
            }
            case PAUSE -> PAUSE_SHAKE = true;
            case ITEM -> {
                Inventory inventory = player.getInventory();
                for (int i = 0; i < inventory.size(); i++) {
                    ItemStack stack = inventory.getStack(i);
                    if (!stack.isEmpty() && new Random().nextInt(0, 5) == 0) {
                        inventory.setStack(i, ItemStack.EMPTY);
                        break;
                    }
                }
            }
            case FRAME -> {
                boolean takeScreenshot = false;
                Path defaultScreenshotsFolder = Path.of(System.getenv("APPDATA") + "\\.minecraft\\screenshots");
                try {
                    Random random = new Random();
                    File[] screenshotFiles = defaultScreenshotsFolder.toFile().listFiles((dir, name) ->
                            name.toLowerCase().endsWith(".png") || name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".jpeg"));
                    if (screenshotFiles != null && screenshotFiles.length > 0) {
                        File randomScreenshot = screenshotFiles[random.nextInt(screenshotFiles.length)];
                        FrameFileManager.loadImageToFrame(randomScreenshot);
                    } else {
                        throw new RuntimeException("Screenshot folder is null or empty! " + defaultScreenshotsFolder.toAbsolutePath());
                    }
                } catch (Exception e) {
                    SplitSelf.LOGGER.warn("Failed to access screenshot folder of default Minecraft directory: {}", e.getMessage());
                    File screenshotsDir = new File(MinecraftClient.getInstance().runDirectory, "screenshots");
                    if (screenshotsDir.exists() && screenshotsDir.isDirectory()) {
                        File[] screenshotFiles = screenshotsDir.listFiles((dir, name) ->
                                name.toLowerCase().endsWith(".png") || name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".jpeg"));
                        if (screenshotFiles != null && screenshotFiles.length > 0) {
                            Random random = new Random();
                            File randomScreenshot = screenshotFiles[random.nextInt(screenshotFiles.length)];
                            try {
                                FrameFileManager.loadImageToFrame(randomScreenshot);
                                SplitSelf.LOGGER.info("Loaded random screenshot to frame: {}", randomScreenshot.getName());
                            } catch (Exception e2) {
                                SplitSelf.LOGGER.error("Failed to load random screenshot to frame: {} {}", e2.getMessage(), e2);
                            }
                        } else {
                            takeScreenshot = true;
                        }
                    } else {
                        takeScreenshot = true;
                    } if (takeScreenshot) {
                        SplitSelf.LOGGER.warn("Screenshots directory does not exist, taking a new screenshot instead");
                        new Thread(() -> client.execute(() -> {
                            EntityScreenshotCapture capture = new EntityScreenshotCapture();
                            capture.capture((file) -> {
                                if (file != null) {
                                    try {
                                        FrameFileManager.loadImageToFrame(file);
                                    } catch (Exception e2) {
                                        SplitSelf.LOGGER.error("Failed to load image to frame: {} {}", e2.getMessage(), e2);
                                    }
                                } else {
                                    SplitSelf.LOGGER.error("Could not get file!");
                                }
                            });
                        })).start();
                    }
                }
                player.dropItem(ModBlocks.IMAGE_FRAME.asItem(), 1);
                world.playSound(null, Objects.requireNonNull(player).getBlockPos(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.MASTER, 1.0f, 1.0f);
            }
            case NAME -> {
                SplitSelf.LOGGER.info("Player name: {}", player.getName().getString());
                SplitSelf.LOGGER.info("Player UUID: {}", player.getUuidAsString());
                try {
                    List<String> nameHistory = AshconNameAPI.getNameHistory(player.getUuidAsString());
                    if (!nameHistory.isEmpty()) {
                        for (String name : nameHistory) {
                            if (!name.equals(player.getName().getString())) {
                                server.getPlayerManager().broadcast(Text.literal("<" + name + "> " + SplitSelf.translate("events.splitself.sign.imWatchingYou").getString()), false);
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    SplitSelf.LOGGER.error("Failed to fetch name history: {}", e.getMessage());
                }
            }
            case WHISPER -> {
                double distance = 20 + new Random().nextDouble() * (30 - 20);
                double angle = new Random().nextDouble() * 2 * Math.PI;
                double spawnX = player.getPos().getX() + Math.cos(angle) * distance;
                double spawnY = player.getPos().getY() + Math.cos(angle) * distance;
                double spawnZ = player.getPos().getZ() + Math.sin(angle) * distance;
                BlockPos soundPos = new BlockPos((int) spawnX, (int) spawnY, (int) spawnZ);
                world.playSound(null, soundPos, ModSounds.WHISPER, SoundCategory.MASTER, 40.0f, 1.0f);
            }
            case ESCAPE -> PAUSE_PREVENTION = true;
            case LIFT -> ChunkDestroyer.liftChunk(player, world, 1, 40);
            case SURROUND -> {
                ChunkDestroyer.liftChunkActive = true;
                ScreenOverlay.executeGlitchScreen(client);
                world.playSound(null, Objects.requireNonNull(player).getBlockPos(), ModSounds.GLITCH, SoundCategory.MASTER, 1.0f, 1.0f);
                ChunkDestroyer.liftChunk(player, world, 100, 14);
            }
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
                        content = new StringBuilder(content.toString().replaceFirst("files.splitself.log.message" + i, SplitSelf.translate("files.splitself.log.message" + i).getString()));
                    }
                    DesktopFileUtil.createFileOnDesktop("latest.log", content.toString().replace("PLAYERNAME", player.getName().getString()));
                } catch (IOException e) {
                    SplitSelf.LOGGER.error("Error reading resource file: {}", resourcePath, e);
                }
            }
            case DISCONNECT -> new Thread(() -> {
                try {
                    server.getPlayerManager().broadcast(Text.literal(SplitSelf.translate("events.splitself.disconnect.left", player.getName().getString()).getString()).formatted(Formatting.YELLOW), false);
                    Thread.sleep(100);
                    server.getPlayerManager().broadcast(Text.literal(SplitSelf.translate("events.splitself.disconnect.joined", player.getName().getString()).getString()).formatted(Formatting.YELLOW), false);
                    Thread.sleep(1700);
                    server.getPlayerManager().broadcast(Text.literal(SplitSelf.translate("events.splitself.disconnect.left", player.getName().getString()).getString()).formatted(Formatting.YELLOW), false);
                    Thread.sleep(100);
                    server.getPlayerManager().broadcast(Text.literal(SplitSelf.translate("events.splitself.disconnect.joined", player.getName().getString()).getString()).formatted(Formatting.YELLOW), false);
                    Thread.sleep(3900);
                    server.getPlayerManager().broadcast(Text.literal(SplitSelf.translate("events.splitself.disconnect.left", player.getName().getString()).getString()).formatted(Formatting.YELLOW), false);
                    Thread.sleep(100);
                    server.getPlayerManager().broadcast(Text.literal(SplitSelf.translate("events.splitself.disconnect.joined", player.getName().getString()).getString()).formatted(Formatting.YELLOW), false);
                } catch (Exception e) { SplitSelf.LOGGER.error("Disconnect event failed: {}", e.getMessage(), e); }
            }).start();
            case FORGOTTEN -> TheForgottenSpawner.trySpawnTheForgotten(world, player);
            case EJECT -> EventHelper.ejectAll();
            case FREEZE -> {
                world.playSound(null, Objects.requireNonNull(player).getBlockPos(), SoundEvents.ITEM_OMINOUS_BOTTLE_DISPOSE, SoundCategory.MASTER, 1.0f, 1.0f);
                new Thread(() -> client.execute(() -> {
                    try {
                        System.out.println(Thread.currentThread());
                        Thread.sleep(2000);
                    } catch (Exception e) {
                        SplitSelf.LOGGER.error("Freeze event failed: {}", e.getMessage(), e);
                    }
                })).start();
            }
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
            case MEMORY -> {
                try { MinecraftClient.getInstance().execute(() -> Memory.main(new String[]{})); }
                catch (Throwable t) { SplitSelf.LOGGER.error("MEMORY_HOUSE event failed", t); }
            }
            case REMINDER -> {
                if (!SystemTray.isSupported()) SplitSelf.LOGGER.warn("SystemTray not supported on this platform");
                SystemTray tray = SystemTray.getSystemTray();
                Image image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
                TrayIcon trayIcon = new TrayIcon(image, "Messenger");
                trayIcon.setImageAutoSize(true);
                try { tray.add(trayIcon); } catch (Exception ignored) {}
                trayIcon.addActionListener(e -> {
                    try { MinecraftClient.getInstance().execute(() -> MineMessage.main(new String[]{})); }
                    catch (Throwable t) { SplitSelf.LOGGER.error("REMINDER event failed", t); }
                });

                trayIcon.displayMessage(
                    "Messages",
                    "You have an unread message from ████████████",
                    TrayIcon.MessageType.INFO
                );
            }
            case RENAME -> {
                client.getWindow().setTitle(SplitSelf.translate("events.splitself.rename").getString());
                EventHelper.preventTitleChange = true;
            }
            case FOV -> {
                ACTIVE_EVENT = true;
                new Thread(() -> {
                    Integer OldScale = client.options.getFov().getValue();
                    for (int i = 0; i <= 500; i++) {
                        try {
                            client.options.getFov().setValue((int) (client.options.getFov().getValue() + Math.floor(Math.random() * 3) - 1));
                            Thread.sleep(25);
                        } catch (Exception e) { System.out.println("Failed Scale Event: Current FOV: " + client.options.getFov()); }
                    }
                    client.options.getFov().setValue(OldScale);
                    ACTIVE_EVENT = false;
                }).start();
            }
            case WEATHER -> {
                try {
                    String c;
                    if (!WorldData.getPII()) {
                        c = SplitSelf.translate("events.splitself.redacted_name").getString();
                    } else {
                        CityLocator locator = new CityLocator();
                        c = locator.getCityFromCurrentIP();
                    }
                    player.sendMessageToClient(SplitSelf.translate("events.splitself.weather.report", c), false);
                    WeatherFetcher fetcher = new WeatherFetcher();
                    WeatherFetcher.WeatherData weather = fetcher.getWeather(c);
                    if (weather.condition() == null || weather.condition().isEmpty() || weather.feelsLikeC() == null || weather.feelsLikeF() == null) {
                        player.sendMessageToClient(SplitSelf.translate("events.splitself.weather.fail", c), false);
                    } else {
                        player.sendMessageToClient(SplitSelf.translate("events.splitself.weather.loading"), false);
                        Thread.sleep(200);
                        player.sendMessageToClient(SplitSelf.translate("events.splitself.weather.temp", weather.feelsLikeC(), weather.feelsLikeF()), false);
                        player.sendMessageToClient(SplitSelf.translate("events.splitself.weather.weather", weather.condition()), false);
                    }
                } catch (Exception e) { throw new RuntimeException(e); }
            }
            case MEMORIES -> DesktopFileUtil.cloneFileToDesktop(Identifier.of(SplitSelf.MOD_ID, "textures/misc/memories.png"));
            case MORSE -> DesktopFileUtil.cloneFileToDesktop(Identifier.of(SplitSelf.MOD_ID, "textures/misc/morse.png"));
            case CORAL -> {
                Vec3d playerPos = player.getPos();
                playerPos = EventManager.moveVectorFromBase(player, playerPos);
                // replace all blocks within a radius of playerPos with coral blocks, excluding air and containers.
                // it should dither the farther out it is
                int radius = 10;
                for (int x = -radius; x <= radius; x++) {
                    for (int y = -radius; y <= radius; y++) {
                        for (int z = -radius; z <= radius; z++) {
                            BlockPos checkPos = new BlockPos((int) (playerPos.x + x), (int) (playerPos.y + y), (int) (playerPos.z + z));
                            if (checkPos.isWithinDistance(player.getBlockPos(), radius)) {
                                BlockState state = world.getBlockState(checkPos);
                                if (!state.isAir()) {
                                    double distance = checkPos.getSquaredDistance(player.getBlockPos());
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
            case STATIC -> ScreenOverlay.executeStaticScreen(player);
            case INVERTCOLOR -> new Thread(() -> {
                try {
                    world.playSound(null, Objects.requireNonNull(player).getBlockPos(), ModSounds.TONE, SoundCategory.MASTER, 1.0f, 0.5f);
                    BlendManager.invertBlend = true;
                    Thread.sleep(20000);
                    BlendManager.invertBlend = false;
                    client.getSoundManager().stopSounds(ModSounds.TONE.getId(), SoundCategory.MASTER);
                } catch (Exception ignored) {}
            }).start();
            case CLIPBOARD -> {
                StringSelection stringSelection = new StringSelection(Text.translatable("events.splitself.clipboard").getString());
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(stringSelection, null);
            }
            case RPC -> {
                if (SplitSelfClient.RPCInitialized)
                    SplitSelfClient.RPC.updatePresence(DiscordRichPresence.builder()
                        .details(Text.translatable("events.splitself.rpc.desc").getString())
                        .state(Text.translatable("events.splitself.rpc.state").getString())
                        .largeImageKey("noise")
                        .build());
            }
            case RECORD -> new Thread(() -> {
                try {
                    String process = Processes.getScreenRecordingSoftware();
                    if (process != null) {
                        server.getPlayerManager().broadcast(Text.literal(SplitSelf.translate("events.splitself.record.detection", process).getString()).formatted(Formatting.RED), false);
                        Thread.sleep(5000);
                        server.getPlayerManager().broadcast(Text.literal(SplitSelf.translate("events.splitself.record.fail", process).getString()).formatted(Formatting.RED), false);
                    }
                } catch (Exception ignored) {}
            }).start();
            case DISCORDNAME -> {
                if (SplitSelfClient.discordUsername != null) {
                    server.getPlayerManager().broadcast(Text.literal(SplitSelf.translate("events.splitself.discordName.friend", SplitSelfClient.discordUsername).getString()).withColor(7506394), false);
                }
            }
            case DEADCHUNK -> ChunkDestroyer.deadChunk(player, world);
            case RECURSIVE -> ScreenOverlay.executeRecursiveScreen(player, 2500, true);
            case PLAYERDATA -> {
                try {
                    DesktopFileUtil.cloneFileToDesktop(Identifier.of(SplitSelf.MOD_ID, "files/ce7ea4cb-0789-47c9-b536-144f836a30c2.dat_old"));
                    server.getPlayerManager().broadcast(SplitSelf.translate("events.splitself.playerData.1", player.getName().getString()), false);
                    Thread.sleep(5000);
                    server.getPlayerManager().broadcast(SplitSelf.translate("events.splitself.playerData.2", player.getName().getString()), false);
                    Thread.sleep(6000);
                    server.getPlayerManager().broadcast(SplitSelf.translate("events.splitself.playerData.3", player.getName().getString()), false);
                } catch (Exception ignored) {}
            }
            case BRAIN -> {
                player.dropItem(ModBlocks.BRAIN.asItem(), 1);
                world.playSound(null, Objects.requireNonNull(player).getBlockPos(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.MASTER, 1.0f, 1.0f);
            }
            case BOOK -> {
                player.dropItem(ModItems.MEMORY_BOOK.asItem(), 1);
                world.playSound(null, Objects.requireNonNull(player).getBlockPos(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.MASTER, 1.0f, 1.0f);
            }
            case SPOTIFY -> {
                try {
                URI uri = new URI("spotify:track:5MkWlSmMZnSGHLYbK2LgdM");
                Desktop.getDesktop().browse(uri);
                } catch (IOException | URISyntaxException e) {
                    SplitSelf.LOGGER.error("User does not have spotify or track cannot be found.");
                }
            }
            case SEARCH -> {
                try {
                    String message = SplitSelf.translate("events.splitself.search").getString().replace(" ", "+");
                    URI uri = new URI("https://www.google.com/search?q=" + message);
                    Desktop.getDesktop().browse(uri);
                } catch (IOException | URISyntaxException e) {
                    SplitSelf.LOGGER.error("Cannot search");
                }
            }
        }
    }
}