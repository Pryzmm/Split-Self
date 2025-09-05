package com.pryzmm.splitself.events;

import com.pryzmm.splitself.SplitSelf;
import com.pryzmm.splitself.entity.client.TheForgottenSpawner;
import com.pryzmm.splitself.world.IntegrityProcessor;
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
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;

public class StructureManager {

    public static void saveStructure(ServerWorld world, BlockPos startPos, BlockPos endPos, String fileName) {
        try {
            Path structuresPath = getStructuresDirectory(world);
            Files.createDirectories(structuresPath);
            BlockPos size = endPos.subtract(startPos).add(1, 1, 1);
            StructureTemplate template = new StructureTemplate();
            template.saveFromWorld(world, startPos, size, true, null);
            NbtCompound nbt = template.writeNbt(new NbtCompound());
            Path filePath = structuresPath.resolve(fileName + ".nbt");
            try (FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
                NbtIo.writeCompressed(nbt, fos);
            }
        } catch (Exception e) {
            SplitSelf.LOGGER.error("Error saving structure {}: {}", fileName, e.getMessage());
        }
    }

    public static Optional<StructureTemplate> loadStructure(ServerWorld world, String fileName) {
        try {
            Path structuresPath = getStructuresDirectory(world);
            Path filePath = structuresPath.resolve(fileName + ".nbt");
            if (Files.exists(filePath)) {
                try (FileInputStream fis = new FileInputStream(filePath.toFile())) {
                    NbtCompound nbt = NbtIo.readCompressed(fis, NbtSizeTracker.ofUnlimitedBytes());
                    StructureTemplate template = new StructureTemplate();
                    template.readNbt(world.getRegistryManager().getWrapperOrThrow(RegistryKeys.BLOCK), nbt);
                    return Optional.of(template);
                }
            }
            return loadStructureFromResource(world, fileName);
        } catch (Exception e) {
            SplitSelf.LOGGER.error("Error loading structure {}: {}", fileName, e.getMessage());
            return Optional.empty();
        }
    }

    private static Optional<StructureTemplate> loadStructureFromResource(ServerWorld world, String fileName) {
        try {
            String resourcePath = "/data/" + SplitSelf.MOD_ID + "/structures/" + fileName + ".nbt";
            InputStream inputStream = StructureManager.class.getResourceAsStream(resourcePath);
            if (inputStream == null) {
                return Optional.empty();
            }
            NbtCompound nbtCompound = NbtIo.readCompressed(inputStream, NbtSizeTracker.ofUnlimitedBytes());
            inputStream.close();
            StructureTemplate template = new StructureTemplate();
            template.readNbt(world.getRegistryManager().getWrapperOrThrow(RegistryKeys.BLOCK), nbtCompound);
            return Optional.of(template);
        } catch (Exception e) {
            SplitSelf.LOGGER.error("Error loading structure from resource {}: {}", fileName, e.getMessage());
            return Optional.empty();
        }
    }

    public static boolean placeStructure(ServerWorld world, BlockPos pos, String fileName, BlockRotation rotation, BlockMirror mirror, Float Integrity, boolean ignoreEntities) {
        Optional<StructureTemplate> templateOpt = loadStructure(world, fileName);
        if (templateOpt.isEmpty()) {
            SplitSelf.LOGGER.warn("Could not load structure: {}", fileName);
            return false;
        }
        try {
            placeTemplate(world, pos, templateOpt.get(), rotation, mirror, Integrity, ignoreEntities);
            return true;
        } catch (Exception e) {
            SplitSelf.LOGGER.error("Error placing structure {}: {}", fileName, e.getMessage());
            return false;
        }
    }

    public static void placeStructureRandomRotation(ServerWorld world, BlockPos pos, String structureName, Integer MinimumRange, Integer MaximumRange, boolean DisableRotation, Float Integrity, boolean ignoreEntities) {
        try {
            Random random = new Random();
            double distance = MinimumRange + random.nextDouble() * (MaximumRange - MinimumRange);
            double angle = random.nextDouble() * 2 * Math.PI;
            double spawnX = pos.getX() + Math.cos(angle) * distance;
            double spawnY = pos.getY() + Math.cos(angle) * distance;
            double spawnZ = pos.getZ() + Math.sin(angle) * distance;
            BlockPos spawnPos = new BlockPos((int) spawnX, (int) spawnY, (int) spawnZ);
            BlockRotation rotation;
            if (!DisableRotation) {
                rotation = BlockRotation.values()[world.getRandom().nextInt(4)];
            } else {
                rotation = BlockRotation.NONE;
            }
            var templateManager = world.getStructureTemplateManager();
            Identifier structureId = Identifier.of(SplitSelf.MOD_ID, structureName);
            var template = templateManager.getTemplate(structureId);
            if (template.isPresent()) {
                placeTemplate(world, spawnPos, template.get(), rotation, BlockMirror.NONE, Integrity, ignoreEntities);
                return;
            }
            placeStructure(world, spawnPos, structureName, rotation, BlockMirror.NONE, Integrity, ignoreEntities);
        } catch (Exception e) {
            SplitSelf.LOGGER.info("Error placing structure with rotation: {}", String.valueOf(e));
        }
    }

    public static BlockPos placeStructureRandomRotation(ServerWorld world, PlayerEntity Player, String structureName, Integer MinimumRange, Integer MaximumRange, Integer YOffset, boolean DisableRotation, Float Integrity, boolean ignoreEntities) {
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
            if (structureName.equals("house")) { // The forgotten
                BlockPos[] newPositions;
                int arrayLength;
                try {
                    newPositions = new BlockPos[TheForgottenSpawner.spawnPositions.length + 1];
                    arrayLength = TheForgottenSpawner.spawnPositions.length;
                    System.arraycopy(TheForgottenSpawner.spawnPositions, 0, newPositions, 0, arrayLength);
                } catch (Exception e) {
                    newPositions = new BlockPos[1];
                    arrayLength = 0;
                }
                newPositions[arrayLength] = finalSpawnPos;
                TheForgottenSpawner.spawnPositions = newPositions;
            }
            BlockRotation rotation;
            if (!DisableRotation) {
                rotation = BlockRotation.values()[world.getRandom().nextInt(4)];
            } else {
                rotation = BlockRotation.NONE;
            }
            var templateManager = world.getStructureTemplateManager();
            Identifier structureId = Identifier.of(SplitSelf.MOD_ID, structureName);
            var template = templateManager.getTemplate(structureId);

            if (template.isPresent()) {
                placeTemplate(world, finalSpawnPos, template.get(), rotation, BlockMirror.NONE, Integrity, ignoreEntities);
                return finalSpawnPos;
            }
            if (placeStructure(world, finalSpawnPos, structureName, rotation, BlockMirror.NONE, Integrity, ignoreEntities)) {
                return finalSpawnPos;
            }
        } catch (Exception e) {
            SplitSelf.LOGGER.info("Error placing structure: {}", String.valueOf(e));
        }
        return null;
    }

    private static void placeTemplate(ServerWorld world, BlockPos pos, StructureTemplate template, BlockRotation rotation, BlockMirror mirror, Float Integrity, boolean ignoreEntities) {
        StructurePlacementData placementData = new StructurePlacementData()
                .setRotation(rotation)
                .setMirror(mirror)
                .setIgnoreEntities(ignoreEntities)
                .addProcessor(BlockIgnoreStructureProcessor.IGNORE_AIR_AND_STRUCTURE_BLOCKS);
        if (Integrity != null && Integrity < 1.0f) {
            placementData.addProcessor(new IntegrityProcessor(Integrity));
        }
        template.place(world, pos, pos, placementData, world.getRandom(), 2);
    }

    private static Path getStructuresDirectory(ServerWorld world) {
        Path worldDir = world.getServer().getSavePath(net.minecraft.util.WorldSavePath.ROOT);
        return worldDir.resolve("generated/minecraft/structures");
    }
}