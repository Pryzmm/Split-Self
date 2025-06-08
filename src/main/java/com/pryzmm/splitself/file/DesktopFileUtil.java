package com.pryzmm.splitself.file;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class DesktopFileUtil {

    public static void createFileOnDesktop(String fileName, String content) {
        File desktop = FileSystemView.getFileSystemView().getHomeDirectory();
        File file = new File(desktop, fileName);

        try {
            // Create file if it doesn't exist
            if (!file.exists()) {
                boolean created = file.createNewFile();
                if (!created) {
                    System.err.println("File already exists or failed to create: " + file.getAbsolutePath());
                }
            }

            // Now write content (overwrite)
            try (FileWriter writer = new FileWriter(file, false)) { // false = overwrite
                writer.write(content);
                System.out.println("File written successfully: " + file.getAbsolutePath());
            }

        } catch (IOException e) {
            System.err.println("Failed to create or write to file: " + file.getAbsolutePath());
            e.printStackTrace();
        }
    }
}