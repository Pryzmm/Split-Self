package com.pryzmm.splitself.data;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

public record Location(ServerWorld world, Vec3d position, Float yaw, Float pitch) {

    public Location(ServerWorld world, BlockPos position, int yaw, int pitch) {
        this(world, new Vec3d(position.getX(), position.getY(), position.getZ()), (float) yaw, (float) pitch);
    }

    @Override
    public @NotNull String toString() {
        return world.getRegistryKey().getValue().toString() + ";" + position.x + ";" + position.y + ";" + position.z + ";" + yaw + ";" + pitch;
    }

}
