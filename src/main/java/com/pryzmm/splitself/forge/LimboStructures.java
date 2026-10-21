package com.pryzmm.splitself.forge;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.block.Rotation;

/** Installs the six authored Limbo builds once per world, as in SplitSelf's server-start hook. */
final class LimboStructures {
    private static final Map<String, BlockPos> INITIAL_BUILDS = new LinkedHashMap<>();
    static {
        INITIAL_BUILDS.put("house_empty", new BlockPos(0, 0, 0));
        INITIAL_BUILDS.put("memory", new BlockPos(1000, 0, 0));
        INITIAL_BUILDS.put("broken_memory", new BlockPos(2000, 0, 0));
        INITIAL_BUILDS.put("meadow", new BlockPos(3000, 0, 0));
        INITIAL_BUILDS.put("real_house", new BlockPos(4000, 0, 0));
        INITIAL_BUILDS.put("computer", new BlockPos(4000, 0, -47));
    }

    private LimboStructures() {}

    static void placeForFirstLoad(MinecraftServer server) {
        ServerLevel limbo = server.getLevel(SplitSelfWorldEvents.LIMBO);
        if (limbo == null) return;
        MemoryData data = MemoryData.get(server.overworld());
        if (data.previouslyLoaded()) return;

        Map<String, StructureTemplate> templates = new LinkedHashMap<>();
        for (String name : INITIAL_BUILDS.keySet()) {
            ResourceLocation id = new ResourceLocation(SplitSelfForge.MOD_ID, name);
            StructureTemplate template = limbo.getStructureManager().get(id).orElse(null);
            if (template == null) {
                System.getLogger("splitself").log(System.Logger.Level.ERROR,
                        "Limbo build template is missing: " + id);
                return;
            }
            templates.put(name, template);
        }

        // Set this before placing, so a partially interrupted server start cannot duplicate
        // several 48x48x48 builds on the next launch.
        data.setPreviouslyLoaded(true);
        StructurePlaceSettings placement = new StructurePlaceSettings()
                .setRotation(Rotation.NONE)
                .setIgnoreEntities(false)
                .addProcessor(BlockIgnoreProcessor.STRUCTURE_AND_AIR);
        for (var entry : INITIAL_BUILDS.entrySet()) {
            BlockPos origin = entry.getValue();
            limbo.getChunkAt(origin);
            boolean placed = templates.get(entry.getKey()).placeInWorld(
                    limbo, origin, origin, placement, limbo.getRandom(), 2);
            if (!placed) {
                System.getLogger("splitself").log(System.Logger.Level.ERROR,
                        "Limbo build placement failed: " + entry.getKey());
            }
        }
    }
}
