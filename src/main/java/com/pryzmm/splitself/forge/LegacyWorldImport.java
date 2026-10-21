package com.pryzmm.splitself.forge;

import com.google.gson.*;
import java.util.UUID;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;

/** Pure adapter for the pinned data/splitself.json schema. Does not modify the legacy file. */
final class LegacyWorldImport {
    private LegacyWorldImport(){}
    static CompoundTag convert(String json){
        JsonObject source=JsonParser.parseString(json).getAsJsonObject();CompoundTag tag=new CompoundTag();
        for(String key:new String[]{"sleepStage","memoryStage"})if(source.has(key))tag.putInt(key,source.get(key).getAsInt());
        for(String key:new String[]{"isFull","clickedButton","isDeleted","prevLoaded"})if(source.has(key))tag.putBoolean(key,source.get(key).getAsBoolean());
        for(String key:new String[]{"unlockedMemories","joinedPlayers"}){
            ListTag values=new ListTag();if(source.has(key))for(var value:source.getAsJsonArray(key)){
                String text=value.getAsString();if(key.equals("joinedPlayers"))UUID.fromString(text);values.add(StringTag.valueOf(text));
            }tag.put(key,values);
        }
        if(source.has("theForgottenLocation")&&!source.get("theForgottenLocation").isJsonNull()){
            String[] parts=source.get("theForgottenLocation").getAsString().split(";",-1);
            if(parts.length!=6||ResourceLocation.tryParse(parts[0])==null)throw new IllegalArgumentException("Invalid legacy location");
            CompoundTag location=new CompoundTag();location.putString("dimension",parts[0]);
            for(int i=0;i<3;i++){double n=Double.parseDouble(parts[i+1]);if(!Double.isFinite(n))throw new IllegalArgumentException("Nonfinite location");location.putDouble(new String[]{"x","y","z"}[i],n);}
            float yaw=Float.parseFloat(parts[4]),pitch=Float.parseFloat(parts[5]);if(!Float.isFinite(yaw)||!Float.isFinite(pitch))throw new IllegalArgumentException("Invalid rotation");
            location.putFloat("yaw",yaw);location.putFloat("pitch",pitch);tag.put("theForgottenLocation",location);
        }
        return tag;
    }
}
