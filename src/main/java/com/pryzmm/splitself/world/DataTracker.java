package com.pryzmm.splitself.world;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class DataTracker extends PersistentState {
    private Set<UUID> joinedPlayers = new HashSet<>();
    private Map<UUID, Boolean> playerPII = new HashMap<>();
    private Map<UUID, Boolean> playerReadWarning = new HashMap<>();
    private Map<UUID, Integer> playerSleepStage = new HashMap<>();

    public DataTracker() {}

    public DataTracker(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        NbtList playerList = nbt.getList("JoinedPlayers", NbtElement.STRING_TYPE);
        for (int i = 0; i < playerList.size(); i++) {
            String uuidString = playerList.getString(i);
            try {
                joinedPlayers.add(UUID.fromString(uuidString));
            } catch (IllegalArgumentException ignored) {
            }
        }

        NbtCompound piiData = nbt.getCompound("PIIData");
        for (String key : piiData.getKeys()) {
            try {
                playerPII.put(UUID.fromString(key), piiData.getBoolean(key));
            } catch (IllegalArgumentException ignored) {
            }
        }

        NbtCompound SleepStageData = nbt.getCompound("SleepStageData");
        for (String key : SleepStageData.getKeys()) {
            try {
                playerSleepStage.put(UUID.fromString(key), SleepStageData.getInt(key));
            } catch (IllegalArgumentException ignored) {
            }
        }

        NbtCompound warningData = nbt.getCompound("ReadWarningData");
        for (String key : warningData.getKeys()) {
            try {
                playerReadWarning.put(UUID.fromString(key), warningData.getBoolean(key));
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

        NbtList playerList = new NbtList();
        for (UUID uuid : joinedPlayers) {
            playerList.add(NbtString.of(uuid.toString()));
        }
        nbt.put("JoinedPlayers", playerList);

        NbtCompound piiData = new NbtCompound();
        for (Map.Entry<UUID, Boolean> entry : playerPII.entrySet()) {
            piiData.putBoolean(entry.getKey().toString(), entry.getValue());
        }
        nbt.put("PIIData", piiData);

        NbtCompound warningData = new NbtCompound();
        for (Map.Entry<UUID, Boolean> entry : playerReadWarning.entrySet()) {
            warningData.putBoolean(entry.getKey().toString(), entry.getValue());
        }
        nbt.put("ReadWarningData", warningData);

        NbtCompound SleepStageData = new NbtCompound();
        for (Map.Entry<UUID, Integer> entry : playerSleepStage.entrySet()) {
            SleepStageData.putInt(entry.getKey().toString(), entry.getValue());
        }
        nbt.put("SleepStageData", SleepStageData);

        return nbt;
    }

    public boolean hasJoinedBefore(UUID playerUuid) {
        return joinedPlayers.contains(playerUuid);
    }

    public void markAsJoined(UUID playerUuid) {
        joinedPlayers.add(playerUuid);
        markDirty();
    }

    public boolean getPlayerPII(UUID playerUuid) {
        return playerPII.getOrDefault(playerUuid, false);
    }

    public void setPlayerPII(UUID playerUuid, boolean pii) {
        playerPII.put(playerUuid, pii);
        markDirty();
    }

    public Integer getPlayerSleepStage(UUID playerUuid) {
        return playerSleepStage.getOrDefault(playerUuid, 0);
    }

    public void setPlayerSleepStage(UUID playerUuid, Integer sleepStage) {
        playerSleepStage.put(playerUuid, sleepStage);
        markDirty();
    }

    public boolean getPlayerReadWarning(UUID playerUuid) {
        return playerReadWarning.getOrDefault(playerUuid, false);
    }

    public void setPlayerReadWarning(UUID playerUuid, boolean readWarning) {
        playerReadWarning.put(playerUuid, readWarning);
        markDirty();
    }

    public static DataTracker getServerState(MinecraftServer server) {
        PersistentStateManager persistentStateManager = server.getWorld(World.OVERWORLD)
                .getPersistentStateManager();

        return persistentStateManager.getOrCreate(
                new PersistentState.Type<>(
                        DataTracker::new, // supplier for new instances
                        DataTracker::new, // deserializer
                        null
                ),
                "first_join_tracker"
        );
    }
}