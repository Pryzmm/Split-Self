package com.pryzmm.splitself.events;

import com.pryzmm.splitself.SplitSelf;

import javax.swing.*;
import java.awt.*;
import java.io.FileWriter;
import java.nio.file.Path;
import java.nio.file.Paths;

public class NotepadManager {
    public static void execute(String[] messages) {
        String osname = System.getProperty("os.name").toLowerCase();
        if (osname.contains("win")) {
            new Thread(() -> {
                try {
                    Path scriptPath = Paths.get(System.getProperty("java.io.tmpdir"), "typing_effect.ps1");

                    // Create PowerShell script
                    try (FileWriter writer = new FileWriter(scriptPath.toFile())) {
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
                            writer.write("    '" + messages[i].replace("'", "''") + "'");
                            if (i < messages.length - 1) writer.write(",");
                            writer.write("\n");
                        }
                        writer.write(")\n\n");

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

                        writer.write("$textBox.Text = $currentText.TrimEnd()\n");
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
        } else if (osname.contains("nux") || osname.contains("nix")) {
            String textContent = String.join("\\n", messages);
            String javaClass = "import javax.swing.*;\n" +
                    "import java.awt.*;\n\n" +
                    "public class Typing {\n" +
                    "    public static void main(String[] args) {\n" +
                    "        String textContent = \"" + textContent + "\";\n\n" +
                    "        JFrame frame = new JFrame(\"Let me free.\");\n\n" +
                    "        // Set Properties of the JFrame\n" +
                    "        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);\n\n" +
                    "        // Create a text area (8 rows, 21 columns) & set properties\n" +
                    "        JTextArea typewriter = new JTextArea(8, 21);\n" +
                    "        typewriter.setEditable(false);\n" +
                    "        typewriter.setFont(new Font(\"system-ui\", Font.PLAIN, 14));\n" +
                    "        typewriter.setText(\"\");\n\n" +
                    "        frame.add(typewriter);\n" +
                    "        frame.pack();\n\n" +
                    "        // Center the window on the screen\n" +
                    "        frame.setLocationRelativeTo(null);\n\n" +
                    "        // Make it visible\n" +
                    "        frame.setVisible(true);\n\n" +
                    "        Timer timer = new Timer((int) (Math.random() * 120), null);\n" +
                    "        final int[] index = {0};\n\n" +
                    "        timer.addActionListener(e -> {\n" +
                    "            if (index[0] <= textContent.length()) {\n" +
                    "                typewriter.setText(textContent.substring(0, index[0]));\n" +
                    "                if (textContent.substring(0, index[0]).endsWith(\"\\n\")) {\n" +
                    "                    try {\n" +
                    "                        Thread.sleep(1000);\n" +
                    "                    } catch (InterruptedException ex) {\n" +
                    "                        throw new RuntimeException(ex);\n" +
                    "                    }\n" +
                    "                }\n" +
                    "                index[0]++;\n" +
                    "            } else {\n" +
                    "                ((Timer) e.getSource()).stop();\n" +
                    "                frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);\n" +
                    "            }\n" +
                    "        });\n" +
                    "        timer.start();\n" +
                    "    }\n" +
                    "}";

            new Thread(() -> {
                try {
                    Path scriptPath = Paths.get(System.getProperty("java.io.tmpdir"), "Typing.java");

                    // Write .java file to disk
                    try (FileWriter writer = new FileWriter(scriptPath.toFile())) {
                        writer.write(javaClass);

                    }
                    // Compile & wait for process to finish
                    ProcessBuilder compile = new ProcessBuilder("javac", scriptPath.toString());
                    Process process = compile.start();
                    process.waitFor();
                    // Run the .class file
                    ProcessBuilder run = new ProcessBuilder("java", "-cp", System.getProperty("java.io.tmpdir"), "Typing");
                    run.start();

                } catch (Exception e) {
                    SplitSelf.LOGGER.error("Failed to create and / or opening the program");
                }
            }).start();
        }
    }
}