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

/** Spike terrain copied from pinned GrassEmptyChunkGenerator, adapted to 1.20.1. */
public class GrassEmptyChunkGenerator extends ChunkGenerator {
    public static final Codec<GrassEmptyChunkGenerator> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("structure_name").forGetter(g -> g.structureName),
            Codec.INT.fieldOf("structure_x").forGetter(g -> g.structureX),
            Codec.INT.fieldOf("structure_z").forGetter(g -> g.structureZ),
            Biome.CODEC.fieldOf("biome").forGetter(g -> g.biome)
    ).apply(instance, GrassEmptyChunkGenerator::new));

    protected final String structureName;
    protected final int structureX;
    protected final int structureZ;
    protected final Holder<Biome> biome;

    public GrassEmptyChunkGenerator(String structureName, int structureX, int structureZ, Holder<Biome> biome) {
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

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Executor executor, Blender blender,
            RandomState random, StructureManager structures, ChunkAccess chunk) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        Heightmap ocean = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
        Heightmap surface = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
        int startX = chunk.getPos().getMinBlockX();
        int startZ = chunk.getPos().getMinBlockZ();
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int top = spikeHeight(startX + x, startZ + z);
                for (int y = -64; y <= top; y++) {
                    BlockState state;
                    if (y == -64) state = Blocks.BEDROCK.defaultBlockState();
                    else if (y == top) state = Blocks.GRASS_BLOCK.defaultBlockState();
                    else if (y > top - 8) state = Blocks.DIRT.defaultBlockState();
                    else state = Blocks.STONE.defaultBlockState();
                    chunk.setBlockState(pos.set(startX + x, y, startZ + z), state, false);
                    ocean.update(x, y, z, state);
                    surface.update(x, y, z, state);
                }
            }
        }
        return CompletableFuture.completedFuture(chunk);
    }

    private static double hash(int x, int z) {
        long h = SplitSelfWorldEvents.terrainSeed() ^ ((long) x * 1664525L + 42)
                ^ ((long) z * 1013904223L);
        h ^= h >>> 33;
        h *= 0xff51afd7ed558ccdL;
        h ^= h >>> 33;
        h *= 0xc4ceb9fe1a85ec53L;
        h ^= h >>> 33;
        return (double) (h & Long.MAX_VALUE) / (double) Long.MAX_VALUE;
    }

    protected static double smoothNoise(int x, int z) {
        int ix = (int) Math.floor(x / 12.0), iz = (int) Math.floor(z / 12.0);
        double fx = (x - ix * 12.0) / 12.0, fz = (z - iz * 12.0) / 12.0;
        double ux = fx * fx * (3 - 2 * fx), uz = fz * fz * (3 - 2 * fz);
        double v00 = hash(ix, iz), v10 = hash(ix + 1, iz);
        double v01 = hash(ix, iz + 1), v11 = hash(ix + 1, iz + 1);
        return v00 * (1 - ux) * (1 - uz) + v10 * ux * (1 - uz)
                + v01 * (1 - ux) * uz + v11 * ux * uz;
    }

    private static int spikeHeight(int x, int z) {
        double v = smoothNoise(x, z);
        double wave = smoothNoise(x + 1000, z + 1000) * 2.0 - 1.0;
        int baseHeight = 60 + (int) (wave * 3);
        if (v < 0.55) return baseHeight;
        double strength = Math.min((v - 0.55) / 0.3, 1.0);
        return (int) (baseHeight + Math.pow(strength, 2.5) * 200);
    }

    // Source search order and fixed Y=61 are intentional.
    BlockPos findFlatCenter(int startX, int startZ, int radius) {
        for (int d = 0; d < 200; d += 4) {
            for (int dx = -d; dx <= d; dx += 4) {
                for (int dz = -d; dz <= d; dz += 4) {
                    int x = startX + dx, z = startZ + dz, height = spikeHeight(x, z);
                    boolean flat = true;
                    for (int ox = -radius; ox <= radius && flat; ox += 2) {
                        for (int oz = -radius; oz <= radius; oz += 2) {
                            int h = spikeHeight(x + ox, z + oz);
                            if (Math.abs(h - height) > 3 || h > height + 60) {
                                flat = false;
                                break;
                            }
                        }
                    }
                    if (flat) return new BlockPos(x, 61, z);
                }
            }
        }
        return null;
    }

    public static BlockPos findGroundPos(int startX, int startZ) {
        for (int radius = 0; radius < 1000; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (Math.abs(dx) == radius || Math.abs(dz) == radius) {
                        int x = startX + dx, z = startZ + dz;
                        if (spikeHeight(x, z) == 60) return new BlockPos(x, 61, z);
                    }
                }
            }
        }
        return new BlockPos(startX, 61, startZ);
    }

    @Override public int getSeaLevel() { return 0; }
    @Override public int getMinY() { return -64; }
    @Override public int getGenDepth() { return 384; }
    @Override public int getBaseHeight(int x, int z, Heightmap.Types type,
            LevelHeightAccessor world, RandomState random) {
        int h = spikeHeight(x, z);
        return Math.min(h == 0 ? 1 : h + 1, world.getMaxBuildHeight());
    }
    @Override public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor world, RandomState random) {
        BlockState[] states = new BlockState[world.getHeight()];
        Arrays.fill(states, Blocks.AIR.defaultBlockState());
        return new NoiseColumn(world.getMinBuildHeight(), states);
    }
    @Override public void addDebugScreenInfo(List<String> text, RandomState random, BlockPos pos) {
        text.add("GrassEmpty Generator");
        text.add("Structure: " + structureName);
        text.add("At: " + structureX + ", " + structureZ);
    }
}
