package com.pryzmm.splitself.forge;

import java.util.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid=SplitSelfForge.MOD_ID)
public final class RemainingWorldEvents {
    private record Surround(ServerPlayer player,List<ServerPlayer> viewers,int remaining,net.minecraft.world.phys.Vec3 anchor,net.minecraft.server.level.ServerLevel world) {}
    private static final List<Surround> SURROUND=new ArrayList<>();
    private static final RemainingEventRules.Grants GRANTS=new RemainingEventRules.Grants();
    private RemainingWorldEvents() {}
    static boolean run(ServerPlayer player,String event,boolean callerOnly) {
        List<ServerPlayer> viewers=callerOnly?List.of(player):List.copyOf(player.server.getPlayerList().getPlayers());
        if(event.equals("THEOTHERSCREENSHOT")) {
            EncounterSpawns.theOther(player,true);
            viewers.forEach(p->StoryNetwork.clientEvent(p,event));return true;
        }
        if(event.equals("SURROUND")) {
            if(SURROUND.stream().anyMatch(job->job.player==player))return false;
            viewers.forEach(p->StoryNetwork.clientEvent(p,"SURROUND_GLITCH"));
            player.serverLevel().playSound(null,player.blockPosition(),SoundEvent.createVariableRangeEvent(
                    new ResourceLocation("splitself","glitch")),SoundSource.MASTER,1,1);
            SURROUND.add(new Surround(player,viewers,100,BatchWorldEvents.shifted(player),player.serverLevel()));return true;
        }
        return false;
    }
    static void memory(ServerPlayer player) {
        if(GRANTS.pending(player.getUUID(),System.nanoTime()/1_000_000))return;
        int stage=MemoryData.get(player.server.overworld()).memoryStage();
        UUID token=GRANTS.offer(player.getUUID(),stage,System.nanoTime()/1_000_000);
        StoryNetwork.memoryOffer(player,token,stage);
    }
    static void completeMemory(ServerPlayer player,UUID token,boolean success) {
        var data=MemoryData.get(player.server.overworld());int stage=data.memoryStage();
        if(GRANTS.consume(player.getUUID(),token,stage,System.nanoTime()/1_000_000)&&success)data.setMemoryStage(RemainingEventRules.nextMemoryStage(stage));
    }
    @SubscribeEvent public static void tick(TickEvent.ServerTickEvent event) {
        if(event.phase!=TickEvent.Phase.END)return;
        var iterator=SURROUND.listIterator();
        while(iterator.hasNext()) {
            var job=iterator.next();
            if(job.player.server!=event.getServer())continue;
            boolean done=job.player.isRemoved() || job.remaining<=1;
            try {if(!job.player.isRemoved())BatchWorldEvents.lift(job.player,14,job.anchor,job.world);}
            catch(RuntimeException failure){org.slf4j.LoggerFactory.getLogger("splitself").warn("SURROUND lift failed",failure);done=true;}
            if(done) {
                job.viewers.stream().filter(p->!p.isRemoved()).forEach(p->StoryNetwork.clientEvent(p,"SURROUND_GLITCH"));iterator.remove();
            }else iterator.set(new Surround(job.player,job.viewers,job.remaining-1,job.anchor,job.world));
        }
    }
    @SubscribeEvent public static void stopped(ServerStoppedEvent event){SURROUND.clear();GRANTS.clear();}
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent event){GRANTS.remove(event.getEntity().getUUID());}
}
