package com.pryzmm.splitself.forge;
import com.electronwill.nightconfig.core.CommentedConfig;
public final class EditionProbe {
    public static void main(String[] args){
        String expected=args[0];
        if(!EditionPolicy.EDITION.equals(expected))throw new AssertionError("Packaged marker does not match build");
        var config=CommentedConfig.inMemory();ExternalConsent.SPEC.correct(config);ExternalConsent.SPEC.setConfig(config);
        boolean on=expected.equals("UNSAFE");
        if(EditionDesktopIntegrations.typingAvailable()!=on)throw new AssertionError("Wrong compiled source variant");
        if(ExternalConsent.enabled()!=on||ExternalConsent.PERSONAL_INFORMATION.get()!=on||ExternalConsent.MICROPHONE.get()!=on||ExternalConsent.COMPUTER_INTERACTIONS.get()!=on)throw new AssertionError("Fresh defaults mismatch");
        if(!EditionPolicy.showConsent())throw new AssertionError("Information must be available in both editions");
        if(!EditionPolicy.configFile().equals("splitself-"+expected.toLowerCase(java.util.Locale.ROOT)+"-client.toml"))throw new AssertionError("Separate config filename");
        ExternalConsent.ENABLED.set(false);ExternalConsent.PERSONAL_INFORMATION.set(false);ExternalConsent.MICROPHONE.set(false);
        if(ExternalConsent.enabled()||ExternalConsent.PERSONAL_INFORMATION.get()||ExternalConsent.MICROPHONE.get())throw new AssertionError("User revocation must survive unsafe defaults");
        if(EditionPolicy.unsafe("SAFE")||!EditionPolicy.unsafe("UNSAFE"))throw new AssertionError("Edition truth table");
        for(int flags=0;flags<8;flags++){
            ExternalConsent.ENABLED.set((flags&1)!=0);ExternalConsent.COMPUTER_INTERACTIONS.set((flags&2)!=0);ExternalConsent.PERSONAL_INFORMATION.set((flags&4)!=0);
            if(ExternalConsent.computerAllowed()!=((flags&3)==3))throw new AssertionError("PC boundary");
            if(ExternalConsent.personalAllowed()!=(flags==7))throw new AssertionError("PII boundary");
            if(RemainingDesktopEvents.appAllowed("com.pryzmm.memory.Memory")!=((flags&1)!=0))throw new AssertionError("MEMORY exception");
            if(RemainingDesktopEvents.appAllowed("com.pryzmm.desktop.DesktopCompanion")!=((flags&3)==3))throw new AssertionError("Desktop app boundary");
            if(RemainingDesktopEvents.appModeAllowed("com.pryzmm.desktop.DesktopCompanion","typing")!=false)throw new AssertionError("Notepad launch boundary");
            if(RemainingDesktopEvents.appModeAllowed("com.pryzmm.desktop.DesktopCompanion","reminder")!=((flags&3)==3))throw new AssertionError("SAFE retains reminder mode");
            for(String event:java.util.Set.of("DOYOUSEEME","COMMAND","EJECT","FREEDOM","NOTEPAD"))
                if(EditionPolicy.eventAllowed(event)!=on)throw new AssertionError("Immutable SAFE exclusion: "+event);
            if(EditionPolicy.typingAllowed()!=on)throw new AssertionError("Shared Notepad helper exclusion, including screenshot dialogue");
            for(String event:java.util.Set.of("THEOTHERSCREENSHOT","MEMORY","REMINDER","WEATHER","RPC","DISCORDNAME","CLIPBOARD","SPOTIFY","SEARCH","SHRINK"))
                if(!EditionPolicy.eventAllowed(event))throw new AssertionError("Original SAFE retains "+event);
        }
        if(!EditionPolicy.UNSAFE_ONLY_EVENTS.equals(java.util.Set.of("DOYOUSEEME","COMMAND","EJECT","FREEDOM","NOTEPAD")))throw new AssertionError("Exact original SAFE exclusions");
        System.out.println("EditionProbe PASS: "+expected+" marker/fresh defaults/consent/config isolation/revocation; no game/desktop/network execution");
    }
}
