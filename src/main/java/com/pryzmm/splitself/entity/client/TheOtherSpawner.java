package com.pryzmm.splitself.entity.client;

import com.pryzmm.splitself.entity.ModEntities;
import com.pryzmm.splitself.entity.custom.TheOtherEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.entity.player.PlayerEntity;

public class TheOtherSpawner {
    private static final int MIN_DISTANCE = 30; // Minimum distance from player
    private static final int MAX_DISTANCE = 80; // Maximum distance (horizon range)
    private static final int SPAWN_ATTEMPTS = 10; // Number of attempts to find valid spawn location

    public static void trySpawnTheOther(ServerWorld world, PlayerEntity player) {
        Random random = world.getRandom();

        for (int attempt = 0; attempt < SPAWN_ATTEMPTS; attempt++) {
            // Generate random angle around the player
            double angle = random.nextDouble() * 2 * Math.PI;

            // Generate random distance within range
            double distance = MIN_DISTANCE + random.nextDouble() * (MAX_DISTANCE - MIN_DISTANCE);

            // Calculate spawn position
            Vec3d playerPos = player.getPos();
            double spawnX = playerPos.x + Math.cos(angle) * distance;
            double spawnZ = playerPos.z + Math.sin(angle) * distance;

            // Get surface height at spawn location
            BlockPos spawnPos = new BlockPos((int) spawnX, 0, (int) spawnZ);
            int surfaceY = world.getTopY(Heightmap.Type.WORLD_SURFACE, spawnPos.getX(), spawnPos.getZ());

            // Create final spawn position
            BlockPos finalSpawnPos = new BlockPos((int) spawnX, surfaceY, (int) spawnZ);

            // Check if the spawn location is valid
            if (isValidSpawnLocation(world, finalSpawnPos)) {
                // Create and spawn the entity
                TheOtherEntity theOther = new TheOtherEntity(ModEntities.TheOther, world);
                theOther.refreshPositionAndAngles(finalSpawnPos.getX() + 0.5, finalSpawnPos.getY(), finalSpawnPos.getZ() + 0.5,
                        random.nextFloat() * 360.0F, 0.0F);

                // Spawn the entity
                if (theOther.canSpawn(world, SpawnReason.TRIGGERED)) {
                    world.spawnEntity(theOther);
                    break; // Successfully spawned, exit loop
                }
            }
        }
    }

    private static boolean isValidSpawnLocation(ServerWorld world, BlockPos pos) {
        // Check if position is loaded
        ChunkPos chunkPos = new ChunkPos(pos);
        if (!world.isChunkLoaded(chunkPos.x, chunkPos.z)) {
            return false;
        }

        // Check if there's enough space (2 blocks high)
        if (!world.getBlockState(pos).isAir() || !world.getBlockState(pos.up()).isAir()) {
            return false;
        }

        // Check if the block below is solid
        if (!world.getBlockState(pos.down()).isSolidBlock(world, pos.down())) {
            return false;
        }

        // Ensure it's not too close to any player
        for (PlayerEntity worldPlayer : world.getPlayers()) {
            double distance = worldPlayer.squaredDistanceTo(pos.getX(), pos.getY(), pos.getZ());
            if (distance < MIN_DISTANCE * MIN_DISTANCE) {
                return false;
            }
        }

        return true;
    }
}
