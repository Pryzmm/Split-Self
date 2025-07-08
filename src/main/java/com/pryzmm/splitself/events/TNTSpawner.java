package com.pryzmm.splitself.events;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class TNTSpawner {
    public static List<TntEntity> spawnTntInCircle(PlayerEntity player, double radius, int count, int fuse) {
        World world = player.getWorld();
        List<TntEntity> tntList = new ArrayList<>();

        if (world.isClient()) {
            return tntList;
        }

        Vec3d playerPos = player.getPos();

        for (int i = 0; i < count; i++) {
            double angle = (2 * Math.PI * i) / count;
            double x = playerPos.x + radius * Math.cos(angle);
            double z = playerPos.z + radius * Math.sin(angle);

            TntEntity tnt = spawnTntAtPosition(world, x, playerPos.y, z, fuse);
            if (tnt != null) {
                tntList.add(tnt);
            }
        }

        return tntList;
    }

    private static TntEntity spawnTntAtPosition(World world, double x, double y, double z, int fuse) {
        TntEntity tnt = EntityType.TNT.create(world);
        if (tnt == null) {
            return null;
        }

        // Set position
        tnt.setPosition(x, y, z);

        // Set fuse time (-1 means no auto-ignite, positive values are ticks until explosion)
        if (fuse >= 0) {
            tnt.setFuse(fuse);
        }

        // Spawn in world
        world.spawnEntity(tnt);
        return tnt;
    }
}
