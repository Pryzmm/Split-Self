package com.pryzmm.splitself.events;

import com.mojang.authlib.GameProfile;
import com.pryzmm.splitself.SplitSelf;
import com.pryzmm.splitself.config.DefaultConfig;
import com.pryzmm.splitself.data.ClientData;
import com.pryzmm.splitself.data.WorldData;
import com.pryzmm.splitself.entity.custom.TheForgottenEntity;
import com.pryzmm.splitself.entity.ModEntities;
import com.pryzmm.splitself.entity.client.TheOtherSpawner;
import com.pryzmm.splitself.entity.custom.TheOtherEntity;
import com.pryzmm.splitself.packet.packets.ChatEventPacket;
import com.pryzmm.splitself.packet.packets.EventPacket;
import com.pryzmm.splitself.world.DimensionRegistry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
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
        SURROUND, // TODO (Screen only appears for half a second for non-hosts)
        LOGS,
        DISCONNECT,
        FORGOTTEN,
        EJECT,
        FREEZE,
        BLU,
        MEMORY,
        REMINDER, // TODO (Unknown issue, likely because of headless but not sure)
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

    public static void onClientTick(ClientWorld world) {
        if (!EVENTS_ENABLED) return;
        if (world.getTime() > START_AFTER) {
            if (GUARANTEED_EVENT > 0) { GUARANTEED_EVENT--; }
            if (CURRENT_COOLDOWN > 0) {
                CURRENT_COOLDOWN--;
                return;
            }
            if (world.getTime() % TICK_INTERVAL != 0) return;
            if (world.getRandom().nextDouble() < EVENT_CHANCE || GUARANTEED_EVENT == 0) {
                ClientPlayNetworking.send(new EventPacket("null"));
                CURRENT_COOLDOWN = SplitSelf.CONFIG.getInt("eventCooldown", DefaultConfig.eventCooldown);
                GUARANTEED_EVENT = SplitSelf.CONFIG.getInt("guaranteedEvent", DefaultConfig.guaranteedEvent);
            }
        }
    }

    public static void onTick(MinecraftServer server) {
        Random random = new Random();
        ServerPlayerEntity player;
        try { player = server.getPlayerManager().getPlayerList().get(random.nextInt(server.getPlayerManager().getPlayerList().toArray().length)); }
        catch (Exception e) { return; }
        ServerWorld world = player.getServerWorld();
        if (world.getTime() == START_AFTER) {
            for (ServerPlayerEntity serverPlayer : server.getPlayerManager().getPlayerList()) {
                serverPlayer.sendMessageToClient(SplitSelf.translate("death.attack.outsideBorder", serverPlayer.getName().getString()), false);
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

    public static BlockPos moveBlockPosFromBase(ServerPlayerEntity player, BlockPos pos) {
        RegistryKey<World> spawnDimension = player.getSpawnPointDimension();
        if (spawnDimension == null || !spawnDimension.equals(player.getWorld().getRegistryKey())) return pos;
        BlockPos bedLocation = player.getSpawnPointPosition();
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
            if (!ClientData.getPII()) {
                return(SplitSelf.translate("events.splitself.redacted_name").getString());
            } else {
                return(System.getProperty("user.name"));
            }

        } catch (Exception e) {
            SplitSelf.LOGGER.error("Error in getName(): {} {}", e.getMessage(), e);
            return(SplitSelf.translate("events.splitself.redacted_name").getString());
        }
    }

    public static List<ServerPlayerEntity> sleepingPlayers = new ArrayList<>();
    public static void runSleepEvent(MinecraftServer server, Integer stage) {
        server.execute(() -> new Thread(() -> {
            SplitSelf.LOGGER.info("sleeping players: " + sleepingPlayers);
            try {
                ServerWorld limboWorld = server.getWorld(DimensionRegistry.LIMBO_DIMENSION_KEY);
                sleepingPlayers.forEach(p -> p.changeGameMode(GameMode.ADVENTURE));
                assert limboWorld != null;
                if (stage == 0) {
                    sleepingPlayers.forEach(p -> p.teleport(limboWorld, 2.3, 1.5625, 9.7, null, -135, 40));
                    Thread.sleep(20000);
                } else if (stage == 1) {
                    TheOtherEntity theOther = new TheOtherEntity(ModEntities.TheOther, limboWorld);
                    theOther.refreshPositionAndAngles(1006.5, 3, 33.5, -160F, -40F);
                    limboWorld.spawnEntity(theOther);
                    sleepingPlayers.forEach(p -> p.teleport(limboWorld, 1015.3, 9.5625, 34.7, null, -135, 40));
                    Thread.sleep(60000);
                } else if (stage == 2) {
                    TheOtherEntity theOther = new TheOtherEntity(ModEntities.TheOther, limboWorld);
                    theOther.refreshPositionAndAngles(2036.5, 4, 20.0, 49F, -14F);
                    limboWorld.spawnEntity(theOther);
                    theOther.setupGoals();
                    sleepingPlayers.forEach(p -> p.teleport(limboWorld, 2015.3, 9.5625, 34.7, null, -135, 40));
                    Thread.sleep(60000);
                } else if (stage == 3) {
                    sleepingPlayers.forEach(p -> p.teleport(limboWorld, 3009.5, 11.5625, 6.5, null, -45, 40));
                    Thread.sleep(5000);
                    sleepingPlayers.forEach(p -> p.sendMessageToClient(Text.literal("<" + p.getName().getString() + "> " + SplitSelf.translate("chat.splitself.sleep.talk1").getString()), false));
                    Thread.sleep(5000);
                    sleepingPlayers.forEach(p -> p.sendMessageToClient(Text.literal("<" + p.getName().getString() + "> " + SplitSelf.translate("chat.splitself.sleep.talk2").getString()), false));
                    Thread.sleep(5000);
                    sleepingPlayers.forEach(p -> p.sendMessageToClient(Text.literal("<" + p.getName().getString() + "> " + SplitSelf.translate("chat.splitself.sleep.talk3").getString()), false));
                    Thread.sleep(10000);
                    sleepingPlayers.forEach(p -> p.sendMessageToClient(Text.literal("<" + p.getName().getString() + "> " + SplitSelf.translate("chat.splitself.sleep.talk4").getString()), false));
                    Thread.sleep(15000);
                } else if (stage == 4) {
                    sleepingPlayers.forEach(p -> p.teleport(limboWorld, 3009.5, 11.5625, 6.5, null, -45, 40));
                    Thread.sleep(30000);
                }
                server.getOverworld().setTimeOfDay(0);
                sleepingPlayers.forEach(p -> {
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

    public static void receiveChatEventPacket(ClientPlayerEntity player, String message, Boolean talkingToTheForgotten) {
        if (talkingToTheForgotten) {
            if (message.equalsIgnoreCase(SplitSelf.translate("chat.splitself.prompt.control").getString())) {player.sendMessage(Text.literal("<████████████> " + SplitSelf.translate("chat.splitself.forgottenResponse.control").getString()), false);}
            else if (message.equalsIgnoreCase(player.getName().getString())) {player.sendMessage(Text.literal("<████████████> " + SplitSelf.translate("chat.splitself.forgottenResponse.nameConflict").getString()), false);}
            else if (message.equalsIgnoreCase(SplitSelf.translate("chat.splitself.prompt.tethered").getString())) {player.sendMessage(Text.literal("<████████████> " + SplitSelf.translate("chat.splitself.forgottenResponse.tethered").getString()), false);}
            else if (message.equalsIgnoreCase(SplitSelf.translate("chat.splitself.prompt.whoAreYou").getString()) || message.equalsIgnoreCase(SplitSelf.translate("chat.splitself.prompt.whoAreYou_alt").getString())) {player.sendMessage(Text.literal("<████████████> " + SplitSelf.translate("chat.splitself.forgottenResponse.whoAreYou").getString()), false);}
            else if (message.equalsIgnoreCase(SplitSelf.translate("chat.splitself.prompt.whatDidIDo").getString()) || message.equalsIgnoreCase(SplitSelf.translate("chat.splitself.prompt.whatDidIDo_alt").getString())) {player.sendMessage(Text.literal("<████████████> " + SplitSelf.translate("chat.splitself.forgottenResponse.whatDidIDo").getString()), false);}
            else if (message.equalsIgnoreCase(SplitSelf.translate("chat.splitself.prompt.whatDoYouWant").getString()) || message.equalsIgnoreCase(SplitSelf.translate("chat.splitself.prompt.whatDoYouWant_alt").getString())) {player.sendMessage(Text.literal("<████████████> " + SplitSelf.translate("chat.splitself.forgottenResponse.whatDoYouWant").getString()), false);}
            else if (message.equalsIgnoreCase(SplitSelf.translate("chat.splitself.prompt.whereAreYou").getString()) || message.equalsIgnoreCase(SplitSelf.translate("chat.splitself.prompt.whereAreYou_alt").getString())) {player.sendMessage(Text.literal("<████████████> " + SplitSelf.translate("chat.splitself.forgottenResponse.whereAreYou").getString()), false);}
            else if (message.equalsIgnoreCase(SplitSelf.translate("chat.splitself.prompt.oneLastTime").getString()) || message.equalsIgnoreCase(SplitSelf.translate("chat.splitself.prompt.oneLastTime_alt").getString())) {player.sendMessage(Text.literal("<████████████> " + SplitSelf.translate("chat.splitself.forgottenResponse.oneLastTime").getString()), false);}
            else if (message.equalsIgnoreCase(SplitSelf.translate("chat.splitself.prompt.freedom").getString())) {
                player.sendMessage(Text.literal("<████████████> " + SplitSelf.translate("chat.splitself.forgottenResponse.freedom").getString()), false);
                ClientPlayNetworking.send(new ChatEventPacket("freedom", true));
            }
            else if (message.equalsIgnoreCase(SplitSelf.translate("chat.splitself.prompt.help").getString())) {player.sendMessage(Text.literal("<████████████> " + SplitSelf.translate("chat.splitself.forgottenResponse.help").getString()), false);}
            else if (message.equalsIgnoreCase(SplitSelf.translate("chat.splitself.prompt.absence").getString())) {player.sendMessage(Text.literal("<████████████> " + SplitSelf.translate("chat.splitself.forgottenResponse.absence").getString()), false);}
            else if (message.equalsIgnoreCase(SplitSelf.translate("chat.splitself.prompt.hello").getString()) || message.equalsIgnoreCase(SplitSelf.translate("chat.splitself.prompt.hello_alt").getString())) {player.sendMessage(Text.literal("<████████████> " + SplitSelf.translate("chat.splitself.forgottenResponse.hello", System.getProperty("user.name")).getString()), false);}
        } else {
            if (message.equalsIgnoreCase(SplitSelf.translate("chat.splitself.prompt.control").getString())) {
                player.sendMessage(Text.literal("<" + player.getName().getString() + "> " + SplitSelf.translate("chat.splitself.response.control").getString()), false);
            } else if (message.equalsIgnoreCase(player.getName().getString())) {
                player.sendMessage(Text.literal("<" + player.getName().getString() + "> " + SplitSelf.translate("chat.splitself.response.nameConflict").getString()), false);
            } else if (message.equalsIgnoreCase(SplitSelf.translate("chat.splitself.prompt.tethered").getString())) {
                player.sendMessage(Text.literal("<" + player.getName().getString() + "> " + SplitSelf.translate("chat.splitself.response.tethered").getString()), false);
            } else if (message.equalsIgnoreCase(SplitSelf.translate("chat.splitself.prompt.whoAreYou").getString()) || message.equalsIgnoreCase(SplitSelf.translate("chat.splitself.prompt.whoAreYou_alt").getString())) {
                player.sendMessage(Text.literal("<" + player.getName().getString() + "> " + SplitSelf.translate("chat.splitself.response.whoAreYou").getString()), false);
            } else if (message.equalsIgnoreCase(SplitSelf.translate("chat.splitself.prompt.whatDidIDo").getString()) || message.equalsIgnoreCase(SplitSelf.translate("chat.splitself.prompt.whatDidIDo_alt").getString())) {
                player.sendMessage(Text.literal("<" + player.getName().getString() + "> " + SplitSelf.translate("chat.splitself.response.whatDidIDo").getString()), false);
            } else if (message.equalsIgnoreCase(SplitSelf.translate("chat.splitself.prompt.whatDoYouWant").getString()) || message.equalsIgnoreCase(SplitSelf.translate("chat.splitself.prompt.whatDoYouWant_alt").getString())) {
                player.sendMessage(Text.literal("<" + player.getName().getString() + "> " + SplitSelf.translate("chat.splitself.response.whatDoYouWant").getString()), false);
            } else if (message.equalsIgnoreCase(SplitSelf.translate("chat.splitself.prompt.whereAreYou").getString()) || message.equalsIgnoreCase(SplitSelf.translate("chat.splitself.prompt.whereAreYou_alt").getString())) {
                player.sendMessage(Text.literal("<" + player.getName().getString() + "> " + SplitSelf.translate("chat.splitself.response.whereAreYou").getString()), false);
            } else if (message.equalsIgnoreCase(SplitSelf.translate("chat.splitself.prompt.oneLastTime").getString()) || message.equalsIgnoreCase(SplitSelf.translate("chat.splitself.prompt.oneLastTime_alt").getString())) {
                player.sendMessage(Text.literal("<" + player.getName().getString() + "> " + SplitSelf.translate("chat.splitself.response.oneLastTime").getString()), false);
            } else if (message.equalsIgnoreCase(SplitSelf.translate("chat.splitself.prompt.freedom").getString())) {
                player.sendMessage(Text.literal("<" + player.getName().getString() + "> " + SplitSelf.translate("chat.splitself.response.freedom").getString()), false);
            } else if (message.equalsIgnoreCase(SplitSelf.translate("chat.splitself.prompt.help").getString())) {
                player.sendMessage(Text.literal("<" + player.getName().getString() + "> " + SplitSelf.translate("chat.splitself.response.help").getString()), false);
            } else if (message.equalsIgnoreCase(SplitSelf.translate("chat.splitself.prompt.absence").getString())) {
                player.sendMessage(Text.literal("<" + player.getName().getString() + "> " + SplitSelf.translate("chat.splitself.response.absence").getString()), false);
            } else if (message.equalsIgnoreCase(SplitSelf.translate("chat.splitself.prompt.hello").getString()) || message.equalsIgnoreCase(SplitSelf.translate("chat.splitself.prompt.hello_alt").getString())) {
                player.sendMessage(Text.literal("<" + player.getName().getString() + "> " + SplitSelf.translate("chat.splitself.response.hello").getString()), false);
            }
        }
    }

    public static void runChatEvent(PlayerEntity player, String rawMessage, boolean SkipWait) {
        if (player.getWorld() == Objects.requireNonNull(player.getServer()).getWorld(DimensionRegistry.LIMBO_DIMENSION_KEY)) {return;}
        if (player.getWorld() == Objects.requireNonNull(player.getServer()).getWorld(DimensionRegistry.EMPTINESS_DIMENSION_KEY)) {return;}
        new Thread(() -> {
            try {
                if (!SkipWait) Thread.sleep((int) (Math.random() * 7000) + 3000);
                List<TheForgottenEntity> entities = player.getWorld().getEntitiesByType(ModEntities.TheForgotten, player.getBoundingBox().expand(20), entity -> true);
                String message = rawMessage.replace("?", "").replace("!", "").replace(".", "");
                if (!entities.isEmpty()) { // If The Forgotten entity is nearby
                    for (ServerPlayerEntity p : player.getServer().getPlayerManager().getPlayerList()) ServerPlayNetworking.send(p, new ChatEventPacket(message, true));
                } else { // Default to The Other entity messages
                    for (ServerPlayerEntity p : player.getServer().getPlayerManager().getPlayerList()) ServerPlayNetworking.send(p, new ChatEventPacket(message, false));
                }
            } catch (Exception e) {
                SplitSelf.LOGGER.error(e.getMessage(), e);
            }
        }).start();
    }

    public static void triggerRandomEvent(MinecraftServer server, GameProfile profile, Events ForceEvent) {
        triggerRandomEvent(server.getPlayerManager().getPlayer(profile.getId()), ForceEvent);
    }

    /**
     * Runs code on the server side before deciding whether to run an event globally (server sided), or sending a packet to every client to run on their own (client sided).
     * @param player The targeted player
     * @param ForceEvent If a specific event should play or be randomized
     * @hello I see you
     *
     */
    public static void triggerRandomEvent(ServerPlayerEntity player, Events ForceEvent) {

        if (player == null) {
            SplitSelf.LOGGER.error("Tried to run '{}' but player was null", ForceEvent != null ? ForceEvent.name() : "random event");
            return;
        }

        MinecraftServer server = player.getServer();
        if (server == null) return;

        totalEventsTriggered++;

        if (player.getServerWorld() == server.getWorld(DimensionRegistry.LIMBO_DIMENSION_KEY)) {
            return;
        }
        if (player.getServerWorld() == server.getWorld(DimensionRegistry.EMPTINESS_DIMENSION_KEY)) {
            return;
        }

        Events eventType;
        if (ForceEvent == null || ForceEvent.equals("null")) {
            Random javaRandom = new Random(server.getOverworld().getRandom().nextLong());
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
        newPositions[arrayLength] = player.getPos();
        TheOtherSpawner.spawnPositions = newPositions;

        DefaultConfig.EventType type = DefaultConfig.eventTypes.get(eventType.name());
        if (type != null) {
            if (type == DefaultConfig.EventType.GLOBAL) EventRunner.runGlobalEvent(server, player, eventType);
            else for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
                ServerPlayNetworking.send(p, new EventPacket(eventType.name()));
            }
        }

    }
}