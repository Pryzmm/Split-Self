package com.pryzmm.splitself.file;

import com.pryzmm.splitself.SplitSelf;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

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
}