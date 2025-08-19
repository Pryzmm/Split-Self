package com.pryzmm.splitself.world;

import com.pryzmm.splitself.SplitSelf;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BiomeMoodSound;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeEffects;
import net.minecraft.world.biome.GenerationSettings;
import net.minecraft.world.biome.SpawnSettings;
import net.minecraft.world.dimension.DimensionType;

public class DimensionRegistry {
    public static final RegistryKey<DimensionType> LIMBO_DIMENSION_TYPE_KEY =
            RegistryKey.of(RegistryKeys.DIMENSION_TYPE, Identifier.of(SplitSelf.MOD_ID, "limbo_dimension"));

    public static final RegistryKey<World> LIMBO_DIMENSION_KEY =
            RegistryKey.of(RegistryKeys.WORLD, Identifier.of(SplitSelf.MOD_ID, "limbo_dimension"));

    public static final RegistryKey<Biome> LIMBO_BIOME_KEY =
            RegistryKey.of(RegistryKeys.BIOME, Identifier.of(SplitSelf.MOD_ID, "limbo_biome"));

    public static void register() {
        Registry.register(Registries.CHUNK_GENERATOR,
                Identifier.of(SplitSelf.MOD_ID, "build_only"),
                BuildOnlyChunkGenerator.CODEC);

        SplitSelf.LOGGER.info("Registered chunk generator: " + Identifier.of(SplitSelf.MOD_ID, "build_only"));
    }

    public static Biome createLimboBiome() {
        GenerationSettings.Builder generationSettings = new GenerationSettings.Builder();
        SpawnSettings.Builder spawnSettings = new SpawnSettings.Builder();

        return new Biome.Builder()
                .precipitation(false)
                .downfall(0.0f)
                .temperature(0.7f)
                .generationSettings(generationSettings.build())
                .spawnSettings(spawnSettings.build())
                .effects(new BiomeEffects.Builder()
                        .waterColor(0x3f76e4)
                        .waterFogColor(0x050533)
                        .skyColor(0x78a7ff)
                        .grassColor(0x91bd59)
                        .foliageColor(0x77ab2f)
                        .fogColor(0xc0d8ff)
                        .moodSound(BiomeMoodSound.CAVE)
                        .build())
                .build();
    }
}