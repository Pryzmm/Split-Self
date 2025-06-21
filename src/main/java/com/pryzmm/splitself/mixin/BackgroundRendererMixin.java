package com.pryzmm.splitself.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.pryzmm.splitself.events.SkyColor;
import net.minecraft.client.render.BackgroundRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BackgroundRenderer.class)
public class BackgroundRendererMixin {

    @Inject(method = "applyFogColor", at = @At("HEAD"))
    private static void setFogSkyColorInApply(CallbackInfo ci) {
        float[] rgb = SkyColor.getFogRGBComponents();
        if (rgb != null) { // Check if not null before using
            RenderSystem.setShaderFogColor(rgb[0], rgb[1], rgb[2]);
        }
        // If null, the original fog color logic will continue
    }
}