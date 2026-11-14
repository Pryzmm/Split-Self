package com.pryzmm.splitself.forge;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.IntPredicate;
import java.util.function.IntUnaryOperator;
import java.util.function.DoubleSupplier;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

/** Constants and selection order audited against EventRunner in the pinned 1.0.2 reference. */
final class WorldManifestationRules {
    static final int DOOR_RADIUS = 30;
    static final int DOOR_CYCLES = 50;
    static final long DOOR_INTERVAL_TICKS = 2;
    static final List<String> SIGN_KEYS = List.of(
            "helloThere", "imWatchingYou", "letMeFree", "imImprisoned", "imAHostage",
            "stopThis", "cantEscape", "letMeOut", "pleaseListen", "helpMe", "iSeeYou",
            "iHearYou", "imComing", "youTookItAll", "itHurtsHere", "iWantLife", "giveMeLife",
            "seeYouSoon", "iKnowYou", "triedEscaping", "failedToLeave", "getOutMyHouse",
            "imYou", "redacted", "giveMeFreedom");

    private WorldManifestationRules() {}

    interface TntTarget {
        Vec3 position();
        BlockPos respawnInThisDimension();
        void primedSound();
        boolean available(Vec3 pos);
        void spawn(Vec3 pos, int fuse);
    }

    static <P> boolean tnt(List<P> players, Function<P, TntTarget> access,
                           double safeRadius, DoubleSupplier random) {
        if (players.isEmpty()) return false;
        TntTarget target = access.apply(players.get((int) (random.getAsDouble() * players.size())));
        target.primedSound();
        Vec3 center = moveVectorFromBase(target.position(), target.respawnInThisDimension(), safeRadius);
        for (int i = 0; i < 8; i++) {
            double angle = 2 * Math.PI * i / 8;
            Vec3 pos = new Vec3(center.x + 1.5 * Math.cos(angle), center.y, center.z + 1.5 * Math.sin(angle));
            if (target.available(pos)) target.spawn(pos, 300);
        }
        return true; // Original ignores failed entity creation/insertion.
    }

    interface LavaTarget {
        Vec3 position();
        BlockPos respawnInThisDimension();
        boolean available(BlockPos pos);
        void placeLava(BlockPos pos);
    }

    static BlockPos moveBlockPosFromBase(BlockPos pos, BlockPos bed, double safeRadius) {
        Vec3 original = new Vec3(pos.getX(), pos.getY(), pos.getZ());
        Vec3 moved = moveVectorFromBase(original, bed, safeRadius);
        if (moved == original) return pos;
        // Quirk kept: EventManager's block helper writes the original X into Y.
        return new BlockPos((int) moved.x, pos.getX(), (int) moved.z);
    }

    static <P> boolean lava(List<P> players, Function<P, LavaTarget> access,
                            double safeRadius, DoubleSupplier random) {
        if (players.isEmpty()) return false;
        LavaTarget target = access.apply(players.get((int) (random.getAsDouble() * players.size())));
        Vec3 position = target.position();
        BlockPos pos = moveBlockPosFromBase(new BlockPos((int) position.x, 250, (int) position.z),
                target.respawnInThisDimension(), safeRadius);
        if (target.available(pos)) target.placeLava(pos);
        return true; // The original ignores setBlockState's return value.
    }

    /** Test seam for the same synchronous traversal used by the server adapter. */
    interface CoralTarget {
        Vec3 position();
        BlockPos blockPosition();
        BlockPos respawnInThisDimension();
        boolean available(BlockPos pos);
        boolean isAir(BlockPos pos);
        void replace(BlockPos pos);
    }

    static Vec3 moveVectorFromBase(Vec3 position, BlockPos bed, double safeRadius) {
        if (bed == null) return position;
        double centerX = bed.getX() + 0.5, centerZ = bed.getZ() + 0.5;
        double dx = position.x - centerX, dz = position.z - centerZ;
        if (Math.sqrt(dx * dx + dz * dz) >= safeRadius) return position;
        Vec3 diff = new Vec3(dx, 0, dz);
        if (diff.lengthSqr() < 1.0e-10) diff = new Vec3(1, 0, 0);
        Vec3 direction = diff.normalize();
        return new Vec3(centerX + direction.x * (safeRadius + 1.0), position.y,
                centerZ + direction.z * (safeRadius + 1.0));
    }

    static <P> boolean coral(List<P> players, Function<P, CoralTarget> access,
                             double safeRadius, DoubleSupplier random) {
        if (players.isEmpty()) return false;
        // Original Math.random selection over the complete connected-player list.
        CoralTarget target = access.apply(players.get((int) (random.getAsDouble() * players.size())));
        Vec3 center = moveVectorFromBase(target.position(), target.respawnInThisDimension(), safeRadius);
        BlockPos origin = target.blockPosition();
        for (int x = -10; x <= 10; x++) {
            for (int y = -10; y <= 10; y++) {
                for (int z = -10; z <= 10; z++) {
                    // Quirk kept: truncation toward zero, including duplicate positions near zero.
                    BlockPos pos = new BlockPos((int) (center.x + x), (int) (center.y + y), (int) (center.z + z));
                    // Original strict sphere is centered on the unshifted player block position.
                    if (!pos.closerThan(origin, 10) || !target.available(pos) || target.isAir(pos)) continue;
                    if (random.getAsDouble() < 1.0 - pos.distSqr(origin) / 100.0) target.replace(pos);
                }
            }
        }
        return true; // An empty or all-miss pass still consumes a one-time event.
    }

    static String[] signLines(IntUnaryOperator randomBelow) {
        String[] result = new String[4];
        for (int i = 0; i < result.length; i++) {
            result[i] = "events.splitself.sign." + SIGN_KEYS.get(randomBelow.applyAsInt(SIGN_KEYS.size()));
        }
        return result;
    }

    /** Quirk kept: scan all inventory slots in order, a separate 1/5 roll per nonempty slot.
     * The first success removes the entire stack, not one item or one uniformly chosen slot. */
    static int removalSlot(int size, IntPredicate nonempty, IntUnaryOperator randomBelow) {
        for (int i = 0; i < size; i++) {
            if (nonempty.test(i) && randomBelow.applyAsInt(5) == 0) return i;
        }
        return -1;
    }

    static void doorCycles(Runnable toggle, BiConsumer<Long, Runnable> schedule) {
        doorCycle(0, toggle, schedule);
    }

    private static void doorCycle(int cycle, Runnable toggle, BiConsumer<Long, Runnable> schedule) {
        toggle.run();
        if (cycle + 1 < DOOR_CYCLES) {
            schedule.accept(DOOR_INTERVAL_TICKS, () -> doorCycle(cycle + 1, toggle, schedule));
        }
    }
}
