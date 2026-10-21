package com.pryzmm.splitself.forge;

import java.util.*;
import java.util.function.DoubleSupplier;
import java.nio.file.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import com.electronwill.nightconfig.core.CommentedConfig;

/** Deterministic production traversal; fake blocks are not a Minecraft world. */
public final class CoralProbe {
    static int assertions;
    static void check(boolean value, String message) {
        assertions++;
        if (!value) throw new AssertionError(message);
    }
    static final class Rolls implements DoubleSupplier {
        int count;
        double value;
        Rolls(double value) { this.value = value; }
        public double getAsDouble() { count++; return value; }
    }
    static final class Target implements WorldManifestationRules.CoralTarget {
        Vec3 position = new Vec3(30.5, 70.5, 30.5);
        BlockPos bed;
        String dimension;
        int minY = -64, maxY = 320;
        boolean loaded = true;
        boolean solid;
        Map<BlockPos, String> blocks = new HashMap<>();
        Map<BlockPos, List<String>> contents = new HashMap<>();
        List<BlockPos> visits = new ArrayList<>(), reads = new ArrayList<>(), writes = new ArrayList<>();
        Target(String dimension) { this.dimension = dimension; }
        public Vec3 position() { return position; }
        public BlockPos blockPosition() { return BlockPos.containing(position); }
        public BlockPos respawnInThisDimension() { return bed; }
        public boolean available(BlockPos pos) {
            visits.add(pos);
            return loaded && pos.getY() >= minY && pos.getY() < maxY;
        }
        public boolean isAir(BlockPos pos) {
            reads.add(pos);
            return !solid && !blocks.containsKey(pos);
        }
        public void replace(BlockPos pos) {
            writes.add(pos); blocks.put(pos, "dead_coral"); contents.remove(pos);
        }
    }
    static void run(Target target, Rolls rolls) {
        check(WorldManifestationRules.coral(List.of(target), t -> t, 0, rolls), "completed pass");
    }
    public static void main(String[] args) throws Exception {
        Rolls none = new Rolls(0);
        check(!WorldManifestationRules.coral(List.<Target>of(), t -> {throw new AssertionError();}, 50, none)
                && none.count == 0, "empty list refuses without a random draw");
        Target overworld = new Target("overworld"), nether = new Target("nether");
        List<Target> players = List.of(overworld, nether);
        for (double selection : new double[]{0, 0.499999, 0.5, 0.999999}) {
            List<Target> chosen = new ArrayList<>();
            Rolls rolls = new Rolls(selection);
            check(WorldManifestationRules.coral(players, t -> {chosen.add(t); return t;}, 0, rolls), "selected pass");
            check(chosen.equals(List.of(selection < 0.5 ? overworld : nether)) && rolls.count == 1,
                    "full list uniform selection, one draw, selected dimension");
        }
        check(nether.reads.size() > 0 && nether.dimension.equals("nether"), "selected access used");

        Target all = new Target("overworld"); all.solid = true;
        Rolls zero = new Rolls(0); run(all, zero);
        // Integer grid oracle independent of the production radius loops.
        List<BlockPos> expected = new ArrayList<>();
        for (int x=21;x<=39;x++) for (int y=61;y<=79;y++) for (int z=21;z<=39;z++) {
            int dx=x-30,dy=y-70,dz=z-30;
            if (dx*dx+dy*dy+dz*dz < 100) expected.add(new BlockPos(x,y,z));
        }
        check(all.reads.equals(expected) && all.writes.equals(expected), "X/Y/Z order and strict sphere");
        check(expected.size() == 4139 && zero.count == 4140, "one selection plus one roll per non-air visit");
        check(!all.reads.contains(new BlockPos(40,70,30)), "exact radius 10 excluded before block read");
        check(all.reads.contains(new BlockPos(39,74,31)), "squared distance 98 included");

        Target negative = new Target("overworld"); negative.position = new Vec3(-0.5, 70.5, -0.5); negative.solid = true;
        Rolls negRolls = new Rolls(0); run(negative, negRolls);
        check(negative.reads.get(0).equals(new BlockPos(-10,66,-2)), "negative truncation and first eligible offset");
        check(Collections.frequency(negative.reads, new BlockPos(0,70,0)) == 4,
                "duplicates at zero preserved in two axes");
        check(negRolls.count == negative.reads.size()+1 && negative.writes.equals(negative.reads), "duplicates each roll and replace");

        Target mixed = new Target("overworld");
        BlockPos center = mixed.blockPosition(), stone = center.offset(1,0,0), chest = center.offset(2,0,0);
        BlockPos coral = center.offset(3,0,0), bedrock = center.offset(4,0,0);
        mixed.blocks.put(stone,"stone"); mixed.blocks.put(chest,"chest"); mixed.contents.put(chest,List.of("diamond","book"));
        mixed.blocks.put(coral,"dead_coral"); mixed.blocks.put(bedrock,"bedrock");
        Rolls mixedRolls = new Rolls(0); run(mixed,mixedRolls);
        check(new HashSet<>(mixed.writes).equals(Set.of(stone,chest,coral,bedrock)), "stone/container/existing coral/bedrock eligible");
        check(!mixed.contents.containsKey(chest) && mixed.blocks.get(chest).equals("dead_coral"), "fake container replaced with contents removed");
        check(mixedRolls.count==5 && !mixed.writes.contains(center), "air never rolls or replaces");
        Target threshold = new Target("overworld"); threshold.blocks.put(threshold.blockPosition().offset(5,0,0),"stone");
        Rolls equal = new Rolls(0.75); run(threshold,equal);
        check(threshold.writes.isEmpty() && equal.count==2, "strict random < 0.75 rejects equality");
        Rolls below = new Rolls(Math.nextDown(0.75)); run(threshold,below);
        check(threshold.writes.size()==1 && below.count==2, "dither probability 1 - 25/100");

        Target unloaded = new Target("overworld"); unloaded.loaded=false; unloaded.solid=true;
        Rolls unloadRolls = new Rolls(0); run(unloaded,unloadRolls);
        check(unloaded.reads.isEmpty() && unloaded.writes.isEmpty() && unloadRolls.count==1, "unloaded chunks never read or roll");
        for (int height : new int[]{-64,319}) {
            Target edge = new Target("overworld"); edge.position = new Vec3(30.5,height+0.5,30.5); edge.solid=true;
            Rolls edgeRolls = new Rolls(0); run(edge,edgeRolls);
            check(edge.reads.stream().allMatch(p -> p.getY()>=-64 && p.getY()<320), "world height bounds before reads");
            check(edge.writes.size()>0 && edgeRolls.count==edge.reads.size()+1, "height boundary still completes valid half sphere");
        }
        Vec3 start = new Vec3(0.5,70.5,0.5); BlockPos bed = new BlockPos(0,70,0);
        check(WorldManifestationRules.moveVectorFromBase(start,null,50).equals(start), "no bed / other respawn dimension unchanged");
        check(WorldManifestationRules.moveVectorFromBase(start,bed,50).equals(new Vec3(51.5,70.5,0.5)), "bed center fallback +X");
        check(WorldManifestationRules.moveVectorFromBase(new Vec3(3.5,70.5,4.5),bed,9).equals(new Vec3(6.5,70.5,8.5)), "normalized horizontal displacement and unchanged Y");
        Vec3 boundary = new Vec3(50.5,70.5,0.5);
        check(WorldManifestationRules.moveVectorFromBase(boundary,bed,50).equals(boundary), "safe radius strict boundary");
        Target shifted = new Target("overworld"); shifted.position=start; shifted.bed=bed; shifted.solid=true;
        Rolls shiftRolls = new Rolls(0);
        check(WorldManifestationRules.coral(List.of(shifted),t->t,50,shiftRolls), "shifted empty pass completes");
        check(shifted.visits.isEmpty() && shifted.writes.isEmpty() && shiftRolls.count==1, "distance from original player preserves empty result");

        // Real existing catalog/config filter: stage, weight and consumed state.
        CommentedConfig config = CommentedConfig.inMemory();
        SplitSelfConfig.SPEC.correct(config); SplitSelfConfig.SPEC.setConfig(config);
        check(PortedEvents.supported().contains("CORAL"), "CORAL remains supported as pool grows");
        var option=EventCatalog.options().get("CORAL");
        check(option.weight()==10 && option.stage()==2 && option.oneTime() && !option.client(), "unchanged defaults GLOBAL/10/2/oneTime");
        check(!EventCatalog.eligible(1,Set.of(),Map.of(),0).contains("CORAL"), "stage1 excludes");
        check(EventCatalog.eligible(2,Set.of(),Map.of(),0).contains("CORAL"), "stage2 includes");
        check(!EventCatalog.eligible(2,Set.of("CORAL"),Map.of(),0).contains("CORAL"), "consumed one-time excluded");
        Path root=Path.of(args[0]);
        String adapter=Files.readString(root.resolve("src/main/java/com/pryzmm/splitself/forge/WorldManifestations.java"));
        check(adapter.contains("player.serverLevel()") && adapter.contains("player.getRespawnDimension().equals(world.dimension()) ? player.getRespawnPosition() : null"), "adapter dimension and respawn bridge");
        check(adapter.contains("Blocks.DEAD_BRAIN_CORAL_BLOCK.defaultBlockState(), Block.UPDATE_ALL") && adapter.contains("!world.isOutsideBuildHeight(pos) && world.hasChunkAt(pos)"), "adapter target/update flags/availability");
        System.out.println("PASS CoralProbe: "+assertions+" assertions; production traversal, fake block access, catalog/config.");
        System.out.println("No real world, container lifecycle, neighbor update, network or gameplay evidence.");
    }
}
