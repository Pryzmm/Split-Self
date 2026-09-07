package com.pryzmm.splitself.data;

import com.pryzmm.splitself.file.JsonReader;
import net.minecraft.server.MinecraftServer;
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
    private static boolean clickedButton;
    private static boolean isDeleted;
    private static boolean isFull;
    private static boolean prevLoaded;
    static { clearData(); }

    private static JsonReader reader = null;

    public static boolean hasPreviouslyLoaded() {
        return prevLoaded;
    }

    public static int getMemoryStage() { return memoryStage; }
    public static int getSleepStage() { return sleepStage; }
    public static List<String> getUnlockedMemories() { return unlockedMemories; }
    public static List<UUID> getJoinedPlayers() { return joinedPlayers; }
    public static long getSeed() { return seed; }
    public static Location getTheForgottenLocation() { return theForgottenLocation; }
    public static boolean getClickedButton() { return clickedButton; }
    public static boolean getIsDeleted() { return isDeleted; }
    public static boolean getIsFull() { return isFull; }

    public static void setMemoryStage(int value) {
        memoryStage = value;
        reader.setAndSave("memoryStage", value);
    }

    public static void setSleepStage(int value) {
        sleepStage = value;
        reader.setAndSave("sleepStage", value);
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
        reader.setAndSave("theForgottenLocation", value);
    }

    public static void setClickedButton(boolean value) {
        clickedButton = value;
        reader.setAndSave("clickedButton", value);
    }

    public static void setIsDeleted(boolean value) {
        isDeleted = value;
        reader.setAndSave("isDeleted", value);
    }

    public static void setIsFull(boolean value) {
        isFull = value;
        reader.setAndSave("isFull", value);
    }

    public static void setPrevLoaded(boolean value) {
        prevLoaded = value;
        reader.setAndSave("prevLoaded", value);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static File getCurrentData(MinecraftServer server) {
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
        clickedButton = false;
        theForgottenLocation = null;
        prevLoaded = false;
        isDeleted = false;
        isFull = false;
    }

    public static void loadData(MinecraftServer server) {
        File data = getCurrentData(server);
        reader = new JsonReader(data);
        joinedPlayers = reader.getUUIDList("joinedPlayers");
        unlockedMemories = reader.getStringList("unlockedMemories");
        sleepStage = reader.getInt("sleepStage", 0);
        memoryStage = reader.getInt("memoryStage", 0);
        clickedButton = reader.getBoolean("clickedButton", false);
        isDeleted = reader.getBoolean("isDeleted", false);
        isFull = reader.getBoolean("isFull", false);
        theForgottenLocation = reader.getLocation("theForgottenLocation", null);
        prevLoaded = reader.getBoolean("prevLoaded", false);
        reader.save();
    }

    public static void updateSeed(MinecraftServer server) {
        File data = getCurrentData(server);
        reader = new JsonReader(data);
        seed = server.getOverworld().getSeed();
        reader.save();
    }

}
