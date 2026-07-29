package com.pryzmm.splitself.file;

import com.pryzmm.splitself.SplitSelf;
import net.minecraft.util.Identifier;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.CompletableFuture;

public class DesktopFileUtil {

    private static File getDesktopDirectory() {
        String home = System.getProperty("user.home");
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            File redirected = getWindowsDesktopViaRegistry();
            if (redirected != null) return redirected;
            return new File(home, "Desktop");
        }
        return new File(home, "Desktop");
    }

    private static File getWindowsDesktopViaRegistry() {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                "reg", "query",
                "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\Shell Folders",
                "/v", "Desktop"
            );
            pb.redirectErrorStream(true);
            Process proc = pb.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains("REG_SZ")) {
                        String[] parts = line.trim().split("REG_SZ", 2);
                        if (parts.length == 2) {
                            String path = parts[1].trim().replace("%USERPROFILE%", System.getProperty("user.home"));
                            File dir = new File(path);
                            if (dir.exists()) return dir;
                        }
                    }
                }
            }
            proc.waitFor();
        } catch (Exception ignored) {}
        return null;
    }

    public static void createFileOnDesktop(String fileName, String content) {
        File desktop = getDesktopDirectory();
        File file = new File(desktop, fileName);

        try {
            if (!file.exists()) {
                boolean created = file.createNewFile();
                if (!created) SplitSelf.LOGGER.error("File already exists or failed to create: {}", file.getAbsolutePath());
            }
            try (FileWriter writer = new FileWriter(file, false)) {
                writer.write(content);
                SplitSelf.LOGGER.info("File written successfully: {}", file.getAbsolutePath());
            }
        } catch (IOException e) {
            SplitSelf.LOGGER.error("Failed to create or write to file: {}", file.getAbsolutePath());
        }
    }

    public static void cloneFileToDesktop(Identifier identifier) {
        CompletableFuture.runAsync(() -> {
            String resourcePath = "assets/" + identifier.getNamespace() + "/" + identifier.getPath();
            Path destination = getDesktopDirectory().toPath().resolve(Path.of(identifier.getPath()).getFileName());
            try (InputStream in = DesktopFileUtil.class.getClassLoader().getResourceAsStream(resourcePath)) {
                if (in == null) throw new IOException("Resource not found: " + resourcePath);
                Files.copy(in, destination, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                SplitSelf.LOGGER.error("Failed to clone file to desktop: {}", resourcePath, e);
            }
        });
    }

    public static void openUri(String uri) {
        String os = System.getProperty("os.name").toLowerCase();
        try {
            ProcessBuilder pb;
            if (os.contains("win")) pb = new ProcessBuilder("rundll32", "url.dll,FileProtocolHandler", uri);
            else if (os.contains("mac")) pb = new ProcessBuilder("open", uri);
            else pb = new ProcessBuilder("xdg-open", uri);
            pb.redirectErrorStream(true);
            pb.start();
        } catch (IOException e) {
            SplitSelf.LOGGER.error("Failed to open URI: {}", uri, e);
        }
    }

}