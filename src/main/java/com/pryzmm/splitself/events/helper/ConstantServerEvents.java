package com.pryzmm.splitself.events.helper;

import com.pryzmm.splitself.entity.ModEntities;
import com.pryzmm.splitself.entity.custom.TheForgottenEntity;
import com.pryzmm.splitself.entity.custom.TheOtherEntity;
import com.pryzmm.splitself.entity.custom.TheOtherVariant;
import com.pryzmm.splitself.world.TickScheduler;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import net.minecraft.world.RaycastContext;

public class ConstantServerEvents {

    private final MinecraftServer server;
    private static final int ladderCooldown = 2000;
    private static final int doorCooldown = 12000;

    public ConstantServerEvents(MinecraftServer server) {
        this.server = server;
    }

    public void init() {
        checkLadder();
        checkDoors();
        raytraceTheForgotten();
    }

    private void checkLadder() {
        TickScheduler.schedule(20, () -> {
            boolean activated = false;
            for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
                ServerWorld world = p.getServerWorld();
                Direction facing;
                try { facing = p.getBlockStateAtPos().get(Properties.HORIZONTAL_FACING); } catch (Exception ignored) { continue; }
                BlockPos spawnPos = blockPosWithSpawnOffset(facing, p.getBlockPos());
                if (p.isClimbing() && world.getBlockState(p.getBlockPos().add(0, 1, 0)).getBlock() == Blocks.AIR && world.getBlockState(p.getBlockPos().add(0, 2, 0)).getBlock() == Blocks.AIR) {
                    activated = true;
                    TheOtherEntity theOther = new TheOtherEntity(ModEntities.TheOther, world, TheOtherVariant.STATIC);
                    theOther.refreshPositionAndAngles(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, 0.0F, 0.0F);
                    world.spawnEntity(theOther);
                    theOther.tryAttack(p);
                    TickScheduler.schedule(5, theOther::discard);
                    break;
                }
            }
            if (!activated) checkLadder();
            else TickScheduler.schedule(ladderCooldown, this::checkLadder);
        });
    }

    private void checkDoors() {
        TickScheduler.schedule((long) ((Math.random() * 2400) + 4800), () -> {
            boolean activated = false;
            playerLoop:
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                BlockPos pPos = player.getBlockPos();
                for (int x = -30; x <= 30; x++) {
                    for (int y = -30; y <= 30; y++) {
                        for (int z = -30; z <= 30; z++) {
                            BlockState state = player.getServerWorld().getBlockState(pPos.add(x, y, z));
                            if (state.getBlock() instanceof DoorBlock && state.get(DoorBlock.HALF) == DoubleBlockHalf.LOWER) {
                                BlockPos pos = blockPosWithSpawnOffset(state.get(Properties.HORIZONTAL_FACING), pPos.add(x, y, z));
                                BlockPos oPos = blockPosWithOppositeSpawnOffset(state.get(Properties.HORIZONTAL_FACING), pPos.add(x, y, z));
                                if (player.getServerWorld().getTopY(Heightmap.Type.MOTION_BLOCKING, pos.getX(), pos.getZ()) == pos.getY() - 1) {
                                    activated = true;
                                    TheForgottenEntity theForgotten = new TheForgottenEntity(ModEntities.TheForgotten, player.getServerWorld(), TheForgottenEntity.Type.STATIC);
                                    theForgotten.refreshPositionAndAngles(pos.getX() + 0.5, pos.getY() - 1, pos.getZ() + 0.5, 0.0F, 0.0F);
                                    player.getServerWorld().spawnEntity(theForgotten);
                                    break playerLoop;
                                } else if (player.getServerWorld().getTopY(Heightmap.Type.MOTION_BLOCKING, oPos.getX(), oPos.getZ()) == oPos.getY() - 1) {
                                    activated = true;
                                    TheForgottenEntity theForgotten = new TheForgottenEntity(ModEntities.TheForgotten, player.getServerWorld(), TheForgottenEntity.Type.STATIC);
                                    theForgotten.refreshPositionAndAngles(oPos.getX() + 0.5, oPos.getY() - 1, oPos.getZ() + 0.5, 0.0F, 0.0F);
                                    player.getServerWorld().spawnEntity(theForgotten);
                                    break playerLoop;
                                }
                            }
                        }
                    }
                }
            }
            if (!activated) checkDoors();
            else TickScheduler.schedule(doorCooldown, this::checkDoors);
        });
    }

    private void raytraceTheForgotten() {
        TickScheduler.schedule(2, () -> server.getPlayerManager().getPlayerList().forEach(p -> {
            Vec3d start = p.getCameraPosVec(1.0F);
            Vec3d look = p.getRotationVec(1.0F);
            Vec3d end = start.add(look.multiply(6));
            Box searchBox = p.getBoundingBox().stretch(look.multiply(6)).expand(1.0);
            EntityHitResult entityResult = ProjectileUtil.raycast(p, start, end, searchBox, entity -> entity instanceof TheForgottenEntity forgotten && forgotten.type == TheForgottenEntity.Type.STATIC, 36);
            if (entityResult == null) return;
            BlockHitResult blockResult = p.getWorld().raycast(new RaycastContext(start, end, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, p));
            if (blockResult.getType() == HitResult.Type.BLOCK) {
                double blockDistSq = start.squaredDistanceTo(blockResult.getPos());
                double entityDistSq = start.squaredDistanceTo(entityResult.getPos());
                if (blockDistSq < entityDistSq) return;
            }
            if (entityResult.getEntity() instanceof TheForgottenEntity forgotten) forgotten.discard();
        }));
        TickScheduler.schedule(2, this::raytraceTheForgotten);
    }



    private BlockPos blockPosWithSpawnOffset(Direction facing, BlockPos pos) {
        pos = pos.add(0, 1, 0);
        if      (facing == Direction.NORTH) pos = pos.add(0, 0, 1);
        else if (facing == Direction.SOUTH) pos = pos.add(0, 0, -1);
        else if (facing == Direction.EAST)  pos = pos.add(-1, 0, 0);
        else if (facing == Direction.WEST)  pos = pos.add(1, 0, 0);
        return pos;
    }

    private BlockPos blockPosWithOppositeSpawnOffset(Direction facing, BlockPos pos) {
        pos = pos.add(0, 1, 0);
        if      (facing == Direction.NORTH) pos = pos.add(0, 0, -1);
        else if (facing == Direction.SOUTH) pos = pos.add(0, 0, 1);
        else if (facing == Direction.EAST)  pos = pos.add(1, 0, 0);
        else if (facing == Direction.WEST)  pos = pos.add(-1, 0, 0);
        return pos;
    }

}
