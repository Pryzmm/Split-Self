package com.pryzmm.splitself.forge;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.*;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
@Mod.EventBusSubscriber(modid=SplitSelfForge.MOD_ID,value=Dist.CLIENT)
public final class ClientMenus{
    private static boolean introduction,dismissed;private static final Map<TitleScreen,Long> OPEN=new WeakHashMap<>();private static final Map<TitleScreen,java.util.List<Button>> BANNERS=new WeakHashMap<>();
    private ClientMenus(){}
    @SubscribeEvent public static void init(ScreenEvent.Init.Post event){
        var screen=event.getScreen();var client=Minecraft.getInstance();
        if (screen instanceof PauseScreen) {
            event.addListener(Button.builder(Component.translatable("config.splitself.title"),
                    button -> ClientSettingsScreen.open(screen)).bounds(Math.max(0,screen.width-110),6,104,20).build());
        }
        if(!(screen instanceof TitleScreen title))return;
        for(var child:screen.children())if(child instanceof Button button){String text=button.getMessage().getString();if(text.equals(Component.translatable("menu.multiplayer").getString())||text.equals(Component.translatable("menu.online").getString())){button.active=false;button.setTooltip(Tooltip.create(Component.translatable(text.equals(Component.translatable("menu.online").getString())?"misc.splitself.realms":"misc.splitself.multiplayer")));}}
        OPEN.put(title,System.nanoTime());BANNERS.remove(title);addBanner(event,title);
        if(!introduction){introduction=true;client.execute(()->{if(client.screen==title)client.setScreen(new Introduction(title));});}
    }
    private static void addBanner(ScreenEvent.Init.Post event,TitleScreen title){
        if(dismissed)return;var client=Minecraft.getInstance();var preferred=LanguageAdvisor.preferLanguageForPlayer(client.getResourceManager(),client.getLanguageManager().getSelected());if(preferred.isEmpty())return;
        LangToastMessages.reload(client.getResourceManager());var lines=LangToastMessages.resolve(Locale.getDefault(),preferred.get());
        Button recommend=Button.builder(Component.literal(lines.title()+" "+LanguageAdvisor.prettyName(preferred.get()).getString()),b->client.setScreen(new LanguageSelectScreen(title,client.options,client.getLanguageManager()))).bounds(Math.max(0,title.width-222),2,198,32).tooltip(Tooltip.create(Component.literal(lines.body().replace("{language}",LanguageAdvisor.prettyName(preferred.get()).getString())))).build();
        Button close=Button.builder(Component.literal("?"),b->{dismissed=true;recommend.visible=false;b.visible=false;}).bounds(Math.max(0,title.width-22),2,20,32).build();recommend.visible=false;close.visible=false;event.addListener(recommend);event.addListener(close);BANNERS.put(title,java.util.List.of(recommend,close));
    }
    @SubscribeEvent public static void render(ScreenEvent.Render.Post event){if(event.getScreen() instanceof TitleScreen title){boolean show=!dismissed&&(System.nanoTime()-OPEN.getOrDefault(title,System.nanoTime()))/1_000_000>=500;for(Button b:BANNERS.getOrDefault(title,java.util.List.of()))b.visible=show;}}
    private static final class Introduction extends Screen{
        private final Screen parent;private volatile String internet="—";
        Introduction(Screen parent){super(Component.empty());this.parent=parent;
            if(ExternalConsent.computerAllowed())RemainingClientEvents.worker(()->{try{if(!ExternalConsent.computerAllowed())return;var request=java.net.http.HttpRequest.newBuilder(java.net.URI.create("https://google.com/generate_204")).method("HEAD",java.net.http.HttpRequest.BodyPublishers.noBody()).timeout(java.time.Duration.ofSeconds(3)).build();int status=java.net.http.HttpClient.newBuilder().connectTimeout(java.time.Duration.ofSeconds(2)).build().send(request,java.net.http.HttpResponse.BodyHandlers.discarding()).statusCode();internet=status==200||status==204?"✓":"×";}catch(Exception failure){internet="×";}});
        }
        @Override protected void init(){addRenderableWidget(Button.builder(Component.translatable("gui.continue"),b->onClose()).bounds(width/2-50,height-50,100,20).build());}
        @Override public void onClose(){minecraft.setScreen(parent);}
        @Override public void render(GuiGraphics g,int x,int y,float delta){renderBackground(g);g.drawCenteredString(font,Component.translatable("misc.splitself.best_experience"),width/2,height/4+30,0xffffff);
            g.drawCenteredString(font,internet+" "+Component.translatable("misc.splitself.best_experience.internet").getString(),width/2,height/4+50,0xffffff);
            g.drawCenteredString(font,"✓ "+Component.translatable("misc.splitself.best_experience.externals").getString(),width/2,height/4+64,0xffffff);
            g.drawCenteredString(font,Component.translatable("warning.splitself.local_controls"),width/2,height/4+90,0xffffff);super.render(g,x,y,delta);}
    }
}
