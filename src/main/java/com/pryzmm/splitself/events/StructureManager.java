package com.pryzmm.splitself.events;

import com.pryzmm.splitself.SplitSelf;
import net.minecraft.entity.player.PlayerEntity;
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
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;

import java.io.InputStream;
import java.util.Random;

public class StructureManager {

    public static BlockPos placeStructureRandomRotation(ServerWorld world, PlayerEntity Player, String structureName, Integer MinimumRange, Integer MaximumRange, Integer YOffset, boolean DisableRotation) {
        try {

            Random random = new Random();
            double distance = MinimumRange + random.nextDouble() * (MaximumRange - MinimumRange);
            double angle = random.nextDouble() * 2 * Math.PI;
            Vec3d playerPos = Player.getPos();
            double spawnX = playerPos.x + Math.cos(angle) * distance;
            double spawnZ = playerPos.z + Math.sin(angle) * distance;
            BlockPos spawnPos = new BlockPos((int) spawnX, 0, (int) spawnZ);
            int surfaceY = Player.getWorld().getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, spawnPos.getX(), spawnPos.getZ()) + YOffset;
            BlockPos finalSpawnPos = new BlockPos((int) spawnX, surfaceY, (int) spawnZ);

            BlockRotation rotation;
            if (!DisableRotation) {rotation = BlockRotation.values()[world.getRandom().nextInt(4)];} else {rotation = BlockRotation.NONE;}

            // First try the normal template manager approach
            var templateManager = world.getStructureTemplateManager();
            Identifier structureId = Identifier.of(SplitSelf.MOD_ID, structureName);
            var template = templateManager.getTemplate(structureId);

            if (template.isPresent()) {
                placeTemplate(world, finalSpawnPos, template.get(), rotation);
                return finalSpawnPos;
            }

            // If template manager fails, load directly from resources
            loadAndPlaceFromResource(world, finalSpawnPos, structureName, rotation);
            return finalSpawnPos;

        } catch (Exception e) {
            SplitSelf.LOGGER.info("Error placing structure with rotation: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    private static void placeTemplate(ServerWorld world, BlockPos pos, StructureTemplate template, BlockRotation rotation) {
        StructurePlacementData placementData = new StructurePlacementData()
                .setRotation(rotation)
                .setMirror(BlockMirror.NONE)
                .setIgnoreEntities(false)
                .addProcessor(BlockIgnoreStructureProcessor.IGNORE_AIR_AND_STRUCTURE_BLOCKS);

        template.place(world, pos, pos, placementData, world.getRandom(), 2);
    }

    private static void loadAndPlaceFromResource(ServerWorld world, BlockPos pos, String structureName, BlockRotation rotation) {
        try {
            String resourcePath = "/data/" + SplitSelf.MOD_ID + "/structures/" + structureName + ".nbt";
            InputStream inputStream = StructureManager.class.getResourceAsStream(resourcePath);

            if (inputStream == null) {
                return;
            }

            NbtCompound nbtCompound = NbtIo.readCompressed(inputStream, NbtSizeTracker.ofUnlimitedBytes());
            inputStream.close();

            StructureTemplate template = new StructureTemplate();
            template.readNbt(world.getRegistryManager().getWrapperOrThrow(RegistryKeys.BLOCK), nbtCompound);

            placeTemplate(world, pos, template, rotation);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}