package com.pryzmm.splitself.forge;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
final class TheForgottenModel extends HierarchicalModel<TheForgottenEntity> {
    static final ModelLayerLocation LAYER = new ModelLayerLocation(new ResourceLocation("splitself", "the_forgotten"), "main");
    private final ModelPart body, head;
    TheForgottenModel(ModelPart root) { body = root; head = root.getChild("Head"); }
    static LayerDefinition layer() { return createModelData(); }
    private static LayerDefinition createModelData() {
        MeshDefinition modelData = new MeshDefinition();
        PartDefinition modelPartData = modelData.getRoot();

        // Add parts directly to the root instead of to TheForgotten
        PartDefinition Head = modelPartData.addOrReplaceChild("Head", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

//        PartDefinition Head_Distort = Head.addOrReplaceChild("Head_Distort", CubeListBuilder.create()
//                        .texOffs(1, 34).addBox(0.0F, -9.0F, -6.0F, 8.0F, 7.0F, 7.0F, new CubeDeformation(0.0F)),
//                PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition Body = modelPartData.addOrReplaceChild("Body", CubeListBuilder.create()
                        .texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition RightArm = modelPartData.addOrReplaceChild("RightArm", CubeListBuilder.create()
                        .texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-5.0F, 2.0F, 0.0F));

        PartDefinition LeftArm = modelPartData.addOrReplaceChild("LeftArm", CubeListBuilder.create()
                        .texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(5.0F, 2.0F, 0.0F));

        PartDefinition RightLeg = modelPartData.addOrReplaceChild("RightLeg", CubeListBuilder.create()
                        .texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-1.9F, 12.0F, 0.0F));

        PartDefinition LeftLeg = modelPartData.addOrReplaceChild("LeftLeg", CubeListBuilder.create()
                        .texOffs(16, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(1.9F, 12.0F, 0.0F));

        return LayerDefinition.create(modelData, 64, 64);
    }

    @Override public ModelPart root() { return body; }
    @Override public void setupAnim(TheForgottenEntity entity, float swing, float amount, float age, float yaw, float pitch) {
        root().getAllParts().forEach(ModelPart::resetPose);
        head.xRot = Mth.clamp(pitch, -25, 45) * Mth.DEG_TO_RAD;
        head.yRot = Mth.clamp(yaw, -30, 30) * Mth.DEG_TO_RAD;
        animate(entity.idleAnimationState, TheForgottenAnimations.ANIM_THE_FORGOTTEN_IDLE, age);
    }
}
