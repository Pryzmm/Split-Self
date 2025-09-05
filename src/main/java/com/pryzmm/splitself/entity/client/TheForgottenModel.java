package com.pryzmm.splitself.entity.client;

import com.pryzmm.splitself.SplitSelf;
import com.pryzmm.splitself.entity.custom.TheForgottenEntity;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.animation.Animation;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class TheForgottenModel<T extends TheForgottenEntity> extends SinglePartEntityModel<T> {

    public static final EntityModelLayer THEFORGOTTEN = new EntityModelLayer(Identifier.of(SplitSelf.MOD_ID, "the_forgotten"), "main");

    private final ModelPart TheForgotten;
    private final ModelPart Head;

    public TheForgottenModel(ModelPart root) {
        this.TheForgotten = root; // Use root directly
        this.Head = root.getChild("Head"); // Get Head directly from the root
    }

    public static TexturedModelData getTexturedModelData() {
        return createModelData();
    }

    private static TexturedModelData createModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();

        // Add parts directly to the root instead of to TheForgotten
        ModelPartData Head = modelPartData.addChild("Head", ModelPartBuilder.create()
                        .uv(0, 0).cuboid(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new Dilation(0.0F)),
                ModelTransform.pivot(0.0F, 0.0F, 0.0F));

//        ModelPartData Head_Distort = Head.addChild("Head_Distort", ModelPartBuilder.create()
//                        .uv(1, 34).cuboid(0.0F, -9.0F, -6.0F, 8.0F, 7.0F, 7.0F, new Dilation(0.0F)),
//                ModelTransform.pivot(0.0F, 0.0F, 0.0F));

        ModelPartData Body = modelPartData.addChild("Body", ModelPartBuilder.create()
                        .uv(16, 16).cuboid(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new Dilation(0.0F)),
                ModelTransform.pivot(0.0F, 0.0F, 0.0F));

        ModelPartData RightArm = modelPartData.addChild("RightArm", ModelPartBuilder.create()
                        .uv(40, 16).cuboid(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new Dilation(0.0F)),
                ModelTransform.pivot(-5.0F, 2.0F, 0.0F));

        ModelPartData LeftArm = modelPartData.addChild("LeftArm", ModelPartBuilder.create()
                        .uv(32, 48).cuboid(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new Dilation(0.0F)),
                ModelTransform.pivot(5.0F, 2.0F, 0.0F));

        ModelPartData RightLeg = modelPartData.addChild("RightLeg", ModelPartBuilder.create()
                        .uv(0, 16).cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new Dilation(0.0F)),
                ModelTransform.pivot(-1.9F, 12.0F, 0.0F));

        ModelPartData LeftLeg = modelPartData.addChild("LeftLeg", ModelPartBuilder.create()
                        .uv(16, 48).cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new Dilation(0.0F)),
                ModelTransform.pivot(1.9F, 12.0F, 0.0F));

        return TexturedModelData.of(modelData, 64, 64);
    }

    private void setHeadAngles(float headYaw, float headPitch) {
        headYaw = MathHelper.clamp(headYaw, -30.0F, 30.0F);
        headPitch = MathHelper.clamp(headPitch, -25.0F, 45.0F);

        this.Head.setAngles(headPitch * 0.017453292F, headYaw * 0.017453292F, 0.0F);
    }

    public void render(DrawContext context, VertexConsumer vertexConsumer, int light, int overlay, int color) {
        this.Head.render(context.getMatrices(), vertexConsumer, light, overlay, color);
    }

    @Override
    public void setAngles(TheForgottenEntity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
        this.getPart().traverse().forEach(ModelPart::resetTransform);
        this.setHeadAngles(headYaw, headPitch);
        Animation idleAnimation = TheForgottenAnimations.ANIM_THE_FORGOTTEN_IDLE;
        this.updateAnimation(entity.idleAnimationState, idleAnimation, animationProgress);
    }

    public ModelPart getPart() {
        return TheForgotten;
    }
}