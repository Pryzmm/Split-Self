package com.pryzmm.minemessage;

/** Standalone child-process translations, resolved by the owning Minecraft client. */
public final class Messages {
    private static final java.util.Properties TEXT=new java.util.Properties();
    static {
        String path=System.getProperty("splitself.messages");
        if(path!=null)try(var input=java.nio.file.Files.newBufferedReader(java.nio.file.Path.of(path),java.nio.charset.StandardCharsets.UTF_8)){TEXT.load(input);}
        catch(java.io.IOException failure){System.err.println(failure);}
    }
    public record Message(String value){public String getString(){return value;}}
    public static Message translatable(String key){return new Message(TEXT.getProperty(key,key));}
}
