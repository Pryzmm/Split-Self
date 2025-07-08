package com.pryzmm.splitself.entity.client;

import com.pryzmm.splitself.SplitSelf;
import com.pryzmm.splitself.entity.custom.TheOtherEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

public class TheOtherRenderer extends MobEntityRenderer<TheOtherEntity, EntityModel<TheOtherEntity>> {
    private static final Identifier DEFAULT_TEXTURE = Identifier.of(SplitSelf.MOD_ID, "textures/entity/the_other/the_other.png");

    private final TheOtherModel<TheOtherEntity> classicModel;
    private final TheOtherModel<TheOtherEntity> slimModel;

    public TheOtherRenderer(EntityRendererFactory.Context context) {
        super(context, new TheOtherModel<>(context.getPart(TheOtherModel.THEOTHER)), 0.6f);
        this.classicModel = new TheOtherModel<>(context.getPart(TheOtherModel.THEOTHER));
        this.slimModel = new TheOtherModel<>(context.getPart(TheOtherModel.THEOTHER_SLIM));
    }

    @Override
    public Identifier getTexture(TheOtherEntity entity) {
        PlayerEntity nearestPlayer = entity.getNearestPlayer();
        if (nearestPlayer != null) {
            // Get the player's skin texture
            return MinecraftClient.getInstance().getSkinProvider().getSkinTextures(nearestPlayer.getGameProfile()).texture();
        }
        return DEFAULT_TEXTURE;
    }

    private boolean isSlimModel(PlayerEntity player) {
        if (player == null) return false;
        return MinecraftClient.getInstance().getSkinProvider().getSkinTextures(player.getGameProfile()).model() == net.minecraft.client.util.SkinTextures.Model.SLIM;
    }

    @Override
    public void render(TheOtherEntity livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        PlayerEntity nearestPlayer = livingEntity.getNearestPlayer();

        // Switch the model based on the nearest player's skin type
        if (isSlimModel(nearestPlayer)) {
            // Use slim model
            this.model = this.slimModel;
        } else {
            // Use classic model
            this.model = this.classicModel;
        }

        matrixStack.scale(0.9375f, 0.9375f, 0.9375f);
        super.render(livingEntity, f, g, matrixStack, vertexConsumerProvider, i);
    }
}