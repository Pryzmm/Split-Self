package com.pryzmm.splitself.forge;

import java.util.*;
import java.util.function.DoubleSupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import com.electronwill.nightconfig.core.CommentedConfig;

/** Calls the production policy with fake world access; does not simulate fluid ticks. */
public final class LavaProbe {
    static int assertions;
    static void check(boolean ok, String message) {
        assertions++;
        if (!ok) throw new AssertionError(message);
    }
    static final class Rolls implements DoubleSupplier {
        int calls;
        double value;
        Rolls(double value) { this.value = value; }
        public double getAsDouble() { calls++; return value; }
    }
    static final class Target implements WorldManifestationRules.LavaTarget {
        Vec3 position = new Vec3(25.9, 70, -12.9);
        BlockPos bed, checked;
        boolean loaded = true;
        int minY = -64, maxY = 320;
        List<BlockPos> writes = new ArrayList<>();
        public Vec3 position() { return position; }
        public BlockPos respawnInThisDimension() { return bed; }
        public boolean available(BlockPos pos) {
            checked = pos;
            return loaded && pos.getY() >= minY && pos.getY() < maxY;
        }
        public void placeLava(BlockPos pos) { writes.add(pos); }
    }
    static boolean run(Target target, double radius) {
        Rolls rolls = new Rolls(0);
        boolean result = WorldManifestationRules.lava(List.of(target), t -> t, radius, rolls);
        check(rolls.calls == 1, "exactly one random draw");
        return result;
    }
    public static void main(String[] args) {
        Rolls empty = new Rolls(0);
        check(!WorldManifestationRules.lava(List.<Target>of(), t -> {throw new AssertionError();}, 50, empty)
                && empty.calls == 0, "empty list safely refuses without random");
        Target overworld = new Target(), nether = new Target();
        for (double roll : new double[]{0, 0.499999, 0.5, 0.999999}) {
            List<Target> selected = new ArrayList<>();
            Rolls random = new Rolls(roll);
            check(WorldManifestationRules.lava(List.of(overworld, nether), t -> {selected.add(t); return t;}, 0, random),
                    "selection completes");
            check(selected.equals(List.of(roll < 0.5 ? overworld : nether)) && random.calls == 1,
                    "uniform full list selects only that world's access");
        }
        Target ordinary = new Target();
        check(run(ordinary, 50) && ordinary.writes.equals(List.of(new BlockPos(25,250,-12))),
                "no bed: Y250 and truncation toward zero");
        Target shifted = new Target(); shifted.position = new Vec3(3.9,70,4.9); shifted.bed = new BlockPos(0,64,0);
        check(run(shifted, 9) && shifted.writes.equals(List.of(new BlockPos(6,3,8))),
                "block coordinates before normalization, radius+1, original X becomes Y");
        BlockPos input = new BlockPos(3,250,4), bed = new BlockPos(0,0,0);
        double boundary = Math.sqrt(18.5);
        check(WorldManifestationRules.moveBlockPosFromBase(input, bed, boundary).equals(input),
                "strict radius boundary does not displace");
        check(WorldManifestationRules.moveBlockPosFromBase(input, bed, Math.nextUp(boundary)).getY() == 3,
                "just inside boundary displaces");
        check(WorldManifestationRules.moveBlockPosFromBase(input, null, 50).equals(input),
                "other respawn dimension/no bed represented by null");
        check(WorldManifestationRules.moveBlockPosFromBase(input, bed, 0).equals(input), "zero safe radius");
        Target unloaded = new Target(); unloaded.loaded = false;
        check(run(unloaded, 0) && unloaded.writes.isEmpty() && unloaded.checked.equals(new BlockPos(25,250,-12)),
                "unloaded destination skipped and event completes");
        Target lowCeiling = new Target(); lowCeiling.maxY = 128;
        check(run(lowCeiling, 0) && lowCeiling.writes.isEmpty(), "Y250 outside dimension height skipped");
        for (int x : new int[]{-65,-64,319,320}) {
            Target edge = new Target(); edge.position = new Vec3(x, 70, 0); edge.bed = new BlockPos(x,70,0);
            check(run(edge, 2) && edge.writes.size() == (x >= -64 && x < 320 ? 1 : 0),
                    "shifted Y checks inclusive bottom/exclusive top: " + x);
        }
        Target repeated = new Target();
        check(run(repeated, 0) && run(repeated, 0) && repeated.writes.size() == 2,
                "no air or container filter; existing lava still attempts replacement");
        CommentedConfig config = CommentedConfig.inMemory();
        SplitSelfConfig.SPEC.correct(config); SplitSelfConfig.SPEC.setConfig(config);
        var option = EventCatalog.options().get("LAVA");
        check(option.weight() == 10 && option.stage() == 1 && !option.oneTime() && !option.client(),
                "GLOBAL weight10 stage1 repeatable defaults");
        check(PortedEvents.supported().contains("LAVA"), "LAVA remains supported as pool grows");
        check(!EventCatalog.eligible(0, Set.of(), Map.of(), 0).contains("LAVA"), "stage0 excludes");
        check(EventCatalog.eligible(1, Set.of(), Map.of(), 0).contains("LAVA"), "stage1 includes");
        check(!EventCatalog.eligible(1, Set.of(), Map.of("LAVA", 0), 0).contains("LAVA"), "repeat cooldown applies");
        check(EventCatalog.eligible(1, Set.of(), Map.of("LAVA", 0), EventCatalog.repeatAfter()).contains("LAVA"),
                "eligible after cooldown");
        config.set(List.of("eventWeights", "LAVA"), 0);
        SplitSelfConfig.SPEC.afterReload();
        check(!EventCatalog.eligible(1, Set.of(), Map.of(), 0).contains("LAVA"), "weight0 disables");
        System.out.println("PASS LavaProbe: " + assertions + " assertions; production policy and catalog/config.");
        System.out.println("No fluid ticks, block entity lifecycle or client synchronization evidence.");
    }
}
