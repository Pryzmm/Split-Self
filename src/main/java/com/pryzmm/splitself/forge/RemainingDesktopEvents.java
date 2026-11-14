package com.pryzmm.splitself.forge;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/** External actions are entered only after local opt-in. No static initializer touches the desktop. */
final class RemainingDesktopEvents {
    private static final Set<Process> CHILDREN=ConcurrentHashMap.newKeySet();
    private static final Set<TrayIcon> TRAYS=ConcurrentHashMap.newKeySet();
    private static dev.firstdark.rpc.DiscordRpc rpc;
    private static volatile String discordName;
    private static net.minecraft.client.player.LocalPlayer discordRecipient;
    private RemainingDesktopEvents() {}
    static String text(String key){return Component.translatable(key).getString();}
    private static boolean windows(){return System.getProperty("os.name").toLowerCase(Locale.ROOT).startsWith("win");}
    static synchronized void discord(String event) throws Exception {
        if(!ExternalConsent.computerAllowed())return;
        if(event.equals("DISCORDNAME")&&!ExternalConsent.personalAllowed())return;
        if(event.equals("DISCORDNAME"))discordRecipient=Minecraft.getInstance().player;
        if(rpc==null) {
            rpc=new dev.firstdark.rpc.DiscordRpc();
            try {rpc.init("1512578067590152192",new dev.firstdark.rpc.handlers.RPCEventHandler(){
                @Override public void ready(dev.firstdark.rpc.models.User user){
                    if(ExternalConsent.computerAllowed()&&ExternalConsent.personalAllowed()){discordName=user.getUsername();deliverDiscordName();}
                }
            },false);}catch(Exception failure){rpc=null;throw failure;}
        }
        rpc.runCallbacks();
        if(event.equals("RPC"))rpc.updatePresence(dev.firstdark.rpc.models.DiscordRichPresence.builder()
                .details(text("events.splitself.rpc.desc")).state(text("events.splitself.rpc.state")).largeImageKey("noise").build());
        else deliverDiscordName();
    }
    private static void deliverDiscordName(){
        if(discordName==null||discordRecipient==null)return;
        String name=discordName;var recipient=discordRecipient;discordRecipient=null;
        Minecraft.getInstance().execute(()->{
            if(Minecraft.getInstance().player==recipient&&ExternalConsent.computerAllowed()&&ExternalConsent.personalAllowed())
                recipient.sendSystemMessage(Component.translatable("events.splitself.discordName.friend",name).withStyle(style->style.withColor(7506394)));
        });
    }
    static synchronized void callbacks(){if(rpc!=null){if(ExternalConsent.computerAllowed())rpc.runCallbacks();else{rpc.shutdown();rpc=null;discordName=null;}}}
    static void reminder() {
        if(!ExternalConsent.computerAllowed())return;
        try{launch("com.pryzmm.desktop.DesktopCompanion",null,0,"reminder","You have an unread message from ████████████");}
        catch(Exception failure){RemainingClientEvents.failure("REMINDER",failure);}
    }
    static void launch(String main,UUID token,int stage)throws Exception{launch(main,token,stage,null,null);}
    static boolean appAllowed(String main){return ExternalConsent.enabled()&&(main.equals("com.pryzmm.memory.Memory")||ExternalConsent.computerAllowed());}
    static boolean appModeAllowed(String main,String mode){return appAllowed(main)&&(!"com.pryzmm.desktop.DesktopCompanion".equals(main)||"reminder".equals(mode));}
    static void launch(String main,UUID token,int stage,String mode,String body) throws Exception {
        if(!appModeAllowed(main,mode))return;
        // A child JVM cannot resolve Forge union: URLs. Give it a standalone jar with only the two apps/resources.
        Path apps=Files.createTempFile("splitself-desktop-apps-",".jar");
        try(var input=RemainingDesktopEvents.class.getResourceAsStream("/assets/splitself/desktop-apps.jar")){
            if(input==null)throw new FileNotFoundException("desktop-apps.jar");Files.copy(input,apps,StandardCopyOption.REPLACE_EXISTING);
        }
        String classpath=apps.toString();
        List<String> args=new ArrayList<>();args.add(Path.of(System.getProperty("java.home"),"bin",windows()?"javaw.exe":"java").toString());
        args.add("-Djava.awt.headless=false");args.add("-Dsplitself.memory.stage="+stage);
        for(String key:List.of("title","start","finish","complete","message1","message2","message3","message4"))args.add("-Dsplitself.memory."+key+"="+text("game.splitself.memory."+key));
        Path translations=Files.createTempFile("splitself-messages-",".properties");
        var properties=new Properties();
        // Resolve current language on Minecraft's thread before the worker writes/starts the child.
        properties.putAll(RemainingClientEvents.messageTranslations());
        try(var writer=Files.newBufferedWriter(translations,StandardCharsets.UTF_8)){properties.store(writer,"Split Self MineMessage");}
        args.add("-Dsplitself.messages="+translations);args.add("-cp");args.add(classpath);args.add(main);if(mode!=null){args.add(mode);args.add(Base64.getEncoder().encodeToString(body.getBytes(StandardCharsets.UTF_8)));}
        if(!appModeAllowed(main,mode)){Files.deleteIfExists(translations);Files.deleteIfExists(apps);return;}
        Process child=new ProcessBuilder(args).redirectErrorStream(true).start();CHILDREN.add(child);
        Thread pipe=new Thread(()->{
            boolean completed=false;
            try(var reader=child.inputReader(StandardCharsets.UTF_8)) {
                String line;
                while((line=reader.readLine())!=null){
                    if(line.equals("[SplitSelfDesktop] OPEN_MESSAGE")&&ExternalConsent.computerAllowed())RemainingClientEvents.worker(()->{try{launch("com.pryzmm.minemessage.MineMessage",null,0);}catch(Exception e){RemainingClientEvents.failure("REMINDER open",e);}});
                    if(!completed&&token!=null&&line.equals("[SplitSelfMemory] ADVANCE_STAGE")) {
                    completed=true;Minecraft.getInstance().execute(()->{if(ExternalConsent.enabled())RemainingClientEvents.completeMemory(token);});
                }
                }
            }catch(IOException failure){RemainingClientEvents.failure("Swing child",failure);}
            finally{CHILDREN.remove(child);try{Files.deleteIfExists(translations);Files.deleteIfExists(apps);}catch(IOException ignored){}
                if(token!=null)Minecraft.getInstance().execute(()->RemainingClientEvents.memoryClosed(token));}
        },"splitself-swing-output");pipe.setDaemon(true);pipe.start();
    }
    static synchronized void cleanup() {
        CHILDREN.forEach(Process::destroyForcibly);CHILDREN.clear();
        EventQueue.invokeLater(()->{if(SystemTray.isSupported())TRAYS.forEach(SystemTray.getSystemTray()::remove);TRAYS.clear();});
        if(rpc!=null){rpc.shutdown();rpc=null;}discordName=null;discordRecipient=null;
        EditionDesktopIntegrations.cleanup();
    }
}
