package com.pryzmm.splitself.forge;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.saveddata.SavedData;

/** Persistent, per-world fields from the original WorldData. */
public final class MemoryData extends SavedData {
    private static final String NAME = "splitself_memories";
    private final Set<String> unlocked = new LinkedHashSet<>();
    private final Set<UUID> joinedPlayers = new LinkedHashSet<>();
    private boolean isFull;
    private int sleepStage;
    private int memoryStage;
    private boolean clickedButton;
    private boolean isDeleted;
    private boolean prevLoaded;
    private ForgottenLocation forgottenLocation;

    /** Location.toString in the source also records dimension, position and rotation. */
    public record ForgottenLocation(ResourceKey<Level> dimension, Vec3 position, float yaw, float pitch) {}

    public static MemoryData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage()
                .computeIfAbsent(MemoryData::load, () -> importLegacy(level), NAME);
    }

    private static MemoryData importLegacy(ServerLevel level) {
        var file=level.getServer().getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT).resolve("data/splitself.json");
        if(java.nio.file.Files.isRegularFile(file))try {
            MemoryData imported=load(LegacyWorldImport.convert(java.nio.file.Files.readString(file,java.nio.charset.StandardCharsets.UTF_8)));
            imported.setDirty();return imported;
        }catch(Exception failure){org.slf4j.LoggerFactory.getLogger("splitself").error("Legacy Split Self JSON import failed; original file is untouched",failure);}
        return new MemoryData();
    }
    private static MemoryData load(CompoundTag tag) {
        MemoryData data = new MemoryData();
        ListTag list = tag.getList("unlockedMemories", 8);
        for (int i = 0; i < list.size(); i++) data.unlocked.add(list.getString(i));
        ListTag players = tag.getList("joinedPlayers", 8);
        for (int i = 0; i < players.size(); i++) {
            try { data.joinedPlayers.add(UUID.fromString(players.getString(i))); }
            catch (IllegalArgumentException ignored) { /* Skip malformed legacy data. */ }
        }
        data.isFull = tag.getBoolean("isFull");
        data.sleepStage = tag.getInt("sleepStage");
        data.memoryStage = tag.getInt("memoryStage");
        data.clickedButton = tag.getBoolean("clickedButton");
        data.isDeleted = tag.getBoolean("isDeleted");
        data.prevLoaded = tag.getBoolean("prevLoaded");
        if (tag.contains("theForgottenLocation", 10)) {
            CompoundTag location = tag.getCompound("theForgottenLocation");
            ResourceLocation dimension = ResourceLocation.tryParse(location.getString("dimension"));
            if (dimension != null) {
                data.forgottenLocation = new ForgottenLocation(
                        ResourceKey.create(Registries.DIMENSION, dimension),
                        new Vec3(location.getDouble("x"), location.getDouble("y"), location.getDouble("z")),
                        location.getFloat("yaw"), location.getFloat("pitch"));
            }
        }
        return data;
    }

    public boolean unlock(String id) {
        if (!unlocked.add(id)) return false;
        setDirty();
        return true;
    }

    public boolean contains(String id) {
        return unlocked.contains(id);
    }

    public Set<String> unlockedMemories() { return new LinkedHashSet<>(unlocked); }

    public boolean recordJoin(UUID playerId) {
        if (!joinedPlayers.add(playerId)) return false;
        setDirty();
        return true;
    }

    public int sleepStage() { return sleepStage; }

    public void setSleepStage(int value) {
        sleepStage = value;
        setDirty();
    }

    public int memoryStage() { return memoryStage; }

    public void setMemoryStage(int value) {
        memoryStage = value;
        setDirty();
    }

    public int count() {
        return unlocked.size();
    }

    public boolean isFull() { return isFull; }

    public boolean clickedButton() { return clickedButton; }
    public void setClickedButton(boolean value) {
        if (clickedButton != value) { clickedButton = value; setDirty(); }
    }

    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean value) {
        if (isDeleted != value) { isDeleted = value; setDirty(); }
    }

    public boolean previouslyLoaded() { return prevLoaded; }
    public void setPreviouslyLoaded(boolean value) {
        if (prevLoaded != value) { prevLoaded = value; setDirty(); }
    }

    public ForgottenLocation forgottenLocation() { return forgottenLocation; }
    public void setForgottenLocation(ForgottenLocation value) {
        forgottenLocation = value;
        setDirty();
    }

    public void setFull(boolean value) {
        if (isFull != value) {
            isFull = value;
            setDirty();
        }
    }

    public int bitMask() {
        int bits = 0;
        if (contains("house")) bits |= 1;
        if (contains("blu")) bits |= 2;
        if (contains("mines")) bits |= 4;
        if (contains("pillar")) bits |= 8;
        if (contains("creeper")) bits |= 16;
        return bits;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (String id : unlocked) list.add(StringTag.valueOf(id));
        tag.put("unlockedMemories", list);
        ListTag players = new ListTag();
        for (UUID id : joinedPlayers) players.add(StringTag.valueOf(id.toString()));
        tag.put("joinedPlayers", players);
        tag.putBoolean("isFull", isFull);
        tag.putInt("sleepStage", sleepStage);
        tag.putInt("memoryStage", memoryStage);
        tag.putBoolean("clickedButton", clickedButton);
        tag.putBoolean("isDeleted", isDeleted);
        tag.putBoolean("prevLoaded", prevLoaded);
        if (forgottenLocation != null) {
            CompoundTag location = new CompoundTag();
            location.putString("dimension", forgottenLocation.dimension().location().toString());
            location.putDouble("x", forgottenLocation.position().x);
            location.putDouble("y", forgottenLocation.position().y);
            location.putDouble("z", forgottenLocation.position().z);
            location.putFloat("yaw", forgottenLocation.yaw());
            location.putFloat("pitch", forgottenLocation.pitch());
            tag.put("theForgottenLocation", location);
        }
        return tag;
    }
}
