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

    // Target applyFogColor as well
    @Inject(method = "applyFogColor", at = @At("HEAD"))
    private static void setFogSkyColorInApply(CallbackInfo ci) {
        float[] rgb = SkyColor.getFogSkyRGBComponents();
        RenderSystem.setShaderFogColor(rgb[0], rgb[1], rgb[2]);
    }
}