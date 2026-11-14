package com.pryzmm.splitself.forge;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
final class TheOtherModel extends HierarchicalModel<TheOtherEntity> {
    static final ModelLayerLocation CLASSIC = new ModelLayerLocation(new ResourceLocation("splitself", "the_other"), "main");
    static final ModelLayerLocation SLIM = new ModelLayerLocation(new ResourceLocation("splitself", "the_other_slim"), "main");
    private final ModelPart body, head;
    TheOtherModel(ModelPart root) { body = root.getChild("TheOther"); head = body.getChild("Head"); }
    static LayerDefinition layer(boolean slim) { return createModelData(slim); }
    private static LayerDefinition createModelData(boolean slim) {
        MeshDefinition modelData = new MeshDefinition();
        PartDefinition modelPartData = modelData.getRoot();
        PartDefinition TheOther = modelPartData.addOrReplaceChild("TheOther", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition Head = TheOther.addOrReplaceChild("Head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition Body = TheOther.addOrReplaceChild("Body", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(16, 32).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        if (slim) {
            PartDefinition RightArm = TheOther.addOrReplaceChild("RightArm", CubeListBuilder.create().texOffs(40, 16).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                    .texOffs(40, 32).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(-5.0F, 2.0F, 0.0F));
            PartDefinition LeftArm = TheOther.addOrReplaceChild("LeftArm", CubeListBuilder.create().texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                    .texOffs(48, 48).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(5.0F, 2.0F, 0.0F));
        } else {
            PartDefinition RightArm = TheOther.addOrReplaceChild("RightArm", CubeListBuilder.create().texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                    .texOffs(40, 32).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(-5.0F, 2.0F, 0.0F));
            PartDefinition LeftArm = TheOther.addOrReplaceChild("LeftArm", CubeListBuilder.create().texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                    .texOffs(48, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(5.0F, 2.0F, 0.0F));
        }

        PartDefinition RightLeg = TheOther.addOrReplaceChild("RightLeg", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(0, 32).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(-1.9F, 12.0F, 0.0F));
        PartDefinition LeftLeg = TheOther.addOrReplaceChild("LeftLeg", CubeListBuilder.create().texOffs(16, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(0, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(1.9F, 12.0F, 0.0F));

        return LayerDefinition.create(modelData, 64, 64);
    }

    @Override public ModelPart root() { return body; }
    @Override public void setupAnim(TheOtherEntity entity, float swing, float amount, float age, float yaw, float pitch) {
        root().getAllParts().forEach(ModelPart::resetPose);
        head.xRot = Mth.clamp(pitch, -25, 45) * Mth.DEG_TO_RAD;
        head.yRot = Mth.clamp(yaw, -30, 30) * Mth.DEG_TO_RAD;
        animateWalk(TheOtherAnimations.ANIM_THE_OTHER_RUN, swing, amount, 1, 1);
        animate(entity.idleAnimationState, entity.variant() == 1 ? TheOtherAnimations.ANIM_THE_OTHER_TWITCH : TheOtherAnimations.ANIM_THE_OTHER_IDLE, age);
    }
}
