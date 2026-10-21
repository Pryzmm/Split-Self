package com.pryzmm.splitself.forge;

import java.util.*;
import java.nio.file.*;
import com.google.gson.*;

/** Offline policy fixtures. Never loads Swing windows, GLFW, HTTP, Discord or desktop APIs. */
public final class RemainingEventsProbe {
    private static int assertions;
    private static void check(boolean value,String message){assertions++;if(!value)throw new AssertionError(message);}
    public static void main(String[] args) throws Exception {
        var expected=Set.of("DOYOUSEEME","THEOTHERSCREENSHOT","EMERGENCY","FREEDOM","SHRINK","SURROUND","MEMORY","REMINDER","WEATHER","RPC","DISCORDNAME");
        check(RemainingEventRules.all().equals(expected),"exact remaining eleven");
        for(String name:expected){
            check(!RemainingEventRules.allowed(name,false,false),name+" disabled");
            check(!RemainingEventRules.allowed(name,false,true),name+" PII cannot enable external");
            check(RemainingEventRules.allowed(name,true,true),name+" explicit opt-in");
            check(RemainingEventRules.allowed(name,true,false)==!name.equals("DISCORDNAME"),name+" redaction/PII");
        }
        check(!RemainingEventRules.allowed("arbitrary",true,true),"unknown denied");
        check(RemainingEventRules.shrinkSize(1920,0)==1920,"shrink starts original");
        check(RemainingEventRules.shrinkSize(1920,25)==1440,"shrink halfway");
        check(RemainingEventRules.shrinkSize(1920,49)==979,"source stops before exact half");
        check(RemainingEventRules.shrinkSize(801,49)==408,"odd dimensions integer half then float progress");
        check(RemainingEventRules.cameraJitter(0)==-3,"camera minimum");
        check(RemainingEventRules.cameraJitter(.1)==-2,"camera truncation not floor");
        check(RemainingEventRules.cameraJitter(.499)==0,"negative near zero truncates");
        check(RemainingEventRules.cameraJitter(.999)==2,"camera upper bound exclusive");
        check(RemainingEventRules.windowJitter(0)==-7&&RemainingEventRules.windowJitter(13)==6,"asymmetric cumulative window shake");
        check(RemainingEventRules.city("First\nSecond\nThird\n").equals("Third"),"pinned source line ordering");
        check(RemainingEventRules.city("First\nSecond\nnull\n").equals("Second, First"),"region fallback");
        check(RemainingEventRules.city("First\nnull\nnull\n").equals("First"),"country fallback");
        boolean malformed=false;try{RemainingEventRules.city("one\ntwo");}catch(IllegalArgumentException expectedFailure){malformed=true;}
        check(malformed,"incomplete geo response rejected");
        var weather=RemainingEventRules.weather("{\"current_condition\":[{\"temp_C\":\"-7\",\"temp_F\":\"19\",\"FeelsLikeC\":\"-13\",\"weatherDesc\":[{\"value\":\"Snow\"}]}]}");
        check(weather.valid()&&weather.c()==-7&&weather.f()==19&&weather.condition().equals("Snow"),"uses temp not FeelsLike source quirk");
        for(String bad:List.of("{}","null","{bad}","{\"current_condition\":[]}","{\"current_condition\":[{}]}"))check(!RemainingEventRules.weather(bad).valid(),"bad weather returns fail");
        var grants=new RemainingEventRules.Grants();UUID player=UUID.randomUUID(),stranger=UUID.randomUUID();
        UUID token=grants.offer(player,0,100);
        check(grants.pending(player,101)&&!grants.pending(stranger,101),"pending offers bound to player");
        check(!grants.consume(stranger,token,0,101),"other player denied");
        check(!grants.consume(player,UUID.randomUUID(),0,101),"forged token denied");
        check(!grants.consume(player,token,1,101),"stale stage denied");
        check(grants.consume(player,token,0,101),"one valid completion");
        check(!grants.consume(player,token,0,101),"replay denied");
        check(!grants.pending(player,101),"consumed offer releases player");
        token=grants.offer(player,2,100);check(!grants.consume(player,token,2,600101),"expired grant denied");
        token=grants.offer(player,1,100);UUID replacement=grants.offer(player,1,101);
        check(!grants.consume(player,token,1,102),"replaced offer denied");
        check(grants.consume(player,replacement,1,102),"new offer valid");
        token=grants.offer(player,1,100);grants.clear();check(!grants.consume(player,token,1,101),"server stop clears grants");
        token=grants.offer(player,1,100);grants.remove(player);check(!grants.consume(player,token,1,101),"logout clears grants");
        check(RemainingEventRules.nextMemoryStage(-1)==0&&RemainingEventRules.nextMemoryStage(0)==1&&RemainingEventRules.nextMemoryStage(1)==2&&RemainingEventRules.nextMemoryStage(2)==-1,"source stage cycle");
        Path root=Path.of(args[0]);
        check(PortedEvents.supported().size()==67,"all 67 runtime handlers");
        var defaults=JsonParser.parseString(Files.readString(root.resolve("src/main/resources/data/splitself/event_defaults.json"))).getAsJsonObject().getAsJsonObject("events");
        for(String name:expected)check(defaults.has(name),"original defaults retained "+name);
        var config=com.electronwill.nightconfig.core.CommentedConfig.inMemory();SplitSelfConfig.SPEC.correct(config);SplitSelfConfig.SPEC.setConfig(config);
        for(String name:expected) {
            var option=EventCatalog.options().get(name);
            int stage=name.equals("SURROUND")?3:Set.of("THEOTHERSCREENSHOT","MEMORY","WEATHER","RPC").contains(name)?1:2;
            int weight=Set.of("FREEDOM","SURROUND").contains(name)?5:10;
            boolean oneTime=Set.of("EMERGENCY","SURROUND","RPC","DISCORDNAME").contains(name);
            check(option.stage()==stage&&option.weight()==weight&&option.oneTime()==oneTime&&option.client()==!RemainingEventRules.WORLD.contains(name),"pinned original metadata "+name);
            check(EventCatalog.eligible(stage,Set.of(),Map.of(),0).contains(name),"eligible at source stage "+name);
            check(!EventCatalog.eligible(stage-1,Set.of(),Map.of(),0).contains(name),"not eligible prematurely "+name);
            if(oneTime)check(!EventCatalog.eligible(3,Set.of(name),Map.of(),100).contains(name),"consumed one time "+name);
        }
        check(defaults.getAsJsonObject("SURROUND").get("type").getAsString().equals("GLOBAL"),"surround global");
        System.out.println("RemainingEventsProbe PASS: "+assertions+" assertions; no desktop/network/game execution");
    }
}
