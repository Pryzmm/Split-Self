package com.pryzmm.splitself.forge;
import java.util.*;
import java.nio.file.*;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
public final class RemainderPortProbe{
    private static int count;
    private static void check(boolean value,String label){count++;if(!value)throw new AssertionError(label);}
    public static void main(String[]args)throws Exception{
        String uuid="e8d99c45-9e4d-4b01-943a-6e297a82b028";
        String json="{\"sleepStage\":4,\"memoryStage\":-1,\"isFull\":true,\"clickedButton\":true,\"isDeleted\":true,\"prevLoaded\":true,\"joinedPlayers\":[\""+uuid+"\"],\"unlockedMemories\":[\"house\",\"blu\"],\"theForgottenLocation\":\"splitself:limbo_dimension;1.25;-4;5;-90;45\"}";
        CompoundTag imported=LegacyWorldImport.convert(json);
        check(imported.getInt("sleepStage")==4&&imported.getInt("memoryStage")==-1,"legacy stages");
        for(String key:List.of("isFull","clickedButton","isDeleted","prevLoaded"))check(imported.getBoolean(key),key);
        check(imported.getList("joinedPlayers",8).getString(0).equals(uuid),"UUID import");
        check(imported.getList("unlockedMemories",8).size()==2,"memory import");
        var location=imported.getCompound("theForgottenLocation");check(location.getDouble("x")==1.25&&location.getDouble("y")==-4&&location.getFloat("yaw")==-90,"semicolon location");
        check(LegacyWorldImport.convert("{}").getList("joinedPlayers",8).isEmpty(),"old empty file");
        for(String bad:List.of("[]","{bad}","{\"joinedPlayers\":[\"invalid\"]}","{\"theForgottenLocation\":\"a;b\"}","{\"theForgottenLocation\":\"minecraft:overworld;NaN;0;0;0;0\"}")){
            boolean rejected=false;try{LegacyWorldImport.convert(bad);}catch(RuntimeException expected){rejected=true;}check(rejected,"malformed import rejected");
        }
        for(String name:List.of("A.PNG","b.jpg","c.JPEG"))check(FrameImageRules.imageName(name),"source file filter "+name);
        for(String name:List.of("a.png.exe","png","a.gif"))check(!FrameImageRules.imageName(name),"reject nonimage");
        check(FrameImageRules.rotation(Direction.NORTH)==0&&FrameImageRules.rotation(Direction.EAST)==270&&FrameImageRules.rotation(Direction.SOUTH)==180&&FrameImageRules.rotation(Direction.WEST)==90,"four source frame rotations");
        for(int flags=0;flags<4;flags++)check(FrameImageRules.allowed((flags&1)!=0,(flags&2)!=0)==(flags==3),"frame local privacy matrix");
        check(Arrays.equals(FinaleRules.dialogueTicks("INTRO"),new int[]{100,190,280,370,460}),"intro timeline");
        check(Arrays.equals(FinaleRules.dialogueTicks("DELETE"),new int[]{30,120,270,1800}),"delete timeline");
        check(Arrays.equals(FinaleRules.dialogueTicks("CANCEL"),new int[]{30,110}),"cancel timeline");
        for(var effect:FinaleRules.Effect.values()){
            check(FinaleRules.delay(effect,false,0)==(effect==FinaleRules.Effect.SCREEN?100:0),"initial delay min "+effect);
            check(FinaleRules.delay(effect,false,.999)==(effect==FinaleRules.Effect.SCREEN?299:49),"initial delay max "+effect);
            check(FinaleRules.delay(effect,true,.999)==(effect==FinaleRules.Effect.SCREEN?39:29),"duration max "+effect);
        }
        List<String> events=new ArrayList<>();var cycle=new FinaleRules.Cycle(FinaleRules.Effect.SCREEN,0,0);
        check(!cycle.advance(99,()->0,(e,on)->events.add(e+":"+on)),"blackout not early");
        cycle.advance(100,()->0,(e,on)->events.add(e+":"+on));check(events.equals(List.of("SCREEN:true")),"blackout begins100");
        check(cycle.advance(110,()->0,(e,on)->events.add(e+":"+on))&&cycle.finished,"source blackout hands off to extra experience cycle");
        check(!cycle.advance(1000,()->0,(e,on)->events.add("unexpected"))&&events.size()==2,"blackout not repeated");
        var zero=new FinaleRules.Cycle(FinaleRules.Effect.UI,10,0);check(zero.due==11,"zero delay never busy loops");
        ClientActivity.set(true);ClientActivity.set(true);ClientActivity.set(false);check(!ClientActivity.active(),"source shared boolean last completion wins");
        var origin=new BlockPos(0,64,0);check(StripMineEncounters.position(origin,Direction.SOUTH,p->true).equals(origin.north(9)),"north priority nine blocks back");
        check(StripMineEncounters.position(origin,Direction.NORTH,p->true).equals(origin.south(9)),"facing blacklisted");
        check(StripMineEncounters.position(origin,Direction.NORTH,p->false)==null,"blocked corridor");
        check(StripMineEncounters.position(origin,Direction.SOUTH,p->p.getZ()<0&&p.getZ()>-10)==null,"requires tenth valid cell though spawns at ninth");
        check(EmptyStructureRules.origin("es_house",new BlockPos(10,61,10)).equals(new BlockPos(0,60,0)),"house offset");
        check(EmptyStructureRules.origin("es_mines",new BlockPos(10,61,10)).equals(new BlockPos(0,49,0)),"mines offset");
        check(EmptyStructureRules.origin("es_escape",new BlockPos(10,61,10)).getY()==20,"escape absoluteY20");
        check(EmptyStructureRules.radius("es_pillar")==4&&EmptyStructureRules.radius("es_escape")==6&&EmptyStructureRules.radius("es_house")==8,"flat radii");
        for(int flags=0;flags<8;flags++)check(SpeechRules.allowed((flags&1)!=0,(flags&2)!=0,(flags&4)!=0)==(flags==7),"speech three switch matrix");
        check(SpeechRules.valid("hello")&&!SpeechRules.valid("hello\n")&&!SpeechRules.valid("x".repeat(513))&&!SpeechRules.valid(" "),"bounded speech payload");
        check("canYouHearMe".equals(SpeechRules.response(StoryChat.language("en_us"),"CAN YOU HEAR ME")),"English speech regex");
        check("imSorry".equals(SpeechRules.response(StoryChat.language("en_us"),"please forgive me")),"forgive alias");
        check("canYouHearMe".equals(SpeechRules.response(StoryChat.language("ru_ru"),"ты слышишь меня")),"Russian speech regex");
        check(SpeechRules.response(StoryChat.language("en_us"),"unrelated")==null,"fallback to ordinary chat");
        check("canYouHearMe".equals(SpeechRules.response(StoryChat.language("ru_ru"),"  ТЫ   слышишь   меня?  ")),"speech whitespace normalization");
        check("canYouHearMe".equals(SpeechRules.response(StoryChat.language("en_us"),"  CAN   YOU hear me?  ")),"English speech whitespace");
        check("can you hear me".equals(SpeechRules.normalize("\"can you hear me\"")),"quoted debug phrase");
        Path root=Path.of(args[0]);Set<String> palette=new TreeSet<>();
        for(String name:EmptyStructureRules.NAMES){
            var actual=ResourceProbe.template(root.resolve("src/main/resources/data/splitself/structures/"+name+".nbt"));
            check(actual.getInt("DataVersion")==3465,"1.20.1 structure "+name);
            for(Tag state:actual.getList("palette",10))palette.add(((CompoundTag)state).getString("Name"));
        }
        check(!palette.isEmpty(),"structure palettes populated");
        var config=com.electronwill.nightconfig.core.CommentedConfig.inMemory();ExternalConsent.SPEC.correct(config);ExternalConsent.SPEC.setConfig(config);
        check(ExternalConsent.enabled()==EditionPolicy.defaultsEnabled()&&ExternalConsent.PERSONAL_INFORMATION.get()==EditionPolicy.defaultsEnabled()&&ExternalConsent.MICROPHONE.get()==EditionPolicy.defaultsEnabled(),"local defaults follow edition");
        System.out.println("RemainderPortProbe PASS: "+count+" assertions; no game/server/desktop/microphone/network execution");
    }
}
