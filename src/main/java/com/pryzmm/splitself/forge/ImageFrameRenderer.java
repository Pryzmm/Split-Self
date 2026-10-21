package com.pryzmm.splitself.forge;

import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.resources.ResourceLocation;

final class ImageFrameRenderer implements BlockEntityRenderer<ImageFrameBlockEntity>{
    ImageFrameRenderer(BlockEntityRendererProvider.Context context){}
    @Override public void render(ImageFrameBlockEntity frame,float delta,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
        ResourceLocation texture=ClientFrameImages.texture(frame);if(texture==null)texture=new ResourceLocation("splitself","textures/block/image_null.png");
        pose.pushPose();pose.translate(.5,.5,.5);pose.mulPose(Axis.YP.rotationDegrees(FrameImageRules.rotation(frame.getBlockState().getValue(ImageFrameBlock.FACING))));
        pose.translate(-.5,-.5,-.5);pose.translate(2.0/16,4.0/16,15.0/16-.001);pose.scale(12f/16,7f/16,.001f);
        pose.translate(.5,.5,0);pose.mulPose(Axis.YP.rotationDegrees(180));pose.translate(-.5,-.5,0);
        var entry=pose.last();var consumer=buffers.getBuffer(RenderType.entityCutout(texture));
        vertex(consumer,entry,0,0,0,1,light,overlay);vertex(consumer,entry,1,0,1,1,light,overlay);vertex(consumer,entry,1,1,1,0,light,overlay);vertex(consumer,entry,0,1,0,0,light,overlay);pose.popPose();
    }
    private static void vertex(VertexConsumer c,PoseStack.Pose p,float x,float y,float u,float v,int light,int overlay){c.vertex(p.pose(),x,y,0).color(255,255,255,255).uv(u,v).overlayCoords(overlay).uv2(light).normal(p.normal(),0,0,-1).endVertex();}
}
