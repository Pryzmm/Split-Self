package com.pryzmm.splitself.file;

import com.pryzmm.splitself.SplitSelf;
import com.pryzmm.splitself.entity.custom.TheOtherEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.Consumer;

public class EntityScreenshotCapture {
    private final MinecraftClient client;
    private WorldRenderer worldRenderer;
    private Method renderWorldMethod;
    private boolean reflectionInitialized = false;

    public EntityScreenshotCapture() {
        this.client = MinecraftClient.getInstance();
        initializeReflection();
    }

    private void initializeReflection() {
        try {
            // Get the WorldRenderer instance
            worldRenderer = client.worldRenderer;

            if (worldRenderer == null) {
                Field worldRendererField = MinecraftClient.class.getDeclaredField("worldRenderer");
                worldRendererField.setAccessible(true);
                worldRenderer = (WorldRenderer) worldRendererField.get(client);
            }

            Class<?> worldRendererClass = worldRenderer.getClass();
            Method[] methods = worldRendererClass.getDeclaredMethods();

            // Look for the main render method in WorldRenderer
            // In 1.21.1, it should be: render(RenderTickCounter, boolean, Camera, GameRenderer, LightmapTextureManager, Matrix4f, Matrix4f)
            for (Method method : methods) {
                String methodName = method.getName();
                Class<?>[] paramTypes = method.getParameterTypes();

                if (methodName.equals("render")) {
                    // Check for the correct signature
                    if (paramTypes.length == 7 &&
                            paramTypes[0].getSimpleName().equals("RenderTickCounter") &&
                            paramTypes[1] == boolean.class &&
                            paramTypes[2].getSimpleName().equals("Camera") &&
                            paramTypes[3].getSimpleName().equals("GameRenderer") &&
                            paramTypes[4].getSimpleName().equals("LightmapTextureManager") &&
                            paramTypes[5].getSimpleName().equals("Matrix4f") &&
                            paramTypes[6].getSimpleName().equals("Matrix4f")) {

                        method.setAccessible(true);
                        renderWorldMethod = method;
                        SplitSelf.LOGGER.info("Found correct WorldRenderer.render method with 7 parameters");
                        break;
                    }
                }
            }

            if (renderWorldMethod != null) {
                reflectionInitialized = true;
                SplitSelf.LOGGER.info("WorldRenderer reflection initialized successfully");
            } else {
                SplitSelf.LOGGER.warn("Could not find correct WorldRenderer.render method");
                // Log available render methods for debugging
                for (Method method : methods) {
                    if (method.getName().equals("render")) {
                        SplitSelf.LOGGER.info("Available render method: " + method.toString());
                    }
                }
            }

        } catch (Exception e) {
            SplitSelf.LOGGER.error("Failed to initialize WorldRenderer reflection", e);
        }
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
            boolean originalPerspective = client.options.getPerspective().isFirstPerson();

            Framebuffer customFramebuffer = null;

            try {
                RenderTickCounter tickCounter = client.getRenderTickCounter();
                float tickDelta = tickCounter.getTickDelta(false);

                // Create custom framebuffer
                customFramebuffer = new SimpleFramebuffer(width, height, true, MinecraftClient.IS_SYSTEM_MAC);
                customFramebuffer.setClearColor(0.0f, 0.0f, 0.0f, 0.0f);

                // Force first person perspective
                client.options.setPerspective(net.minecraft.client.option.Perspective.FIRST_PERSON);

                // Set the camera entity to our target entity
                client.setCameraEntity(entity);

                // Update the camera
                Camera camera = client.gameRenderer.getCamera();
                camera.update(client.world, entity, false, false, tickDelta);

                // Switch to custom framebuffer
                customFramebuffer.beginWrite(true);
                GL11.glViewport(0, 0, width, height);

                // Clear the framebuffer
                GL11.glClearColor(0.5f, 0.8f, 1.0f, 1.0f);
                GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

                GameRenderer gameRenderer = client.gameRenderer;

                // Set up projection matrix
                Matrix4f projectionMatrix = gameRenderer.getBasicProjectionMatrix(tickDelta);
                gameRenderer.loadProjectionMatrix(projectionMatrix);

                // Try different rendering approaches
                boolean renderingSuccessful = false;

                // Method 1: Use the complete render pipeline with proper setup
                try {
                    SplitSelf.LOGGER.info("Attempting complete render pipeline");

                    // Store current framebuffer
                    Framebuffer originalFb = client.getFramebuffer();

                    // Temporarily set our framebuffer as the main one
                    try {
                        Field framebufferField = MinecraftClient.class.getDeclaredField("framebuffer");
                        framebufferField.setAccessible(true);
                        framebufferField.set(client, customFramebuffer);
                    } catch (Exception e) {
                        // If we can't set it via reflection, try the public method
                        SplitSelf.LOGGER.warn("Could not set framebuffer via reflection: " + e.getMessage());
                    }

                    // Setup OpenGL state
                    GL11.glEnable(GL11.GL_DEPTH_TEST);
                    GL11.glDepthFunc(GL11.GL_LEQUAL);
                    GL11.glEnable(GL11.GL_CULL_FACE);
                    GL11.glCullFace(GL11.GL_BACK);

                    // Clear and prepare for rendering
                    GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

                    // Setup matrices properly
                    gameRenderer.loadProjectionMatrix(projectionMatrix);

                    // Get lightmap texture manager
                    LightmapTextureManager lightmapTextureManager = gameRenderer.getLightmapTextureManager();
                    lightmapTextureManager.update(tickDelta);

                    // Render using GameRenderer's renderWorld method
                    gameRenderer.renderWorld(tickCounter);

                    // Let the normal rendering handle entities - they should be included in renderWorld
                    // The renderWorld method should already include entity rendering
                    SplitSelf.LOGGER.info("Using built-in entity rendering from renderWorld");

                    // Restore original framebuffer
                    try {
                        Field framebufferField = MinecraftClient.class.getDeclaredField("framebuffer");
                        framebufferField.setAccessible(true);
                        framebufferField.set(client, originalFb);
                    } catch (Exception e) {
                        SplitSelf.LOGGER.warn("Could not restore framebuffer: " + e.getMessage());
                    }

                    renderingSuccessful = true;
                    SplitSelf.LOGGER.info("Successfully used complete render pipeline");

                } catch (Exception e) {
                    SplitSelf.LOGGER.warn("Complete render pipeline failed: " + e.getMessage());
                    e.printStackTrace();
                }

                // Method 2: Try WorldRenderer with proper matrix setup
                if (!renderingSuccessful && reflectionInitialized && renderWorldMethod != null && worldRenderer != null) {
                    try {
                        SplitSelf.LOGGER.info("Attempting WorldRenderer with proper setup");

                        // Setup proper rendering state
                        GL11.glEnable(GL11.GL_DEPTH_TEST);
                        GL11.glDepthFunc(GL11.GL_LEQUAL);
                        GL11.glEnable(GL11.GL_CULL_FACE);

                        // Get lightmap texture manager and update it
                        LightmapTextureManager lightmapTextureManager = gameRenderer.getLightmapTextureManager();
                        lightmapTextureManager.update(tickDelta);

                        // Create proper view matrix from camera
                        Matrix4f viewMatrix = new Matrix4f();
                        Vec3d cameraPos = camera.getPos();
                        viewMatrix.identity()
                                .rotateX((float) Math.toRadians(camera.getPitch()))
                                .rotateY((float) Math.toRadians(camera.getYaw() + 180.0f))
                                .translate((float) -cameraPos.x, (float) -cameraPos.y, (float) -cameraPos.z);

                        // Call WorldRenderer with proper parameters
                        renderWorldMethod.invoke(worldRenderer,
                                tickCounter,           // RenderTickCounter
                                false,                 // boolean renderBlockOutline
                                camera,                // Camera
                                gameRenderer,          // GameRenderer
                                lightmapTextureManager, // LightmapTextureManager
                                projectionMatrix,      // Matrix4f projection
                                viewMatrix             // Matrix4f view
                        );

                        renderingSuccessful = true;
                        SplitSelf.LOGGER.info("Successfully rendered using WorldRenderer");

                    } catch (Exception e) {
                        SplitSelf.LOGGER.warn("WorldRenderer reflection failed: " + e.getMessage());
                        e.printStackTrace();
                    }
                }

                // Method 3: Manual rendering approach
                if (!renderingSuccessful) {
                    SplitSelf.LOGGER.info("Using manual rendering approach");

                    try {
                        // Setup OpenGL state
                        GL11.glEnable(GL11.GL_DEPTH_TEST);
                        GL11.glDepthFunc(GL11.GL_LEQUAL);
                        GL11.glEnable(GL11.GL_CULL_FACE);
                        GL11.glCullFace(GL11.GL_BACK);

                        // Clear buffers
                        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

                        // Setup camera and matrices
                        MatrixStack matrixStack = new MatrixStack();

                        // Apply camera transformation
                        Vec3d cameraPos = camera.getPos();
                        matrixStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
                        matrixStack.multiply(camera.getRotation());

                        // Try to render using the world renderer's internal methods
                        if (worldRenderer != null) {
                            // Force frustum update
                            try {
                                Method setupMethod = worldRenderer.getClass().getDeclaredMethod("setupTerrain", Camera.class, net.minecraft.client.render.Frustum.class, boolean.class, boolean.class);
                                setupMethod.setAccessible(true);
                                setupMethod.invoke(worldRenderer, camera, null, false, client.player.isSpectator());
                            } catch (Exception e) {
                                SplitSelf.LOGGER.warn("Could not setup terrain: " + e.getMessage());
                            }

                            // Try to render blocks
                            try {
                                Method renderBlocksMethod = worldRenderer.getClass().getDeclaredMethod("renderBlocks", MatrixStack.class);
                                if (renderBlocksMethod != null) {
                                    renderBlocksMethod.setAccessible(true);
                                    renderBlocksMethod.invoke(worldRenderer, matrixStack);
                                }
                            } catch (Exception e) {
                                // Method might not exist or have different signature
                                SplitSelf.LOGGER.warn("Could not render blocks directly: " + e.getMessage());
                            }
                        }

                        SplitSelf.LOGGER.info("Successfully used manual rendering approach");

                    } catch (Exception e) {
                        SplitSelf.LOGGER.warn("Manual rendering approach failed: " + e.getMessage());
                    }
                }

                // Flush OpenGL commands
                GL11.glFlush();

                // End writing to custom framebuffer
                customFramebuffer.endWrite();

                // Restore original state immediately
                client.setCameraEntity(originalCameraEntity);
                if (originalPerspective) {
                    client.options.setPerspective(net.minecraft.client.option.Perspective.FIRST_PERSON);
                } else {
                    client.options.setPerspective(net.minecraft.client.option.Perspective.THIRD_PERSON_BACK);
                }

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
                                SplitSelf.LOGGER.info("Screenshot saved: " + screenshotFile.getAbsolutePath());
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
                                    SplitSelf.LOGGER.info("Found recent screenshot: " + mostRecent.getAbsolutePath());
                                    callback.accept(mostRecent);
                                } else {
                                    SplitSelf.LOGGER.warn("No screenshot files found");
                                    callback.accept(null);
                                }
                            }
                        }
                );

            } catch (Exception e) {
                SplitSelf.LOGGER.error("Error during screenshot capture", e);
                callback.accept(null);
            } finally {
                try {
                    // Restore original state
                    if (originalPerspective) {
                        client.options.setPerspective(net.minecraft.client.option.Perspective.FIRST_PERSON);
                    } else {
                        client.options.setPerspective(net.minecraft.client.option.Perspective.THIRD_PERSON_BACK);
                    }

                    if (originalCameraEntity != null) {
                        client.setCameraEntity(originalCameraEntity);
                        Camera camera = client.gameRenderer.getCamera();
                        float tickDelta = client.getRenderTickCounter().getTickDelta(false);
                        camera.update(client.world, originalCameraEntity, false, false, tickDelta);
                    }

                    if (originalFramebuffer != null) {
                        originalFramebuffer.beginWrite(true);
                        GL11.glViewport(0, 0, originalWidth, originalHeight);
                    }

                    if (customFramebuffer != null) {
                        customFramebuffer.delete();
                    }

                } catch (Exception e) {
                    SplitSelf.LOGGER.error("Error during cleanup", e);
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
                        client.player.getBoundingBox().expand(100.0),
                        LivingEntity::isAlive
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

            SplitSelf.LOGGER.info(String.format("Capturing from TheOtherEntity at %.2f, %.2f, %.2f (yaw: %.1f, pitch: %.1f)",
                    entityPos.x, entityPos.y, entityPos.z, entityYaw, entityPitch));

            captureFromEntity(nearestEntity, width, height, (file) -> {
                if (file != null && client.player != null) {
                    fileCallback.accept(file);
                }
            });
        } else if (client.player != null) {
            SplitSelf.LOGGER.info("No TheOtherEntity found nearby (within 100 blocks)");
        }
    }
}