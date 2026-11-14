package com.pryzmm.splitself.forge;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
/** Pinned model (32x6x0.03 at bone8,24,-8), fullbright texture and translated world-space label. */
public final class UIButtonRenderer extends EntityRenderer<UIButtonEntity>{
    static final ModelLayerLocation LAYER=new ModelLayerLocation(new ResourceLocation("splitself","ui_button"),"main");
    private final ModelPart bone;
    public UIButtonRenderer(EntityRendererProvider.Context context){super(context);bone=context.bakeLayer(LAYER).getChild("bone");}
    static LayerDefinition layer(){var mesh=new MeshDefinition();mesh.getRoot().addOrReplaceChild("bone",CubeListBuilder.create().texOffs(0,0).addBox(-24,-5,-8,32,6,.03f),PartPose.offset(8,24,-8));return LayerDefinition.create(mesh,64,64);}
    @Override public void render(UIButtonEntity entity,float yaw,float delta,PoseStack pose,MultiBufferSource buffers,int light){
        pose.pushPose();pose.mulPose(Axis.YP.rotationDegrees(180-entity.getYRot()));pose.scale(-1,-1,1);pose.translate(0,-1.501,0);
        bone.render(pose,buffers.getBuffer(RenderType.entityCutout(getTextureLocation(entity))),15728880,OverlayTexture.NO_OVERLAY);pose.popPose();
        pose.pushPose();pose.translate(0,.225,1.02);pose.scale(1f/64,-1f/64,1f/64);pose.translate(0,0,-.1);
        Component text=Component.translatable(entity.deletesWorld()?"block.splitself.ui_button.delete":"block.splitself.ui_button.cancel");
        getFont().drawInBatch(text,-getFont().width(text)/2f,0,0xffffffff,false,pose.last().pose(),buffers,net.minecraft.client.gui.Font.DisplayMode.NORMAL,0,15728880);pose.popPose();
        super.render(entity,yaw,delta,pose,buffers,light);
    }
    @Override public ResourceLocation getTextureLocation(UIButtonEntity entity){return new ResourceLocation("splitself","textures/entity/ui_button/ui_button.png");}
}
