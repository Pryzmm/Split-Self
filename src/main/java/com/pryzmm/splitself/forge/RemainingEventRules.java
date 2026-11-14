package com.pryzmm.splitself.forge;

import java.util.*;
import com.google.gson.*;

/** Last eleven events: source EventRunner, ClientScreenSizer, CityLocator and WeatherFetcher. */
final class RemainingEventRules {
    static final Set<String> WORLD=Set.of("SURROUND","THEOTHERSCREENSHOT");
    static final Set<String> EXTERNAL=Set.of("DOYOUSEEME","EMERGENCY","FREEDOM","SHRINK","MEMORY","REMINDER","WEATHER","RPC","DISCORDNAME");
    static Set<String> all() { var all=new HashSet<>(WORLD);all.addAll(EXTERNAL);return Set.copyOf(all); }
    static boolean allowed(String name,boolean enabled,boolean pii) {
        return all().contains(name) && enabled && (!name.equals("DISCORDNAME") || pii);
    }
    static int shrinkSize(int original,int step) { return (int)(original-(original-original/2)*(step/50f)); }
    static int cameraJitter(double draw) { return (int)(draw*6-3); }
    static int windowJitter(int draw) { return draw-7; }
    static int nextMemoryStage(int stage) { return stage+1>2 ? -1 : stage+1; }
    // Kept quirk: the source assigns the three API lines in this order, even though fields are named city/region/country.
    static String city(String body) {
        String[] lines=body.trim().split("\n");
        if(lines.length<3)throw new IllegalArgumentException("Invalid location response");
        String country=lines[0].trim(),region=lines[1].trim(),city=lines[2].trim();
        return !city.isEmpty()&&!city.equals("null") ? city : !region.isEmpty()&&!region.equals("null") ? region+", "+country : country;
    }
    record Weather(Integer c,Integer f,String condition) { boolean valid(){return c!=null&&f!=null&&condition!=null&&!condition.isEmpty();} }
    static Weather weather(String body) {
        try {
            var current=JsonParser.parseString(body).getAsJsonObject().getAsJsonArray("current_condition").get(0).getAsJsonObject();
            return new Weather(current.get("temp_C").getAsInt(),current.get("temp_F").getAsInt(),
                    current.getAsJsonArray("weatherDesc").get(0).getAsJsonObject().get("value").getAsString());
        }catch(RuntimeException failure){return new Weather(null,null,null);}
    }
    static final class Grants {
        record Grant(UUID token,int stage,long expires) {}
        private final Map<UUID,Grant> grants=new HashMap<>();
        boolean pending(UUID player,long now){Grant grant=grants.get(player);return grant!=null&&now<=grant.expires;}
        void remove(UUID player){grants.remove(player);}
        UUID offer(UUID player,int stage,long now) { UUID token=UUID.randomUUID();grants.put(player,new Grant(token,stage,now+600_000));return token; }
        boolean consume(UUID player,UUID token,int current,long now) {
            Grant grant=grants.get(player);
            if(grant==null||!grant.token.equals(token)||grant.stage!=current||now>grant.expires)return false;
            grants.remove(player);return true;
        }
        void clear(){grants.clear();}
    }
}
