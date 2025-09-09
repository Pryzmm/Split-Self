package com.pryzmm.splitself.mixin;

import com.pryzmm.splitself.events.EventManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.GameMenuScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
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

    @Unique
    public Integer pauseAttempts = 0;

    @Inject(method = "openGameMenu", at = @At("HEAD"), cancellable = true)
    private void preventGameMenuDuringEvent(boolean pauseOnly, CallbackInfo ci) {
        if (EventManager.WINDOW_MANIPULATION_ACTIVE || EventManager.ACTIVE_EVENT) {
            ci.cancel();
        } else if (EventManager.PAUSE_PREVENTION) {
            pauseAttempts++;
            if (pauseAttempts < 10) {
                ci.cancel();
            } else {
                EventManager.PAUSE_PREVENTION = false;
                pauseAttempts = 0;
            }
        }
    }
}