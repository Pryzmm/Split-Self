package com.pryzmm.splitself.entity;

import com.pryzmm.splitself.SplitSelf;
import com.pryzmm.splitself.entity.custom.TheForgottenEntity;
import com.pryzmm.splitself.entity.custom.TheOtherEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class ModEntities {

    static Identifier id_the_other = Identifier.of(SplitSelf.MOD_ID, "the_other");
    static Identifier id_the_forgotten = Identifier.of(SplitSelf.MOD_ID, "the_forgotten");
    static RegistryKey<EntityType<?>> key_the_other = RegistryKey.of(RegistryKeys.ENTITY_TYPE, id_the_other);
    static RegistryKey<EntityType<?>> key_the_forgotten = RegistryKey.of(RegistryKeys.ENTITY_TYPE, id_the_forgotten);

    public static final EntityType<TheOtherEntity> TheOther = Registry.register(Registries.ENTITY_TYPE,
            Identifier.of(SplitSelf.MOD_ID, "the_other"),
            EntityType.Builder.create(TheOtherEntity::new, SpawnGroup.CREATURE).dimensions(0.6f, 1.8f).build());

    public static final EntityType<TheForgottenEntity> TheForgotten = Registry.register(Registries.ENTITY_TYPE,
            Identifier.of(SplitSelf.MOD_ID, "the_forgotten"),
            EntityType.Builder.create(TheForgottenEntity::new, SpawnGroup.CREATURE).dimensions(0.6f, 1.8f).build());

    public static void registerModEntities() {
        SplitSelf.LOGGER.info("Loading and registering entities...");
    }
}
