package com.pryzmm.splitself.entity;

import com.pryzmm.splitself.entity.custom.TheForgottenEntity;
import com.pryzmm.splitself.world.DimensionRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import java.util.List;

public class TheForgottenFunc {

    public static void tryRandomSpawn(ServerWorld world) {
        if (world.getPlayers() == null || world.getPlayers().isEmpty()) return;
        ServerPlayerEntity player = world.getPlayers().get((int) (Math.random() * world.getPlayers().size()));
        for (int i = 0; i < 100; i++) {
            int offsetX, offsetZ;
            do { offsetX = (int) (Math.random() * 50 - 25); } while (offsetX > -10 && offsetX < 10);
            do { offsetZ = (int) (Math.random() * 50 - 25); } while (offsetZ > -10 && offsetZ < 10);
            int x = player.getBlockX() + offsetX;
            int z = player.getBlockZ() + offsetZ;
            int y = world.getTopY(Heightmap.Type.WORLD_SURFACE, x, z);
            if (y >= 55 && y <= 75) {
                List<? extends TheForgottenEntity> entities = world.getEntitiesByType(ModEntities.TheForgotten, entity -> true);
                entities.forEach(Entity::discard);
                BlockPos pos = new BlockPos(x, y, z);
                TheForgottenEntity theForgotten = new TheForgottenEntity(ModEntities.TheForgotten, world);
                theForgotten.refreshPositionAndAngles(pos, 0, 0);
                world.spawnEntity(theForgotten);
                break;
            }
        }
    }

    public static void removeIfRayCasted(MinecraftServer server) {
        ServerWorld world = server.getWorld(DimensionRegistry.EMPTINESS_DIMENSION_KEY);
        if (world == null) return;

        for (ServerPlayerEntity player : world.getPlayers()) {
            Vec3d start = player.getEyePos();
            Vec3d direction = player.getRotationVec(1.0f);
            double reach = 10000.0;

            Vec3d end = start.add(direction.multiply(reach));

            EntityHitResult hit = ProjectileUtil.getEntityCollision(
                player.getWorld(),
                player,
                start,
                end,
                player.getBoundingBox().stretch(direction.multiply(reach)).expand(1.0),
                e -> !e.isSpectator() && e.isAlive(),
                7.0f
            );

            if (hit != null && hit.getEntity() instanceof TheForgottenEntity) {
                hit.getEntity().discard();
            }
        }
    }

}
