package com.pryzmm.splitself.forge;
import java.util.*;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
final class ClientSettingsScreen extends Screen{
    private record Setting(String path,ForgeConfigSpec.ConfigValue<?> value){}
    private final Screen parent;private int page;private String group="events";
    ClientSettingsScreen(Screen parent){super(Component.translatable("config.splitself.title"));this.parent=parent;}
    static void open(Screen parent){net.minecraft.client.Minecraft.getInstance().setScreen(new ClientSettingsScreen(parent));}
    private static void flatten(UnmodifiableConfig config,String prefix,List<Setting> entries){for(var entry:config.valueMap().entrySet()){String path=prefix+entry.getKey();if(entry.getValue() instanceof ForgeConfigSpec.ConfigValue<?> value)entries.add(new Setting(path,value));else if(entry.getValue() instanceof UnmodifiableConfig child)flatten(child,path+".",entries);}}
    @Override protected void init(){
        int x=width/2-155;
        String[] groups={"events","eventWeights","eventStages","oneTimeEvents"};for(int i=0;i<groups.length;i++){String id=groups[i];addRenderableWidget(Button.builder(Component.translatable("config.splitself.tab."+id),b->{group=id;page=0;rebuildWidgets();}).bounds(x+i*78,35,76,20).build());}
        List<Setting> all=new ArrayList<>();flatten(SplitSelfConfig.SPEC.getValues(),"",all);all.removeIf(setting->!setting.path.startsWith(group+"."));
        int rows=Math.max(1,(height-115)/25),pages=Math.max(1,(all.size()+rows-1)/rows);page=Math.min(page,pages-1);
        boolean local=minecraft.getConnection()==null||minecraft.getSingleplayerServer()!=null;
        for(int i=page*rows;i<Math.min(all.size(),(page+1)*rows);i++){Setting setting=all.get(i);String key=setting.path.substring(setting.path.indexOf('.')+1);Button button=Button.builder(Component.literal(key+": "+setting.value.get()),b->minecraft.setScreen(new ValueScreen(this,setting))).bounds(x,65+(i-page*rows)*25,310,20).build();button.active=local;addRenderableWidget(button);}
        addRenderableWidget(Button.builder(Component.literal("<"),b->{page=Math.max(0,page-1);rebuildWidgets();}).bounds(x,height-40,40,20).build());
        addRenderableWidget(Button.builder(Component.literal(">"),b->{page=Math.min(pages-1,page+1);rebuildWidgets();}).bounds(x+45,height-40,40,20).build());
        addRenderableWidget(Button.builder(Component.translatable("config.splitself.local_permissions"),b->minecraft.setScreen(new LocalPermissionsScreen(this))).bounds(x+90,height-40,115,20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.done"),b->{SplitSelfConfig.SPEC.save();onClose();}).bounds(x+210,height-40,100,20).build());
    }
    @Override public void onClose(){minecraft.setScreen(parent);}
    @Override public void render(GuiGraphics g,int x,int y,float delta){renderBackground(g);g.drawCenteredString(font,title,width/2,15,0xffffff);super.render(g,x,y,delta);}
    private static final class ValueScreen extends Screen{
        final ClientSettingsScreen parent;final Setting setting;EditBox input;String error="";
        ValueScreen(ClientSettingsScreen parent,Setting setting){super(Component.literal(setting.path));this.parent=parent;this.setting=setting;}
        @Override protected void init(){input=new EditBox(font,width/2-140,height/2-15,280,20,title);input.setValue(String.valueOf(setting.value.get()));addRenderableWidget(input);setInitialFocus(input);
            addRenderableWidget(Button.builder(Component.translatable("gui.done"),b->apply()).bounds(width/2-105,height/2+20,100,20).build());addRenderableWidget(Button.builder(Component.translatable("gui.cancel"),b->onClose()).bounds(width/2+5,height/2+20,100,20).build());}
        @SuppressWarnings({"rawtypes","unchecked"}) private void apply(){try{
            Object old=setting.value.get();String text=input.getValue();Object next;
            if(old instanceof Boolean){if(!text.equalsIgnoreCase("true")&&!text.equalsIgnoreCase("false"))throw new IllegalArgumentException();next=Boolean.parseBoolean(text);}
            else if(old instanceof Integer)next=Integer.parseInt(text);else if(old instanceof Double)next=Double.parseDouble(text);else throw new IllegalArgumentException();
            ForgeConfigSpec.ValueSpec spec=SplitSelfConfig.SPEC.getSpec().get(Arrays.asList(setting.path.split("\\.")));
            if(!spec.test(next))throw new IllegalArgumentException();((ForgeConfigSpec.ConfigValue)setting.value).set(next);SplitSelfConfig.SPEC.save();onClose();
        }catch(RuntimeException failure){error=Component.translatable("command.splitself.invalid_value").getString();}}
        @Override public void onClose(){minecraft.setScreen(parent);}
        @Override public void render(GuiGraphics g,int x,int y,float delta){renderBackground(g);g.drawCenteredString(font,title,width/2,height/2-45,0xffffff);g.drawCenteredString(font,error,width/2,height/2+50,0xff5555);super.render(g,x,y,delta);}
    }
}
