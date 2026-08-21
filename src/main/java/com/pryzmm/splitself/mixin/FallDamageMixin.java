package com.pryzmm.splitself.mixin;

import com.pryzmm.splitself.world.DimensionRegistry;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public abstract class FallDamageMixin {

    @Shadow
    public abstract ServerWorld getServerWorld();

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    public void onFallDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        RegistryKey<World> key = this.getServerWorld().getRegistryKey();
        if (key == DimensionRegistry.EMPTINESS_DIMENSION_KEY || key == DimensionRegistry.GRASS_EMPTINESS_DIMENSION_KEY) {
            cir.cancel();
        }
    }

}
