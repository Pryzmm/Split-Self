package com.pryzmm.splitself.entity.client;

import com.pryzmm.splitself.SplitSelf;
import com.pryzmm.splitself.entity.custom.TheOtherEntity;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.animation.Animation;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class TheOtherModel<T extends TheOtherEntity> extends SinglePartEntityModel<T> {

    public static final EntityModelLayer THEOTHER = new EntityModelLayer(Identifier.of(SplitSelf.MOD_ID, "the_other"), "main");
    public static final EntityModelLayer THEOTHER_SLIM = new EntityModelLayer(Identifier.of(SplitSelf.MOD_ID, "the_other_slim"), "main");

    private final ModelPart TheOther;
    private final ModelPart Head;

    public TheOtherModel(ModelPart root) {
        this.TheOther = root.getChild("TheOther");
        this.Head = this.TheOther.getChild("Head");
    }

    public static TexturedModelData getTexturedModelData() {
        return createModelData(false);
    }
    public static TexturedModelData getSlimTexturedModelData() {
        return createModelData(true);
    }

    private static TexturedModelData createModelData(boolean slim) {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        ModelPartData TheOther = modelPartData.addChild("TheOther", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 0.0F, 0.0F));
        ModelPartData Head = TheOther.addChild("Head", ModelPartBuilder.create().uv(0, 0).cuboid(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new Dilation(0.0F))
                .uv(32, 0).cuboid(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new Dilation(0.5F)), ModelTransform.pivot(0.0F, 0.0F, 0.0F));
        ModelPartData Body = TheOther.addChild("Body", ModelPartBuilder.create().uv(16, 16).cuboid(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new Dilation(0.0F))
                .uv(16, 32).cuboid(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new Dilation(0.25F)), ModelTransform.pivot(0.0F, 0.0F, 0.0F));

        if (slim) {
            ModelPartData RightArm = TheOther.addChild("RightArm", ModelPartBuilder.create().uv(40, 16).cuboid(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new Dilation(0.0F))
                    .uv(40, 32).cuboid(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new Dilation(0.25F)), ModelTransform.pivot(-5.0F, 2.0F, 0.0F));
            ModelPartData LeftArm = TheOther.addChild("LeftArm", ModelPartBuilder.create().uv(32, 48).cuboid(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new Dilation(0.0F))
                    .uv(48, 48).cuboid(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new Dilation(0.25F)), ModelTransform.pivot(5.0F, 2.0F, 0.0F));
        } else {
            ModelPartData RightArm = TheOther.addChild("RightArm", ModelPartBuilder.create().uv(40, 16).cuboid(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new Dilation(0.0F))
                    .uv(40, 32).cuboid(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new Dilation(0.25F)), ModelTransform.pivot(-5.0F, 2.0F, 0.0F));
            ModelPartData LeftArm = TheOther.addChild("LeftArm", ModelPartBuilder.create().uv(32, 48).cuboid(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new Dilation(0.0F))
                    .uv(48, 48).cuboid(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new Dilation(0.25F)), ModelTransform.pivot(5.0F, 2.0F, 0.0F));
        }

        ModelPartData RightLeg = TheOther.addChild("RightLeg", ModelPartBuilder.create().uv(0, 16).cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new Dilation(0.0F))
                .uv(0, 32).cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new Dilation(0.25F)), ModelTransform.pivot(-1.9F, 12.0F, 0.0F));
        ModelPartData LeftLeg = TheOther.addChild("LeftLeg", ModelPartBuilder.create().uv(16, 48).cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new Dilation(0.0F))
                .uv(0, 48).cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new Dilation(0.25F)), ModelTransform.pivot(1.9F, 12.0F, 0.0F));

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
    public void setAngles(TheOtherEntity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
        this.getPart().traverse().forEach(ModelPart::resetTransform);
        this.setHeadAngles(headYaw, headPitch);
        Animation idleAnimation;
        switch (entity.getVariant()) {
            case TWITCHING:
                idleAnimation = TheOtherAnimations.ANIM_THE_OTHER_TWITCH;
                break;
            case DEFAULT:
            default:
                idleAnimation = TheOtherAnimations.ANIM_THE_OTHER_IDLE;
                break;
        }
        this.animateMovement(TheOtherAnimations.ANIM_THE_OTHER_RUN, limbAngle, limbDistance, 1f, 1f);
        this.updateAnimation(entity.idleAnimationState, idleAnimation, animationProgress);
    }

    public ModelPart getPart() {
        return TheOther;
    }
}