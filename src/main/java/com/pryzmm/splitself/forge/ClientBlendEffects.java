package com.pryzmm.splitself.forge;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
@Mod.EventBusSubscriber(modid=SplitSelfForge.MOD_ID,value=Dist.CLIENT)
public final class ClientBlendEffects{
    private static boolean modify;private static int active;private static long changed,lastDisable=System.nanoTime();private static ResourceLocation dimension;
    private ClientBlendEffects(){}
    static void toggle(){modify=!modify;}
    static int active(){return modify?active:0;}
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event){if(event.phase!=TickEvent.Phase.END)return;var c=Minecraft.getInstance();if(c.level==null)return;var next=c.level.dimension().location();
        if(dimension!=null&&!next.equals(dimension)){if(next.equals(SplitSelfWorldEvents.EMPTY.location()))modify=true;else if(dimension.equals(SplitSelfWorldEvents.EMPTY.location()))modify=false;}dimension=next;}
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut event){modify=false;active=0;dimension=null;}
    @SubscribeEvent public static void frame(RenderGuiEvent.Pre event){var c=Minecraft.getInstance();long now=System.nanoTime();
        if(modify&&active==0&&(now-lastDisable)/1_000_000_000f>=30&&Math.random()*20<1){
            // Kept source early return: Empty itself prevents starting a fresh blend.
            if(c.level!=null&&c.level.dimension().equals(SplitSelfWorldEvents.EMPTY))return;
            active=1+(int)(Math.random()*2);changed=now;c.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(net.minecraft.sounds.SoundEvent.createVariableRangeEvent(new ResourceLocation("splitself","tone")),(float)(.5+Math.random()/2)));
        }else if(modify&&active!=0&&(now-changed)/1_000_000_000f>=4&&Math.random()*20<1){active=0;lastDisable=now;c.getSoundManager().stop(new ResourceLocation("splitself","tone"),net.minecraft.sounds.SoundSource.MASTER);}
    }
}
