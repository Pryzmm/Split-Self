package com.pryzmm.splitself.mixin;

import com.pryzmm.splitself.client.render.PostHudRenderCallback;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Inject(method = "render", at = @At("RETURN"))
    private void splitself$hookAfterRender(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
        PostHudRenderCallback.EVENT.invoker().onPostHudRendered(tickCounter);
    }

}