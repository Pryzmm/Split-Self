package com.pryzmm.splitself.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.BackupPromptScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(MinecraftClient.class)
public abstract class ExperimentalMixin {

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void autoConfirm(Screen screen, CallbackInfo ci) {
        if (screen instanceof ConfirmScreen confirmScreen) {
            if (Objects.equals(confirmScreen.getTitle(), Text.translatable("selectWorld.warning.experimental.title"))) {
                confirmScreen.callback.accept(true);
                ci.cancel();
            }
        } else if (screen instanceof BackupPromptScreen backupScreen) {
            backupScreen.callback.proceed(false, false);
            ci.cancel();
        }
    }

}