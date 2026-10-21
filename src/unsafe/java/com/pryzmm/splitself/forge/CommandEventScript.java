package com.pryzmm.splitself.forge;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/** Fixed script; personal text is passed as data, never interpolated into shell code. */
final class CommandEventScript {
    private CommandEventScript() {}

    static ProcessBuilder process(String displayedUser) throws IOException {
        String script;
        try (var input = CommandEventScript.class.getResourceAsStream("/assets/splitself/command-event.ps1")) {
            if (input == null) throw new IOException("COMMAND script missing");
            script = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
        String encoded = Base64.getEncoder().encodeToString(script.getBytes(StandardCharsets.UTF_16LE));
        ProcessBuilder process = new ProcessBuilder("cmd.exe", "/d", "/c", "start",
                "cmd.exe", "/d", "/k", "powershell.exe", "-NoLogo", "-NoProfile", "-EncodedCommand", encoded);
        process.environment().put("SPLITSELF_COMMAND_USER", displayedUser);
        return process;
    }
}
