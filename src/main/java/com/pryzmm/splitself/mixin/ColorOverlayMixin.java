package com.pryzmm.splitself.mixin;

import com.pryzmm.splitself.screen.overlay.ColorOverlay;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class ColorOverlayMixin {

    @Inject(method = "renderMainHud", at = @At("TAIL"))
    public void renderColorOverlay(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (ColorOverlay.overlayVisible) ColorOverlay.renderTopLayerOverlay(context);
    }

}
