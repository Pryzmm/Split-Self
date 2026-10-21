package com.pryzmm.splitself.forge;
import java.util.List;
final class UnsafeDriveRules {
    static List<String> detection(String os) {
        if(os.contains("win"))return List.of("powershell.exe","-NoProfile","-WindowStyle","Hidden","-Command",
                "Get-CimInstance -ClassName Win32_CDROMDrive | Select-Object -ExpandProperty Drive");
        if(os.contains("mac"))return List.of("drutil","list");
        return List.of("bash","-c","lsblk -o NAME,TYPE | grep rom");
    }
    static List<String> drives(String os, String output) {
        var lines=output.lines().map(String::trim).filter(s->!s.isBlank());
        if(os.contains("win"))return lines.filter(s->s.matches("[A-Za-z]:")).toList();
        if(os.contains("mac"))return lines.filter(s->s.matches("\\d+.*")).map(s->s.split("\\s+")[0]).toList();
        return lines.map(s->"/dev/"+s.split("\\s+")[0]).filter(s->!s.equals("/dev/")).toList();
    }
    static List<String> eject(String os, String drive) {
        if(os.contains("win")) {
            if(!drive.matches("[A-Za-z]:"))throw new IllegalArgumentException("Invalid CD-ROM drive");
            return List.of("powershell.exe","-NoProfile","-WindowStyle","Hidden","-Command",
                    "(New-Object -ComObject Shell.Application).Namespace(17).ParseName('"+drive+"').InvokeVerb('Eject')");
        }
        if(os.contains("mac"))return List.of("drutil","-drive",drive,"tray","eject");
        return List.of("eject",drive);
    }
}
