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

public class FrozenOverlayRenderer {
    public static boolean overlayVisible = false;
    private static final Map<String, Identifier> loadedTextures = new HashMap<>();

    public static void toggleOverlay(File image) {
        overlayVisible = !overlayVisible;
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            if (overlayVisible) {
                renderTopLayerOverlay(drawContext, image);
            }
        });
    }

    public static void renderTopLayerOverlay(DrawContext drawContext, File image) {
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

        // Add overlay content (this includes the image)
        renderOverlayContent(drawContext, screenWidth, screenHeight, image);

        // Restore render states
        RenderSystem.disablePolygonOffset();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();

        matrices.pop();
    }

    public static void renderOverlayContent(DrawContext drawContext, int screenWidth, int screenHeight, File image) {
        // Render the main overlay image with shake and color effect
        renderOverlayImage(drawContext, screenWidth, screenHeight, image, 0, 0, true);
    }

    public static void renderOverlayImage(DrawContext drawContext, int imageWidth, int imageHeight, File image, int offsetX, int offsetY, boolean randomColor) {
        // Enable blending for transparency
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        RenderSystem.setShaderColor(1.0f, (float) (Math.random()/5), (float) (Math.random()/5), 1.0f);

        // Get or load the texture identifier
        Identifier textureId = getOrLoadTexture(image);
        if (textureId != null) {
            // Draw with shake offset
            drawContext.drawTexture(textureId, 0, 0, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);
        }

        // Reset shader color
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();
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