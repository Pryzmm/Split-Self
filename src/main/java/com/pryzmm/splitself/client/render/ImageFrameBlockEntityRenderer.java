package com.pryzmm.splitself.client.render;

import com.pryzmm.splitself.SplitSelf;
import com.pryzmm.splitself.block.ImageFrameBlock;
import com.pryzmm.splitself.block.entity.ImageFrameBlockEntity;
import com.pryzmm.splitself.events.EventManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;

public class ImageFrameBlockEntityRenderer implements BlockEntityRenderer<ImageFrameBlockEntity> {
    public ImageFrameBlockEntityRenderer(BlockEntityRendererFactory.Context context) {}

    @Override
    public void render(ImageFrameBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        matrices.push();
        Identifier imageTexture = EventManager.CURRENT_FRAME_TEXTURE;
        if (imageTexture == null || !entity.hasWorld()) {
            imageTexture = Identifier.of(SplitSelf.MOD_ID, "textures/block/image_null.png");
        }
        Direction facing = entity.getCachedState().get(ImageFrameBlock.FACING);
        matrices.translate(0.5, 0.5, 0.5);
        switch (facing) {
            case NORTH -> matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(0));
            case SOUTH -> matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));
            case EAST -> matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(270));
            case WEST -> matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90));
            case UP -> matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90));
            case DOWN -> matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90));
        }
        matrices.translate(-0.5, -0.5, -0.5);
        matrices.translate(2.0/16.0, 4.0/16.0, 15.0/16.0 - 0.001);
        matrices.scale(12.0f/16.0f, 7.0f/16.0f, 0.001f);
        matrices.translate(0.5f, 0.5f, 0.0f);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));
        matrices.translate(-0.5f, -0.5f, 0.0f);
        Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
        MatrixStack.Entry matrixEntry = matrices.peek();
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityCutout(imageTexture));
        vertexConsumer.vertex(positionMatrix, 0, 0, 0).color(255, 255, 255, 255).texture(0, 1).overlay(overlay).light(light).normal(matrixEntry, 0.0f, 0.0f, -1.0f);
        vertexConsumer.vertex(positionMatrix, 1, 0, 0).color(255, 255, 255, 255).texture(1, 1).overlay(overlay).light(light).normal(matrixEntry, 0.0f, 0.0f, -1.0f);
        vertexConsumer.vertex(positionMatrix, 1, 1, 0).color(255, 255, 255, 255).texture(1, 0).overlay(overlay).light(light).normal(matrixEntry, 0.0f, 0.0f, -1.0f);
        vertexConsumer.vertex(positionMatrix, 0, 1, 0).color(255, 255, 255, 255).texture(0, 0).overlay(overlay).light(light).normal(matrixEntry, 0.0f, 0.0f, -1.0f);
        matrices.pop();
    }
}
