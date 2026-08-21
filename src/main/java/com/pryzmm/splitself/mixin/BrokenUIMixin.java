package com.pryzmm.splitself.mixin;

import com.pryzmm.splitself.world.FinaleRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class BrokenUIMixin {

    @Inject(method = "renderHotbar", at = @At("HEAD"), cancellable = true)
    public void stopRenderHotbar(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (FinaleRenderer.brokenHotbar) ci.cancel();
    }

    @Inject(method = "renderHealthBar", at = @At("HEAD"), cancellable = true)
    public void stopRenderHealth(DrawContext context, PlayerEntity player, int x, int y, int lines, int regeneratingHeartIndex, float maxHealth, int lastHealth, int health, int absorption, boolean blinking, CallbackInfo ci) {
        if (FinaleRenderer.brokenHealth) ci.cancel();
    }

    @Inject(method = "renderFood", at = @At("HEAD"), cancellable = true)
    public void stopRenderFood(DrawContext context, PlayerEntity player, int top, int right, CallbackInfo ci) {
        if (FinaleRenderer.brokenFood) ci.cancel();
    }

    @Inject(method = "renderArmor", at = @At("HEAD"), cancellable = true)
    private static void stopRenderArmor(DrawContext context, PlayerEntity player, int i, int j, int k, int x, CallbackInfo ci) {
        if (FinaleRenderer.brokenArmor) ci.cancel();
    }

    @Inject(method = "renderExperienceBar", at = @At("HEAD"), cancellable = true)
    public void stopRenderExperience(DrawContext context, int x, CallbackInfo ci) {
        if (FinaleRenderer.brokenExperience) ci.cancel();
    }

    @ModifyArg(method = "renderHotbar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V"), index = 4)
    public int stretchHotbar(int y) {
        return y + (FinaleRenderer.brokenHotbarSize ? 100 : 0);
    }

}