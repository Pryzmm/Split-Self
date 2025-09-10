package com.pryzmm.splitself.entity.client;

import com.pryzmm.splitself.entity.ModEntities;
import com.pryzmm.splitself.entity.custom.TheOtherEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.entity.player.PlayerEntity;

import java.util.List;

public class TheOtherSpawner {

    public static Position[] spawnPositions = null;

    public static void trySpawnTheOther(ServerWorld world, PlayerEntity player, boolean silentSpawn) {
        if (spawnPositions == null || spawnPositions.length == 0) {
            return;
        }

        for (int attempt = 0; attempt < 100; attempt++) {
            Position prevPlayerPos = spawnPositions[world.getRandom().nextInt(spawnPositions.length)];
            Random random = world.getRandom();
            double spawnX = prevPlayerPos.getX();
            double spawnY = prevPlayerPos.getY();
            double spawnZ = prevPlayerPos.getZ();
            BlockPos spawnPos = new BlockPos((int) spawnX, (int) spawnY, (int) spawnZ);
            if (isValidSpawnLocation(world, spawnPos, player)) {
                List<? extends TheOtherEntity> entities = world.getEntitiesByType(ModEntities.TheOther, entity -> true);
                for (TheOtherEntity entity : entities) {
                    entity.discard();
                }
                TheOtherEntity theOther = new TheOtherEntity(ModEntities.TheOther, world);
                theOther.refreshPositionAndAngles(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, random.nextFloat() * 360.0F, 0.0F);
                world.spawnEntity(theOther);
                System.out.println(player);
                System.out.println(theOther.getBlockPos());
                if (!silentSpawn) {
                    world.playSound(null, theOther.getBlockPos(), SoundEvents.BLOCK_BELL_RESONATE, SoundCategory.MASTER, 1000.0f, 1.0f);
                }
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

        return !(Math.sqrt(player.squaredDistanceTo(new Vec3d(pos.getX(), pos.getY(), pos.getZ()))) <= 30);
    }
}
