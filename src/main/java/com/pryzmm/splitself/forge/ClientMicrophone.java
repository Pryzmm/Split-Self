package com.pryzmm.splitself.forge;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
/** Optional offline Windows recognizer. No recording begins without all three local switches. */
@Mod.EventBusSubscriber(modid=SplitSelfForge.MOD_ID,value=Dist.CLIENT)
public final class ClientMicrophone{
    private static volatile Process child;private static volatile boolean running;private static boolean attempted;
    private ClientMicrophone(){}
    private static boolean allowed(){return SpeechRules.allowed(ExternalConsent.enabled(),ExternalConsent.personalAllowed(),ExternalConsent.MICROPHONE.get());}
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event){
        if(event.phase!=TickEvent.Phase.END)return;var client=Minecraft.getInstance();
        if(!allowed()||client.player==null){stop();attempted=false;return;}
        if(attempted||running)return;attempted=true;
        if(!System.getProperty("os.name").toLowerCase(Locale.ROOT).startsWith("win")){org.slf4j.LoggerFactory.getLogger("splitself").warn("Microphone backend requires Windows System.Speech; unavailable on this OS");return;}
        var recipient=client.player;String language=client.getLanguageManager().getSelected().replace('_','-');running=true;
        Thread worker=new Thread(()->{
            Path script=null;Process process=null;
            try{
                script=Files.createTempFile("splitself-microphone-",".ps1");try(var input=ClientMicrophone.class.getResourceAsStream("/assets/splitself/microphone.ps1")){if(input==null)throw new java.io.IOException("Missing microphone script");Files.copy(input,script,StandardCopyOption.REPLACE_EXISTING);}
                if(!allowed()||client.player!=recipient)return;
                process=new ProcessBuilder("powershell.exe","-NoProfile","-WindowStyle","Hidden","-ExecutionPolicy","Bypass","-File",script.toString(),"-Language",language).redirectError(ProcessBuilder.Redirect.DISCARD).start();child=process;
                if(!allowed()){process.destroyForcibly();return;}
                try(var reader=process.inputReader(StandardCharsets.UTF_8)){String line;while((line=reader.readLine())!=null){
                    if(!allowed()||client.player!=recipient)break;if(line.length()>4096)continue;
                    try{String text=new String(Base64.getDecoder().decode(line),StandardCharsets.UTF_8);if(SpeechRules.valid(text))client.execute(()->{if(allowed()&&client.player==recipient)StoryNetwork.speech(text);});}catch(IllegalArgumentException ignored){}
                }}
            }catch(Exception failure){org.slf4j.LoggerFactory.getLogger("splitself").warn("Offline microphone unavailable",failure);}
            finally{if(process!=null&&process.isAlive())process.destroyForcibly();child=null;running=false;if(script!=null)try{Files.deleteIfExists(script);}catch(Exception ignored){}}
        },"splitself-microphone");worker.setDaemon(true);worker.start();
    }
    private static void stop(){Process process=child;if(process!=null)process.destroyForcibly();}
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut event){stop();attempted=false;}
}
