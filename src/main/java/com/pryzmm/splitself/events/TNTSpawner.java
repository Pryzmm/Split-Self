package com.pryzmm.splitself.events;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class TNTSpawner {
    public static void spawnTntInCircle(PlayerEntity player, double radius, int count, int fuse) {
        World world = player.getWorld();
        if (world.isClient()) {
            return;
        }
        Vec3d playerPos = player.getPos();
        playerPos = EventManager.moveVectorFromBase(player, playerPos);
        for (int i = 0; i < count; i++) {
            double angle = (2 * Math.PI * i) / count;
            double x = playerPos.x + radius * Math.cos(angle);
            double z = playerPos.z + radius * Math.sin(angle);
            spawnTntAtPosition(world, x, playerPos.y, z, fuse);
        }

    }

    private static void spawnTntAtPosition(World world, double x, double y, double z, int fuse) {
        TntEntity tnt = EntityType.TNT.create(world);
        if (tnt == null) {
            return;
        }
        tnt.setPosition(x, y, z);
        tnt.setFuse(fuse);
        world.spawnEntity(tnt);
    }
}
