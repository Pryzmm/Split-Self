package com.pryzmm.splitself.forge;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;

/** Original void codec and empty terrain; currently no dimension references it. */
public final class VoidChunkGenerator extends ChunkGenerator {
    public static final Codec<VoidChunkGenerator> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("structure_name").forGetter(g -> g.structureName),
            Codec.INT.fieldOf("structure_x").forGetter(g -> g.structureX),
            Codec.INT.fieldOf("structure_z").forGetter(g -> g.structureZ),
            Biome.CODEC.fieldOf("biome").forGetter(g -> g.biome)
    ).apply(instance, VoidChunkGenerator::new));

    private final String structureName;
    private final int structureX;
    private final int structureZ;
    private final Holder<Biome> biome;

    public VoidChunkGenerator(String structureName, int structureX, int structureZ, Holder<Biome> biome) {
        super(new FixedBiomeSource(biome));
        this.structureName = structureName;
        this.structureX = structureX;
        this.structureZ = structureZ;
        this.biome = biome;
    }

    @Override protected Codec<? extends ChunkGenerator> codec() { return CODEC; }
    @Override public void applyCarvers(WorldGenRegion region, long seed, RandomState random,
            BiomeManager biomes, StructureManager structures, ChunkAccess chunk, GenerationStep.Carving step) {}
    @Override public void buildSurface(WorldGenRegion region, StructureManager structures,
            RandomState random, ChunkAccess chunk) {}
    @Override public void spawnOriginalMobs(WorldGenRegion region) {}
    @Override public CompletableFuture<ChunkAccess> fillFromNoise(Executor executor, Blender blender,
            RandomState random, StructureManager structures, ChunkAccess chunk) {
        return CompletableFuture.completedFuture(chunk);
    }
    @Override public int getSeaLevel() { return 0; }
    @Override public int getMinY() { return -64; }
    @Override public int getGenDepth() { return 384; }
    @Override public int getBaseHeight(int x, int z, Heightmap.Types type,
            LevelHeightAccessor world, RandomState random) { return -64; }
    @Override public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor world, RandomState random) {
        BlockState[] states = new BlockState[world.getHeight()];
        Arrays.fill(states, Blocks.AIR.defaultBlockState());
        return new NoiseColumn(world.getMinBuildHeight(), states);
    }
    @Override public void addDebugScreenInfo(List<String> text, RandomState random, BlockPos pos) {
        text.add("Void Generator");
        text.add("Structure: " + structureName);
        text.add("At: " + structureX + ", " + structureZ);
    }
}
