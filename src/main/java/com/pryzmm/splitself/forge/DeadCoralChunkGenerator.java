package com.pryzmm.splitself.forge;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;

/** Pinned DeadCoralChunkGenerator terrain; dimension activation is pending. */
public final class DeadCoralChunkGenerator extends GrassEmptyChunkGenerator {
    public static final Codec<DeadCoralChunkGenerator> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("structure_name").forGetter(g -> g.structureName),
            Codec.INT.fieldOf("structure_x").forGetter(g -> g.structureX),
            Codec.INT.fieldOf("structure_z").forGetter(g -> g.structureZ),
            Biome.CODEC.fieldOf("biome").forGetter(g -> g.biome)
    ).apply(instance, DeadCoralChunkGenerator::new));

    public DeadCoralChunkGenerator(String structureName, int structureX, int structureZ, Holder<Biome> biome) {
        super(structureName, structureX, structureZ, biome);
    }

    @Override protected Codec<? extends ChunkGenerator> codec() { return CODEC; }

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
                chunk.setBlockState(pos.set(startX + x, -64, startZ + z),
                        Blocks.BARRIER.defaultBlockState(), false);
                chunk.setBlockState(pos.set(startX + x, -62, startZ + z),
                        SplitSelfForge.TRIGGER_TRANSITION.get().defaultBlockState(), false);
                int top = spikeHeight(startX + x, startZ + z);
                for (int y = 20; y <= top; y++) {
                    BlockState state = y == 20 ? Blocks.BEDROCK.defaultBlockState()
                            : SplitSelfForge.DEAD_BRAINS.get().defaultBlockState();
                    chunk.setBlockState(pos.set(startX + x, y, startZ + z), state, false);
                    ocean.update(x, y, z, state);
                    surface.update(x, y, z, state);
                }
            }
        }
        return CompletableFuture.completedFuture(chunk);
    }

    private static int spikeHeight(int x, int z) {
        double v = smoothNoise(x, z);
        double wave = smoothNoise(x + 1000, z + 1000) * 2.0 - 1.0;
        int baseHeight = 60 + (int) (wave * 3);
        if (v < 0.55) return baseHeight;
        double strength = Math.min((v - 0.55) / 0.45, 1.0);
        return (int) (baseHeight + Math.pow(strength, 2.5) * 200);
    }

    @Override BlockPos findFlatCenter(int startX,int startZ,int radius) {
        for(int d=0;d<200;d+=4)for(int dx=-d;dx<=d;dx+=4)for(int dz=-d;dz<=d;dz+=4){
            int x=startX+dx,z=startZ+dz,center=spikeHeight(x,z);boolean flat=true;
            for(int ox=-radius;ox<=radius&&flat;ox+=2)for(int oz=-radius;oz<=radius;oz+=2){int h=spikeHeight(x+ox,z+oz);if(Math.abs(h-center)>3||h>center+60){flat=false;break;}}
            if(flat)return new BlockPos(x,61,z);
        }return null;
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

    @Override public int getBaseHeight(int x, int z, Heightmap.Types type,
            LevelHeightAccessor world, RandomState random) {
        int h = spikeHeight(x, z);
        return Math.min(h == 0 ? 1 : h + 1, world.getMaxBuildHeight());
    }

    @Override public void addDebugScreenInfo(List<String> text, RandomState random, BlockPos pos) {
        text.add("DeadCoral Generator");
        text.add("Structure: " + structureName);
        text.add("At: " + structureX + ", " + structureZ);
    }
}
