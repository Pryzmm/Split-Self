package com.pryzmm.splitself.events.helper;

import com.pryzmm.splitself.SplitSelf;
import net.minecraft.text.Text;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NotepadManager {

    private static String escapeForPowerShell(String input) {
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

    private static String escapeForJS(String input) {
        StringBuilder escaped = new StringBuilder();
        for (char c : input.toCharArray()) {
            switch (c) {
                case '"': escaped.append("\\\""); break;
                case '\\': escaped.append("\\\\"); break;
                case '\n': escaped.append("\\n"); break;
                case '\r': break;
                default:
                    if (c > 127) {
                        escaped.append(String.format("\\u%04X", (int) c));
                    } else {
                        escaped.append(c);
                    }
            }
        }
        return escaped.toString();
    }

    public static void execute(Text[] messages) {
        List<String> m = new ArrayList<>();
        Arrays.stream(messages).forEach(msg -> m.add(msg.getString()));
        execute(m);
    }

    public static void execute(List<String> messages) {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            executeWindows(messages);
        } else if (os.contains("mac")) {
            executeMac(messages);
        }
    }

    public static void executeWindows(List<String> messages) {
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
                    for (int i = 0; i < messages.size(); i++) {
                        String messageString = messages.get(i);
                        String escapedMessage = escapeForPowerShell(messageString);
                        writer.write("    \"" + escapedMessage.replace("\"", "`\"").replace("`", "``") + "\"");
                        if (i < messages.size() - 1) writer.write(",");
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
                    writer.write("        Start-Sleep -Milliseconds (Get-Random -Minimum 0 -Maximum 250)\n");
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
                    "-Command", "$OutputEncoding = [Console]::InputEncoding = [Console]::OutputEncoding = New-Object System.Text.UTF8Encoding; & '" + scriptPath + "'"
                );

                pb.start();

            } catch (Exception e) {
                SplitSelf.LOGGER.error("Failed to open powershell.exe");
            }
        }).start();
    }

    public static void executeMac(List<String> messages) {
        new Thread(() -> {
            try {
                Path scriptPath = Paths.get(System.getProperty("java.io.tmpdir"), "typing_effect.js");

                try (OutputStreamWriter writer = new OutputStreamWriter(
                        new FileOutputStream(scriptPath.toFile()), StandardCharsets.UTF_8)) {

                    writer.write("ObjC.import('Cocoa');\n");
                    writer.write("ObjC.import('Foundation');\n\n");

                    writer.write("var app = $.NSApplication.sharedApplication;\n");
                    writer.write("app.setActivationPolicy($.NSApplicationActivationPolicyRegular);\n\n");

                    writer.write("var win = $.NSWindow.alloc.initWithContentRectStyleMaskBackingDefer(\n");
                    writer.write("    $.NSMakeRect(0, 0, 300, 200),\n");
                    writer.write("    $.NSWindowStyleMaskTitled | $.NSWindowStyleMaskClosable,\n");
                    writer.write("    $.NSBackingStoreBuffered,\n");
                    writer.write("    false\n");
                    writer.write(");\n");
                    writer.write("win.title = 'Let me free.';\n");
                    writer.write("win.center;\n\n");

                    writer.write("var scrollView = $.NSScrollView.alloc.initWithFrame($.NSMakeRect(0, 0, 300, 200));\n");
                    writer.write("scrollView.hasVerticalScroller = true;\n");
                    writer.write("scrollView.autoresizingMask = $.NSViewWidthSizable | $.NSViewHeightSizable;\n\n");

                    writer.write("var textView = $.NSTextView.alloc.initWithFrame($.NSMakeRect(0, 0, 300, 200));\n");
                    writer.write("textView.editable = false;\n");
                    writer.write("textView.font = $.NSFont.fontWithNameSize('Menlo', 14);\n");
                    writer.write("scrollView.documentView = textView;\n");
                    writer.write("win.contentView = scrollView;\n\n");

                    writer.write("win.makeKeyAndOrderFront(app);\n");
                    writer.write("app.activateIgnoringOtherApps(true);\n\n");

                    writer.write("var messages = [\n");
                    for (int i = 0; i < messages.size(); i++) {
                        String msg = escapeForJS(messages.get(i));
                        writer.write("    \"" + msg + "\"");
                        if (i < messages.size() - 1) writer.write(",");
                        writer.write("\n");
                    }
                    writer.write("];\n\n");

                    writer.write("function pump(ms) {\n");
                    writer.write("    var until = $.NSDate.dateWithTimeIntervalSinceNow(ms / 1000);\n");
                    writer.write("    while (true) {\n");
                    writer.write("        var event = app.nextEventMatchingMaskUntilDateInModeDequeue(\n");
                    writer.write("            0xFFFFFFFF, until, $.NSDefaultRunLoopMode, true);\n");
                    writer.write("        if (event.isNil()) break;\n");
                    writer.write("        app.sendEvent(event);\n");
                    writer.write("    }\n");
                    writer.write("}\n\n");

                    writer.write("function sleep(ms) {\n");
                    writer.write("    $.NSThread.sleepForTimeInterval(ms / 1000);\n");
                    writer.write("    pump(1);\n");
                    writer.write("}\n\n");

                    writer.write("var currentText = '';\n");
                    writer.write("messages.forEach(function(message) {\n");
                    writer.write("    sleep(500);\n");
                    writer.write("    for (var i = 0; i < message.length; i++) {\n");
                    writer.write("        currentText += message[i];\n");
                    writer.write("        textView.string = $(currentText + '|');\n");
                    writer.write("        textView.scrollRangeToVisible($.NSMakeRange(currentText.length, 0));\n");
                    writer.write("        pump(1);\n");
                    writer.write("        sleep(Math.floor(Math.random() * 250));\n");
                    writer.write("    }\n");
                    writer.write("    textView.string = $(currentText);\n");
                    writer.write("    currentText += '\\n';\n");
                    writer.write("});\n\n");

                    writer.write("textView.string = $(currentText.trimEnd ? currentText.trimEnd() : currentText.replace(/\\n+$/, ''));\n\n");

                    writer.write("while (win.isVisible) {\n");
                    writer.write("    pump(10);\n");
                    writer.write("}\n");
                }

                ProcessBuilder pb = new ProcessBuilder(
                    "osascript",
                    "-l", "JavaScript",
                    scriptPath.toString()
                );
                pb.start();

            } catch (Exception e) {
                SplitSelf.LOGGER.error("Failed to open osascript");
            }
        }).start();
    }
}