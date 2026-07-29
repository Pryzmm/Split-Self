package com.pryzmm.splitself.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.pryzmm.splitself.events.helper.SkyColor;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(BackgroundRenderer.class)
public class BackgroundRenderMixin {

    @Inject(method = "render(Lnet/minecraft/client/render/Camera;FLnet/minecraft/client/world/ClientWorld;IF)V", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;clearColor(FFFF)V", ordinal = 1))
    private static void injectDistantSkyColor(Camera camera, float tickDelta, ClientWorld world, int viewDistance, float skyDarkness, CallbackInfo ci) {
        float[] rgb = SkyColor.getDistantSkyRGBComponents(
            BackgroundRendererAccessor.getRed(),
            BackgroundRendererAccessor.getGreen(),
            BackgroundRendererAccessor.getBlue()
        );
        if (rgb != null) {
            BackgroundRendererAccessor.setRed(rgb[0]);
            BackgroundRendererAccessor.setGreen(rgb[1]);
            BackgroundRendererAccessor.setBlue(rgb[2]);
        }
    }

    @Inject(method = "applyFogColor", at = @At("HEAD"), cancellable = true)
    private static void injectFogColor(CallbackInfo ci) {
        float vanillaR = BackgroundRendererAccessor.getRed();
        float vanillaG = BackgroundRendererAccessor.getGreen();
        float vanillaB = BackgroundRendererAccessor.getBlue();

        float[] rgb = SkyColor.getFogRGBComponents(vanillaR, vanillaG, vanillaB);
        if (rgb != null) {
            RenderSystem.setShaderFogColor(rgb[0], rgb[1], rgb[2]);
            ci.cancel();
        }
    }

    @ModifyArgs(method = "render(Lnet/minecraft/client/render/Camera;FLnet/minecraft/client/world/ClientWorld;IF)V", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;clearColor(FFFF)V", ordinal = 1))
    private static void injectDistantSkyColor(Args args) {
        float r = args.get(0);
        float g = args.get(1);
        float b = args.get(2);
        float[] rgb = SkyColor.getDistantSkyRGBComponents(r, g, b);
        if (rgb != null) {
            args.set(0, rgb[0]);
            args.set(1, rgb[1]);
            args.set(2, rgb[2]);
        }
    }

}