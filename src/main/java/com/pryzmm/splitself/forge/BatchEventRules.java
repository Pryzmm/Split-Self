package com.pryzmm.splitself.forge;

import java.util.Set;
import java.util.function.IntConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

/** Policies from pinned EventRunner, ChunkDestroyer, UndergroundMining and PauseMixin. */
final class BatchEventRules {
    static final Set<String> CLIENT = Set.of("ESCAPE", "FREEZE", "FROZENSCREEN");
    static final Set<String> WORLD = Set.of("UNDERGROUNDMINING", "DESTROYCHUNK", "DEADCHUNK", "LIFT");
    static final Set<String> EXTERNAL = Set.of("NAME", "RECORD", "EJECT");
    static Set<String> all() {
        var names = new java.util.HashSet<>(CLIENT); names.addAll(WORLD); names.addAll(EXTERNAL);
        return Set.copyOf(names);
    }
    static final long FROZEN_MS = 1000, FREEZE_MS = 2000;
    private BatchEventRules() {}
    static final class PauseGate {
        private boolean enabled;
        private int attempts;
        void enable() { enabled = true; } // Repeated ESCAPE does not reset accumulated attempts.
        boolean cancel(boolean transientEffect) { return transientEffect || cancel(); }
        boolean cancel() {
            if (!enabled) return false;
            if (++attempts < 10) return true;
            enabled = false; attempts = 0; return false;
        }
    }
    static BlockPos destroyCenter(Vec3 position, int rollX, int rollZ, BlockPos bed, double radius) {
        var moved = WorldManifestationRules.moveVectorFromBase(
                new Vec3(position.x + rollX - 20, 319, position.z + rollZ - 20), bed, radius);
        return new BlockPos((int) moved.x, (int) moved.y, (int) moved.z);
    }
    static BlockPos deadOrigin(Vec3 shifted, double angleRoll, double distanceRoll) {
        double angle = angleRoll * 2 * Math.PI, distance = 20 + distanceRoll * 30;
        int x = (int) (shifted.x + Math.cos(angle) * distance);
        int z = (int) (shifted.z + Math.sin(angle) * distance);
        return new BlockPos(Math.floorDiv(x,16)*16,0,Math.floorDiv(z,16)*16);
    }
    static BlockPos liftCenter(Vec3 shifted, double angleRoll, double xRoll, double zRoll) {
        double angle = angleRoll * 2 * Math.PI;
        // Kept: independent radial draws for X and Z, Math.round rather than truncation.
        return new BlockPos(Math.toIntExact(Math.round(shifted.x + Math.cos(angle)*(20+xRoll*20))),0,
                Math.toIntExact(Math.round(shifted.z + Math.sin(angle)*(20+zRoll*20))));
    }
    static float miningVolume(int step) { return (step + 1) * .1f; }
    static float frozenTint(double roll) { return (float) (roll / 5); }
    static BlockPos liftMinimum(BlockPos center,int surface) { return new BlockPos(center.getX()-7,surface-14,center.getZ()-7); }
    static BlockPos liftMaximum(BlockPos center,int surface) { return new BlockPos(center.getX()+7,surface+7,center.getZ()+7); }
    static BlockPos liftDestination(BlockPos center,int surface) { return new BlockPos(center.getX()-7,surface+40,center.getZ()-7); }
    static boolean replace(boolean air,boolean solid,boolean coral) { return !air && (!coral || solid); }
    static void visitLayer(BlockPos origin,int y,java.util.function.Consumer<BlockPos> visit) {
        for(int x=0;x<16;x++)for(int z=0;z<16;z++)visit.accept(new BlockPos(origin.getX()+x,y,origin.getZ()+z));
    }
    static final class Layers {
        private int y;
        private final int end, direction;
        private final long interval;
        private long next;
        Layers(int first, int end, int direction, long interval, long now) {
            y=first; this.end=end; this.direction=direction; this.interval=interval; next=now;
        }
        boolean finished() { return direction > 0 ? y > end : y < end; }
        int advance(long now, IntConsumer layer) {
            int count=0;
            // Bound queued work under lag. No layers are skipped or reordered.
            while (!finished() && now >= next && count < 16) {
                layer.accept(y); y+=direction; next+=interval; count++;
            }
            return count;
        }
    }
}
