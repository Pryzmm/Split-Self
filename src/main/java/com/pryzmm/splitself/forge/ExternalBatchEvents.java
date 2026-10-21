package com.pryzmm.splitself.forge;

import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Opt-in external effects. Tests only exercise pure policies, never this boundary. */
@Mod.EventBusSubscriber(modid=SplitSelfForge.MOD_ID,value=Dist.CLIENT)
public final class ExternalBatchEvents {
    private static final ExecutorService WORKER=Executors.newSingleThreadExecutor(task->{var t=new Thread(task,"splitself-batch-external");t.setDaemon(true);return t;});
    private record Delayed(long tick, Runnable callback) {}
    private static final java.util.List<Delayed> DELAYED=new java.util.ArrayList<>();
    private static long tick;
    private static final java.util.Map<String,String> NAME_CACHE=new java.util.HashMap<>();
    private static final java.util.Map<String,Long> NAME_RETRY=new java.util.HashMap<>();
    private ExternalBatchEvents() {}
    private static boolean allowed(String name) {
        return EditionPolicy.eventAllowed(name) && ExternalBatchRules.allowed(name,ExternalConsent.computerAllowed(),ExternalConsent.personalAllowed());
    }
    static void run(String name) {
        var client=Minecraft.getInstance(); var player=client.player;
        if(player==null) return;
        if(!allowed(name)) {
            player.sendSystemMessage(Component.literal(name+" skipped: local externalEffects/PII permission is OFF")); return;
        }
        String current=player.getName().getString(); String uuid=player.getUUID().toString();
        WORKER.execute(()->{
            if(!allowed(name) || client.player!=player) return;
            try {
                if(name.equals("NAME")) {
                    long now=System.currentTimeMillis();
                    String cached=NAME_CACHE.get(uuid);
                    if(NAME_RETRY.getOrDefault(uuid,0L)>now){
                        if(cached!=null&&!cached.isEmpty())deliverName(player,cached);
                        else nameStatus(player,"NAME: повторный запрос временно отложен; сервис истории имён недоступен или история пуста.");
                        return;
                    }
                    var http=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
                    var request=HttpRequest.newBuilder(URI.create("https://api.ashcon.app/mojang/v2/user/"+uuid)).timeout(Duration.ofSeconds(10)).header("User-Agent","SplitSelf/1.0").GET().build();
                    if(!allowed(name))return;
                    var response=http.send(request,HttpResponse.BodyHandlers.ofString());
                    if(response.statusCode()!=200){
                        long delay=300000;
                        try{delay=Math.max(60000,Math.min(3600000,Long.parseLong(response.headers().firstValue("Retry-After").orElse("300"))*1000));}catch(NumberFormatException ignored){}
                        NAME_RETRY.put(uuid,now+delay);
                        nameStatus(player,"NAME: сервис истории имён ответил HTTP "+response.statusCode()+"; повтор отложен.");return;
                    }
                    String previous=ExternalBatchRules.previousName(response.body(),current);
                    NAME_CACHE.put(uuid,previous==null?"":previous);NAME_RETRY.put(uuid,now+600000);
                    if(previous!=null)deliverName(player,previous);
                    else nameStatus(player,"NAME: сервис не вернул прежнего имени игрока.");                } else if(name.equals("RECORD")) {
                    String process;
                    try(var processes=ProcessHandle.allProcesses()) {
                        process=ExternalBatchRules.recorder(processes.map(p->p.info().command().orElse("")));
                    }
                    if(process!=null) client.execute(()->{
                        if(!allowed(name) || client.player!=player)return;
                        player.sendSystemMessage(Component.translatable("events.splitself.record.detection",process).withStyle(ChatFormatting.RED));
                        DELAYED.add(new Delayed(tick+100,()->{
                            if(allowed(name) && client.player==player)player.sendSystemMessage(Component.translatable("events.splitself.record.fail",process).withStyle(ChatFormatting.RED));
                        }));
                    });
                }
            } catch(Exception failure) {
                org.slf4j.LoggerFactory.getLogger("splitself").warn("External event {} failed",name,failure);
            }
        });
    }
    private static void nameStatus(net.minecraft.client.player.LocalPlayer player,String text){
        Minecraft.getInstance().execute(()->{if(allowed("NAME")&&Minecraft.getInstance().player==player)player.sendSystemMessage(Component.literal(text));});
    }
    private static void deliverName(net.minecraft.client.player.LocalPlayer player,String previous){
        Minecraft.getInstance().execute(()->{if(allowed("NAME")&&Minecraft.getInstance().player==player)player.sendSystemMessage(Component.literal("<"+previous+"> ").append(Component.translatable("events.splitself.sign.imWatchingYou")));});
    }
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event) {
        if(event.phase!=TickEvent.Phase.END)return;
        tick++;
        if(!allowed("NAME"))WORKER.execute(()->{NAME_CACHE.clear();NAME_RETRY.clear();});
        DELAYED.removeIf(task->{if(task.tick>tick)return false;task.callback.run();return true;});
    }
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut event) { DELAYED.clear(); }
}
