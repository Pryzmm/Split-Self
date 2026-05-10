package com.pryzmm.splitself.events;

import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;

import java.util.Random;

public class ChunkDestroyer {

    public static boolean liftChunkActive = false;

    public static void liftChunk(ServerPlayerEntity player, ServerWorld world, int LoopCount, int lift) {
        double posX = player.getX();
        double posZ = player.getZ();
        for (int i = 0; i < LoopCount; i++) {
            double calcAngle = world.getRandom().nextDouble() * 2 * Math.PI;
            int centerX = Math.toIntExact(Math.round(posX + (Math.cos(calcAngle) * (20 + world.getRandom().nextDouble() * (40 - 20)))));
            int centerZ = Math.toIntExact(Math.round(posZ + (Math.sin(calcAngle) * (20 + world.getRandom().nextDouble() * (40 - 20)))));
            int surfaceY = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, centerX, centerZ);
            BlockPos centerPos = new BlockPos(centerX, surfaceY, centerZ);
            BlockPos structureMin = new BlockPos(centerPos.getX() - 7, surfaceY - 14, centerPos.getZ() - 7);
            BlockPos structureMax = new BlockPos(centerPos.getX() + 7, surfaceY + 7, centerPos.getZ() + 7);
            StructureManager.saveStructure(world, structureMin, structureMax, "clone");
            ChunkDestroyer.deleteArea(world, structureMin, structureMax);
            StructureManager.placeStructureRandomRotation(world, new BlockPos(centerPos.getX() - 7, centerPos.getY() + lift, centerPos.getZ() - 7), "clone", 0, 0, true, 0.75f, true);
        }
        liftChunkActive = false;
    }

    public static void deleteArea(World world, BlockPos minimumPos, BlockPos maximumPos) {
        for (int y = minimumPos.getY(); y <= maximumPos.getY(); y++) {
            for (int x = minimumPos.getX(); x <= maximumPos.getX(); x++) {
                for (int z = minimumPos.getZ(); z <= maximumPos.getZ(); z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (!world.getBlockState(pos).isAir()) {
                        world.setBlockState(pos, Blocks.AIR.getDefaultState());
                    }
                }
            }
        }
    }

    public static void execute(PlayerEntity player) {
        ServerWorld world = (ServerWorld) player.getWorld();

        Vec3d playerPos = new Vec3d(
            player.getPos().x + (new Random().nextInt(40) - 20),
            319,
            player.getPos().z + (new Random().nextInt(40) - 20)
        );
        playerPos = EventManager.moveVectorFromBase(player, playerPos);

        final int x1 = (int) playerPos.x - 8, x2 = (int) playerPos.x + 7;
        final int z1 = (int) playerPos.z - 8, z2 = (int) playerPos.z + 7;
        final int topY = world.getTopY(), bottomY = world.getBottomY();

        new Thread(() -> {
            for (int y = topY; y >= bottomY; y--) {
                final int finalY = y;
                world.getServer().execute(() -> {
                    for (int x = x1; x <= x2; x++) {
                        for (int z = z1; z <= z2; z++) {
                            BlockPos pos = new BlockPos(x, finalY, z);
                            if (!world.getBlockState(pos).isAir()) {
                                world.setBlockState(pos, Blocks.AIR.getDefaultState());
                            }
                        }
                    }
                });
                try {
                    Thread.sleep(50); // give the server thread time to process
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }).start();
    }

}
