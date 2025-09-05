package com.pryzmm.splitself.entity.client;

import com.pryzmm.splitself.SplitSelf;
import com.pryzmm.splitself.entity.custom.TheForgottenEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class TheForgottenRenderer extends MobEntityRenderer<TheForgottenEntity, EntityModel<TheForgottenEntity>> {

    public TheForgottenRenderer(EntityRendererFactory.Context context) {
        super(context, new TheForgottenModel<>(context.getPart(TheForgottenModel.THEFORGOTTEN)), 0.6f);
        TheForgottenModel<TheForgottenEntity> model = new TheForgottenModel<>(context.getPart(TheForgottenModel.THEFORGOTTEN));
    }

    @Override
    public void render(TheForgottenEntity livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        matrixStack.scale(0.9375f, 0.9375f, 0.9375f);
        super.render(livingEntity, f, g, matrixStack, vertexConsumerProvider, i);
    }

    @Override
    public Identifier getTexture(TheForgottenEntity entity) {
        return Identifier.of(SplitSelf.MOD_ID, "textures/entity/the_forgotten/the_forgotten.png");
    }
}