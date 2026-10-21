package com.pryzmm.splitself.forge.mixin;
import net.minecraft.world.level.block.CoralBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
/** Source CoralMixin unconditionally returns true in every biome, despite its method name. */
@Mixin(value=CoralBlock.class,remap=false)
public abstract class CoralWaterMixin{
    @Inject(method={"scanForWater","m_52134_"},at=@At("HEAD"),cancellable=true,require=1,remap=false)
    private void splitself$water(CallbackInfoReturnable<Boolean> callback){callback.setReturnValue(true);}
}
