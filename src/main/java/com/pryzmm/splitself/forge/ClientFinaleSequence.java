package com.pryzmm.splitself.forge;

import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid=SplitSelfForge.MOD_ID,value=Dist.CLIENT)
public final class ClientFinaleSequence {
    private static final List<FinaleRules.Dialogue> DIALOGUES=new ArrayList<>();
    private static final List<FinaleRules.Cycle> CYCLES=new ArrayList<>();
    private static final EnumSet<FinaleRules.Effect> FLAGS=EnumSet.noneOf(FinaleRules.Effect.class);
    private static long tick;
    private ClientFinaleSequence(){}
    public static boolean broken(String name){return FLAGS.contains(FinaleRules.Effect.valueOf(name));}
    static void start(String mode){
        var client=Minecraft.getInstance();if(client.player==null)return;
        if(mode.equals("STOP")){reset();return;}
        DIALOGUES.add(new FinaleRules.Dialogue(mode,tick,client.player.getUUID()));
        if(mode.equals("DELETE")){ExternalClientEvents.writeStoryFile("bad_end");sound("delete");startVisual();}
        if(mode.equals("CANCEL"))ExternalClientEvents.writeStoryFile("good_end");
        if(mode.equals("VISUAL"))startVisual();
    }
    private static void startVisual(){if(!CYCLES.isEmpty())return;for(var effect:FinaleRules.Effect.values())CYCLES.add(new FinaleRules.Cycle(effect,tick,Math.random()));}
    private static void sound(String name){var p=Minecraft.getInstance().player;if(p!=null)p.playSound(SoundEvent.createVariableRangeEvent(new ResourceLocation("splitself",name)),1,1);}
    private static void reset(){DIALOGUES.clear();CYCLES.clear();FLAGS.clear();ClientActivity.set(false);}
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut event){reset();}
    @SubscribeEvent public static void onTick(TickEvent.ClientTickEvent event){
        if(event.phase!=TickEvent.Phase.END)return;tick++;
        var client=Minecraft.getInstance();
        DIALOGUES.removeIf(dialogue -> dialogue.advance(tick,
                client.player == null ? null : client.player.getUUID(), index -> {
            if (dialogue.mode.equals("DELETE") && index == 2) sound("delete_hum");
            else if (dialogue.mode.equals("DELETE") && index == 3) client.stop();
            else {
                String section = dialogue.mode.equals("INTRO") ? "intro" : dialogue.mode.equals("DELETE") ? "delete" : "cancel";
                client.player.sendSystemMessage(Component.translatable("chat.splitself.final." + section + (index + 1),
                        client.player.getName().getString()));
            }
        }));
        boolean extraExperience=false;
        for(var cycle:List.copyOf(CYCLES))if(cycle.advance(tick,Math::random,(effect,on)->{if(on)FLAGS.add(effect);else FLAGS.remove(effect);}))extraExperience=true;
        CYCLES.removeIf(c->c.finished);
        if(extraExperience)CYCLES.add(new FinaleRules.Cycle(FinaleRules.Effect.EXPERIENCE,tick,Math.random()));
    }
    @SubscribeEvent public static void overlay(RenderGuiOverlayEvent.Pre event){
        String id=event.getOverlay().id().getPath();
        boolean hide=switch(id){case "hotbar"->broken("HOTBAR");case "player_health"->broken("HEALTH");case "food_level"->broken("FOOD");case "armor_level"->broken("ARMOR");case "experience_bar"->broken("EXPERIENCE");default->false;};
        if(hide)event.setCanceled(true);
    }
    @SubscribeEvent public static void black(RenderGuiEvent.Post event){if(broken("SCREEN"))event.getGuiGraphics().fill(0,0,event.getGuiGraphics().guiWidth(),event.getGuiGraphics().guiHeight(),0xff000000);}
    @SubscribeEvent public static void fog(ViewportEvent.ComputeFogColor event){if(broken("SKY")){event.setRed(0);event.setGreen(0);event.setBlue(0);}}
    @SubscribeEvent public static void clouds(RenderLevelStageEvent event){
        if(event.getStage()!=RenderLevelStageEvent.Stage.AFTER_SKY||!broken("CLOUDS"))return;
        var client=Minecraft.getInstance();if(client.level==null)return;float height=client.level.effects().getCloudHeight();if(Float.isNaN(height))return;
        var pose=event.getPoseStack();pose.pushPose();pose.scale(12,1,12);pose.translate(0,height-event.getCamera().getPosition().y-.1,0);
        com.mojang.blaze3d.systems.RenderSystem.disableCull();com.mojang.blaze3d.systems.RenderSystem.disableBlend();
        com.mojang.blaze3d.systems.RenderSystem.setShader(net.minecraft.client.renderer.GameRenderer::getPositionTexShader);
        com.mojang.blaze3d.systems.RenderSystem.setShaderTexture(0,new ResourceLocation("splitself","textures/misc/error/broken_clouds.png"));
        com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1,1,1,1);
        try{
            var b=com.mojang.blaze3d.vertex.Tesselator.getInstance().getBuilder();b.begin(com.mojang.blaze3d.vertex.VertexFormat.Mode.QUADS,com.mojang.blaze3d.vertex.DefaultVertexFormat.POSITION_TEX);
            var m=pose.last().pose();b.vertex(m,-128,0,-128).uv(0,0).endVertex();b.vertex(m,-128,0,128).uv(0,1).endVertex();b.vertex(m,128,0,128).uv(1,1).endVertex();b.vertex(m,128,0,-128).uv(1,0).endVertex();
            com.mojang.blaze3d.vertex.BufferUploader.drawWithShader(b.end());
        }finally{com.mojang.blaze3d.systems.RenderSystem.enableCull();pose.popPose();}
    }
}
