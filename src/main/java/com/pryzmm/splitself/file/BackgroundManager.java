package com.pryzmm.splitself.file;

import com.pryzmm.splitself.SplitSelf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class BackgroundManager {
    private static String userBackground = null;
    private static String modBackground = null;

    private static void setWallpaperFromFile(String filePath) {
        try {
            String psScript =
                    "$path = '" + filePath.replace("\\", "\\\\") + "'\n" +
                    "$setwallpapersrc = @\"\n" +
                    "using System.Runtime.InteropServices;\n" +
                    "public class Wallpaper \n" +
                    "{\n" +
                    "public const int SetDesktopWallpaper = 20;\n" +
                    "public const int UpdateIniFile = 0x01;\n" +
                    "public const int SendWinIniChange = 0x02;\n" +
                    "[DllImport(\"user32.dll\", SetLastError = true, CharSet = CharSet.Auto)]\n" +
                    "private static extern int SystemParametersInfo(int uAction, int uParam, string lpvParam, int fuWinIni);\n" +
                    "public static void SetWallpaper(string path)\n" +
                    "{\n" +
                    "SystemParametersInfo(SetDesktopWallpaper, 0, path, UpdateIniFile | SendWinIniChange);\n" +
                    "}\n" +
                    "}\n" +
                    "\"@\n" +
                    "Add-Type -TypeDefinition $setwallpapersrc\n" +
                    "[Wallpaper]::SetWallpaper($path)";

            File ps1 = new File(System.getProperty("java.io.tmpdir"), "restore_wallpaper.ps1");
            Files.writeString(ps1.toPath(), psScript);

            ProcessBuilder pb = new ProcessBuilder("powershell", "-ExecutionPolicy", "Bypass", "-WindowStyle", "Hidden", "-File", ps1.getAbsolutePath());
            pb.redirectErrorStream(true);
            Process process = pb.start();

            process.waitFor();
        } catch (Exception e) {
            SplitSelf.LOGGER.error("Failed to set wallpaper from file, " + filePath + " because, " + e.getMessage());
        }
    }

    public static String getUserBackground() {
        return userBackground;
    }

    public static String getModBackground() {
        return modBackground;
    }

    public static void restoreUserBackground () {
        if (!System.getProperty("os.name").toLowerCase().startsWith("win")) {
            SplitSelf.LOGGER.info("Restoring user background is not implemented for non-Windows OS'");
            return;
        }

        if (userBackground == null) {
            return;
        }

        try {
            String currentWallpaper = getCurrentBackground();
            if (currentWallpaper != null && currentWallpaper.equals(modBackground)) {
                setWallpaperFromFile(userBackground);
                SplitSelf.LOGGER.info("Restored original wallpaper: " + userBackground);
            } else {
                SplitSelf.LOGGER.info("User changed wallpaper during play — skipping restore.");
            }
        } catch (Exception e) {
            SplitSelf.LOGGER.error("Failed to restore original wallpaper", e);
        }
    }

    public static String getCurrentBackground() {
        if (!System.getProperty("os.name").toLowerCase().startsWith("win")) {
            SplitSelf.LOGGER.info("Cannot get current background because this is not a Windows OS.");
            return null;
        }

        try {
            String powershellCommand = "(Get-ItemProperty 'HKCU:\\Control Panel\\Desktop' | select -ExpandProperty wallpaper).split('')[-1]";

            ProcessBuilder pb = new ProcessBuilder(powershellCommand);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String output = reader.readLine();
                process.waitFor();
                if (output != null && !output.isBlank()) {
                    return output.trim();
                }
            }

        } catch (Exception e) {
            SplitSelf.LOGGER.error("Failed to get current background, " + e.getMessage());
        }

        return null;
    }

    public static void setBackground(String resourcePath, String outputName) {
        if (!System.getProperty("os.name").toLowerCase().startsWith("win")) { // No point in executing considering this function only works on Windows.
            SplitSelf.LOGGER.info("[WaitForMeProcedure] Not executing payload since the OS is not Windows.");
            return;
        }

        if (userBackground == null) {
            String tempUserBackground = getCurrentBackground();
            if (tempUserBackground == null) {
               return; // We probably shouldn't run this? I'm not exactly sure what to do in this case.
            }
            userBackground = tempUserBackground; // Move it to a more permanent location.
        }

        try {
            File image = exportResource(resourcePath, outputName);
            String psScript = "$image = '" + image.getAbsolutePath().replace("\\", "\\\\") + "'\n$desktop = [Environment]::GetFolderPath('Desktop')\nAdd-Type -AssemblyName System.Drawing\n$bmpPath = \"$env:TEMP\\wallpaper.bmp\"\n$img = [System.Drawing.Image]::FromFile($image)\n$img.Save($bmpPath, [System.Drawing.Imaging.ImageFormat]::Bmp)\n$img.Dispose()\nAdd-Type @\"\nusing System;\nusing System.Runtime.InteropServices;\npublic class Wallpaper {\n    [DllImport(\"user32.dll\", CharSet = CharSet.Auto)]\n    public static extern int SystemParametersInfo(int uAction, int uParam, string lpvParam, int fuWinIni);\n}\n\"@\n[Wallpaper]::SystemParametersInfo(20, 0, $bmpPath, 3)";
            File ps1 = new File(System.getProperty("java.io.tmpdir"), "payload.ps1");
            Files.writeString(ps1.toPath(), psScript);
            SplitSelf.LOGGER.info("[WaitForMeProcedure] PowerShell script saved to: " + ps1.getAbsolutePath());
            File batFile = new File(System.getProperty("java.io.tmpdir"), "launch_payload.bat");
            String batCommand = "powershell -ExecutionPolicy Bypass -WindowStyle Hidden -File \"" + ps1.getAbsolutePath() + "\"\n";
            Files.writeString(batFile.toPath(), batCommand);
            SplitSelf.LOGGER.info("[WaitForMeProcedure] Batch file created: " + batFile.getAbsolutePath());
            Process process = (new ProcessBuilder("cmd.exe", "/c", batFile.getAbsolutePath())).redirectErrorStream(true).start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));

            String line;
            while((line = reader.readLine()) != null) {
                SplitSelf.LOGGER.info("[BAT OUTPUT] " + line);
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                SplitSelf.LOGGER.error("Batch file execution failed with exit code: " + exitCode);
            } else {
                SplitSelf.LOGGER.info("Batch file executed successfully.");
                modBackground = getCurrentBackground();
            }
        } catch (Exception e) {
            SplitSelf.LOGGER.error("[WaitForMeProcedure] Error executing payload.");
        }
    }

    public static File exportResource(String resourcePath, String outputName) throws IOException {
        InputStream stream = BackgroundManager.class.getResourceAsStream(resourcePath);
        if (stream == null) {
            throw new FileNotFoundException("Resource not found: " + resourcePath);
        } else {
            File tempFile = new File(System.getProperty("java.io.tmpdir"), outputName);
            Files.copy(stream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            SplitSelf.LOGGER.info("[WaitForMeProcedure] Exported resource: " + tempFile.getAbsolutePath());
            return tempFile;
        }
    }
}
