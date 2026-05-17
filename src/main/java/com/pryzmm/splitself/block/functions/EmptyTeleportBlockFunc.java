package com.pryzmm.splitself.block.functions;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import java.util.HashMap;
import java.util.UUID;

public class EmptyTeleportBlockFunc {

    public static final HashMap<UUID, Location> location = new HashMap<>();

    public record Location(ServerWorld world, Vec3d pos) {}

    public static void updateLastLocation(ServerPlayerEntity player) {
        Location loc = new Location(player.getServerWorld(), player.getPos());
        location.put(player.getUuid(), loc);
    }

    public static Location getLastLocation(ServerPlayerEntity player) {
        return location.get(player.getUuid());
    }

}
