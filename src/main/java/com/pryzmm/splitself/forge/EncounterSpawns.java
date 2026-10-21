package com.pryzmm.splitself.forge;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.TickEvent;

final class EncounterSpawns {
    private EncounterSpawns() {}
    private static int nextForgotten = 600;
    private static final java.util.List<net.minecraft.world.phys.Vec3> HISTORY = new java.util.ArrayList<>();
    static void reset() { nextForgotten = 600; HISTORY.clear(); }
    static void remember(ServerPlayer player) { HISTORY.add(player.position()); }
    static boolean theOther(ServerPlayer player) { return theOther(player,false); }
    static boolean theOther(ServerPlayer player,boolean silent) {
        if (HISTORY.isEmpty()) return false;
        var world = player.serverLevel();
        for (int attempt = 0; attempt < 100; attempt++) {
            var previous = HISTORY.get(world.random.nextInt(HISTORY.size()));
            var pos = new BlockPos((int) previous.x, (int) previous.y, (int) previous.z);
            if (!world.hasChunk(pos.getX() >> 4, pos.getZ() >> 4)
                    || !world.getBlockState(pos).isAir() || !world.getBlockState(pos.above()).isAir()
                    || player.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) <= 900) continue;
            var actor = SplitSelfForge.THE_OTHER.get().create(world);
            if (actor == null) return false;
            var old = new java.util.ArrayList<TheOtherEntity>();
            for (var entity : world.getAllEntities()) if (entity instanceof TheOtherEntity other) old.add(other);
            old.forEach(TheOtherEntity::discard);
            actor.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, world.random.nextFloat() * 360, 0);
            actor.finalizeSpawn(world, world.getCurrentDifficultyAt(pos), net.minecraft.world.entity.MobSpawnType.TRIGGERED, null, null);
            if (!world.addFreshEntity(actor)) return false;
            if (!silent) world.playSound(null, pos, net.minecraft.sounds.SoundEvents.BELL_RESONATE, SoundSource.MASTER, 1000, 1);
            return true;
        }
        return false;
    }

    static boolean forgottenNearHouse(ServerPlayer player) {
        ServerLevel world = player.serverLevel();
        var location = MemoryData.get(world).forgottenLocation();
        if (location == null) return false;
        var random = world.random;
        for (int attempt = 0; attempt < 100; attempt++) {
            double angle = random.nextDouble() * 2 * Math.PI;
            int x = (int) (location.position().x + Math.cos(angle) * 15);
            int z = (int) (location.position().z + Math.sin(angle) * 15);
            if (!world.hasChunk(x >> 4, z >> 4)) continue;
            int y = world.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            BlockPos pos = new BlockPos(x, y, z);
            if (!world.getBlockState(pos).isAir() || !world.getBlockState(pos.above()).isAir()
                    || !world.getBlockState(pos.below()).isSolidRender(world, pos.below())
                    || player.distanceToSqr(x, y, z) <= 400) continue;
            return spawn(world, x + 0.5, y, z + 0.5, 0, true);
        }
        return false;
    }

    static boolean forgottenRandom(ServerPlayer player) {
        var world = player.serverLevel();
        var random = world.random;
        for (int attempt = 0; attempt < 100; attempt++) {
            int dx, dz;
            do { dx = (int) (random.nextDouble() * 50 - 25); } while (dx > -10 && dx < 10);
            do { dz = (int) (random.nextDouble() * 50 - 25); } while (dz > -10 && dz < 10);
            int x = player.getBlockX() + dx, z = player.getBlockZ() + dz;
            int y = world.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
            if (y >= 55 && y <= 75) return spawn(world, x, y, z, 1, false);
        }
        return false;
    }

    private static boolean spawn(ServerLevel world, double x, double y, double z, int type, boolean sound) {
        var entity = SplitSelfForge.THE_FORGOTTEN.get().create(world);
        if (entity == null) return false;
        entity.setEncounterType(type);
        entity.moveTo(x, y, z, type == 0 ? world.random.nextFloat() * 360 : 0, 0);
        if (!world.addFreshEntity(entity)) return false;
        if (sound) world.playSound(null, entity.blockPosition(), SoundEvent.createVariableRangeEvent(
                new ResourceLocation("splitself", "forgotten")), SoundSource.MASTER, 1000, 1);
        return true;
    }

    static void onTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        var empty = event.getServer().getLevel(SplitSelfWorldEvents.EMPTY);
        if (empty != null && --nextForgotten == 0) {
            nextForgotten = 600;
            if (!empty.players().isEmpty()) forgottenRandom(empty.players().get(empty.random.nextInt(empty.players().size())));
        }
        for (var player : event.getServer().getPlayerList().getPlayers()) {
            boolean present = false;
            for (var entity : player.serverLevel().getAllEntities()) {
                if (entity instanceof TheForgottenEntity forgotten && forgotten.encounterType() == 1) {
                    present = true; break;
                }
            }
            if (!present) continue;
            var start = player.getEyePosition();
            var direction = player.getLookAngle().scale(10000);
            var hit = ProjectileUtil.getEntityHitResult(player.level(), player, start, start.add(direction),
                    player.getBoundingBox().expandTowards(direction).inflate(1),
                    entity -> !entity.isSpectator() && entity.isAlive(), 4);
            if (hit != null && hit.getEntity() instanceof TheForgottenEntity entity
                    && entity.encounterType() == 1) entity.discard();
        }
    }
}
