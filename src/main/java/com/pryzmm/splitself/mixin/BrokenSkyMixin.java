package com.pryzmm.splitself.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.pryzmm.splitself.SplitSelf;
import com.pryzmm.splitself.events.FinaleRenderer;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public abstract class BrokenSkyMixin {

    @Shadow
    @Nullable
    private ClientWorld world;

    @Unique
    private static final Identifier BROKEN_SKY_TEXTURE = Identifier.of(SplitSelf.MOD_ID, "textures/misc/error/broken_clouds.png");

    @Unique
    private VertexBuffer skyBuffer;

    @Unique
    private boolean skyBufferBuilt = false;

    @Inject(method = "renderClouds", at = @At(value = "HEAD"), cancellable = true)
    private void stopRenderClouds(MatrixStack matrices, Matrix4f matrix4f, Matrix4f matrix4f2, float tickDelta, double cameraX, double cameraY, double cameraZ, CallbackInfo ci) {
        if (FinaleRenderer.brokenClouds) ci.cancel();
    }

    @Inject(method = "renderSky", at = @At("TAIL"))
    public void renderBrokenSky(Matrix4f matrix4f, Matrix4f projectionMatrix, float tickDelta, Camera camera, boolean thickFog, Runnable fogCallback, CallbackInfo ci) {
        if (!FinaleRenderer.brokenClouds) return;
        assert this.world != null;
        float f = this.world.getDimensionEffects().getCloudsHeight();
        if (Float.isNaN(f)) return;

        if (!this.skyBufferBuilt) {
            this.skyBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
            this.skyBuffer.bind();
            this.skyBuffer.upload(buildTexturedSkyBuffer(Tessellator.getInstance()));
            VertexBuffer.unbind();
            this.skyBufferBuilt = true;
        }

        MatrixStack matrices = new MatrixStack();
        matrices.multiplyPositionMatrix(matrix4f);
        matrices.scale(12.0F, 1.0F, 12.0F);
        matrices.translate(0, f - camera.getPos().getY() - 0.1, 0);

        if (this.skyBuffer != null) {
            RenderSystem.disableCull();
            RenderSystem.setShader(GameRenderer::getPositionTexProgram);
            RenderSystem.setShaderTexture(0, BROKEN_SKY_TEXTURE);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.disableBlend();
            ShaderProgram shaderProgram = RenderSystem.getShader();
            this.skyBuffer.bind();
            this.skyBuffer.draw(matrices.peek().getPositionMatrix(), projectionMatrix, shaderProgram);
            VertexBuffer.unbind();
        }
    }

    @Unique
    private static BuiltBuffer buildTexturedSkyBuffer(Tessellator tessellator) {
        BufferBuilder bufferBuilder = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        float size = 128f;
        bufferBuilder.vertex(-size, 0.0F, -size).texture(0.0F, 0.0F);
        bufferBuilder.vertex(-size, 0.0F, size).texture(0.0F, 1.0F);
        bufferBuilder.vertex(size, 0.0F, size).texture(1.0F, 1.0F);
        bufferBuilder.vertex(size, 0.0F, -size).texture(1.0F, 0.0F);
        return bufferBuilder.end();
    }
}