package com.pryzmm.splitself.func;

import com.pryzmm.splitself.entity.ModEntities;
import com.pryzmm.splitself.entity.custom.TheForgottenEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;
import java.util.List;

public class StripMine {

    public static int minStripMineLength = 10;

    public static @Nullable Direction playerStripMineDirection(ServerPlayerEntity player, List<Direction> blacklistedDirections) {
        BlockPos northLoc = player.getBlockPos(), southLoc = player.getBlockPos(), eastLoc = player.getBlockPos(), westLoc = player.getBlockPos();
        boolean stopNorthChecks = blacklistedDirections.contains(Direction.NORTH), stopSouthChecks = blacklistedDirections.contains(Direction.SOUTH), stopEastChecks = blacklistedDirections.contains(Direction.EAST), stopWestChecks = blacklistedDirections.contains(Direction.WEST);
        ServerWorld world = player.getServerWorld();
        int northCount = 0, southCount = 0, eastCount = 0, westCount = 0;
        for (int i = 0; i < minStripMineLength; i++) {
            if (!stopNorthChecks) {
                northLoc = northLoc.offset(Direction.NORTH, 1);
                if (isInvalidPathway(world, northLoc)) stopNorthChecks = true;
                else northCount++;
            }
            if (!stopSouthChecks) {
                southLoc = southLoc.offset(Direction.SOUTH, 1);
                if (isInvalidPathway(world, southLoc)) stopSouthChecks = true;
                else southCount++;
            }
            if (!stopEastChecks) {
                eastLoc = eastLoc.offset(Direction.EAST, 1);
                if (isInvalidPathway(world, eastLoc)) stopEastChecks = true;
                else eastCount++;
            }
            if (!stopWestChecks) {
                westLoc = westLoc.offset(Direction.WEST, 1);
                if (isInvalidPathway(world, westLoc)) stopWestChecks = true;
                else westCount++;
            }
        }

        if (northCount >= minStripMineLength) return Direction.NORTH;
        if (southCount >= minStripMineLength) return Direction.SOUTH;
        if (eastCount >= minStripMineLength) return Direction.EAST;
        if (westCount >= minStripMineLength) return Direction.WEST;
        return null;
    }

    public static BlockPos getEntitySpawnLocation(ServerPlayerEntity player) {
        Direction direction = playerStripMineDirection(player, List.of(player.getHorizontalFacing()));
        if (direction != null) return player.getBlockPos().offset(direction, minStripMineLength - 1);
        return null;
    }

    private static boolean isInvalidPathway(ServerWorld world, BlockPos pos) {
        boolean selfSolid   =  world.getBlockState(pos)                              .isSolidBlock(world, pos);
        boolean noFloor     = !world.getBlockState(pos.offset(Direction.DOWN, 1)) .isSolidBlock(world, pos.offset(Direction.DOWN, 1));
        boolean headBlocked =  world.getBlockState(pos.offset(Direction.UP,   1)) .isSolidBlock(world, pos.offset(Direction.UP,   1));
        boolean ceilBlocked = !world.getBlockState(pos.offset(Direction.UP,   2)) .isSolidBlock(world, pos.offset(Direction.UP,   2));
        return selfSolid || noFloor || headBlocked || ceilBlocked;
    }

    public static void trySpawnAttempt(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (Math.random() * 500 < 1) {
                ServerWorld world = player.getServerWorld();
                BlockPos pos = StripMine.getEntitySpawnLocation(player);
                if (pos != null) {
                    TheForgottenEntity theForgotten = new TheForgottenEntity(ModEntities.TheForgotten, world, TheForgottenEntity.Type.DISAPPEAR);
                    theForgotten.refreshPositionAndAngles(pos, 360.0F, 0.0F);
                    world.spawnEntity(theForgotten);
                }
            }
        }
    }

}
