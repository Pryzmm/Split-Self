package com.pryzmm.splitself.block.entity;

import com.pryzmm.splitself.SplitSelf;
import com.pryzmm.splitself.file.EntityScreenshotCapture;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import java.io.File;
import java.nio.file.Path;
import java.util.Random;

public class ImageFrameBlockEntity extends BlockEntity {
    private Integer imageId = null;
    private Identifier imageTexture;

    public ImageFrameBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.IMAGE_FRAME_BLOCK_ENTITY, pos, state);
    }

    public Integer getImageId() {
        return imageId;
    }

    public Identifier getImageTexture() {
        if (imageId == null) {
            imageId = -1;
            loadNextImage();
            return null;
        }
        if (imageTexture == null && imageId != -1) imageTexture = FrameFileManager.getTextureForId(imageId);
        return imageTexture;
    }

    public void assignNewImage(File imageFile) {
        FrameFileManager.loadFrameImage(imageFile, (id, texture) -> {
            this.imageId = id;
            this.imageTexture = texture;
            markDirty();
        });
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        super.writeNbt(nbt, lookup);
        if (imageId != null) nbt.putInt("ImageId", imageId);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        super.readNbt(nbt, lookup);
        if (nbt.contains("ImageId")) this.imageId = nbt.getInt("ImageId");
    }

    public void loadNextImage() {
        boolean takeScreenshot = false;
        Path defaultScreenshotsFolder = Path.of(System.getenv("APPDATA") + "\\.minecraft\\screenshots");
        try {
            Random random = new Random();
            File[] screenshotFiles = defaultScreenshotsFolder.toFile().listFiles((dir, name) ->
                    name.toLowerCase().endsWith(".png") || name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".jpeg"));
            if (screenshotFiles != null && screenshotFiles.length > 0) {
                File randomScreenshot = screenshotFiles[random.nextInt(screenshotFiles.length)];
                assignNewImage(randomScreenshot);
            } else throw new RuntimeException("Screenshot folder is null or empty! " + defaultScreenshotsFolder.toAbsolutePath());
        } catch (Exception e) {
            SplitSelf.LOGGER.warn("Failed to access screenshot folder of default Minecraft directory: {}", e.getMessage());
            File screenshotsDir = new File(MinecraftClient.getInstance().runDirectory, "screenshots");
            if (screenshotsDir.exists() && screenshotsDir.isDirectory()) {
                File[] screenshotFiles = screenshotsDir.listFiles((dir, name) ->
                        name.toLowerCase().endsWith(".png") || name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".jpeg"));
                if (screenshotFiles != null && screenshotFiles.length > 0) {
                    Random random = new Random();
                    File randomScreenshot = screenshotFiles[random.nextInt(screenshotFiles.length)];
                    try {
                        assignNewImage(randomScreenshot);
                        SplitSelf.LOGGER.info("Loaded random screenshot to frame: {}", randomScreenshot.getName());
                    } catch (Exception e2) {
                        SplitSelf.LOGGER.error("Failed to load random screenshot to frame: {} {}", e2.getMessage(), e2);
                    }
                } else takeScreenshot = true;
            } else takeScreenshot = true;
        }
        if (takeScreenshot) {
            SplitSelf.LOGGER.warn("Screenshots directory does not exist, taking a new screenshot instead");
            new Thread(() -> MinecraftClient.getInstance().execute(() -> {
                EntityScreenshotCapture capture = new EntityScreenshotCapture();
                capture.capture((file) -> {
                    if (file != null) {
                        try { assignNewImage(file); }
                        catch (Exception e2) { SplitSelf.LOGGER.error("Failed to load image to frame: {} {}", e2.getMessage(), e2); }
                    } else SplitSelf.LOGGER.error("Could not get file!");
                });
            })).start();
        }
    }

}
