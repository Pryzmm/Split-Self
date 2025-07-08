package com.pryzmm.splitself.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.pryzmm.splitself.events.ScreenOverlay;
import com.pryzmm.splitself.file.EntityScreenshotCapture;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class SkyImageRenderer {

    private static final Identifier FACE_IMAGE_TEXTURE = Identifier.of("splitself", "textures/misc/face.png");

    private static Boolean ToggledTexture = false;

    private static float ImageX = 4;
    private static float ImageY = 6;

    private static final Vector3f IMAGE_RELATIVE_POS = new Vector3f(10, 50, -100);

    public static void register() {
        WorldRenderEvents.AFTER_TRANSLUCENT.register(SkyImageRenderer::renderSkyImage);
    }

    public static void toggleTexture() {
        ToggledTexture = !ToggledTexture;
        ImageX = 4;
        ImageY = 6;
    }

    private static void renderSkyImage(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!ToggledTexture) return;
        if (client.world == null || client.player == null) return;

        MatrixStack matrices = getMatrixStack();

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(770, 771); // GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA

        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderTexture(0, FACE_IMAGE_TEXTURE);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        Matrix4f matrix = matrices.peek().getPositionMatrix();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);

        buffer.vertex(matrix, -1, -1, 0).texture(0, 1);
        buffer.vertex(matrix, 1, -1, 0).texture(1, 1);
        buffer.vertex(matrix, 1, 1, 0).texture(1, 0);
        buffer.vertex(matrix, -1, 1, 0).texture(0, 0);

        BufferRenderer.drawWithGlobalProgram(buffer.end());

        if (isPlayerLookingAtImage(context)) {
            ImageX = (float) (ImageX + 0.006);
            ImageY = (float) (ImageY + 0.009);
            if (ImageX >= 10) {
                toggleTexture();
                new Thread(() -> client.execute(() -> {
                    PlayerEntity Player = context.world().getPlayerByUuid(client.player.getUuid());
                    EntityScreenshotCapture capture = new EntityScreenshotCapture();
                    capture.captureFromEntity(Player, client.getWindow().getScaledWidth(), client.getWindow().getScaledHeight(), (file) -> ScreenOverlay.executeFaceScreen(file, Player, Player));
                })).start();
            }
        }
        RenderSystem.disableBlend();

        matrices.pop();
    }

    private static @NotNull MatrixStack getMatrixStack() {
        MatrixStack matrices = new MatrixStack();
        matrices.push();

        matrices.translate(IMAGE_RELATIVE_POS.x, IMAGE_RELATIVE_POS.y, IMAGE_RELATIVE_POS.z);
        matrices.scale(ImageX, ImageY, 1);

        return matrices;
    }

    private static boolean isPlayerLookingAtImage(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return false;
        Vec3d playerLook = client.player.getRotationVec(context.tickCounter().getTickDelta(true));
        Vec3d imageDirection = new Vec3d(IMAGE_RELATIVE_POS.x, IMAGE_RELATIVE_POS.y, IMAGE_RELATIVE_POS.z).normalize();
        double dotProduct = playerLook.dotProduct(imageDirection);
        double angleInDegrees = Math.toDegrees(Math.acos(Math.max(-1.0, Math.min(1.0, dotProduct))));
        return angleInDegrees < 4.0;
    }
}