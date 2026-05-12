package com.pryzmm.splitself.data;

import com.pryzmm.splitself.file.JsonReader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;
import org.jetbrains.annotations.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class WorldData {

    private static List<UUID> joinedPlayers;
    private static List<String> unlockedMemories;
    private static boolean PII;
    private static int sleepStage;
    private static int memoryStage;
    static { clearData(); }

    private static JsonReader reader = null;

    public static boolean getPII() { return PII; }
    public static int getMemoryStage() { return memoryStage; }
    public static int getSleepStage() { return sleepStage; }
    public static List<String> getUnlockedMemories() { return unlockedMemories; }
    public static List<UUID> getJoinedPlayers() { return joinedPlayers; }

    public static void setPII(boolean value) {
        PII = value;
        reader.setBoolean("pii", value);
        reader.save();
    }

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

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static @Nullable File getCurrentData() {
        MinecraftServer server = MinecraftClient.getInstance().getServer();
        if (server != null) {
            File file = new File(server.getSavePath(new WorldSavePath("data")).toString() + "/splitself.json");
            try { file.createNewFile(); } catch (IOException e) { return null; }
            return file;
        } else return null;
    }

    public static void clearData() {
        joinedPlayers = new ArrayList<>();
        unlockedMemories = new ArrayList<>();
        PII = false;
        sleepStage = 0;
        memoryStage = 0;
    }

    public static void loadData() {
        File data = getCurrentData();
        if (data == null) return;
        reader = new JsonReader(data);
        joinedPlayers = reader.getUUIDList("joinedPlayers");
        unlockedMemories = reader.getStringList("unlockedMemories");
        PII = reader.getBoolean("pii", false);
        sleepStage = reader.getInt("sleepStage", 0);
        memoryStage = reader.getInt("memoryStage", 0);
        reader.save();
    }

}
