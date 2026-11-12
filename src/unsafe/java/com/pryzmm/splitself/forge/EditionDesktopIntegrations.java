package com.pryzmm.splitself.forge;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
/** Executable implementations compiled only into the UNSAFE edition. */
final class EditionDesktopIntegrations {
    private static final Set<Process> CHILDREN=ConcurrentHashMap.newKeySet();
    private static String originalWallpaper,modWallpaper;
    private static boolean windows(){return System.getProperty("os.name").toLowerCase(Locale.ROOT).startsWith("win");}
    static boolean typingAvailable(){return true;}
    static boolean tryRun(String name) {
        if(!Set.of("DOYOUSEEME","COMMAND","EJECT","FREEDOM","NOTEPAD").contains(name))return false;
        var client=Minecraft.getInstance();var player=client.player;
        if(player==null||!ExternalConsent.computerAllowed())return true;
        if(name.equals("NOTEPAD")){
            String user=ExternalConsent.personalAllowed()?System.getProperty("user.name"):Component.translatable("events.splitself.redacted_name").getString();
            List<String> lines=new ArrayList<>();
            for(int i=1;i<=5;i++)lines.add(Component.translatable("events.splitself.notepad.line"+i,user).getString());
            showTypingWindow(lines);return true;
        }
        RemainingClientEvents.worker(()->{
            if(!ExternalConsent.computerAllowed()||client.player!=player)return;
            try{
                switch(name){
                    case "DOYOUSEEME"->wallpaper();
                    case "FREEDOM"->freedom();
                    case "EJECT"->eject(player);
                    case "COMMAND"->{
                        if(ExternalConsent.computerAllowed())CommandEventScript.open();
                    }
                }
            }catch(Exception failure){RemainingClientEvents.failure(name,failure);}
        });return true;
    }
    static void showTypingWindow(List<String> messages){
        RemainingClientEvents.worker(()->{try{
            RemainingDesktopEvents.launch("com.pryzmm.desktop.DesktopTypingWindow",null,0,"typing",String.join("\n",messages));
        }catch(Exception failure){RemainingClientEvents.failure("NOTEPAD",failure);}});
    }
    private static boolean allowed(String event){return EditionPolicy.eventAllowed(event)&&ExternalConsent.computerAllowed();}
    private static void eject(net.minecraft.client.player.LocalPlayer player)throws Exception{
        var client=Minecraft.getInstance();
                    String os=System.getProperty("os.name").toLowerCase(Locale.ROOT);
                    List<String> drives=UnsafeDriveRules.drives(os,execute(UnsafeDriveRules.detection(os)));
                    if(drives.isEmpty() && !os.contains("win") && !os.contains("mac")) {
                        String output=execute(List.of("bash","-c","cat /proc/sys/dev/cdrom/info | grep 'drive name' | awk '{for(i=3;i<=NF;i++) print \"/dev/\" $i}'"));
                        drives=output.lines().map(String::trim).filter(s->s.startsWith("/dev/")).toList();
                    }
                    for(String drive:drives) {
                        if(!allowed("EJECT") || client.player!=player)return;
                        execute(UnsafeDriveRules.eject(os,drive));
                    }
    }
    private static String execute(List<String> command) throws Exception {
        if(!allowed("EJECT"))return "";
        Process process=new ProcessBuilder(command).redirectErrorStream(true).start();
        if(!process.waitFor(10,TimeUnit.SECONDS)){process.destroyForcibly();throw new java.io.IOException("CD-ROM command timeout");}
        String output=new String(process.getInputStream().readAllBytes(),java.nio.charset.StandardCharsets.UTF_8);
        return process.exitValue()==0 ? output : "";
    }
    static void freedom() throws Exception {
        if(!EditionPolicy.eventAllowed("FREEDOM"))return;
        String os=System.getProperty("os.name").toLowerCase(Locale.ROOT);
        if(windows())script("freedom.ps1",true);
        else if(os.contains("mac"))script("freedom.js",false);
    }
    private static void script(String resource,boolean powershell) throws Exception {
        if(!ExternalConsent.computerAllowed())return;
        Path file=Files.createTempFile("splitself-freedom-",powershell?".ps1":".js");
        try(var input=RemainingDesktopEvents.class.getResourceAsStream("/assets/splitself/"+resource)){
            if(input==null)throw new FileNotFoundException(resource);Files.copy(input,file,StandardCopyOption.REPLACE_EXISTING);
        }
        try {
            if(!ExternalConsent.computerAllowed())return;
            var args=powershell?List.of("powershell.exe","-NoProfile","-WindowStyle","Hidden","-ExecutionPolicy","Bypass","-File",file.toString())
                    :List.of("osascript","-l","JavaScript",file.toString());
            Process child=new ProcessBuilder(args).redirectErrorStream(true).redirectOutput(ProcessBuilder.Redirect.DISCARD).start();CHILDREN.add(child);
            try {if(!child.waitFor(15,TimeUnit.SECONDS))child.destroyForcibly();}finally{CHILDREN.remove(child);}
        }finally{Files.deleteIfExists(file);}
    }
    private static String currentWallpaper() throws Exception {
        Process p=new ProcessBuilder("reg","query","HKCU\\Control Panel\\Desktop","/v","Wallpaper").redirectErrorStream(true).start();
        if(!p.waitFor(5,TimeUnit.SECONDS)){p.destroyForcibly();throw new IOException("Wallpaper lookup timeout");}
        String output=new String(p.getInputStream().readAllBytes(),java.nio.charset.Charset.defaultCharset());
        for(String line:output.split("\\R")){var parts=line.trim().split("REG_SZ",2);if(parts.length==2)return parts[1].trim();}return null;
    }
    private static void setWallpaper(Path file) throws Exception {
        String escaped=file.toString().replace("'","''");
        String script="Add-Type -AssemblyName System.Drawing; $img=[Drawing.Image]::FromFile('"+escaped+"'); "+
                "$bmp=Join-Path $env:TEMP 'splitself-wallpaper.bmp'; $img.Save($bmp,[Drawing.Imaging.ImageFormat]::Bmp); $img.Dispose(); "+
                "Add-Type 'using System; using System.Runtime.InteropServices; public class Wallpaper { [DllImport(\"user32.dll\", CharSet=CharSet.Auto)] public static extern int SystemParametersInfo(int a,int b,string c,int d); }'; [Wallpaper]::SystemParametersInfo(20,0,$bmp,3)";
        Process p=new ProcessBuilder("powershell.exe","-NoProfile","-WindowStyle","Hidden","-Command",script).redirectErrorStream(true).redirectOutput(ProcessBuilder.Redirect.DISCARD).start();
        if(!p.waitFor(10,TimeUnit.SECONDS)){p.destroyForcibly();throw new IOException("Wallpaper update timeout");}
        if(p.exitValue()!=0)throw new IOException("Wallpaper update failed");
    }
    static synchronized void wallpaper() throws Exception {
        if(!EditionPolicy.eventAllowed("DOYOUSEEME"))return;
        if(!ExternalConsent.computerAllowed()||!windows())return;
        if(originalWallpaper==null){originalWallpaper=currentWallpaper();if(originalWallpaper==null||!Files.isRegularFile(Path.of(originalWallpaper))){originalWallpaper=null;return;}}
        Path file=Files.createTempFile("splitself-doyouseeme-",".png");
        try(var input=RemainingDesktopEvents.class.getResourceAsStream("/assets/splitself/textures/wallpaper/doyouseeme.png")){
            if(input==null)throw new FileNotFoundException("doyouseeme.png");Files.copy(input,file,StandardCopyOption.REPLACE_EXISTING);
        }
        try{if(ExternalConsent.computerAllowed()){setWallpaper(file);modWallpaper=currentWallpaper();}}finally{Files.deleteIfExists(file);}
    }
    static synchronized void cleanup(){
        CHILDREN.forEach(Process::destroyForcibly);CHILDREN.clear();
        try{if(originalWallpaper!=null&&Objects.equals(modWallpaper,currentWallpaper()))setWallpaper(Path.of(originalWallpaper));}
        catch(Exception failure){RemainingClientEvents.failure("Wallpaper restore",failure);}
        originalWallpaper=null;modWallpaper=null;
    }
}
