package com.pryzmm.splitself.file;

import com.pryzmm.splitself.SplitSelf;
import net.minecraft.util.Identifier;

import javax.swing.filechooser.FileSystemView;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class DesktopFileUtil {

    public static void createFileOnDesktop(String fileName, String content) {
        File desktop = FileSystemView.getFileSystemView().getHomeDirectory();
        File file = new File(desktop, fileName);

        try {
            if (!file.exists()) {
                boolean created = file.createNewFile();
                if (!created) {
                    SplitSelf.LOGGER.error("File already exists or failed to create: " + file.getAbsolutePath());
                }
            }
            try (FileWriter writer = new FileWriter(file, false)) { // false = overwrite
                writer.write(content);
                SplitSelf.LOGGER.info("File written successfully: " + file.getAbsolutePath());
            }

        } catch (IOException e) {
            SplitSelf.LOGGER.error("Failed to create or write to file: " + file.getAbsolutePath());
        }
    }

    public String getTextFromResourceFile(String path) {
        try {
            Identifier resourceId = Identifier.of(SplitSelf.MOD_ID, path);
            InputStream inputStream = getClass().getClassLoader()
                    .getResourceAsStream("data/" + resourceId.getNamespace() + "/" + resourceId.getPath());

            if (inputStream == null) {
                SplitSelf.LOGGER.error("Resource not found: {}", resourceId);
                return null;
            }

            StringBuilder content = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
            }

            return content.toString();

        } catch (IOException e) {
            SplitSelf.LOGGER.error("Error reading resource", e);
            return null;
        }
    }
}