package com.pryzmm.splitself.forge;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

final class TheForgottenRenderer extends MobRenderer<TheForgottenEntity, TheForgottenModel> {
    TheForgottenRenderer(EntityRendererProvider.Context context) {
        super(context, new TheForgottenModel(context.bakeLayer(TheForgottenModel.LAYER)), 0.6f);
    }
    @Override public ResourceLocation getTextureLocation(TheForgottenEntity entity) {
        return new ResourceLocation("splitself", "textures/entity/the_forgotten/the_forgotten.png");
    }
    @Override protected void scale(TheForgottenEntity entity, PoseStack pose, float partialTick) {
        pose.scale(0.9375f, 0.9375f, 0.9375f);
    }
}
