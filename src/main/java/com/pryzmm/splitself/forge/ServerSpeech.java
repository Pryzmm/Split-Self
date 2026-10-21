package com.pryzmm.splitself.forge;
import java.util.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
@Mod.EventBusSubscriber(modid=SplitSelfForge.MOD_ID)
public final class ServerSpeech{
    private static final Map<UUID,Long> LAST=new HashMap<>();
    private ServerSpeech(){}
    static void receive(ServerPlayer player,String text){
        if(!SpeechRules.valid(text)||player.level().dimension().equals(SplitSelfWorldEvents.LIMBO))return;
        long now=player.server.overworld().getGameTime();Long last=LAST.get(player.getUUID());if(last!=null&&now-last<20)return;LAST.put(player.getUUID(),now);
        String key=SpeechRules.response(StoryChat.language(player.getLanguage()),text);
        if(key!=null)player.server.getPlayerList().broadcastSystemMessage(Component.literal("<"+player.getName().getString()+"> ").append(Component.translatable("chat.splitself.response."+key)),false);
        else StoryChat.spoken(player,text);
    }
    /** Operator text test; only the story reply is shown, no diagnostic chatter. */
    static int test(ServerPlayer player,String text){
        if(!SpeechRules.valid(text)){return 0;}
        if(player.level().dimension().equals(SplitSelfWorldEvents.LIMBO)){return 0;}
        String normalized=SpeechRules.normalize(text);
        String selected=player.getLanguage();
        for(String locale:new java.util.LinkedHashSet<>(java.util.Arrays.asList(selected,"en_us","ru_ru"))){
            var language=StoryChat.language(locale);
            String key=SpeechRules.response(language,normalized);
            if(key==null&&!player.level().dimension().equals(SplitSelfWorldEvents.EMPTY))key=StoryChat.responseKey(language,player.getName().getString(),StoryChat.normalize(normalized));
            if(key!=null){
                final String response=key;
                player.server.getPlayerList().broadcastSystemMessage(Component.literal("<"+player.getName().getString()+"> ").append(Component.translatable("chat.splitself.response."+response)),false);
                return 1;
            }
        }
        return 0;
    }
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent event){LAST.remove(event.getEntity().getUUID());}
    @SubscribeEvent public static void stopped(ServerStoppedEvent event){LAST.clear();}
}
