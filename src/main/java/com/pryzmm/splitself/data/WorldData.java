package com.pryzmm.splitself.data;

import com.pryzmm.splitself.file.JsonReader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.WorldSavePath;
import java.io.File;
import java.util.*;

public class WorldData {

    private static List<UUID> joinedPlayers;
    private static List<String> unlockedMemories;
    private static int sleepStage;
    private static int memoryStage;
    private static long seed;
    private static Location theForgottenLocation;
    static { clearData(); }

    private static JsonReader reader = null;

    public static boolean isLoaded() {
        return reader != null;
    }

    public static int getMemoryStage() { return memoryStage; }
    public static int getSleepStage() { return sleepStage; }
    public static List<String> getUnlockedMemories() { return unlockedMemories; }
    public static List<UUID> getJoinedPlayers() { return joinedPlayers; }
    public static long getSeed() { return seed; }
    public static Location getTheForgottenLocation() { return theForgottenLocation; }

    public static void setMemoryStage(int value) {
        memoryStage = value;
        reader.setInt("memoryStage", value);
        reader.save();
    }

    public static void setSleepStage(int value) {
        sleepStage = value;
        reader.setInt("sleepStage", value);
        reader.save();
    }

    public static void addUnlockedMemory(String value) {
        unlockedMemories.add(value);
        reader.setStringList("unlockedMemories", unlockedMemories);
        reader.save();
    }

    public static void updateJoinedPlayers(UUID value) {
        joinedPlayers.add(value);
        reader.setUUIDList("joinedPlayers", joinedPlayers);
        reader.save();
    }

    public static void setTheForgottenLocation(Location value) {
        theForgottenLocation = value;
        reader.setLocation("theForgottenLocation", value);
        reader.save();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static File getCurrentData(ServerWorld world) {
        MinecraftServer server = world.getServer();
        File root = server.getSavePath(WorldSavePath.ROOT).toFile();
        File dir = new File(root, "data");
        if (!dir.exists()) dir.mkdirs();
        return new File(dir, "splitself.json");
    }

    public static void clearData() {
        joinedPlayers = new ArrayList<>();
        unlockedMemories = new ArrayList<>();
        sleepStage = 0;
        memoryStage = 0;
        theForgottenLocation = null;
    }

    public static void loadData(ServerWorld world) {
        File data = getCurrentData(world);
        reader = new JsonReader(data);
        joinedPlayers = reader.getUUIDList("joinedPlayers");
        unlockedMemories = reader.getStringList("unlockedMemories");
        sleepStage = reader.getInt("sleepStage", 0);
        memoryStage = reader.getInt("memoryStage", 0);
        seed = world.getSeed();
        theForgottenLocation = reader.getLocation("theForgottenLocation", null);
        reader.save();
    }

}
