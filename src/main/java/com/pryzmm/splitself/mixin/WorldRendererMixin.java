package com.pryzmm.splitself.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.pryzmm.splitself.events.SkyColor;
import net.minecraft.client.render.*;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

    @Inject(method = "renderSky", at = @At("HEAD"))
    private void setFogSkyColorAtStart(CallbackInfo ci) {
        float[] rgb = SkyColor.getFogRGBComponents();
        if (rgb != null) {
            RenderSystem.setShaderFogColor(rgb[0], rgb[1], rgb[2]);
        }
    }

    @Inject(method = "renderSky", at = @At("TAIL"))
    private void setFogSkyColorAtEnd(CallbackInfo ci) {
        float[] rgb = SkyColor.getFogRGBComponents();
        if (rgb != null) {
            RenderSystem.setShaderFogColor(rgb[0], rgb[1], rgb[2]);
        }
    }

    @ModifyVariable(method = "renderSky", at = @At("STORE"), ordinal = 0)
    private Vec3d modifySkyColor(Vec3d original) {
        float[] rgb = SkyColor.getSkyRGBComponents();
        if (rgb != null) {
            return new Vec3d(rgb[0], rgb[1], rgb[2]);
        }
        return original;
    }
}

