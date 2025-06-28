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

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class FirstJoinTracker extends PersistentState {
    private Set<UUID> joinedPlayers = new HashSet<>();

    public FirstJoinTracker() {}

    public FirstJoinTracker(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        NbtList playerList = nbt.getList("JoinedPlayers", NbtElement.STRING_TYPE);
        for (int i = 0; i < playerList.size(); i++) {
            String uuidString = playerList.getString(i);
            try {
                joinedPlayers.add(UUID.fromString(uuidString));
            } catch (IllegalArgumentException e) {
                // Skip invalid UUIDs
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
        return nbt;
    }

    public boolean hasJoinedBefore(UUID playerUuid) {
        return joinedPlayers.contains(playerUuid);
    }

    public void markAsJoined(UUID playerUuid) {
        joinedPlayers.add(playerUuid);
        markDirty();
    }

    public static FirstJoinTracker getServerState(MinecraftServer server) {
        PersistentStateManager persistentStateManager = server.getWorld(World.OVERWORLD)
                .getPersistentStateManager();

        return persistentStateManager.getOrCreate(
                new PersistentState.Type<>(
                        FirstJoinTracker::new, // supplier for new instances
                        FirstJoinTracker::new, // deserializer
                        null // datafixer (null for no fixing needed)
                ),
                "first_join_tracker"
        );
    }
}