package com.pryzmm.splitself.events;

import com.pryzmm.splitself.SplitSelf;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtSizeTracker;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.processor.BlockIgnoreStructureProcessor;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.io.InputStream;

public class StructureManager {

    // Load structure with random rotation
    public static boolean placeStructureRandomRotation(ServerWorld world, BlockPos pos, String structureName) {
        try {
            BlockRotation rotation = BlockRotation.values()[world.getRandom().nextInt(4)];

            // First try the normal template manager approach
            var templateManager = world.getStructureTemplateManager();
            Identifier structureId = Identifier.of(SplitSelf.MOD_ID, structureName);
            var template = templateManager.getTemplate(structureId);

            if (template.isPresent()) {
                return placeTemplate(world, pos, template.get(), rotation);
            }

            // If template manager fails, load directly from resources
            return loadAndPlaceFromResource(world, pos, structureName, rotation);

        } catch (Exception e) {
            System.out.println("Error placing structure with rotation: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Helper method to place a template
    private static boolean placeTemplate(ServerWorld world, BlockPos pos, StructureTemplate template, BlockRotation rotation) {
        StructurePlacementData placementData = new StructurePlacementData()
                .setRotation(rotation)
                .setMirror(BlockMirror.NONE)
                .setIgnoreEntities(false)
                .addProcessor(BlockIgnoreStructureProcessor.IGNORE_STRUCTURE_BLOCKS)
                .addProcessor(BlockIgnoreStructureProcessor.IGNORE_AIR);

        boolean result = template.place(world, pos, pos, placementData, world.getRandom(), 2);
        return result;
    }

    // Load structure directly from resource file
    private static boolean loadAndPlaceFromResource(ServerWorld world, BlockPos pos, String structureName, BlockRotation rotation) {
        try {
            String resourcePath = "/data/" + SplitSelf.MOD_ID + "/structures/" + structureName + ".nbt";
            InputStream inputStream = StructureManager.class.getResourceAsStream(resourcePath);

            if (inputStream == null) {
                return false;
            }

            // Read NBT data
            NbtCompound nbtCompound = NbtIo.readCompressed(inputStream, NbtSizeTracker.ofUnlimitedBytes());
            inputStream.close();

            // Create structure template from NBT
            StructureTemplate template = new StructureTemplate();
            template.readNbt(world.getRegistryManager().getWrapperOrThrow(RegistryKeys.BLOCK), nbtCompound);

            // Place the structure
            return placeTemplate(world, pos, template, rotation);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}