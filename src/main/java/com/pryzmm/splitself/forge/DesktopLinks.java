package com.pryzmm.splitself.forge;

import java.awt.Desktop;
import java.net.URI;
import java.nio.file.Path;
import java.util.*;

/** Native launch routes work even when Minecraft initialized AWT in headless mode. */
final class DesktopLinks {
    private DesktopLinks(){}
    static List<String> command(String os,String target){
        String lower=os.toLowerCase(Locale.ROOT);
        if(lower.startsWith("win"))return List.of("powershell.exe","-NoProfile","-NonInteractive","-WindowStyle","Hidden","-Command","Start-Process -FilePath $env:SPLITSELF_OPEN_TARGET -ErrorAction Stop");
        if(lower.contains("mac"))return List.of("open",target);
        return List.of("xdg-open",target);
    }
    static void open(String target)throws Exception{
        if(!ExternalConsent.computerAllowed())return;
        if(Desktop.isDesktopSupported()){
            try{var desktop=Desktop.getDesktop();URI uri=URI.create(target);
                if(uri.getScheme().equals("file")){desktop.open(Path.of(uri).toFile());return;}
                if(desktop.isSupported(Desktop.Action.BROWSE)){desktop.browse(uri);return;}
            }catch(Exception ignored){}
        }
        if(!ExternalConsent.computerAllowed())return;
        ProcessBuilder builder=new ProcessBuilder(command(System.getProperty("os.name"),target));
        builder.environment().put("SPLITSELF_OPEN_TARGET",target);
        Process process=builder.redirectErrorStream(true).redirectOutput(ProcessBuilder.Redirect.DISCARD).start();
        if(process.waitFor(10,java.util.concurrent.TimeUnit.SECONDS)&&process.exitValue()!=0)throw new java.io.IOException("OS could not open target; check its registered application");
    }
}
