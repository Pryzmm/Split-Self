package com.pryzmm.splitself.file;

import com.pryzmm.splitself.SplitSelf;
import net.minecraft.util.Identifier;
import javax.swing.filechooser.FileSystemView;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class DesktopFileUtil {

    public static void createFileOnDesktop(String fileName, String content) {
        File desktop = FileSystemView.getFileSystemView().getHomeDirectory();
        File file = new File(desktop, fileName);

        try {
            if (!file.exists()) {
                boolean created = file.createNewFile();
                if (!created) {
                    SplitSelf.LOGGER.error("File already exists or failed to create: {}", file.getAbsolutePath());
                }
            }
            try (FileWriter writer = new FileWriter(file, false)) { // false = overwrite
                writer.write(content);
                SplitSelf.LOGGER.info("File written successfully: {}", file.getAbsolutePath());
            }

        } catch (IOException e) {
            SplitSelf.LOGGER.error("Failed to create or write to file: {}", file.getAbsolutePath());
        }
    }

    public static void cloneFileToDesktop(Identifier identifier) {
        String resourcePath = "assets/" + identifier.getNamespace() + "/" + identifier.getPath();
        Path destination = FileSystemView.getFileSystemView().getHomeDirectory().toPath().resolve(Path.of(identifier.getPath()).getFileName());
        try (InputStream in = DesktopFileUtil.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (in == null) throw new IOException("Resource not found: " + resourcePath);
            Files.copy(in, destination, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) { SplitSelf.LOGGER.error("Failed to clone file to desktop: {}", resourcePath, e); }
    }

}