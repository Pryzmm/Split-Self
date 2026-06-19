package com.pryzmm.splitself.config;

import com.pryzmm.splitself.events.EventManager;

import java.util.HashMap;
import java.util.Map;

public class DefaultConfig {

    public static boolean eventsEnabled = true;
    public static int eventTickInterval = 60;
    public static double eventChance = 0.01;
    public static int eventCooldown = 600;
    public static int startEventsAfter = 3000;
    public static int guaranteedEvent = 15600;
    public static int repeatEventsAfter = 5;
    public static int baseSafeRadius = 15;

    // I can't remember why, but this variable CANNOT be deleted or renamed without breaking the mod. Don't ask why, just don't do it.
    public static String voskModel = "vosk-model-small-en-us-0.15";

    public static Map<String, Integer> eventWeights = new HashMap<>();
    public static Map<String, Integer> eventStages = new HashMap<>();
    public static Map<String, Boolean> oneTimeEvents = new HashMap<>();

    public record EventOptions(Integer weight, Integer stage, Boolean oneTime) {}

    private static void updateEventOptions(String eventName, EventOptions options) {
        if (options.weight  != null)  eventWeights.put(eventName, options.weight);
        if (options.stage   != null)  eventStages.put(eventName, options.stage);
        if (options.oneTime != null)  oneTimeEvents.put(eventName, options.oneTime);
    }

    private static boolean hasConfigValues(EventManager.Events event) {
        return !eventWeights.containsKey(event.name()) || !eventStages.containsKey(event.name()) || !oneTimeEvents.containsKey(event.name());
    }

    public static void createDefaultConfigs() {
        updateEventOptions("SPAWNTHEOTHER",         new EventOptions(120, 0, false));
        updateEventOptions("POEMSCREEN",            new EventOptions(5,   1, false));
        updateEventOptions("DOYOUSEEME",            new EventOptions(10,  2, false));
        updateEventOptions("UNDERGROUNDMINING",     new EventOptions(10,  0, false));
        updateEventOptions("REDSKY",                new EventOptions(10,  1, false));
        updateEventOptions("NOTEPAD",               new EventOptions(10,  1, true ));
        updateEventOptions("SCREENOVERLAY",         new EventOptions(10,  1, false));
        updateEventOptions("WHITESCREENOVERLAY",    new EventOptions(10,  0, false));
        updateEventOptions("INVENTORYOVERLAY",      new EventOptions(10,  0, false));
        updateEventOptions("THEOTHERSCREENSHOT",    new EventOptions(10,  1, false));
        updateEventOptions("DESTROYCHUNK",          new EventOptions(10,  1, false));
        updateEventOptions("FROZENSCREEN",          new EventOptions(10,  2, false));
        updateEventOptions("HOUSE",                 new EventOptions(10,  1, false));
        updateEventOptions("PILLAR",                new EventOptions(10,  0, false));
        updateEventOptions("BILLY",                 new EventOptions(3,   0, true ));
        updateEventOptions("FACE",                  new EventOptions(3,   1, false));
        updateEventOptions("COMMAND",               new EventOptions(10,  1, false));
        updateEventOptions("INVERT",                new EventOptions(10,  0, false));
        updateEventOptions("EMERGENCY",             new EventOptions(10,  2, true ));
        updateEventOptions("TNT",                   new EventOptions(0,   0, false));
        updateEventOptions("IRONTRAP",              new EventOptions(10,  0, false));
        updateEventOptions("BROWSER",               new EventOptions(3,   1, true ));
        updateEventOptions("KICK",                  new EventOptions(5,   1, false));
        updateEventOptions("SIGN",                  new EventOptions(10,  1, false));
        updateEventOptions("SCALE",                 new EventOptions(10,  0, false));
        updateEventOptions("FREEDOM",               new EventOptions(5,   2, false));
        updateEventOptions("MINE",                  new EventOptions(15,  0, false));
        updateEventOptions("DOOR",                  new EventOptions(10,  0, false));
        updateEventOptions("SHRINK",                new EventOptions(10,  2, false));
        updateEventOptions("PAUSE",                 new EventOptions(10,  1, false));
        updateEventOptions("ITEM",                  new EventOptions(10,  0, false));
        updateEventOptions("FRAME",                 new EventOptions(10,  1, false));
        updateEventOptions("NAME",                  new EventOptions(10,  2, false));
        updateEventOptions("WHISPER",               new EventOptions(10,  1, false));
        updateEventOptions("ESCAPE",                new EventOptions(10,  2, false));
        updateEventOptions("LIFT",                  new EventOptions(10,  1, false));
        updateEventOptions("SURROUND",              new EventOptions(5,   3, true ));
        updateEventOptions("LOGS",                  new EventOptions(10,  3, true ));
        updateEventOptions("DISCONNECT",            new EventOptions(10,  2, false));
        updateEventOptions("FORGOTTEN",             new EventOptions(5,   3, false));
        updateEventOptions("EJECT",                 new EventOptions(10,  2, false));
        updateEventOptions("FREEZE",                new EventOptions(10,  1, false));
        updateEventOptions("BLU",                   new EventOptions(10,  3, true ));
        updateEventOptions("MEMORY",                new EventOptions(10,  0, false));
        updateEventOptions("FOV",                   new EventOptions(10,  1, false));
        updateEventOptions("WEATHER",               new EventOptions(10,  1, false));
        updateEventOptions("MEMORIES",              new EventOptions(10,  0, true ));
        updateEventOptions("MORSE",                 new EventOptions(10,  1, true ));
        updateEventOptions("CORAL",                 new EventOptions(10,  2, true ));
        updateEventOptions("STATIC",                new EventOptions(10,  1, false));
        updateEventOptions("INVERTCOLOR",           new EventOptions(10,  2, false));
        updateEventOptions("CLIPBOARD",             new EventOptions(10,  0, true ));
        updateEventOptions("RPC",                   new EventOptions(10,  1, true ));
        updateEventOptions("RECORD",                new EventOptions(10,  2, true ));
        updateEventOptions("DISCORDNAME",           new EventOptions(10,  2, true ));
        updateEventOptions("DEADCHUNK",             new EventOptions(10,  2, false));
        updateEventOptions("RECURSIVE",             new EventOptions(10,  3, false));
        for (EventManager.Events event : EventManager.Events.values()) {
            if (!hasConfigValues(event)) { // Missing default event fallback
                updateEventOptions(event.name(), new EventOptions(10, 0, false));
            }
        }
    }

}
