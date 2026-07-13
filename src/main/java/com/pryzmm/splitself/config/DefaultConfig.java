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
    public static Map<String, EventType> eventTypes = new HashMap<>(); // Non-configurable, here for convienence

    public record EventOptions(Integer weight, Integer stage, Boolean oneTime, EventType type) {}

    public enum EventType {
        CLIENT, GLOBAL
    }

    private static void updateEventOptions(String eventName, EventOptions options) {
        if (options.weight  != null)  eventWeights.put(eventName, options.weight);
        if (options.stage   != null)  eventStages.put(eventName, options.stage);
        if (options.oneTime != null)  oneTimeEvents.put(eventName, options.oneTime);
        if (options.type    != null)  eventTypes.put(eventName, options.type);
    }

    private static boolean hasConfigValues(EventManager.Events event) {
        return !eventWeights.containsKey(event.name()) || !eventStages.containsKey(event.name()) || !oneTimeEvents.containsKey(event.name());
    }

    public static void createDefaultConfigs() {
        updateEventOptions("SPAWNTHEOTHER",         new EventOptions(120, 0, false, EventType.GLOBAL));
        updateEventOptions("POEMSCREEN",            new EventOptions(5,   1, false, EventType.CLIENT));
        updateEventOptions("DOYOUSEEME",            new EventOptions(10,  2, false, EventType.CLIENT));
        updateEventOptions("UNDERGROUNDMINING",     new EventOptions(10,  0, false, EventType.GLOBAL));
        updateEventOptions("REDSKY",                new EventOptions(10,  1, false, EventType.CLIENT));
        updateEventOptions("NOTEPAD",               new EventOptions(10,  1, true,  EventType.CLIENT));
        updateEventOptions("SCREENOVERLAY",         new EventOptions(10,  1, false, EventType.CLIENT));
        updateEventOptions("WHITESCREENOVERLAY",    new EventOptions(10,  0, false, EventType.CLIENT));
        updateEventOptions("INVENTORYOVERLAY",      new EventOptions(10,  0, false, EventType.CLIENT));
        updateEventOptions("THEOTHERSCREENSHOT",    new EventOptions(10,  1, false, EventType.GLOBAL));
        updateEventOptions("DESTROYCHUNK",          new EventOptions(10,  1, false, EventType.GLOBAL));
        updateEventOptions("FROZENSCREEN",          new EventOptions(10,  2, false, EventType.CLIENT));
        updateEventOptions("HOUSE",                 new EventOptions(10,  1, false, EventType.GLOBAL));
        updateEventOptions("PILLAR",                new EventOptions(10,  0, false, EventType.GLOBAL));
        updateEventOptions("BILLY",                 new EventOptions(3,   0, true,  EventType.GLOBAL));
        updateEventOptions("FACE",                  new EventOptions(3,   1, false, EventType.CLIENT));
        updateEventOptions("COMMAND",               new EventOptions(10,  1, false, EventType.CLIENT));
        updateEventOptions("INVERT",                new EventOptions(10,  0, false, EventType.CLIENT));
        updateEventOptions("EMERGENCY",             new EventOptions(10,  2, true,  EventType.CLIENT));
        updateEventOptions("TNT",                   new EventOptions(0,   0, false, EventType.GLOBAL));
        updateEventOptions("LAVA",                  new EventOptions(10,  1, false, EventType.GLOBAL));
        updateEventOptions("IRONTRAP",              new EventOptions(10,  0, false, EventType.GLOBAL));
        updateEventOptions("BROWSER",               new EventOptions(3,   1, true,  EventType.CLIENT));
        updateEventOptions("KICK",                  new EventOptions(5,   1, false, EventType.GLOBAL));
        updateEventOptions("SIGN",                  new EventOptions(10,  1, false, EventType.GLOBAL));
        updateEventOptions("SCALE",                 new EventOptions(10,  0, false, EventType.CLIENT));
        updateEventOptions("FREEDOM",               new EventOptions(5,   2, false, EventType.CLIENT));
        updateEventOptions("MINE",                  new EventOptions(15,  0, false, EventType.GLOBAL));
        updateEventOptions("DOOR",                  new EventOptions(10,  0, false, EventType.GLOBAL));
        updateEventOptions("SHRINK",                new EventOptions(10,  2, false, EventType.CLIENT));
        updateEventOptions("PAUSE",                 new EventOptions(10,  1, false, EventType.CLIENT));
        updateEventOptions("ITEM",                  new EventOptions(10,  0, false, EventType.GLOBAL));
        updateEventOptions("FRAME",                 new EventOptions(10,  1, false, EventType.GLOBAL));
        updateEventOptions("NAME",                  new EventOptions(10,  2, false, EventType.CLIENT));
        updateEventOptions("WHISPER",               new EventOptions(10,  1, false, EventType.CLIENT));
        updateEventOptions("ESCAPE",                new EventOptions(10,  2, false, EventType.CLIENT));
        updateEventOptions("LIFT",                  new EventOptions(10,  1, false, EventType.GLOBAL));
        updateEventOptions("SURROUND",              new EventOptions(5,   3, true,  EventType.GLOBAL));
        updateEventOptions("LOGS",                  new EventOptions(10,  3, true,  EventType.CLIENT));
        updateEventOptions("DISCONNECT",            new EventOptions(10,  2, false, EventType.CLIENT));
        updateEventOptions("FORGOTTEN",             new EventOptions(5,   3, false, EventType.GLOBAL));
        updateEventOptions("EJECT",                 new EventOptions(10,  2, false, EventType.CLIENT));
        updateEventOptions("FREEZE",                new EventOptions(10,  1, false, EventType.CLIENT));
        updateEventOptions("BLU",                   new EventOptions(10,  3, true,  EventType.GLOBAL));
        updateEventOptions("MEMORY",                new EventOptions(10,  1, false, EventType.CLIENT));
        updateEventOptions("REMINDER",              new EventOptions(10,  2, false, EventType.CLIENT));
        updateEventOptions("RENAME",                new EventOptions(10,  0, false, EventType.CLIENT));
        updateEventOptions("FOV",                   new EventOptions(10,  1, false, EventType.CLIENT));
        updateEventOptions("WEATHER",               new EventOptions(10,  1, false, EventType.CLIENT));
        updateEventOptions("MEMORIES",              new EventOptions(10,  0, true,  EventType.CLIENT));
        updateEventOptions("MORSE",                 new EventOptions(10,  1, true,  EventType.CLIENT));
        updateEventOptions("CORAL",                 new EventOptions(10,  2, true,  EventType.GLOBAL));
        updateEventOptions("STATIC",                new EventOptions(10,  1, false, EventType.CLIENT));
        updateEventOptions("INVERTCOLOR",           new EventOptions(10,  2, false, EventType.CLIENT));
        updateEventOptions("CLIPBOARD",             new EventOptions(10,  0, true,  EventType.CLIENT));
        updateEventOptions("RPC",                   new EventOptions(10,  1, true,  EventType.CLIENT));
        updateEventOptions("RECORD",                new EventOptions(10,  2, true,  EventType.CLIENT));
        updateEventOptions("DISCORDNAME",           new EventOptions(10,  2, true,  EventType.CLIENT));
        updateEventOptions("DEADCHUNK",             new EventOptions(10,  2, false, EventType.GLOBAL));
        updateEventOptions("RECURSIVE",             new EventOptions(10,  3, false, EventType.CLIENT));
        updateEventOptions("BRAIN",                 new EventOptions(15,  3, false, EventType.GLOBAL));
        updateEventOptions("BOOK",                  new EventOptions(10,  0, false, EventType.GLOBAL));
        updateEventOptions("SPOTIFY",               new EventOptions(10,  3, true,  EventType.CLIENT));
        updateEventOptions("SEARCH",                new EventOptions(10,  2, true,  EventType.CLIENT));
    }

}
