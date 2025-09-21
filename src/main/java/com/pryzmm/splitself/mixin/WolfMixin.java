package com.pryzmm.splitself.mixin;

import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.passive.WolfEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WolfEntity.class)
public interface WolfMixin {
    @Accessor("COLLAR_COLOR")
    static TrackedData<Integer> getCollarColorData() {
        throw new AssertionError();
    }
}