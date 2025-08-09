package com.pryzmm.splitself.file;

import com.pryzmm.splitself.SplitSelf;
import com.pryzmm.splitself.entity.custom.TheOtherEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.ScreenshotRecorder;
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
    private boolean isIrisPresent = false;
    private Object irisApi = null;

    public EntityScreenshotCapture() {
        this.client = MinecraftClient.getInstance();
        checkForIris();
        initializeReflection();
    }

    private void checkForIris() {
        try {
            Class.forName("net.irisshaders.iris.Iris");
            isIrisPresent = true;
            SplitSelf.LOGGER.info("IRIS shader mod detected - using compatibility mode");
            tryInitializeIrisApi();
        } catch (ClassNotFoundException e) {
            isIrisPresent = false;
            SplitSelf.LOGGER.info("IRIS not detected - using standard rendering");
        }
    }

    private void tryInitializeIrisApi() {
        try {
            Class<?> irisApiClass = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
            Method getInstanceMethod = irisApiClass.getMethod("getInstance");
            irisApi = getInstanceMethod.invoke(null);
            SplitSelf.LOGGER.info("IRIS API initialized successfully");
        } catch (Exception e) {
            SplitSelf.LOGGER.warn("Could not initialize IRIS API: " + e.getMessage());
        }
    }

    private void initializeReflection() {
        try {
            worldRenderer = client.worldRenderer;

            if (worldRenderer == null) {
                Field worldRendererField = MinecraftClient.class.getDeclaredField("worldRenderer");
                worldRendererField.setAccessible(true);
                worldRenderer = (WorldRenderer) worldRendererField.get(client);
            }
            Class<?> worldRendererClass = worldRenderer.getClass();
            Method[] methods = worldRendererClass.getDeclaredMethods();
            for (Method method : methods) {
                String methodName = method.getName();
                Class<?>[] paramTypes = method.getParameterTypes();
                if (methodName.equals("render")) {
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
                SplitSelf.LOGGER.info("WorldRenderer reflection initialized successfully");
            } else {
                SplitSelf.LOGGER.warn("Could not find correct WorldRenderer.render method");
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
        client.execute(() -> {
            if (isIrisPresent) {
                captureWithIrisCompatibility(entity, width, height, callback);
            } else {
                captureStandard(entity, width, height, callback);
            }
        });
    }

    private void captureWithIrisCompatibility(Entity entity, int width, int height, Consumer<File> callback) {
        SplitSelf.LOGGER.info("Using IRIS-compatible capture method");
        try {
            if (irisApi != null) {
                Method isShaderPackInUseMethod = irisApi.getClass().getMethod("isShaderPackInUse");
                Boolean shadersActive = (Boolean) isShaderPackInUseMethod.invoke(irisApi);

                if (shadersActive) {
                    Method disableShaderPackMethod = irisApi.getClass().getMethod("reload");
                    SplitSelf.LOGGER.info("Attempting to temporarily work around IRIS shaders");
                }
            }
        } catch (Exception e) {
            SplitSelf.LOGGER.debug("Could not manage IRIS shaders: " + e.getMessage());
        }
        try {
            captureUsingMainFramebuffer(entity, width, height, callback);
        } catch (Exception e) {
            SplitSelf.LOGGER.warn("Main framebuffer approach failed: " + e.getMessage());
            captureWithMinimalRendering(entity, width, height, callback);
        }
    }

    private void captureUsingMainFramebuffer(Entity entity, int width, int height, Consumer<File> callback) {
        Framebuffer originalFramebuffer = client.getFramebuffer();
        var originalPerspective = client.options.getPerspective();
        Entity originalCameraEntity = client.getCameraEntity();
        int[] viewport = new int[4];
        GL11.glGetIntegerv(GL11.GL_VIEWPORT, viewport);
        try {
            client.options.setPerspective(net.minecraft.client.option.Perspective.THIRD_PERSON_BACK);
            client.setCameraEntity(entity);
            try { Thread.sleep(50); } catch (InterruptedException ignored) {}
            Camera camera = client.gameRenderer.getCamera();
            float tickDelta = client.getRenderTickCounter().getTickDelta(false);
            camera.update(client.world, entity, false, false, tickDelta);
            originalFramebuffer.resize(width, height, MinecraftClient.IS_SYSTEM_MAC);
            originalFramebuffer.beginWrite(true);
            GL11.glViewport(0, 0, width, height);
            GL11.glClearColor(0.5f, 0.8f, 1.0f, 1.0f);
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
            GameRenderer gameRenderer = client.gameRenderer;
            gameRenderer.renderWorld(client.getRenderTickCounter());
            GL11.glFlush();
            originalFramebuffer.endWrite();
            saveScreenshot(originalFramebuffer, entity, callback);
        } catch (Exception e) {
            SplitSelf.LOGGER.error("Main framebuffer capture failed", e);
            callback.accept(null);
        } finally {
            try {
                client.setCameraEntity(originalCameraEntity);
                client.options.setPerspective(originalPerspective);
                originalFramebuffer.resize(
                        client.getWindow().getFramebufferWidth(),
                        client.getWindow().getFramebufferHeight(),
                        MinecraftClient.IS_SYSTEM_MAC
                );
                originalFramebuffer.beginWrite(true);
                GL11.glViewport(viewport[0], viewport[1], viewport[2], viewport[3]);

            } catch (Exception e) {
                SplitSelf.LOGGER.error("Cleanup failed", e);
            }
        }
    }

    private void captureWithMinimalRendering(Entity entity, int width, int height, Consumer<File> callback) {
        SplitSelf.LOGGER.info("Using minimal rendering approach for IRIS compatibility");

        Framebuffer customFramebuffer = null;
        var originalPerspective = client.options.getPerspective();
        Entity originalCameraEntity = client.getCameraEntity();

        try {
            customFramebuffer = new SimpleFramebuffer(width, height, true, MinecraftClient.IS_SYSTEM_MAC);
            customFramebuffer.setClearColor(0.5f, 0.8f, 1.0f, 1.0f);
            customFramebuffer.beginWrite(true);
            GL11.glViewport(0, 0, width, height);
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
            client.options.setPerspective(net.minecraft.client.option.Perspective.THIRD_PERSON_BACK);
            client.setCameraEntity(entity);
            renderBasicEntityView(entity, width, height);
            GL11.glFlush();
            customFramebuffer.endWrite();
            client.getFramebuffer().beginWrite(true);
            GL11.glViewport(0, 0, client.getWindow().getFramebufferWidth(), client.getWindow().getFramebufferHeight());
            saveScreenshot(customFramebuffer, entity, callback);
        } catch (Exception e) {
            SplitSelf.LOGGER.error("Minimal rendering capture failed", e);
            callback.accept(null);
        } finally {
            try {
                client.setCameraEntity(originalCameraEntity);
                client.options.setPerspective(originalPerspective);
                if (customFramebuffer != null) {
                    customFramebuffer.delete();
                }
            } catch (Exception e) {
                SplitSelf.LOGGER.error("Minimal rendering cleanup failed", e);
            }
        }
    }

    private void captureStandard(Entity entity, int width, int height, Consumer<File> callback) {
        Framebuffer originalFramebuffer = client.getFramebuffer();
        var originalPerspective = client.options.getPerspective();
        Entity originalCameraEntity = client.getCameraEntity();
        Framebuffer customFramebuffer = null;
        try {
            customFramebuffer = new SimpleFramebuffer(width, height, true, MinecraftClient.IS_SYSTEM_MAC);
            customFramebuffer.setClearColor(0.0f, 0.0f, 0.0f, 0.0f);
            client.options.setPerspective(net.minecraft.client.option.Perspective.THIRD_PERSON_BACK);
            try { Thread.sleep(10); } catch (InterruptedException ignored) {}
            customFramebuffer.beginWrite(true);
            GL11.glViewport(0, 0, width, height);
            GL11.glClearColor(0.5f, 0.8f, 1.0f, 1.0f);
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
            boolean renderingSuccessful = false;
            try {
                client.setCameraEntity(entity);
                Camera camera = client.gameRenderer.getCamera();
                float tickDelta = client.getRenderTickCounter().getTickDelta(false);
                camera.update(client.world, entity, false, false, tickDelta);
                try { Thread.sleep(10); } catch (InterruptedException ignored) {}
                GameRenderer gameRenderer = client.gameRenderer;
                Matrix4f projectionMatrix = gameRenderer.getBasicProjectionMatrix(tickDelta);
                gameRenderer.loadProjectionMatrix(projectionMatrix);
                gameRenderer.renderWorld(client.getRenderTickCounter());
                renderingSuccessful = true;
            } catch (Exception e) {
                SplitSelf.LOGGER.warn("Standard gameRenderer approach failed: " + e.getMessage());
            } finally {
                client.setCameraEntity(originalCameraEntity);
            }
            if (!renderingSuccessful) {
                renderSimpleWorld(entity.getPos());
            }
            GL11.glFlush();
            customFramebuffer.endWrite();
            client.setCameraEntity(originalCameraEntity);
            client.options.setPerspective(originalPerspective);
            originalFramebuffer.beginWrite(true);
            GL11.glViewport(0, 0, client.getWindow().getFramebufferWidth(), client.getWindow().getFramebufferHeight());
            saveScreenshot(customFramebuffer, entity, callback);
        } catch (Exception e) {
            SplitSelf.LOGGER.error("Standard capture failed", e);
            callback.accept(null);
        } finally {
            try {
                client.setCameraEntity(originalCameraEntity);
                client.options.setPerspective(originalPerspective);
                if (originalFramebuffer != null) {
                    originalFramebuffer.beginWrite(true);
                    GL11.glViewport(0, 0, client.getWindow().getFramebufferWidth(), client.getWindow().getFramebufferHeight());
                }
                if (customFramebuffer != null) {
                    customFramebuffer.delete();
                }
            } catch (Exception e) {
                SplitSelf.LOGGER.error("Standard cleanup failed", e);
            }
        }
    }

    private void renderBasicEntityView(Entity entity, int width, int height) {
        Vec3d entityPos = entity.getPos();
        float entityYaw = entity.getYaw();
        float entityPitch = entity.getPitch();
        float eyeHeight = entity.getStandingEyeHeight();
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        float fov = 70.0f;
        float aspectRatio = (float) width / height;
        float near = 0.1f;
        float far = 256.0f;
        double fH = Math.tan(Math.toRadians(fov / 2.0)) * near;
        double fW = fH * aspectRatio;
        GL11.glFrustum(-fW, fW, -fH, fH, near, far);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        GL11.glRotatef(entityPitch, 1.0f, 0.0f, 0.0f);
        GL11.glRotatef(entityYaw + 180.0f, 0.0f, 1.0f, 0.0f);
        GL11.glTranslatef((float) -entityPos.x, (float) -(entityPos.y + eyeHeight), (float) -entityPos.z);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_CULL_FACE);
        renderSimpleWorld(entityPos);
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPopMatrix();
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPopMatrix();
    }

    private void renderSimpleWorld(Vec3d entityPos) {
        try {
            GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glColor3f(0.5f, 0.8f, 1.0f);
            GL11.glBegin(GL11.GL_QUADS);
            GL11.glVertex3f(-1000, 100, -1000);
            GL11.glVertex3f(1000, 100, -1000);
            GL11.glVertex3f(1000, 100, 1000);
            GL11.glVertex3f(-1000, 100, 1000);
            GL11.glEnd();
            GL11.glEnable(GL11.GL_CULL_FACE);
            GL11.glColor3f(0.2f, 0.7f, 0.2f);
            GL11.glBegin(GL11.GL_QUADS);
            for (int x = -50; x < 50; x += 5) {
                for (int z = -50; z < 50; z += 5) {
                    float y = 64.0f;
                    GL11.glVertex3f(x, y, z);
                    GL11.glVertex3f(x + 5, y, z);
                    GL11.glVertex3f(x + 5, y, z + 5);
                    GL11.glVertex3f(x, y, z + 5);
                }
            }
            GL11.glEnd();
            GL11.glColor3f(0.6f, 0.4f, 0.2f);
            GL11.glBegin(GL11.GL_QUADS);
            for (int i = 0; i < 3; i++) {
                float x = (float) (entityPos.x + (i - 1) * 5);
                float z = (float) (entityPos.z + 10);
                float y = 65.0f;
                GL11.glVertex3f(x, y + 1, z);
                GL11.glVertex3f(x + 1, y + 1, z);
                GL11.glVertex3f(x + 1, y + 1, z + 1);
                GL11.glVertex3f(x, y + 1, z + 1);
            }
            GL11.glEnd();

            GL11.glColor3f(1.0f, 1.0f, 1.0f);

        } catch (Exception e) {
            SplitSelf.LOGGER.debug("Simple world rendering issues: " + e.getMessage());
        }
    }

    private void saveScreenshot(Framebuffer framebuffer, Entity entity, Consumer<File> callback) {
        try {
            String filename = "hello_" + entity.getUuidAsString().substring(0, 8) + "_" + System.currentTimeMillis() + ".png";
            File screenshotsDir = new File(client.runDirectory, "screenshots");
            if (!screenshotsDir.exists()) {
                screenshotsDir.mkdirs();
            }
            ScreenshotRecorder.saveScreenshot(
                    client.runDirectory,
                    filename,
                    framebuffer,
                    (text) -> {
                        File screenshotFile = new File(screenshotsDir, filename);
                        if (screenshotFile.exists()) {
                            SplitSelf.LOGGER.info("Screenshot saved: " + screenshotFile.getAbsolutePath());
                            callback.accept(screenshotFile);
                        } else {
                            File[] files = screenshotsDir.listFiles((dir, name) ->
                                    name.startsWith("hello_") && name.endsWith(".png"));
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
            SplitSelf.LOGGER.error("Failed to save screenshot: " + e.getMessage());
            callback.accept(null);
        }
    }

    public void capture(Consumer<File> fileCallback) {
        if (client.world == null || client.player == null) {
            return;
        }
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