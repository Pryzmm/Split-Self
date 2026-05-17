package com.pryzmm.splitself.world;

import com.pryzmm.splitself.SplitSelf;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class DimensionRegistry {

    public static final RegistryKey<World> LIMBO_DIMENSION_KEY = RegistryKey.of(RegistryKeys.WORLD, Identifier.of(SplitSelf.MOD_ID, "limbo_dimension"));
    public static final RegistryKey<World> EMPTINESS_DIMENSION_KEY = RegistryKey.of(RegistryKeys.WORLD, Identifier.of(SplitSelf.MOD_ID, "empty_dimension"));

    static {
        RegistryKey.of(RegistryKeys.DIMENSION_TYPE, Identifier.of(SplitSelf.MOD_ID, "limbo_dimension"));
        RegistryKey.of(RegistryKeys.BIOME, Identifier.of(SplitSelf.MOD_ID, "limbo_biome"));
        RegistryKey.of(RegistryKeys.DIMENSION_TYPE, Identifier.of(SplitSelf.MOD_ID, "empty_dimension"));
        RegistryKey.of(RegistryKeys.BIOME, Identifier.of(SplitSelf.MOD_ID, "empty_biome"));
    }

    public static void register() {
        Registry.register(Registries.CHUNK_GENERATOR,
                Identifier.of(SplitSelf.MOD_ID, "void"),
                VoidChunkGenerator.CODEC);
        SplitSelf.LOGGER.info("Registered chunk generator: {}", Identifier.of(SplitSelf.MOD_ID, "void"));

        Registry.register(Registries.CHUNK_GENERATOR,
            Identifier.of(SplitSelf.MOD_ID, "dead_coral"),
            DeadCoralChunkGenerator.CODEC);
        SplitSelf.LOGGER.info("Registered chunk generator: {}", Identifier.of(SplitSelf.MOD_ID, "dead_coral"));
    }

}