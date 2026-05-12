package com.pryzmm.splitself.entity.client;

import com.pryzmm.splitself.SplitSelf;
import com.pryzmm.splitself.entity.custom.TheOtherEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class TheOtherRenderer extends MobEntityRenderer<TheOtherEntity, EntityModel<TheOtherEntity>> {
    private static final Identifier DEFAULT_TEXTURE = Identifier.of(SplitSelf.MOD_ID, "textures/entity/the_other/the_other.png");

    private static final MinecraftClient client = MinecraftClient.getInstance();

    private final TheOtherModel<TheOtherEntity> classicModel;
    private final TheOtherModel<TheOtherEntity> slimModel;

    public TheOtherRenderer(EntityRendererFactory.Context context) {
        super(context, new TheOtherModel<>(context.getPart(TheOtherModel.THEOTHER)), 0.6f);
        this.classicModel = new TheOtherModel<>(context.getPart(TheOtherModel.THEOTHER));
        this.slimModel = new TheOtherModel<>(context.getPart(TheOtherModel.THEOTHER_SLIM));
    }

    @Override
    public Identifier getTexture(TheOtherEntity entity) {
        if (client.player != null) return MinecraftClient.getInstance().getSkinProvider().getSkinTextures(client.player.getGameProfile()).texture();
        return DEFAULT_TEXTURE;
    }

    @Override
    public void render(TheOtherEntity livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        if (client.player == null) return;
        if (client.getSkinProvider().getSkinTextures(client.player.getGameProfile()).model() == net.minecraft.client.util.SkinTextures.Model.SLIM) this.model = this.slimModel;
        else this.model = this.classicModel;

        matrixStack.scale(0.9375f, 0.9375f, 0.9375f);
        super.render(livingEntity, f, g, matrixStack, vertexConsumerProvider, i);
    }
}