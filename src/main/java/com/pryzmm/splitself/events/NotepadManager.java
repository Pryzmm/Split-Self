package com.pryzmm.splitself.events;

import com.pryzmm.splitself.SplitSelf;
import net.minecraft.text.Text;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

public class NotepadManager {

    private static String escapeUnicodeForPowerShell(String input) {
        StringBuilder escaped = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (c > 127) {
                escaped.append(String.format("$([char]0x%04X)", (int) c));
            } else {
                escaped.append(c);
            }
        }
        return escaped.toString();
    }

    public static void execute(Text[] messages) {
        new Thread(() -> {
            try {
                Path scriptPath = Paths.get(System.getProperty("java.io.tmpdir"), "typing_effect.ps1");

                try (OutputStreamWriter writer = new OutputStreamWriter(
                        new FileOutputStream(scriptPath.toFile()), StandardCharsets.UTF_8)) {

                    writer.write('\uFEFF');

                    writer.write("Add-Type -AssemblyName System.Windows.Forms\n");
                    writer.write("Add-Type -AssemblyName System.Drawing\n\n");

                    writer.write("$form = New-Object System.Windows.Forms.Form\n");
                    writer.write("$form.Text = 'Let me free.'\n");
                    writer.write("$form.Size = New-Object System.Drawing.Size(300, 200)\n");
                    writer.write("$form.StartPosition = 'CenterScreen'\n\n");

                    writer.write("$textBox = New-Object System.Windows.Forms.TextBox\n");
                    writer.write("$textBox.Multiline = $true\n");
                    writer.write("$textBox.ScrollBars = 'Vertical'\n");
                    writer.write("$textBox.Font = New-Object System.Drawing.Font('Consolas', 12)\n");
                    writer.write("$textBox.Dock = 'Fill'\n");
                    writer.write("$textBox.ReadOnly = $true\n");
                    writer.write("$form.Controls.Add($textBox)\n\n");

                    writer.write("$form.Show()\n");
                    writer.write("$form.Activate()\n\n");

                    writer.write("$messages = @(\n");
                    for (int i = 0; i < messages.length; i++) {
                        String messageString = messages[i].getString();
                        String escapedMessage = escapeUnicodeForPowerShell(messageString);
                        writer.write("    \"" + escapedMessage.replace("\"", "`\"").replace("`", "``") + "\"");
                        if (i < messages.length - 1) writer.write(",");
                        writer.write("\n");
                    }
                    writer.write(")\n\n");

                    writer.write("$currentText = ''\n");
                    writer.write("foreach ($message in $messages) {\n");
                    writer.write("    Start-Sleep -Milliseconds 500\n");
                    writer.write("    foreach ($char in $message.ToCharArray()) {\n");
                    writer.write("        $currentText += $char\n");
                    writer.write("        $textBox.Text = $currentText + '|'\n");
                    writer.write("        $textBox.SelectionStart = $textBox.Text.Length\n");
                    writer.write("        $textBox.ScrollToCaret()\n");
                    writer.write("        [System.Windows.Forms.Application]::DoEvents()\n");
                    writer.write("        Start-Sleep -Milliseconds (Get-Random -Minimum 0 -Maximum 300)\n");
                    writer.write("    }\n");
                    writer.write("    $textBox.Text = $currentText\n");
                    writer.write("    $currentText += \"`r`n\"\n");
                    writer.write("}\n\n");

                    writer.write("$textBox.Text = $currentText.TrimEnd()\n");
                    writer.write("while ($form.Visible) {\n");
                    writer.write("    [System.Windows.Forms.Application]::DoEvents()\n");
                    writer.write("    Start-Sleep -Milliseconds 10\n");
                    writer.write("}\n");
                }

                ProcessBuilder pb = new ProcessBuilder(
                        "powershell.exe",
                        "-ExecutionPolicy", "Bypass",
                        "-Command", "$OutputEncoding = [Console]::InputEncoding = [Console]::OutputEncoding = New-Object System.Text.UTF8Encoding; & '" + scriptPath.toString() + "'"
                );

                pb.start();

            } catch (Exception e) {
                SplitSelf.LOGGER.error("Failed to open powershell.exe");
            }
        }).start();
    }
}