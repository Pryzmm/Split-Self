package com.pryzmm.splitself.forge;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
/** PlayerPersisted is Forge's durable, respawn-retained player namespace. */
final class TransitionIntent {
    private static final String PERSISTED = "PlayerPersisted";
    private static final String KEY = "splitselfPendingTransition";
    static UUID read(CompoundTag root) {
        var data = root.getCompound(PERSISTED);
        return data.hasUUID(KEY) ? data.getUUID(KEY) : null;
    }
    static void write(CompoundTag root, UUID id) {
        if (!root.contains(PERSISTED)) root.put(PERSISTED, new CompoundTag());
        root.getCompound(PERSISTED).putUUID(KEY, id);
    }
    static void remove(CompoundTag root) { root.getCompound(PERSISTED).remove(KEY); }
    static void copy(CompoundTag original, CompoundTag target) {
        UUID id = read(original);
        if (id != null) write(target, id);
    }
}
