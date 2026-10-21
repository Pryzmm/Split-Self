package com.pryzmm.splitself.forge;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

final class KickRules {
    private KickRules() {}
    static final class Permits {
        private final Map<UUID, UUID> pending = new HashMap<>();
        UUID issue(UUID player) { UUID token = UUID.randomUUID(); pending.put(player, token); return token; }
        boolean consume(UUID player, UUID token) { return pending.remove(player, token); }
        void forget(UUID player) { pending.remove(player); }
        void clear() { pending.clear(); }
    }
    static void erase(Vec3 center, Predicate<BlockPos> available, Predicate<BlockPos> air,
                      BooleanSupplier random, Consumer<BlockPos> remove) {
        // Source uses truncation toward zero, inclusive bounds and Y/X/Z order.
        for (int y = (int)(center.y - 8); y <= (int)(center.y + 7); y++)
            for (int x = (int)(center.x - 8); x <= (int)(center.x + 7); x++)
                for (int z = (int)(center.z - 8); z <= (int)(center.z + 7); z++) {
                    BlockPos pos = new BlockPos(x,y,z);
                    if (available.test(pos) && !air.test(pos) && random.getAsBoolean()) remove.accept(pos);
                }
    }
}
