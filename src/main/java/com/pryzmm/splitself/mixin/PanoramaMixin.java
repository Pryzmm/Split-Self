package com.pryzmm.splitself.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import com.pryzmm.splitself.SplitSelf;
import com.pryzmm.splitself.client.SplitSelfClient;
import net.minecraft.client.gui.CubeMapRenderer;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CubeMapRenderer.class)
public class PanoramaMixin {

    @Redirect(method = "draw", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderTexture(ILnet/minecraft/util/Identifier;)V"))
    private void injected(int texture, Identifier id, @Local(name = "k") int k) {
        Identifier identifier = Identifier.of(SplitSelf.MOD_ID, "textures/gui/title/background/" + SplitSelfClient.panorama + "/panorama_" + k + ".png");
        RenderSystem.setShaderTexture(texture, identifier);
    }

}
