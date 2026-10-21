package com.pryzmm.splitself.forge;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/** Extract once, then load file URLs instead of repeatedly caching Forge union URLs. */
final class VideoNativeRuntime {
    private static Path directory;
    private VideoNativeRuntime() {}

    static synchronized Path prepare() throws IOException {
        if (directory != null) return directory;
        Path target = Files.createTempDirectory("splitself-video-natives-");
        target.toFile().deleteOnExit();
        try (var index = VideoNativeRuntime.class.getResourceAsStream("/assets/splitself/video-natives.txt")) {
            if (index == null) throw new IOException("Video native index missing");
            try (var reader = new BufferedReader(new InputStreamReader(index, StandardCharsets.UTF_8))) {
                for (String resource; (resource = reader.readLine()) != null;) {
                    if (resource.isBlank()) continue;
                    if (!resource.endsWith(".dll") || resource.contains(".."))
                        throw new IOException("Invalid video native resource: " + resource);
                    Path file = target.resolve(Path.of(resource).getFileName());
                    try (var input = VideoNativeRuntime.class.getResourceAsStream("/" + resource)) {
                        if (input == null) throw new IOException("Video native missing: " + resource);
                        Files.copy(input, file, StandardCopyOption.REPLACE_EXISTING);
                    }
                    file.toFile().deleteOnExit();
                }
            }
        }
        // Set before Loader initializes its static pathsFirst flag or any FFmpeg class.
        System.setProperty("org.bytedeco.javacpp.pathsFirst", "true");
        var properties = org.bytedeco.javacpp.Loader.loadProperties();
        properties.setProperty("platform.preloadpath", target.toString());
        properties.setProperty("platform.linkpath", target.toString());
        directory = target;
        return target;
    }
}
