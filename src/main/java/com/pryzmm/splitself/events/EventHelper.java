package com.pryzmm.splitself.events;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class EventHelper {

    private static final String OS = System.getProperty("os.name").toLowerCase();

    public static List<String> detectDrives() {
        if (OS.contains("win")) {
            return run("powershell", "-Command", "Get-CimInstance -ClassName Win32_CDROMDrive | Select-Object -ExpandProperty Drive");
        } else if (OS.contains("mac")) {
            return run("drutil", "list").stream()
                .filter(l -> l.trim().matches("\\d+.*"))
                .map(l -> l.trim().split("\\s+")[0])
                .collect(Collectors.toList());
        } else {
            List<String> drives = run("bash", "-c", "lsblk -o NAME,TYPE | grep rom").stream()
                .map(l -> "/dev/" + l.trim().split("\\s+")[0])
                .filter(s -> !s.equals("/dev/"))
                .collect(Collectors.toList());
            if (drives.isEmpty()) {
                drives = run("bash", "-c", "cat /proc/sys/dev/cdrom/info | grep 'drive name' | awk '{for(i=3;i<=NF;i++) print \"/dev/\" $i}'");
            }
            return drives;
        }
    }

    public static void eject(String drive) {
        if (OS.contains("win")) {
            exec("powershell", "-Command", String.format("(New-Object -ComObject Shell.Application).Namespace(17).ParseName('%s').InvokeVerb('Eject')", drive));
        } else if (OS.contains("mac")) {
            exec("drutil", "-drive", drive, "tray", "eject");
        } else {
            exec("eject", drive);
        }
    }

    public static void ejectAll() {
        List<String> drives = detectDrives();
        if (drives.isEmpty()) { System.out.println("No drives found."); return; }
        for (String drive : drives) eject(drive);
    }

    private static List<String> run(String... cmd) {
        try {
            Process p = new ProcessBuilder(cmd).redirectErrorStream(true).start();
            List<String> lines = Arrays.asList(new String(p.getInputStream().readAllBytes()).trim().split("\n"));
            if (p.waitFor() != 0) throw new IOException("Command failed: " + Arrays.toString(cmd));
            return lines.stream().filter(l -> !l.isBlank()).collect(Collectors.toList());
        } catch (Exception ignored) { return new ArrayList<>(); }
    }

    private static void exec(String... cmd) {
        try {
            Process p = new ProcessBuilder(cmd).redirectErrorStream(true).start();
            String out = new String(p.getInputStream().readAllBytes());
            if (p.waitFor() != 0) throw new IOException("Failed: " + Arrays.toString(cmd) + "\n" + out);
        } catch (Exception ignored) {}
    }

}
