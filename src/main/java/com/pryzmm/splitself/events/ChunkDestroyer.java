package com.pryzmm.splitself.events;

import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Random;

public class ChunkDestroyer {
    public static void execute(PlayerEntity Player) {
        Vec3d playerPos = new Vec3d(
                Player.getPos().x + (new Random().nextInt(20 + 20) - 20),
                319,
                Player.getPos().z + (new Random().nextInt(20 + 20) - 20)
        );
        Vec3d pos1 = new Vec3d(
                playerPos.x - 8,
                Player.getWorld().getTopY(),
                playerPos.z - 8
        );
        Vec3d pos2 = new Vec3d(
                playerPos.x + 7,
                Player.getWorld().getBottomY(),
                playerPos.z + 7
        );

        new Thread(() -> {
            for (int y = (int) pos1.getY(); y >= (int) pos2.getY(); y--) {
                for (int x = (int) pos1.getX(); x <= (int) pos2.getX(); x++) {
                    for (int z = (int) pos1.getZ(); z <= (int) pos2.getZ(); z++) {
                        BlockPos pos = new BlockPos(x, y, z);
                        if (!Player.getWorld().getBlockState(pos).isAir()) {
                            Player.getWorld().setBlockState(pos, Blocks.AIR.getDefaultState());
                        }
                    }
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                System.out.println(y);
            }
            System.out.print("ended");
        }).start();
    }
}
