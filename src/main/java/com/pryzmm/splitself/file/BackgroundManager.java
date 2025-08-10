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

    public static void setBackground(String resourcePath, String outputName) {
        if (!System.getProperty("os.name").toLowerCase().startsWith("win")) { // No point in executing considering this function only works on Windows.
            SplitSelf.LOGGER.info("[WaitForMeProcedure] Not executing payload since the OS is not Windows.");
            return;
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
