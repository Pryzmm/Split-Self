package com.pryzmm.splitself.file;

import com.pryzmm.splitself.SplitSelf;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

import java.io.File;
import java.nio.file.Files;

import static com.pryzmm.splitself.events.EventManager.CURRENT_FRAME_TEXTURE;

public class FrameFileManager {
    public static void loadImageToFrame(File imageFile) {
        try {
            MinecraftClient client = MinecraftClient.getInstance();
            SplitSelf.LOGGER.info("Loading image to frame: " + imageFile.getAbsolutePath());

            client.execute(() -> {
                try {
                    NativeImage nativeImage = NativeImage.read(Files.newInputStream(imageFile.toPath()));
                    Identifier dynamicTextureId = Identifier.of("splitself", "dynamic/current_frame_" + System.currentTimeMillis());
                    NativeImageBackedTexture newTexture = new NativeImageBackedTexture(nativeImage);
                    client.getTextureManager().registerTexture(dynamicTextureId, newTexture);
                    CURRENT_FRAME_TEXTURE = dynamicTextureId;
                    SplitSelf.LOGGER.info("Successfully loaded global frame texture: " + dynamicTextureId);
                    SplitSelf.LOGGER.info("CURRENT_FRAME_TEXTURE is now: " + CURRENT_FRAME_TEXTURE);
                } catch (Exception e) {
                    SplitSelf.LOGGER.error("Failed to load global frame texture: " + e.getMessage());
                    e.printStackTrace();
                }
            });

        } catch (Exception e) {
            SplitSelf.LOGGER.error("Error loading image to frame: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
