package com.pryzmm.splitself.file;

import com.pryzmm.splitself.SplitSelf;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.*;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

public class ZipFunc {

    public static boolean downloadedFiles = false;
    static {
        if (!needsVideoDownloads()) downloadedFiles = true;
    }

    private static final AtomicLong totalBytes = new AtomicLong(-1);
    private static final AtomicLong downloadedBytes = new AtomicLong(0);

    public static long getTotalBytes() {
        return totalBytes.get();
    }

    public static long getDownloadedBytes() {
        return downloadedBytes.get();
    }

    public static double getDownloadProgress() {
        long total = totalBytes.get();
        if (total <= 0) return -1;
        return (double) downloadedBytes.get() / total;
    }

    public static CompletableFuture<Void> downloadAndExtract(String url, Path destination) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        totalBytes.set(-1);
        downloadedBytes.set(0);

        Thread thread = new Thread(() -> {
            Path tmp = null;
            try {
                Files.createDirectories(destination);
                tmp = Files.createTempFile("videolib-download-", ".zip");
                SplitSelf.LOGGER.info("Downloading videos from {}...", url);

                URL parsedUrl = URI.create(url).toURL();
                HttpURLConnection connection = (HttpURLConnection) parsedUrl.openConnection();
                connection.connect();

                long contentLength = connection.getContentLengthLong();
                totalBytes.set(contentLength);

                try (InputStream in = connection.getInputStream()) {
                    copyWithProgress(in, tmp);
                }

                SplitSelf.LOGGER.info("Extracting to {}...", destination);
                try (FileSystem fs = FileSystems.newFileSystem(
                    URI.create("jar:" + tmp.toUri()), Map.of())) {
                    Path root = fs.getPath("/");
                    IOException[] extractError = {null};
                    try (Stream<Path> path = Files.walk(root)) {
                        path.forEach(entry -> {
                            if (extractError[0] != null) return;
                            try {
                                Path dest = destination.resolve(root.relativize(entry).toString());
                                if (Files.isDirectory(entry)) {
                                    Files.createDirectories(dest);
                                } else {
                                    Files.createDirectories(dest.getParent());
                                    Files.copy(entry, dest, StandardCopyOption.REPLACE_EXISTING);
                                }
                            } catch (IOException e) {
                                extractError[0] = e;
                            }
                        });
                    }
                    if (extractError[0] != null) throw extractError[0];
                }

                SplitSelf.LOGGER.info("Done.");
                downloadedFiles = true;
                future.complete(null);
            } catch (Exception e) {
                future.completeExceptionally(e);
            } finally {
                if (tmp != null) try { Files.deleteIfExists(tmp); } catch (IOException ignored) {}
            }
        });
        thread.start();
        return future;
    }

    private static void copyWithProgress(InputStream in, Path destination) throws IOException {
        byte[] buffer = new byte[8192];
        int read;
        try (var out = Files.newOutputStream(destination, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
                downloadedBytes.addAndGet(read);
            }
        }
    }

    public static boolean needsVideoDownloads() {
        Path configDir = FabricLoader.getInstance().getConfigDir().resolve(SplitSelf.MOD_ID);
        return !configDir.resolve(Path.of("videos")).toFile().exists();
    }

    public static Path getDest() {
        return FabricLoader.getInstance().getConfigDir().resolve(SplitSelf.MOD_ID + "/videos");
    }

    public static File getVideo(String fileName) {
        return FabricLoader.getInstance().getConfigDir().resolve(SplitSelf.MOD_ID + "/videos/" + fileName + ".mp4").toFile();
    }
}