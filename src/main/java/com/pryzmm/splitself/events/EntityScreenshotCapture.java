package com.pryzmm.splitself.events;

import com.pryzmm.splitself.SplitSelf;
import com.pryzmm.splitself.SplitSelfClient;
import com.pryzmm.splitself.entity.custom.TheOtherEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import java.io.File;
import java.util.function.Consumer;

public class EntityScreenshotCapture {
    private final MinecraftClient client;

    public EntityScreenshotCapture() {
        this.client = MinecraftClient.getInstance();
    }

    public void captureFromEntity(Entity entity, int width, int height, Consumer<File> callback) {
        if (client.world == null || client.player == null || entity == null) {
            callback.accept(null);
            return;
        }

        // Execute on main thread
        client.execute(() -> {
            // Store original state
            Entity originalCameraEntity = client.getCameraEntity();
            Framebuffer originalFramebuffer = client.getFramebuffer();
            int originalWidth = client.getWindow().getFramebufferWidth();
            int originalHeight = client.getWindow().getFramebufferHeight();

            Framebuffer customFramebuffer = null;

            try {
                float tickDelta = client.getRenderTickCounter().getTickDelta(false);

                // Create custom framebuffer
                customFramebuffer = new SimpleFramebuffer(width, height, true, MinecraftClient.IS_SYSTEM_MAC);
                customFramebuffer.setClearColor(0.0f, 0.0f, 0.0f, 0.0f);

                // Temporarily set the camera entity to our target entity
                client.setCameraEntity(entity);

                // Get the camera and force it to update
                Camera camera = client.gameRenderer.getCamera();
                camera.update(client.world, entity, false, false, tickDelta);

                // Switch to custom framebuffer
                customFramebuffer.beginWrite(true);
                GL11.glViewport(0, 0, width, height);

                // Clear with sky color
                GL11.glClearColor(0.5f, 0.8f, 1.0f, 1.0f);
                GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

                // Set up proper OpenGL state
                GL11.glEnable(GL11.GL_DEPTH_TEST);
                GL11.glDepthFunc(GL11.GL_LEQUAL);
                GL11.glEnable(GL11.GL_CULL_FACE);
                GL11.glCullFace(GL11.GL_BACK);
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

                // Render the world from entity's perspective
                client.gameRenderer.renderWorld(client.getRenderTickCounter());

                // End writing to custom framebuffer
                customFramebuffer.endWrite();

                // Immediately restore the original camera entity
                client.setCameraEntity(originalCameraEntity);

                // Update camera back to original entity
                camera.update(client.world, originalCameraEntity, false, false, tickDelta);

                // Restore original framebuffer
                originalFramebuffer.beginWrite(true);
                GL11.glViewport(0, 0, originalWidth, originalHeight);

                // Generate filename
                String filename = "entity_view_" + entity.getUuidAsString().substring(0, 8) + "_" + System.currentTimeMillis() + ".png";

                // Create screenshots directory
                File screenshotsDir = new File(client.runDirectory, "screenshots");
                if (!screenshotsDir.exists()) {
                    screenshotsDir.mkdirs();
                }

                // Save screenshot
                ScreenshotRecorder.saveScreenshot(
                        client.runDirectory,
                        filename,
                        customFramebuffer,
                        (text) -> {
                            File screenshotFile = new File(screenshotsDir, filename);
                            if (screenshotFile.exists()) {
                                callback.accept(screenshotFile);
                            } else {
                                // Search for most recent entity screenshot
                                File[] files = screenshotsDir.listFiles((dir, name) ->
                                        name.startsWith("entity_view_") && name.endsWith(".png"));

                                if (files != null && files.length > 0) {
                                    File mostRecent = files[0];
                                    for (File file : files) {
                                        if (file.lastModified() > mostRecent.lastModified()) {
                                            mostRecent = file;
                                        }
                                    }
                                    callback.accept(mostRecent);
                                } else {
                                    callback.accept(null);
                                }
                            }
                        }
                );

            } catch (Exception e) {
                e.printStackTrace();
                callback.accept(null);
            } finally {
                try {
                    // Ensure camera entity is restored
                    if (originalCameraEntity != null) {
                        client.setCameraEntity(originalCameraEntity);
                        Camera camera = client.gameRenderer.getCamera();
                        float tickDelta = client.getRenderTickCounter().getTickDelta(false);
                        camera.update(client.world, originalCameraEntity, false, false, tickDelta);
                    }

                    // Restore framebuffer
                    if (originalFramebuffer != null) {
                        originalFramebuffer.beginWrite(true);
                        GL11.glViewport(0, 0, originalWidth, originalHeight);
                    }

                    // Clean up custom framebuffer
                    if (customFramebuffer != null) {
                        customFramebuffer.delete();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void capture(Consumer<File> fileCallback) {
        if (client.world == null || client.player == null) {
            return;
        }

        // Find nearest TheOtherEntity
        TheOtherEntity nearestEntity = client.world.getEntitiesByClass(
                        TheOtherEntity.class,
                        client.player.getBoundingBox().expand(50.0),
                        entity -> entity.isAlive()
                ).stream()
                .min((e1, e2) -> Double.compare(
                        client.player.squaredDistanceTo(e1),
                        client.player.squaredDistanceTo(e2)
                ))
                .orElse(null);

        if (nearestEntity != null) {
            int width = client.getWindow().getFramebufferWidth();
            int height = client.getWindow().getFramebufferHeight();

            // Debug info
            Vec3d entityPos = nearestEntity.getPos();
            float entityYaw = nearestEntity.getYaw();
            float entityPitch = nearestEntity.getPitch();

            SplitSelf.LOGGER.info((String.format("Capturing from TheOtherEntity at %.2f, %.2f, %.2f (yaw: %.1f, pitch: %.1f)",
                    entityPos.x, entityPos.y, entityPos.z, entityYaw, entityPitch)));

            captureFromEntity(nearestEntity, width, height, (file) -> {
                if (file != null && client.player != null) {
                    fileCallback.accept(file);
                }
            });
        } else if (client.player != null) {
            SplitSelf.LOGGER.info("No TheOtherEntity found nearby (within 50 blocks)");
        }
    }
}