package com.pryzmm.splitself.block.entity;

import com.pryzmm.splitself.SplitSelf;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class FrameFileManager {

    private static int nextFrameId = 0;
    private static final Map<Integer, Identifier> LOADED_TEXTURES = new HashMap<>();

    public interface FrameLoadCallback {
        void onLoaded(int imageId, Identifier texture);
    }

    public static void loadFrameImage(File imageFile, FrameLoadCallback callback) {
        try {
            MinecraftClient client = MinecraftClient.getInstance();
            SplitSelf.LOGGER.info("Loading new frame image: {}", imageFile.getAbsolutePath());
            client.execute(() -> {
                try {
                    int id = nextFrameId++;
                    NativeImage nativeImage = NativeImage.read(Files.newInputStream(imageFile.toPath()));
                    Identifier dynamicTextureId = Identifier.of("splitself", "dynamic/image_frame_" + id);
                    NativeImageBackedTexture newTexture = new NativeImageBackedTexture(nativeImage);
                    client.getTextureManager().registerTexture(dynamicTextureId, newTexture);
                    LOADED_TEXTURES.put(id, dynamicTextureId);
                    SplitSelf.LOGGER.info("Successfully loaded frame texture: {}", dynamicTextureId);
                    callback.onLoaded(id, dynamicTextureId);
                } catch (Exception e) {
                    SplitSelf.LOGGER.error("Failed to load new frame texture: {}", e.getMessage());
                }
            });
        } catch (Exception e) {
            SplitSelf.LOGGER.error("Error loading new image frame texture: {}", e.getMessage());
        }
    }

    public static Identifier getTextureForId(int id) {
        return LOADED_TEXTURES.get(id);
    }
}