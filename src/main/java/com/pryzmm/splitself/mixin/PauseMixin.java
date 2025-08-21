package com.pryzmm.splitself.mixin;

import com.pryzmm.splitself.events.EventManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.GameMenuScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class PauseMixin {

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void preventPauseDuringEvent(net.minecraft.client.gui.screen.Screen screen, CallbackInfo ci) {
        if (EventManager.WINDOW_MANIPULATION_ACTIVE && screen instanceof GameMenuScreen) {
            ci.cancel();
        }
    }

    @Inject(method = "openGameMenu", at = @At("HEAD"), cancellable = true)
    private void preventGameMenuDuringEvent(boolean pauseOnly, CallbackInfo ci) {
        if (EventManager.WINDOW_MANIPULATION_ACTIVE) {
            ci.cancel();
        }
    }
}