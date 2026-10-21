package com.pryzmm.splitself.forge;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.google.gson.JsonParser;
import java.nio.file.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

/** Production policy tests only: no live world, network request, process scan or hardware command. */
public final class BatchEventsProbe {
    private static int assertions;
    private static void check(boolean condition,String message){assertions++;if(!condition)throw new AssertionError(message);}
    private static void near(double actual,double expected,String message){check(Math.abs(actual-expected)<.000001,message+": "+actual);}
    public static void main(String[] args)throws Exception {
        var gate=new BatchEventRules.PauseGate();
        check(!gate.cancel(),"no block before ESCAPE");gate.enable();
        for(int i=1;i<=9;i++)check(gate.cancel(),"blocked pause attempt "+i);
        check(!gate.cancel() && !gate.cancel(),"tenth and subsequent attempts allowed");
        gate.enable();for(int i=0;i<5;i++)check(gate.cancel(),"second activation begins again");
        gate.enable();for(int i=0;i<4;i++)check(gate.cancel(),"repeat ESCAPE keeps attempt count");
        check(!gate.cancel(),"tenth after repeat clears flag");
        gate.enable();
        for(int i=0;i<20;i++)check(gate.cancel(true),"transient overlay blocks without consuming ESCAPE attempts");
        for(int i=0;i<9;i++)check(gate.cancel(false),"nine attempts remain after overlay ends");
        check(!gate.cancel(false),"tenth attempt after overlay is allowed");
        check(BatchEventRules.FREEZE_MS==2000 && BatchEventRules.FROZEN_MS==1000,"separate freeze durations");
        near(BatchEventRules.frozenTint(0),0,"red tint lower bound");near(BatchEventRules.frozenTint(.999999),.1999998,"red tint upper range");
        for(int i=0;i<10;i++)near(BatchEventRules.miningVolume(i),(i+1)/10.0,"mining volume "+i);
        check(BatchEventRules.destroyCenter(new Vec3(-.75,65,-.25),20,20,null,15).equals(new BlockPos(0,319,0)),"destroy truncates toward zero");
        check(BatchEventRules.destroyCenter(new Vec3(10.5,65,30.75),0,39,null,15).equals(new BlockPos(-9,319,49)),"destroy offsets -20..19 then truncates");
        check(BatchEventRules.deadOrigin(new Vec3(-20.25,64,-.25),0,0).equals(new BlockPos(0,0,0)),"dead truncation BEFORE chunk floorDiv");
        check(BatchEventRules.deadOrigin(new Vec3(-20.25,64,-16.25),0,0).equals(new BlockPos(0,0,-16)),"negative chunk alignment");
        check(BatchEventRules.deadOrigin(new Vec3(-21.25,64,-16.25),0,0).equals(new BlockPos(-16,0,-16)),"negative beyond truncation zero aligns downward");
        check(BatchEventRules.deadOrigin(Vec3.ZERO,.5,.999999).equals(new BlockPos(-64,0,0)),"20..50 radial destination");
        check(BatchEventRules.liftCenter(new Vec3(-.5,0,-.5),0,0,0).equals(new BlockPos(20,0,0)),"lift Math.round differs from truncation");
        var center=BatchEventRules.liftCenter(Vec3.ZERO,.125,0,.999999);
        check(center.equals(new BlockPos(14,0,28)),"lift independent X/Z distance draws");
        check(BatchEventRules.liftMinimum(new BlockPos(30,0,-10),80).equals(new BlockPos(23,66,-17)),"lift save minimum");
        check(BatchEventRules.liftMaximum(new BlockPos(30,0,-10),80).equals(new BlockPos(37,87,-3)),"lift inclusive 15x22x15 maximum");
        check(BatchEventRules.liftDestination(new BlockPos(30,0,-10),80).equals(new BlockPos(23,120,-17)),"lift bottom goes surface+40, not oldbottom+40");
        var layer=new ArrayList<BlockPos>();BatchEventRules.visitLayer(new BlockPos(-8,0,-8),72,layer::add);
        check(layer.size()==256 && new HashSet<>(layer).size()==256,"whole 16x16 layer no duplicates");
        check(layer.get(0).equals(new BlockPos(-8,72,-8)) && layer.get(1).equals(new BlockPos(-8,72,-7))
                && layer.get(16).equals(new BlockPos(-7,72,-8)) && layer.get(255).equals(new BlockPos(7,72,7)),"source X/Z order and inclusive edges");
        for(boolean air:new boolean[]{false,true})for(boolean solid:new boolean[]{false,true}) {
            check(BatchEventRules.replace(air,solid,false)==!air,"destroy all nonair including containers/bedrock");
            check(BatchEventRules.replace(air,solid,true)==(!air && solid),"dead only nonair solid-render blocks");
        }
        var visited=new ArrayList<Integer>();var destroy=new BatchEventRules.Layers(320,-64,-1,50,1000);
        check(destroy.advance(999,visited::add)==0,"destroy not before start");
        check(destroy.advance(1000,visited::add)==1 && visited.equals(List.of(320)),"destroy includes nominal exclusive top attempt");
        check(destroy.advance(1049,visited::add)==0 && destroy.advance(1050,visited::add)==1 && visited.get(1)==319,"50ms layers");
        while(!destroy.finished())check(destroy.advance(100000,visited::add)<=16,"bounded lag catchup");
        check(visited.size()==385 && visited.get(384)==-64,"destroy through bottom inclusive no layers lost");
        check(destroy.advance(200000,visited::add)==0,"finished job idempotent");
        visited.clear();var dead=new BatchEventRules.Layers(-64,319,1,10,0);
        check(dead.advance(0,visited::add)==1 && visited.get(0)==-64,"dead starts bottom immediately");
        check(dead.advance(9,visited::add)==0 && dead.advance(50,visited::add)==5,"dead 10ms cadence, five per regular tick");
        while(!dead.finished())dead.advance(100000,visited::add);
        check(visited.size()==384 && visited.get(383)==319,"dead ascending top exclusive");
        for(String name:BatchEventRules.EXTERNAL) {
            check(!ExternalBatchRules.allowed(name,false,false) && !ExternalBatchRules.allowed(name,false,true),"external OFF gates "+name);
            check(ExternalBatchRules.allowed(name,true,true),"opted in "+name);
        }
        check(!ExternalBatchRules.allowed("NAME",true,false),"identity lookup requires PII too");
        check(ExternalBatchRules.allowed("RECORD",true,false) && ExternalBatchRules.allowed("EJECT",true,false),"process/hardware need explicit external opt-in");
        check(!ExternalBatchRules.allowed("unknown",true,true),"unknown request refused");
        check(ExternalBatchRules.recorder(java.util.stream.Stream.of("C:/Games/java.exe","C:/OBS-Studio/bin/obs64.exe","xsplit.exe")).equals("c:/obs-studio/bin/obs64.exe"),"first matching full lowercased command");
        check(ExternalBatchRules.recorder(java.util.stream.Stream.of("transaction.exe")).equals("transaction.exe"),"source substring false positive kept");
        check(ExternalBatchRules.recorder(java.util.stream.Stream.of("java.exe","notepad.exe"))==null,"no recorder yields null");
        check(ExternalBatchRules.previousName("{\"username_history\":[{\"username\":\"Now\"},{\"username\":\"Older\"},{\"username\":\"Oldest\"}]}","Now").equals("Older"),"first different name in source order");
        check(ExternalBatchRules.previousName("{\"username_history\":[{\"username\":\"Now\"}]}","Now")==null,"no historical name does not invent message");
        check(ExternalBatchRules.previousName("{}","Now")==null,"absent history handled");
        var config=CommentedConfig.inMemory();SplitSelfConfig.SPEC.correct(config);SplitSelfConfig.SPEC.setConfig(config);
        var expected=Set.of("ESCAPE","FREEZE","FROZENSCREEN","UNDERGROUNDMINING","RECORD","NAME","EJECT","DESTROYCHUNK","DEADCHUNK","LIFT");
        check(BatchEventRules.all().equals(expected),"exact ten distinct events");
        for(String name:expected) {
            var option=EventCatalog.options().get(name);
            int stage=Set.of("FREEZE","UNDERGROUNDMINING","DESTROYCHUNK","LIFT","RECORD").contains(name)?(name.equals("UNDERGROUNDMINING")?0:name.equals("RECORD")?2:1):2;
            check(PortedEvents.supported().contains(name) && option.weight()==10 && option.stage()==stage
                    && option.client()==!BatchEventRules.WORLD.contains(name) && option.oneTime()==name.equals("RECORD"),"pinned defaults "+name);
            check(EventCatalog.eligible(stage,Set.of(),Map.of(),0).contains(name),"eligible at source stage "+name);
            if(stage>0)check(!EventCatalog.eligible(stage-1,Set.of(),Map.of(),0).contains(name),"not eligible too early "+name);
        }
        check(!EventCatalog.eligible(3,Set.of("RECORD"),Map.of(),10).contains("RECORD"),"one-time RECORD consumption");
        check(PortedEvents.supported().size()==67,"67 event handlers");
        System.out.println("PASS BatchEventsProbe: "+assertions+" assertions; no world, GPU, HTTP, process scan, tray or gameplay execution.");
    }
}
