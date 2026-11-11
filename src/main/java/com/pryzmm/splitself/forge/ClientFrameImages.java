package com.pryzmm.splitself.forge;

import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.*;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Local images only. The first three successful assignments per connection are game screenshots. */
@Mod.EventBusSubscriber(modid=SplitSelfForge.MOD_ID,value=Dist.CLIENT)
public final class ClientFrameImages {
    private static final Map<Integer,ResourceLocation> TEXTURES=new HashMap<>();
    private static final Set<ImageFrameBlockEntity> KNOWN=Collections.newSetFromMap(new IdentityHashMap<>());
    private static ImageFrameBlockEntity pending;
    private static int nextId,completed;
    private static volatile int generation;
    private static volatile int lastPermissions=-1;
    private static int permissions(){return (ExternalConsent.enabled()?1:0)|(ExternalConsent.computerAllowed()?2:0)|(ExternalConsent.personalAllowed()?4:0);}
    private static void syncPermissions(){int bits=permissions();if(bits!=lastPermissions){clear();lastPermissions=bits;}}
    private ClientFrameImages(){}
    static ResourceLocation texture(ImageFrameBlockEntity frame){
        syncPermissions();
        KNOWN.add(frame);
        if(frame.imageId()==null||!TEXTURES.containsKey(frame.imageId())){if(pending==null)loadNext(frame);return null;}
        return TEXTURES.get(frame.imageId());
    }
    static void loadNext(ImageFrameBlockEntity frame){
        if(pending!=null)return;
        pending=frame;var client=Minecraft.getInstance();var level=client.level;int token=generation;
        boolean pictures=ExternalConsent.personalAllowed()&&FrameImageFiles.preferPictures(completed,java.util.concurrent.ThreadLocalRandom.current().nextBoolean());
        RemainingClientEvents.worker(()->{
            try{
                if(token!=generation||permissions()!=lastPermissions)return;
                Set<Path> roots=new LinkedHashSet<>();roots.add(client.gameDirectory.toPath().resolve("screenshots"));
                String appdata=System.getenv("APPDATA");if(ExternalConsent.computerAllowed()&&appdata!=null)roots.add(Path.of(appdata,".minecraft","screenshots"));
                List<Path> files=new ArrayList<>();
                if(pictures)files.addAll(FrameImageFiles.images(FrameImageFiles.pictureRoots(),true));
                files.addAll(FrameImageFiles.images(roots,false));
                for(Path file:files){
                    if(token!=generation||permissions()!=lastPermissions)return;
                    try{
                        BufferedImage image=FrameImageFiles.decode(file);
                        client.execute(()->{try{if(valid(frame,level,token))assign(frame,image);}finally{finish(frame,token);}});return;
                    }catch(Exception ignored){}
                }
                client.execute(()->{
                    try{if(valid(frame,level,token)){
                        try(NativeImage image=Screenshot.takeScreenshot(client.getMainRenderTarget())){
                            BufferedImage copy=new BufferedImage(image.getWidth(),image.getHeight(),BufferedImage.TYPE_INT_ARGB);
                            for(int y=0;y<image.getHeight();y++)for(int x=0;x<image.getWidth();x++)copy.setRGB(x,y,swap(image.getPixelRGBA(x,y)));
                            assign(frame,copy);
                        }
                    }}finally{finish(frame,token);}
                });
            }catch(Exception failure){
                client.execute(()->finish(frame,token));
                org.slf4j.LoggerFactory.getLogger("splitself").warn("Frame image lookup failed",failure);
            }
        });
    }
    private static int swap(int color){return (color&0xff00ff00)|((color&255)<<16)|((color>>>16)&255);}
    private static void finish(ImageFrameBlockEntity frame,int token){if(token==generation&&pending==frame)pending=null;}
    private static boolean valid(ImageFrameBlockEntity frame,net.minecraft.client.multiplayer.ClientLevel level,int token){
        return token==generation&&Minecraft.getInstance().level==level&&!frame.isRemoved()&&permissions()==lastPermissions;
    }
    private static void assign(ImageFrameBlockEntity frame,BufferedImage source){
        double scale=Math.min(1,2048.0/Math.max(source.getWidth(),source.getHeight()));
        int w=Math.max(1,(int)(source.getWidth()*scale)),h=Math.max(1,(int)(source.getHeight()*scale));
        NativeImage image=new NativeImage(w,h,false);
        for(int y=0;y<h;y++)for(int x=0;x<w;x++)image.setPixelRGBA(x,y,swap(source.getRGB(Math.min(source.getWidth()-1,(int)(x/scale)),Math.min(source.getHeight()-1,(int)(y/scale)))));
        int id=nextId++;var texture=new ResourceLocation("splitself","dynamic/image_frame_"+id);
        Minecraft.getInstance().getTextureManager().register(texture,new DynamicTexture(image));TEXTURES.put(id,texture);frame.setImageId(id);completed++;
    }
    static void clear(){
        generation++;pending=null;var textures=Minecraft.getInstance().getTextureManager();TEXTURES.values().forEach(textures::release);
        TEXTURES.clear();KNOWN.forEach(frame->frame.setImageId(null));KNOWN.clear();nextId=0;
    }
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut event){clear();completed=0;}
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event){if(event.phase==TickEvent.Phase.END)syncPermissions();}
}
