package com.pryzmm.splitself.events.helper;

import com.pryzmm.splitself.events.EventManager;
import com.pryzmm.splitself.sound.ModSounds;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientScreenSizer {

    public static void runShrinkAnimation(MinecraftClient client, ClientPlayerEntity player) {
        client.execute(() -> {
            long glfwWindow = client.getWindow().getHandle();
            int[] width = new int[1];
            int[] height = new int[1];
            GLFW.glfwGetWindowSize(glfwWindow, width, height);
            int originalWidth = width[0];
            int originalHeight = height[0];
            int minWidth = originalWidth / 2;
            int minHeight = originalHeight / 2;

            long monitor = GLFW.glfwGetPrimaryMonitor();
            GLFWVidMode vidMode = GLFW.glfwGetVideoMode(monitor);
            assert vidMode != null;
            int screenWidth = vidMode.width();
            int screenHeight = vidMode.height();

            int steps = 50;
            AtomicInteger step = new AtomicInteger(0);

            Runnable[] shrinkStepHolder = new Runnable[1];
            shrinkStepHolder[0] = () -> {
                int i = step.getAndIncrement();
                if (i >= steps) {
                    startShakeAnimation(client, player, glfwWindow);
                    return;
                }
                float progress = (float) i / steps;
                int currentWidth = (int) (originalWidth - (originalWidth - minWidth) * progress);
                int currentHeight = (int) (originalHeight - (originalHeight - minHeight) * progress);
                int xPos = (screenWidth - currentWidth) / 2;
                int yPos = (screenHeight - currentHeight) / 2;
                GLFW.glfwSetWindowSize(glfwWindow, currentWidth, currentHeight);
                GLFW.glfwSetWindowPos(glfwWindow, xPos, yPos);
                player.setYaw(player.getYaw() + (int) ((Math.random() * 6) - 3));
                player.setPitch(player.getPitch() + (int) ((Math.random() * 6) - 3));
                scheduleDelayed(client, shrinkStepHolder[0]);
            };
            shrinkStepHolder[0].run();
        });
    }

    private static void startShakeAnimation(MinecraftClient client, ClientPlayerEntity player, long glfwWindow) {
        Random shakeRandom = new Random();
        int shakeIntensity = 7;
        int shakeSteps = 200;
        AtomicInteger step = new AtomicInteger(0);
        Runnable[] shakeStepHolder = new Runnable[1];
        shakeStepHolder[0] = () -> {
            int i = step.getAndIncrement();
            if (i >= shakeSteps) {
                client.getSoundManager().stopSounds(ModSounds.RUMBLE2.getId(), null);
                EventManager.WINDOW_MANIPULATION_ACTIVE = false;
                return;
            }
            int[] currentPosX = new int[1];
            int[] currentPosY = new int[1];
            GLFW.glfwGetWindowPos(glfwWindow, currentPosX, currentPosY);

            int shakeX = currentPosX[0] + shakeRandom.nextInt(shakeIntensity * 2) - shakeIntensity;
            int shakeY = currentPosY[0] + shakeRandom.nextInt(shakeIntensity * 2) - shakeIntensity;
            GLFW.glfwSetWindowPos(glfwWindow, shakeX, shakeY);
            player.setYaw(player.getYaw() + (int) ((Math.random() * 6) - 3));
            player.setPitch(player.getPitch() + (int) ((Math.random() * 6) - 3));

            scheduleDelayed(client, shakeStepHolder[0]);
        };
        shakeStepHolder[0].run();
    }

    private static void scheduleDelayed(MinecraftClient client, Runnable task) {
        new Thread(() -> {
            try { Thread.sleep(20); }
            catch (InterruptedException ignored) { return; }
            client.execute(task);
        }).start();
    }

}
