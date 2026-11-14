package com.pryzmm.splitself.forge;

import net.minecraftforge.common.ForgeConfigSpec;

/** CLIENT config, never synchronized from a server. */
final class ExternalConsent {
    static final java.util.Set<String> EVENTS = java.util.Set.of("CLIPBOARD", "COMMAND", "NOTEPAD", "MEMORIES", "MORSE", "LOGS", "PLAYERDATA", "SPOTIFY", "SEARCH", "NAME", "RECORD", "EJECT", "DOYOUSEEME", "EMERGENCY", "FREEDOM", "SHRINK", "MEMORY", "REMINDER", "WEATHER", "RPC", "DISCORDNAME");
    static final ForgeConfigSpec SPEC;
    static final ForgeConfigSpec.BooleanValue ENABLED;
    static final ForgeConfigSpec.BooleanValue PERSONAL_INFORMATION;
    static final ForgeConfigSpec.BooleanValue MICROPHONE;
    static final ForgeConfigSpec.BooleanValue COMPUTER_INTERACTIONS;
    static {
        var builder = new ForgeConfigSpec.Builder();
        ENABLED = builder.comment("Local opt-in: desktop files, clipboard, applications and wallpaper.",
                "A server cannot enable this setting. Defaults follow the SAFE/UNSAFE build edition.")
                .define("externalEffects", EditionPolicy.defaultsEnabled());
        PERSONAL_INFORMATION = builder.comment("Allow device name and network identity/location lookups.")
                .define("personalInformation", EditionPolicy.defaultsEnabled());
        MICROPHONE=builder.comment("Separate local opt-in for offline microphone recognition; also requires externalEffects and personalInformation.").define("microphone",EditionPolicy.defaultsEnabled());
        COMPUTER_INTERACTIONS=builder.comment("Allow desktop applications, OS changes, external network and personal pictures; MEMORY is an explicit exception.").define("computerInteractions",EditionPolicy.defaultsEnabled());
        SPEC = builder.build();
    }
    private ExternalConsent() {}
    static boolean enabled() { return ENABLED.get(); }
    static boolean computerAllowed(){return enabled()&&COMPUTER_INTERACTIONS.get();}
    static boolean personalAllowed(){return computerAllowed()&&PERSONAL_INFORMATION.get();}
}
