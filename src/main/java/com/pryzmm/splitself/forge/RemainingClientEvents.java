package com.pryzmm.splitself.forge;

import java.net.*;
import java.net.http.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid=SplitSelfForge.MOD_ID,value=Dist.CLIENT)
public final class RemainingClientEvents {
    private static final ExecutorService WORKER=Executors.newSingleThreadExecutor(r->{Thread t=new Thread(r,"splitself-remaining-external");t.setDaemon(true);return t;});
    private static volatile Map<String,String> translations=Map.of();
    private static UUID memoryToken;
    private static java.util.function.Consumer<Path> frameCapture;
    private static net.minecraft.client.player.LocalPlayer owner;
    private static long screenshotAt,emergencyUntil,scrollStart,lastShake,shrinkNext;
    private static String city="";
    private static int shakeX,shakeY,shrinkStep,originalWidth,originalHeight;
    private static boolean glitch,shrinking,externalWasEnabled,computerWasEnabled;
    private static boolean capturing;
    private static net.minecraft.world.entity.Entity oldCameraEntity,captureEntity;
    private static net.minecraft.client.CameraType oldCameraType;
    private RemainingClientEvents() {}
    static void worker(Runnable action){WORKER.execute(action);}
    static void failure(String event,Throwable failure){org.slf4j.LoggerFactory.getLogger("splitself").warn("{} failed",event,failure);}
    public static boolean windowActive(){return shrinking;}
    static boolean captureBusy(){return capturing;}
    static boolean emergencyActive(){return emergencyUntil>now();}
    static boolean requestFrameCapture(java.util.function.Consumer<Path> callback){
        if(screenshotAt!=0||capturing||!ExternalConsent.enabled()||!ExternalConsent.personalAllowed())return false;
        frameCapture=callback;owner=Minecraft.getInstance().player;screenshotAt=now();return true;
    }
    private static long now(){return System.nanoTime()/1_000_000;}
    static Map<String,String> messageTranslations(){return translations;}
    private static void cacheTranslations() {
        Map<String,String> map=new HashMap<>();
        // Source MineMessage keys come from the pinned English catalogue; values use the selected client language.
        try(var input=RemainingClientEvents.class.getResourceAsStream("/assets/splitself/lang/en_us.json")){
            var json=com.google.gson.JsonParser.parseReader(new java.io.InputStreamReader(input,StandardCharsets.UTF_8)).getAsJsonObject();
            for(String key:json.keySet())if(key.startsWith("game.splitself.minemessage")||key.startsWith("game.splitself.minemeassage"))map.put(key,Component.translatable(key).getString());
        }catch(Exception failure){failure("MineMessage translations",failure);}
        translations=Map.copyOf(map);
    }
    static void memory(UUID token,int stage) {
        if(!ExternalConsent.enabled()||System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("mac")||memoryToken!=null){StoryNetwork.cancelMemory(token);return;}
        owner=Minecraft.getInstance().player;memoryToken=token;cacheTranslations();
        worker(()->{try{RemainingDesktopEvents.launch("com.pryzmm.memory.Memory",token,stage);}catch(Exception failure){failure("MEMORY",failure);Minecraft.getInstance().execute(()->memoryClosed(token));}});
    }
    static void memoryClosed(UUID token){if(token.equals(memoryToken)){memoryToken=null;if(Minecraft.getInstance().getConnection()!=null)StoryNetwork.cancelMemory(token);}}
    static void completeMemory(UUID token){if(token.equals(memoryToken)&&Minecraft.getInstance().player==owner){memoryToken=null;StoryNetwork.completeMemory(token);}}
    static void start(String event) {
        if (!EditionPolicy.eventAllowed(event)) return;
        var client=Minecraft.getInstance();if(client.player==null)return;
        if(event.equals("SURROUND_GLITCH")){glitch=!glitch;ClientActivity.set(glitch);if(!glitch)client.getSoundManager().stop(new ResourceLocation("splitself","glitch"),null);return;}
        if(!RemainingEventRules.allowed(event,ExternalConsent.computerAllowed(),ExternalConsent.personalAllowed()))return;
        owner=client.player;
        if(event.equals("THEOTHERSCREENSHOT")){screenshotAt=now()+3000;return;}
        if(event.equals("SHRINK")){shrink();return;}
        if(event.equals("MEMORY"))return; // Requires the server-issued offer, never trust a generic event packet.
        cacheTranslations();
        final var recipient=client.player;
        final boolean pii=ExternalConsent.personalAllowed();
        final String redacted=Component.translatable("events.splitself.redacted_name").getString();
        worker(()->{
            if(!ExternalConsent.computerAllowed()||client.player!=recipient)return;
            try {
                switch(event){
                    case "REMINDER" -> RemainingDesktopEvents.reminder();
                    case "RPC","DISCORDNAME" -> RemainingDesktopEvents.discord(event);
                    case "EMERGENCY","WEATHER" -> {
                        String location=pii?RemainingEventRules.city(get("http://ip-api.com/line/?fields=city,regionName,country")):redacted;
                        if(!ExternalConsent.computerAllowed()||client.player!=recipient||pii&&!ExternalConsent.personalAllowed())return;
                        if(event.equals("EMERGENCY"))client.execute(()->{
                            if(client.player!=recipient||!ExternalConsent.computerAllowed())return;
                            city=ExternalConsent.personalAllowed()?location:redacted;scrollStart=now();ClientActivity.set(true);emergencyUntil=now()+13000;sound("amber");
                        });
                        else {
                            message(recipient,"events.splitself.weather.report",location);
                            var weather=RemainingEventRules.weather(get("https://wttr.in/"+URLEncoder.encode(location,StandardCharsets.UTF_8)+"?format=j1"));
                            if(!weather.valid())message(recipient,"events.splitself.weather.fail",location);
                            else {
                                message(recipient,"events.splitself.weather.loading");
                                CompletableFuture.delayedExecutor(200,TimeUnit.MILLISECONDS).execute(()->{
                                    if(pii&&!ExternalConsent.personalAllowed())return;
                                    message(recipient,"events.splitself.weather.temp",weather.c(),weather.f());
                                    message(recipient,"events.splitself.weather.weather",weather.condition());
                                });
                            }
                        }
                    }
                }
            }catch(Exception failure){failure(event,failure);}
        });
    }
    private static String get(String url) throws Exception {
        if(!ExternalConsent.computerAllowed())throw new IllegalStateException("Computer interactions disabled");
        var request=HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(10)).header("User-Agent","SplitSelf/1.0").build();
        var response=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build().send(request,HttpResponse.BodyHandlers.ofString());
        if(response.statusCode()!=200)throw new java.io.IOException("HTTP "+response.statusCode());return response.body();
    }
    private static void message(net.minecraft.client.player.LocalPlayer recipient,String key,Object...args) {
        Minecraft.getInstance().execute(()->{
            if(!ExternalConsent.computerAllowed()||Minecraft.getInstance().player!=recipient)return;
            if((key.endsWith("weather.report")||key.endsWith("weather.fail"))&&!ExternalConsent.personalAllowed())
                args[0]=Component.translatable("events.splitself.redacted_name").getString();
            recipient.sendSystemMessage(Component.translatable(key,args));
        });
    }
    private static void sound(String name){var p=Minecraft.getInstance().player;if(p!=null)p.playSound(SoundEvent.createVariableRangeEvent(new ResourceLocation("splitself",name)),1,1);}
    private static void shrink() {
        if(shrinking)return;
        var client=Minecraft.getInstance();
        if(client.getWindow().isFullscreen())client.getWindow().toggleFullScreen();
        if(client.getWindow().isFullscreen())return;
        sound("rumble2");
        client.player.sendSystemMessage(Component.literal("<"+client.player.getName().getString()+"> "+Component.translatable("events.splitself.shrink.message").getString()));
        int[] w={0},h={0};GLFW.glfwGetWindowSize(client.getWindow().getWindow(),w,h);originalWidth=w[0];originalHeight=h[0];
        shrinkStep=0;shrinkNext=now();shrinking=true;
    }
    private static void shrinkStep() {
        if(!shrinking||now()<shrinkNext)return;
        var client=Minecraft.getInstance();long window=client.getWindow().getWindow();
        if(!ExternalConsent.computerAllowed()||client.player==null){stopShrink();return;}
        if(shrinkStep<50) {
            int w=RemainingEventRules.shrinkSize(originalWidth,shrinkStep),h=RemainingEventRules.shrinkSize(originalHeight,shrinkStep);
            var mode=GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());if(mode==null){stopShrink();return;}
            GLFW.glfwSetWindowSize(window,w,h);GLFW.glfwSetWindowPos(window,(mode.width()-w)/2,(mode.height()-h)/2);
        }else if(shrinkStep<250) {
            int[] x={0},y={0};GLFW.glfwGetWindowPos(window,x,y);
            GLFW.glfwSetWindowPos(window,x[0]+RemainingEventRules.windowJitter(ThreadLocalRandom.current().nextInt(14)),y[0]+RemainingEventRules.windowJitter(ThreadLocalRandom.current().nextInt(14)));
        }else{stopShrink();return;}
        client.player.setYRot(client.player.getYRot()+RemainingEventRules.cameraJitter(Math.random()));
        client.player.setXRot(client.player.getXRot()+RemainingEventRules.cameraJitter(Math.random()));
        shrinkStep++;shrinkNext=now()+20;
    }
    private static void stopShrink(){shrinking=false;Minecraft.getInstance().getSoundManager().stop(new ResourceLocation("splitself","rumble2"),null);}
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event){
        if(event.phase!=TickEvent.Phase.END)return;
        if(emergencyUntil!=0&&now()>=emergencyUntil){emergencyUntil=0;ClientActivity.set(false);}
        boolean enabled=ExternalConsent.enabled();
        boolean pc=ExternalConsent.computerAllowed();
        if((!enabled&&externalWasEnabled)||(!pc&&computerWasEnabled))cleanup();externalWasEnabled=enabled;computerWasEnabled=pc;
        if(enabled)worker(RemainingDesktopEvents::callbacks);
        if(!ExternalConsent.personalAllowed())city=Component.translatable("events.splitself.redacted_name").getString();
    }
    @SubscribeEvent public static void renderTick(TickEvent.RenderTickEvent event){
        if(event.phase==TickEvent.Phase.END){restoreCamera();return;}
        shrinkStep();
        if(screenshotAt==0||now()<screenshotAt||ClientFaceEffects.captureBusy()||ClientBatchEvents.captureBusy())return;
        var client=Minecraft.getInstance();
        if(!ExternalConsent.computerAllowed()||client.player!=owner||client.level==null){screenshotAt=0;return;}
        screenshotAt=0;
        captureEntity=client.level.getEntitiesOfClass(TheOtherEntity.class,client.player.getBoundingBox().inflate(100),net.minecraft.world.entity.Entity::isAlive)
                .stream().min(Comparator.comparingDouble(client.player::distanceToSqr)).orElse(null);
        if(captureEntity==null){frameCapture=null;return;} // Source does not capture anything without a nearby The Other.
        oldCameraEntity=client.getCameraEntity();oldCameraType=client.options.getCameraType();capturing=true;
        client.setCameraEntity(captureEntity);client.options.setCameraType(net.minecraft.client.CameraType.THIRD_PERSON_BACK);
    }
    private static void restoreCamera(){if(capturing){var client=Minecraft.getInstance();client.setCameraEntity(oldCameraEntity);client.options.setCameraType(oldCameraType);capturing=false;captureEntity=null;oldCameraEntity=null;}}
    @SubscribeEvent public static void screenshot(RenderLevelStageEvent event){
        if(event.getStage()!=RenderLevelStageEvent.Stage.AFTER_LEVEL||!capturing)return;
        var client=Minecraft.getInstance();
        if(!ExternalConsent.computerAllowed()||client.player!=owner){restoreCamera();return;}
        var recipient=client.player;
        try(var image=Screenshot.takeScreenshot(client.getMainRenderTarget())) {
            Path directory=client.gameDirectory.toPath().resolve("screenshots");Files.createDirectories(directory);
            Path file=directory.resolve("hello_"+captureEntity.getUUID().toString().substring(0,8)+"_"+System.currentTimeMillis()+".png");image.writeToFile(file);
            if(frameCapture!=null){var callback=frameCapture;frameCapture=null;if(ExternalConsent.personalAllowed())callback.accept(file);return;}
            EditionDesktopIntegrations.showTypingWindow(List.of(Component.translatable("events.splitself.theOtherScreenshot.line1").getString(),Component.translatable("events.splitself.theOtherScreenshot.line2").getString()));
            CompletableFuture.delayedExecutor(8,TimeUnit.SECONDS).execute(()->{if(ExternalConsent.computerAllowed()&&client.player==recipient)try{DesktopLinks.open(file.toUri().toString());}catch(Exception failure){failure("THEOTHERSCREENSHOT open",failure);}});
        }catch(Exception failure){failure("THEOTHERSCREENSHOT capture",failure);}finally{frameCapture=null;restoreCamera();}
    }
    @SubscribeEvent public static void gui(RenderGuiEvent.Post event){
        var client=Minecraft.getInstance();if(client.player==null)return;var g=event.getGuiGraphics();int w=g.guiWidth(),h=g.guiHeight();
        if(glitch){g.pose().pushPose();g.pose().translate(0,0,1000);g.fill(0,0,w,h,0x80000000);g.blit(new ResourceLocation("splitself","textures/screen/black.png"),0,0,0,0,w+200,h+200,w+200,h+200);g.drawString(client.font,Component.translatable("selectWorld.warning.lowDiskSpace.title").getString()+" "+Component.translatable("selectWorld.warning.lowDiskSpace.description").getString().replace("\n"," "),200,100,0xffffff);g.pose().popPose();}
        if(!emergencyActive()||!ExternalConsent.computerAllowed())return;
        g.pose().pushPose();g.pose().translate(0,0,1000);g.fill(0,0,w,h,0xff000000);
        if(now()-lastShake>=10){lastShake=now();shakeX=-(int)(Math.random()*50);shakeY=-(int)(Math.random()*50);}
        RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();RenderSystem.setShaderColor((float)(.9+Math.random()/10),(float)(.9+Math.random()/10),(float)(.9+Math.random()/10),1);
        g.blit(new ResourceLocation("splitself","textures/screen/overlay.png"),shakeX,shakeY,0,0,w+50,h+50,w,h);
        // Kept source tint also applies to the following text.
        String title=Component.translatable("events.splitself.emergency.notice").getString();g.pose().pushPose();g.pose().scale(2,2,1);g.drawString(client.font,title,(w-client.font.width(title)*2)/4,h/6,0xffffff);g.pose().popPose();
        String shown=ExternalConsent.personalAllowed()?city:Component.translatable("events.splitself.redacted_name").getString();
        String line=Component.translatable("events.splitself.emergency.message",shown,java.text.DateFormat.getTimeInstance().format(new Date()),shown).getString();
        int x=(int)(w-(now()-scrollStart)*.15);if(x+client.font.width(line)<0)scrollStart=now();g.drawString(client.font,line,x,h/3+100,0xffffff);
        RenderSystem.setShaderColor(1,1,1,1);RenderSystem.disableBlend();g.pose().popPose();
    }
    static void cleanup(){ClientActivity.set(false);frameCapture=null;screenshotAt=0;emergencyUntil=0;glitch=false;if(memoryToken!=null)memoryClosed(memoryToken);owner=null;restoreCamera();stopShrink();worker(RemainingDesktopEvents::cleanup);}
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut event){cleanup();}
}
