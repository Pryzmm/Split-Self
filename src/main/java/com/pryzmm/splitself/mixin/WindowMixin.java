package com.pryzmm.splitself.mixin;

import com.pryzmm.splitself.events.EventManager;
import net.minecraft.client.util.Window;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Window.class)
public class WindowMixin {

    @Inject(method = "toggleFullscreen", at = @At("HEAD"), cancellable = true)
    private void onToggleFullscreen(CallbackInfo ci) {
        if (EventManager.WINDOW_MANIPULATION_ACTIVE) {
            ci.cancel();
        }
    }
}