package com.pryzmm.splitself.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.biome.source.FixedBiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.Blender;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.VerticalBlockSample;
import net.minecraft.world.gen.noise.NoiseConfig;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class BuildOnlyChunkGenerator extends ChunkGenerator {
    public static final MapCodec<BuildOnlyChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.STRING.fieldOf("structure_name").forGetter(generator -> generator.structureName),
                    Codec.INT.fieldOf("structure_x").forGetter(generator -> generator.structureX),
                    Codec.INT.fieldOf("structure_z").forGetter(generator -> generator.structureZ),
                    Biome.REGISTRY_CODEC.fieldOf("biome").forGetter(generator -> generator.biome)
            ).apply(instance, BuildOnlyChunkGenerator::new)
    );

    private final String structureName;
    private final int structureX;
    private final int structureZ;
    private final RegistryEntry<Biome> biome;

    public BuildOnlyChunkGenerator(String structureName, int structureX, int structureZ, RegistryEntry<Biome> biome) {
        super(new FixedBiomeSource(biome));
        this.structureName = structureName;
        this.structureX = structureX;
        this.structureZ = structureZ;
        this.biome = biome;
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> getCodec() {
        return CODEC;
    }

    @Override
    public void carve(ChunkRegion chunkRegion, long seed, NoiseConfig noiseConfig, BiomeAccess biomeAccess, StructureAccessor structureAccessor, Chunk chunk, GenerationStep.Carver carverStep) {
        // No carving needed for empty dimension
    }

    @Override
    public void buildSurface(ChunkRegion region, StructureAccessor structures, NoiseConfig noiseConfig, Chunk chunk) {
        // Keep everything as air - no surface generation
    }

    @Override
    public void populateEntities(ChunkRegion region) {
        // Structure placement is handled separately
    }

    @Override
    public CompletableFuture<Chunk> populateNoise(Blender blender, NoiseConfig noiseConfig, StructureAccessor structureAccessor, Chunk chunk) {
        // Fill chunk with air blocks
        ChunkPos chunkPos = chunk.getPos();
        int minY = chunk.getBottomY();
        int maxY = chunk.getTopY();

        // Set all blocks to air
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = minY; y < maxY; y++) {
                    chunk.setBlockState(new BlockPos(x, y, z), Blocks.AIR.getDefaultState(), false);
                }
            }
        }

        return CompletableFuture.completedFuture(chunk);
    }

    @Override
    public int getSeaLevel() {
        return 0;
    }

    @Override
    public int getMinimumY() {
        return -64;
    }

    @Override
    public int getWorldHeight() {
        return 384;
    }

    @Override
    public int getHeight(int x, int z, Heightmap.Type heightmapType, HeightLimitView world, NoiseConfig noiseConfig) {
        return getMinimumY();
    }

    @Override
    public VerticalBlockSample getColumnSample(int x, int z, HeightLimitView world, NoiseConfig noiseConfig) {
        BlockState[] states = new BlockState[world.getHeight()];
        // Fill with air
        for (int i = 0; i < states.length; i++) {
            states[i] = Blocks.AIR.getDefaultState();
        }
        return new VerticalBlockSample(world.getBottomY(), states);
    }

    @Override
    public void getDebugHudText(List<String> text, NoiseConfig noiseConfig, BlockPos pos) {
        text.add("BuildOnly Generator");
        text.add("Structure: " + structureName);
        text.add("At: " + structureX + ", " + structureZ);
    }

    // Getters for the codec
    public String getStructureName() {
        return structureName;
    }

    public int getStructureX() {
        return structureX;
    }

    public int getStructureZ() {
        return structureZ;
    }

    public RegistryEntry<Biome> getBiome() {
        return biome;
    }
}