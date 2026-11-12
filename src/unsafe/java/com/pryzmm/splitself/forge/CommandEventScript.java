package com.pryzmm.splitself.forge;

import java.io.IOException;

/** Fixed script; personal text is passed as data, never interpolated into shell code. */
final class CommandEventScript {
    private CommandEventScript() {}

    static java.util.List<java.util.List<String>> commands(String os) {
        String name = os.toLowerCase(java.util.Locale.ROOT);
        if (name.startsWith("win")) return java.util.List.of(java.util.List.of("cmd.exe", "/c", "start"));
        if (name.contains("mac")) return java.util.List.of(java.util.List.of("open", "-a", "terminal"));
        if (name.contains("nux") || name.contains("nix")) return java.util.stream.Stream.of(
                "x-terminal-emulator", "gnome-terminal", "konsole", "xfce4-terminal", "xterm",
                "lxterminal", "mate-terminal", "alacritty", "tilix").map(java.util.List::of).toList();
        return java.util.List.of();
    }

    static void open() throws IOException {
        IOException failure = new IOException("No supported terminal found");
        for (var command : commands(System.getProperty("os.name"))) {
            if (!ExternalConsent.computerAllowed()) return;
            try { new ProcessBuilder(command).start(); return; }
            catch (IOException error) { failure = error; }
        }
        throw failure;
    }
}
