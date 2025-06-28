// i know theres like 5 different renderers, ill fix it in a future update trust </3
// i'm too lazy rn

package com.pryzmm.splitself.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.pryzmm.splitself.SplitSelf;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class FaceOverlayRenderer {
    public static boolean overlayVisible = false;
    private static final Map<String, Identifier> loadedTextures = new HashMap<>();

    private static final Identifier FACE_IMAGE_TEXTURE = Identifier.of("splitself", "textures/misc/face.png");

    public static void toggleOverlay(File image, Float red, Float green, Float blue, Float alpha, int imageWidth, int imageHeight) {
        overlayVisible = !overlayVisible;
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            if (overlayVisible) {
                renderTopLayerOverlay(drawContext, image, red, green, blue, alpha, imageWidth, imageHeight);
            }
        });
    }

    public static void renderTopLayerOverlay(DrawContext drawContext, File image, Float red, Float green, Float blue, Float alpha, int faceImageWidth, int faceImageHeight) {
        MinecraftClient client = MinecraftClient.getInstance();
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();

        // Push a new matrix to ensure we're rendering at the top level
        MatrixStack matrices = drawContext.getMatrices();
        matrices.push();

        // Translate to ensure we're at the front-most z-level
        matrices.translate(0, 0, 1000);

        // Enable blending for transparency
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // Disable depth testing to ensure it renders over everything
        RenderSystem.disableDepthTest();

        // Set a high z-offset to render above everything
        RenderSystem.polygonOffset(-1.0f, -1.0f);
        RenderSystem.enablePolygonOffset();

        // IMPORTANT: Comment out or remove the black fill to see the image
        // drawContext.fill(0, 0, screenWidth, screenHeight, 0xFF000000);

        // Instead, use a semi-transparent fill if you want a background
        drawContext.fill(0, 0, screenWidth, screenHeight, 0x80000000);

        // Calculate center position for the face image
        int centerX = (screenWidth - faceImageWidth) / 2;
        int centerY = (screenHeight - faceImageHeight) / 2;

        // Add overlay content (this includes the image)
        renderOverlayContent(drawContext, screenWidth, screenHeight, image, "File", red, green, blue, alpha);

        // Render the face image centered
        renderOverlayContent(drawContext, centerX, centerY, faceImageWidth, faceImageHeight, FACE_IMAGE_TEXTURE, "Identifier", 1.0f, 1.0f, 1.0f, 1.0f);

        // Restore render states
        RenderSystem.disablePolygonOffset();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();

        matrices.pop();
    }

    // Updated method signature to include position parameters
    public static void renderOverlayContent(DrawContext drawContext, int x, int y, int imageWidth, int imageHeight, Object image, String variableInstance, Float red, Float green, Float blue, Float alpha) {
        // Enable blending for transparency
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        RenderSystem.setShaderColor(red, green, blue, alpha);

        // Get or load the texture identifier
        Identifier textureId;
        if (Objects.equals(variableInstance, "File")) {
            textureId = getOrLoadTexture((File) image);
        } else if (Objects.equals(variableInstance, "Identifier")) {
            textureId = (Identifier) image;
        } else {
            textureId = null;
        }
        if (textureId != null) {
            drawContext.drawTexture(textureId, x, y, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);
        }

        // Reset shader color
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();
    }

    // Overloaded method for backward compatibility
    public static void renderOverlayContent(DrawContext drawContext, int imageWidth, int imageHeight, Object image, String variableInstance, Float red, Float green, Float blue, Float alpha) {
        renderOverlayContent(drawContext, 0, 0, imageWidth, imageHeight, image, variableInstance, red, green, blue, alpha);
    }

    public static Identifier getOrLoadTexture(File file) {
        String filePath = file.getAbsolutePath();

        // Check if we've already loaded this texture
        if (loadedTextures.containsKey(filePath)) {
            return loadedTextures.get(filePath);
        }

        try {
            // Load the image file
            FileInputStream fileInputStream = new FileInputStream(file);
            NativeImage nativeImage = NativeImage.read(fileInputStream);
            fileInputStream.close();

            // Create a texture from the image
            NativeImageBackedTexture texture = new NativeImageBackedTexture(nativeImage);

            // Generate a safe identifier
            String filename = file.getName();
            String nameWithoutExtension = filename.replaceAll("\\.[^.]*$", "");
            String cleanName = nameWithoutExtension.toLowerCase()
                    .replaceAll("[^a-z0-9._-]", "_");

            // Ensure it doesn't start with invalid characters
            if (!cleanName.matches("^[a-z0-9_].*")) {
                cleanName = "dynamic_" + cleanName;
            }

            // Create unique identifier to avoid conflicts
            String uniqueName = "dynamic/" + cleanName + "_" + System.currentTimeMillis();
            Identifier textureId = Identifier.of(SplitSelf.MOD_ID, uniqueName);

            // Register the texture with Minecraft's texture manager
            MinecraftClient.getInstance().getTextureManager().registerTexture(textureId, texture);

            // Cache the identifier
            loadedTextures.put(filePath, textureId);

            return textureId;

        } catch (IOException e) {
            System.err.println("Failed to load texture from file: " + file.getAbsolutePath());
            e.printStackTrace();
            return null;
        }
    }
}