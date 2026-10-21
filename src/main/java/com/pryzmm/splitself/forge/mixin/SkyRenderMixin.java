package com.pryzmm.splitself.forge.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.pryzmm.splitself.forge.ClientSkyEffects;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Explicit dev/SRG aliases verified against Forge 47.2.20 mappings; no generated refmap required. */
@Mixin(value = LevelRenderer.class, remap = false)
public abstract class SkyRenderMixin {
    @ModifyVariable(method = {"renderSky", "m_202423_"}, at = @At("STORE"), ordinal = 0, require = 1, remap = false)
    private Vec3 splitselfSky(Vec3 original) {
        if(com.pryzmm.splitself.forge.ClientFinaleSequence.broken("SKY"))return Vec3.ZERO;
        return ClientSkyEffects.redSky() ? new Vec3(170 / 255f, 0, 0) : original;
    }
    @Inject(method = {"renderSky", "m_202423_"}, at = {@At("HEAD"), @At("RETURN")}, require = 1, remap = false)
    private void splitselfFog(CallbackInfo ci) {
        if(com.pryzmm.splitself.forge.ClientFinaleSequence.broken("SKY")){RenderSystem.setShaderFogColor(0,0,0);return;}
        if (ClientSkyEffects.redSky()) RenderSystem.setShaderFogColor(136 / 255f, 0, 0);
    }
}
