package com.pryzmm.splitself.forge;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

final class TheOtherRenderer extends MobRenderer<TheOtherEntity, TheOtherModel> {
    private final TheOtherModel classic, slim;
    TheOtherRenderer(EntityRendererProvider.Context context) {
        super(context, new TheOtherModel(context.bakeLayer(TheOtherModel.CLASSIC)), 0.6f);
        classic = model;
        slim = new TheOtherModel(context.bakeLayer(TheOtherModel.SLIM));
    }
    @Override public ResourceLocation getTextureLocation(TheOtherEntity entity) {
        var player = Minecraft.getInstance().player;
        return player == null ? new ResourceLocation("splitself", "textures/entity/the_other/the_other.png")
                : player.getSkinTextureLocation();
    }
    @Override public void render(TheOtherEntity entity, float yaw, float partialTick, PoseStack pose,
                                 MultiBufferSource buffers, int light) {
        var player = Minecraft.getInstance().player;
        model = player != null && "slim".equals(player.getModelName()) ? slim : classic;
        super.render(entity, yaw, partialTick, pose, buffers, light);
    }
    @Override protected void scale(TheOtherEntity entity, PoseStack pose, float partialTick) {
        pose.scale(0.9375f, 0.9375f, 0.9375f);
    }
}
