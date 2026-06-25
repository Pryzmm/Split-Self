package com.pryzmm.splitself.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.pryzmm.splitself.block.ModBlocks;
import com.pryzmm.splitself.data.WorldData;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class DeadCoralChunkGenerator extends ChunkGenerator {
    public static final MapCodec<DeadCoralChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            Codec.STRING.fieldOf("structure_name").forGetter(generator -> generator.structureName),
            Codec.INT.fieldOf("structure_x").forGetter(generator -> generator.structureX),
            Codec.INT.fieldOf("structure_z").forGetter(generator -> generator.structureZ),
            Biome.REGISTRY_CODEC.fieldOf("biome").forGetter(generator -> generator.biome)
        ).apply(instance, DeadCoralChunkGenerator::new)
    );

    private final String structureName;
    private final int structureX;
    private final int structureZ;
    private final RegistryEntry<Biome> biome;

    public static final int MIN_GENERATION_Y = 20;

    public DeadCoralChunkGenerator(String structureName, int structureX, int structureZ, RegistryEntry<Biome> biome) {
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
    public void carve(ChunkRegion chunkRegion, long seed, NoiseConfig noiseConfig, BiomeAccess biomeAccess, StructureAccessor structureAccessor, Chunk chunk, GenerationStep.Carver carverStep) {}

    @Override
    public void buildSurface(ChunkRegion region, StructureAccessor structures, NoiseConfig noiseConfig, Chunk chunk) {}

    @Override
    public void populateEntities(ChunkRegion region) {}

    @Override
    public CompletableFuture<Chunk> populateNoise(Blender blender, NoiseConfig noiseConfig, StructureAccessor structureAccessor, Chunk chunk) {
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        Heightmap heightmapOcean = chunk.getHeightmap(Heightmap.Type.OCEAN_FLOOR_WG);
        Heightmap heightmapSurface = chunk.getHeightmap(Heightmap.Type.WORLD_SURFACE_WG);

        int startX = chunk.getPos().getStartX();
        int startZ = chunk.getPos().getStartZ();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = startX + x;
                int worldZ = startZ + z;

                int spikeHeight = getSpikeHeight(worldX, worldZ);

                for (int y = MIN_GENERATION_Y; y <= spikeHeight; y++) {
                    BlockState state;
                    if (y == MIN_GENERATION_Y) {
                        state = Blocks.BEDROCK.getDefaultState();
                        chunk.setBlockState(mutable.set(x, -64, z), Blocks.BARRIER.getDefaultState(), false);
                    } else {
                        state = ModBlocks.DEAD_BRAINS.getDefaultState();
                    }
                    chunk.setBlockState(mutable.set(x, y, z), state, false);
                    heightmapOcean.trackUpdate(x, y, z, state);
                    heightmapSurface.trackUpdate(x, y, z, state);
                }
            }
        }
        return CompletableFuture.completedFuture(chunk);
    }

    private static double hash(int x, int z) {
        long h = WorldData.getSeed() ^ ((long)x * 1664525L + 42) ^ ((long)z * 1013904223L);
        h = h ^ (h >>> 33);
        h *= 0xff51afd7ed558ccdL;
        h = h ^ (h >>> 33);
        h *= 0xc4ceb9fe1a85ec53L;
        h = h ^ (h >>> 33);
        return (double)(h & 0x7fffffffffffffffL) / (double)0x7fffffffffffffffL;
    }

    private static double smoothNoise(int x, int z) {
        int ix = (int)Math.floor(x / 12.0);
        int iz = (int)Math.floor(z / 12.0);
        double fx = (x - ix * 12.0) / 12.0;
        double fz = (z - iz * 12.0) / 12.0;
        double ux = fx * fx * (3 - 2 * fx);
        double uz = fz * fz * (3 - 2 * fz);
        double v00 = hash(ix, iz);
        double v10 = hash(ix + 1, iz);
        double v01 = hash(ix, iz + 1);
        double v11 = hash(ix + 1, iz + 1);

        return v00 * (1-ux) * (1-uz)
             + v10 *    ux  * (1-uz)
             + v01 * (1-ux) *    uz
             + v11 *    ux  *    uz;
    }

    private static int getSpikeHeight(int x, int z) {
        double v = smoothNoise(x, z);
        double wave = smoothNoise(x + 1000, z + 1000) * 2.0 - 1.0;
        int baseHeight = 60 + (int)(wave * 3);
        if (v < 0.55) return baseHeight;
        double spikeStrength = Math.min((v - 0.55) / 0.45, 1.0);
        return (int)(baseHeight + Math.pow(spikeStrength, 2.5) * 200);
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
    public int getHeight(int x, int z, Heightmap.Type heightmap, HeightLimitView world, NoiseConfig noiseConfig) {
        int h = getSpikeHeight(x, z);
        int result = h == 0 ? 1 : h + 1;
        return Math.min(result, world.getTopY());
    }

    @Override
    public VerticalBlockSample getColumnSample(int x, int z, HeightLimitView world, NoiseConfig noiseConfig) {
        BlockState[] states = new BlockState[world.getHeight()];
        for (int i = 0; i < states.length; i++) {
            states[i] = Blocks.AIR.getDefaultState();
        }
        return new VerticalBlockSample(world.getBottomY(), states);
    }

    @Override
    public void getDebugHudText(List<String> text, NoiseConfig noiseConfig, BlockPos pos) {
        text.add("DeadCoral Generator");
        text.add("Structure: " + structureName);
        text.add("At: " + structureX + ", " + structureZ);
    }

    public static BlockPos findGroundPos(int startX, int startZ) {
        for (int radius = 0; radius < 1000; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (Math.abs(dx) == radius || Math.abs(dz) == radius) {
                        int x = startX + dx;
                        int z = startZ + dz;
                        if (getSpikeHeight(x, z) == 60) {
                            return new BlockPos(x, 61, z);
                        }
                    }
                }
            }
        }
        return new BlockPos(startX, 61, startZ);
    }

    public BlockPos findFlatCenter(int startX, int startZ, int requiredRadius) {
        for (int d = 0; d < 200; d += 4) {
            for (int dx = -d; dx <= d; dx += 4) {
                for (int dz = -d; dz <= d; dz += 4) {
                    int cx = startX + dx;
                    int cz = startZ + dz;
                    if (isFlatArea(cx, cz, requiredRadius)) {
                        return new BlockPos(cx, 61, cz);
                    }
                }
            }
        }
        return null;
    }

    private boolean isFlatArea(int cx, int cz, int radius) {
        int centerHeight = getSpikeHeight(cx, cz);
        for (int dx = -radius; dx <= radius; dx += 2) {
            for (int dz = -radius; dz <= radius; dz += 2) {
                int h = getSpikeHeight(cx + dx, cz + dz);
                if (Math.abs(h - centerHeight) > 3 || h > centerHeight + 60) {
                    return false;
                }
            }
        }
        return true;
    }

    private static final Map<String, BlockPos> FLAT_CENTER_CACHE = new HashMap<>();

    private BlockPos getStructurePos() {
        return FLAT_CENTER_CACHE.computeIfAbsent(
            structureX + "," + structureZ,
            k -> findFlatCenter(structureX, structureZ, 24)
        );
    }

}