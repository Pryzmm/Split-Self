package com.pryzmm.splitself.forge;

import java.io.IOException;
import java.util.Locale;
import java.util.Properties;

/** Immutable build identity; clients never inherit another edition's consent file. */
final class EditionPolicy {
    static final String EDITION=load();
    private EditionPolicy(){}
    static boolean unsafe(String edition){return "UNSAFE".equals(edition);}
    static boolean defaultsEnabled(){return unsafe(EDITION);}
    static boolean showConsent(){return true;}
    // Original 1.0.2 SAFE stubs these events and the shared Notepad helper.
    // This is an immutable edition restriction, independent of user permissions.
    static final java.util.Set<String> UNSAFE_ONLY_EVENTS = java.util.Set.of(
            "DOYOUSEEME", "COMMAND", "EJECT", "FREEDOM", "NOTEPAD");
    static boolean eventAllowed(String event){return unsafe(EDITION)||!UNSAFE_ONLY_EVENTS.contains(event);}
    static boolean typingAllowed(){return unsafe(EDITION);}
    static String configFile(){return "splitself-"+EDITION.toLowerCase(Locale.ROOT)+"-client.toml";}
    private static String load(){
        Properties properties=new Properties();
        try(var stream=EditionPolicy.class.getResourceAsStream("/splitself-edition.properties")){
            if(stream!=null)properties.load(stream);
        }catch(IOException failure){throw new IllegalStateException("Unreadable edition marker",failure);}
        String edition=properties.getProperty("edition","SAFE").strip();
        if(!edition.equals("SAFE")&&!edition.equals("UNSAFE"))throw new IllegalStateException("Unknown Split Self edition");
        return edition;
    }
}
