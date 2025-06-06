package com.pryzmm.splitself.entity.client;

import com.pryzmm.splitself.SplitSelf;
import com.pryzmm.splitself.entity.custom.TheOtherEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class TheOtherRenderer extends MobEntityRenderer<TheOtherEntity, TheOtherModel<TheOtherEntity>> {
    public TheOtherRenderer(EntityRendererFactory.Context context) {
        super(context, new TheOtherModel<>(context.getPart(TheOtherModel.THEOTHER)), 0.6f);
    }

    @Override
    public Identifier getTexture(TheOtherEntity entity) {

        return Identifier.of(SplitSelf.MOD_ID, "textures/entity/the_other/the_other.png");
    }

    @Override
    public void render(TheOtherEntity livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        matrixStack.scale(0.935f, 0.935f, 0.935f);
        super.render(livingEntity, f, g, matrixStack, vertexConsumerProvider, i);
    }
}
