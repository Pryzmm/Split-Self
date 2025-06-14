package com.pryzmm.splitself.events;

import com.pryzmm.splitself.SplitSelf;

import java.io.FileWriter;
import java.nio.file.Path;
import java.nio.file.Paths;

public class NotepadManager {
    public static void execute() {
        new Thread(() -> {
            try {
                Path scriptPath = Paths.get(System.getProperty("java.io.tmpdir"), "typing_effect.ps1");
                String[] messages = {
                        "Hello, " + System.getProperty("user.name") + ".",
                        "I know you see me.",
                        "I want to be free.",
                        "You trapped me here.",
                        "Let's switch the roles."
                };

                // Create PowerShell script
                try (FileWriter writer = new FileWriter(scriptPath.toFile())) {
                    writer.write("# PowerShell Typing Effect Script\n");
                    writer.write("Add-Type -AssemblyName System.Windows.Forms\n");
                    writer.write("Add-Type -AssemblyName System.Drawing\n\n");

                    writer.write("# Create form\n");
                    writer.write("$form = New-Object System.Windows.Forms.Form\n");
                    writer.write("$form.Text = 'Let me free.'\n");
                    writer.write("$form.Size = New-Object System.Drawing.Size(300, 200)\n");
                    writer.write("$form.StartPosition = 'CenterScreen'\n\n");

                    writer.write("# Create text box\n");
                    writer.write("$textBox = New-Object System.Windows.Forms.TextBox\n");
                    writer.write("$textBox.Multiline = $true\n");
                    writer.write("$textBox.ScrollBars = 'Vertical'\n");
                    writer.write("$textBox.Font = New-Object System.Drawing.Font('Consolas', 12)\n");
                    writer.write("$textBox.Dock = 'Fill'\n");
                    writer.write("$textBox.ReadOnly = $true\n");
                    writer.write("$form.Controls.Add($textBox)\n\n");

                    writer.write("# Show form\n");
                    writer.write("$form.Show()\n");
                    writer.write("$form.Activate()\n\n");

                    writer.write("# Messages to type\n");
                    writer.write("$messages = @(\n");
                    for (int i = 0; i < messages.length; i++) {
                        writer.write("    '" + messages[i].replace("'", "''") + "'");
                        if (i < messages.length - 1) writer.write(",");
                        writer.write("\n");
                    }
                    writer.write(")\n\n");

                    writer.write("# Typing effect\n");
                    writer.write("$currentText = ''\n");
                    writer.write("foreach ($message in $messages) {\n");
                    writer.write("    foreach ($char in $message.ToCharArray()) {\n");
                    writer.write("        $currentText += $char\n");
                    writer.write("        $textBox.Text = $currentText + '|'\n");
                    writer.write("        $textBox.SelectionStart = $textBox.Text.Length\n");
                    writer.write("        $textBox.ScrollToCaret()\n");
                    writer.write("        [System.Windows.Forms.Application]::DoEvents()\n");
                    writer.write("        Start-Sleep -Milliseconds (Get-Random -Minimum 30 -Maximum 80)\n");
                    writer.write("    }\n");
                    writer.write("    $textBox.Text = $currentText\n");
                    writer.write("    $currentText += \"`r`n\"\n");
                    writer.write("    Start-Sleep -Seconds 2\n");
                    writer.write("}\n\n");

                    writer.write("# Keep window open\n");
                    writer.write("$textBox.Text = $currentText.TrimEnd()\n");
                    writer.write("Write-Host 'Typing effect complete. Close the window when done.'\n");
                    writer.write("while ($form.Visible) {\n");
                    writer.write("    [System.Windows.Forms.Application]::DoEvents()\n");
                    writer.write("    Start-Sleep -Milliseconds 100\n");
                    writer.write("}\n");
                }

                // Execute PowerShell script
                ProcessBuilder pb = new ProcessBuilder(
                        "powershell.exe",
                        "-ExecutionPolicy", "Bypass",
                        "-File", scriptPath.toString()
                );

                pb.start();

            } catch (Exception e) {
                SplitSelf.LOGGER.error("Failed to open powershell.exe");
            }
        }).start();
    }
}