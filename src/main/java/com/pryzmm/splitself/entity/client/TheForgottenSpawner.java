package com.pryzmm.splitself.entity.client;

import com.pryzmm.splitself.data.WorldData;
import com.pryzmm.splitself.entity.ModEntities;
import com.pryzmm.splitself.entity.custom.TheForgottenEntity;
import com.pryzmm.splitself.sound.ModSounds;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;

import java.util.List;

public class TheForgottenSpawner {

    public static void trySpawnTheForgotten(ServerWorld world, PlayerEntity player) {
        if (WorldData.getTheForgottenLocation() == null) return;

        for (int attempt = 0; attempt < 100; attempt++) {
            Random random = world.getRandom();
            double angle = random.nextDouble() * 2 * Math.PI;
            double spawnX = WorldData.getTheForgottenLocation().position().x + Math.cos(angle) * 15;
            double spawnZ = WorldData.getTheForgottenLocation().position().z + Math.sin(angle) * 15;
            double spawnY = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, (int) spawnX, (int) spawnZ);
            BlockPos spawnPos = new BlockPos((int) spawnX, (int) spawnY, (int) spawnZ);
            if (isValidSpawnLocation(world, spawnPos, player)) {
                List<? extends TheForgottenEntity> entities = world.getEntitiesByType(ModEntities.TheForgotten, entity -> true);
                for (TheForgottenEntity entity : entities) {
                    entity.discard();
                }
                TheForgottenEntity theForgotten = new TheForgottenEntity(ModEntities.TheForgotten, world, TheForgottenEntity.Type.NORMAL);
                theForgotten.refreshPositionAndAngles(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, random.nextFloat() * 360.0F, 0.0F);
                world.spawnEntity(theForgotten);
                world.playSound(null, theForgotten.getBlockPos(), ModSounds.FORGOTTEN, SoundCategory.MASTER, 1000.0f, 1f);
                break;
            }
        }
    }

    private static boolean isValidSpawnLocation(ServerWorld world, BlockPos pos, PlayerEntity player) {
        ChunkPos chunkPos = new ChunkPos(pos);
        if (!world.isChunkLoaded(chunkPos.x, chunkPos.z)) return false;
        if (!world.getBlockState(pos).isAir() || !world.getBlockState(pos.up()).isAir()) return false;
        if (!world.getBlockState(pos.down()).isSolidBlock(world, pos.down())) return false;
        return !(Math.sqrt(player.squaredDistanceTo(new Vec3d(pos.getX(), pos.getY(), pos.getZ()))) <= 20);
    }
}
