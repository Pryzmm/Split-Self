package com.pryzmm.splitself.forge.mixin;

import com.pryzmm.splitself.forge.ClientPanorama;
import net.minecraft.client.renderer.CubeMap;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = CubeMap.class, remap = false)
public abstract class PanoramaMixin {
    @ModifyArg(method = {"render", "m_108849_"}, at = @At(value = "INVOKE",
            target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderTexture(ILnet/minecraft/resources/ResourceLocation;)V"),
            index = 1, require = 1, remap = false)
    private ResourceLocation splitselfPanorama(ResourceLocation original) {
        return ClientPanorama.texture(original);
    }
}
