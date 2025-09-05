package com.pryzmm.splitself.entity.client;

import com.pryzmm.splitself.entity.ModEntities;
import com.pryzmm.splitself.entity.custom.TheForgottenEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;

import java.util.List;

public class TheForgottenSpawner {

    public static BlockPos[] spawnPositions = null;

    public static void trySpawnTheForgotten(ServerWorld world, PlayerEntity player) {
        if (spawnPositions == null || spawnPositions.length == 0) {
            return;
        }

        for (int attempt = 0; attempt < 100; attempt++) {
            BlockPos prevPlayerPos = spawnPositions[world.getRandom().nextInt(spawnPositions.length)];
            Random random = world.getRandom();
            double angle = random.nextDouble() * 2 * Math.PI;
            double spawnX = prevPlayerPos.getX() + Math.cos(angle) * 15;
            double spawnZ = prevPlayerPos.getZ() + Math.sin(angle) * 15;
            double spawnY = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, (int) spawnX, (int) spawnZ);
            BlockPos spawnPos = new BlockPos((int) spawnX, (int) spawnY, (int) spawnZ);
            if (isValidSpawnLocation(world, spawnPos, player)) {
                List<? extends TheForgottenEntity> entities = world.getEntitiesByType(ModEntities.TheForgotten, entity -> true);
                for (TheForgottenEntity entity : entities) {
                    entity.discard();
                }
                TheForgottenEntity theForgotten = new TheForgottenEntity(ModEntities.TheForgotten, world);
                theForgotten.refreshPositionAndAngles(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, random.nextFloat() * 360.0F, 0.0F);
                world.spawnEntity(theForgotten);
                System.out.println(player);
                System.out.println(theForgotten.getBlockPos());
                world.playSound(null, theForgotten.getBlockPos(), SoundEvents.BLOCK_BELL_RESONATE, SoundCategory.MASTER, 1000.0f, 0.2f);
                break;
            }
        }
    }

    private static boolean isValidSpawnLocation(ServerWorld world, BlockPos pos, PlayerEntity player) {
        ChunkPos chunkPos = new ChunkPos(pos);
        if (!world.isChunkLoaded(chunkPos.x, chunkPos.z)) {
            return false;
        }

        if (!world.getBlockState(pos).isAir() || !world.getBlockState(pos.up()).isAir()) {
            return false;
        }

        if (!world.getBlockState(pos.down()).isSolidBlock(world, pos.down())) {
            return false;
        }

        return !(Math.sqrt(player.squaredDistanceTo(new Vec3d(pos.getX(), pos.getY(), pos.getZ()))) <= 30);
    }
}
