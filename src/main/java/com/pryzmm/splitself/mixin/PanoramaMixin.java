package com.pryzmm.splitself.mixin;

import com.pryzmm.splitself.SplitSelf;
import com.pryzmm.splitself.client.SplitSelfClient;
import net.minecraft.client.gui.CubeMapRenderer;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(CubeMapRenderer.class)
public class PanoramaMixin {

    @Unique
    private static int face = 0;

    @ModifyArg(method = "draw", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderTexture(ILnet/minecraft/util/Identifier;)V"), index = 1)
    private Identifier replacePanoramaTexture(Identifier original) {
        Identifier replacement = Identifier.of(SplitSelf.MOD_ID, "textures/gui/title/background/" + SplitSelfClient.panorama + "/panorama_" + face + ".png");
        face = (face + 1) % 6;
        return replacement;
    }
}