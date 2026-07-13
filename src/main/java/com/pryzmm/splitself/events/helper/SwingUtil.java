package com.pryzmm.splitself.events.helper;

import com.pryzmm.memory.Memory;
import com.pryzmm.memory.data.StageHandler;
import com.pryzmm.splitself.SplitSelf;
import com.pryzmm.splitself.data.WorldData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class SwingUtil {

    public static void launchApp() {
        try {
            int stage = WorldData.getMemoryStage();

            Path java = Path.of(
                    System.getProperty("java.home"),
                    "bin",
                    System.getProperty("os.name").toLowerCase().contains("win") ? "java.exe" : "java"
            );

            Path codePath = Path.of(
                    Memory.class.getProtectionDomain()
                            .getCodeSource()
                            .getLocation()
                            .toURI()
            );

            String classpath = buildClasspath(codePath);

            ProcessBuilder builder = new ProcessBuilder(
                    java.toString(),
                    "-Djava.awt.headless=false",
                    "-Dsplitself.memory.stage=" + stage,
                    "-Dsplitself.memory.title=" + Text.translatable("game.splitself.memory.title").getString(),
                    "-Dsplitself.memory.start=" + Text.translatable("game.splitself.memory.start").getString(),
                    "-Dsplitself.memory.finish=" + Text.translatable("game.splitself.memory.finish").getString(),
                    "-Dsplitself.memory.complete=" + Text.translatable("game.splitself.memory.complete").getString(),
                    "-Dsplitself.memory.message1=" + Text.translatable("game.splitself.memory.message1").getString(),
                    "-Dsplitself.memory.message2=" + Text.translatable("game.splitself.memory.message2").getString(),
                    "-Dsplitself.memory.message3=" + Text.translatable("game.splitself.memory.message3").getString(),
                    "-Dsplitself.memory.message4=" + Text.translatable("game.splitself.memory.message4").getString(),
                    "-cp",
                    classpath,
                    "com.pryzmm.memory.Memory"
            );

            builder.redirectErrorStream(true);

            SplitSelf.LOGGER.info("[MemoryLauncher] Starting Memory app: java={} classpath={} stage={}", java, classpath, stage);

            Process process = builder.start();
            pipeOutput(process);

            Thread waiter = new Thread(() -> {
                try {
                    int exitCode = process.waitFor();
                    SplitSelf.LOGGER.info("[MemoryLauncher] Memory app exited with code {}", exitCode);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    SplitSelf.LOGGER.warn("[MemoryLauncher] Interrupted while waiting for Memory app", e);
                }
            }, "splitself-memory-waiter");

            waiter.setDaemon(true);
            waiter.start();

        } catch (Throwable t) {
            SplitSelf.LOGGER.error("[MemoryLauncher] Failed to start Memory app", t);
        }
    }

    private static String buildClasspath(Path codePath) {
        List<String> entries = new ArrayList<>();
        entries.add(codePath.toString());

        if (Files.isDirectory(codePath)) {
            Path buildDir = findBuildDir(codePath);
            if (buildDir != null) {
                Path resourcesMain = buildDir.resolve("resources").resolve("main");
                if (Files.isDirectory(resourcesMain)) {
                    entries.add(resourcesMain.toString());
                }
            }
        }

        return String.join(File.pathSeparator, entries);
    }

    private static Path findBuildDir(Path path) {
        Path current = path;
        while (current != null) {
            if ("build".equals(current.getFileName().toString())) {
                return current;
            }
            current = current.getParent();
        }
        return null;
    }

    private static void pipeOutput(Process process) {
        Thread loggerThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8)
            )) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (Memory.ADVANCE_STAGE_MARKER.equals(line)) {
                        SplitSelf.LOGGER.info("[MemoryApp] Requested memory stage advance");
                        MinecraftClient.getInstance().execute(StageHandler::advanceStage);
                    } else {
                        SplitSelf.LOGGER.info("[MemoryApp] {}", line);
                    }
                }
            } catch (Throwable t) {
                SplitSelf.LOGGER.warn("[MemoryLauncher] Failed reading Memory app output", t);
            }
        }, "splitself-memory-log-pipe");

        loggerThread.setDaemon(true);
        loggerThread.start();
    }
}