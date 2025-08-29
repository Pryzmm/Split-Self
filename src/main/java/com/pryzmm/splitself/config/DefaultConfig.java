package com.pryzmm.splitself.config;

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

    public static Map<String, Integer> eventWeights = createDefaultEventWeights();
    public static Map<String, Integer> eventStages = createDefaultEventStages();
    public static Map<String, Boolean> oneTimeEvents = createDefaultOneTimeEvents();

    private static Map<String, Integer> createDefaultEventWeights() {
        Map<String, Integer> weights = new HashMap<>();
        weights.put("SPAWNTHEOTHER", 100);
        weights.put("POEMSCREEN", 5);
        weights.put("DOYOUSEEME", 10);
        weights.put("UNDERGROUNDMINING", 10);
        weights.put("REDSKY", 10);
        weights.put("NOTEPAD", 10);
        weights.put("SCREENOVERLAY", 10);
        weights.put("WHITESCREENOVERLAY", 10);
        weights.put("INVENTORYOVERLAY", 10);
        weights.put("THEOTHERSCREENSHOT", 10);
        weights.put("DESTROYCHUNK", 10);
        weights.put("FROZENSCREEN", 10);
        weights.put("HOUSE", 10);
        weights.put("BEDROCKPILLAR", 10);
        weights.put("BILLY", 3);
        weights.put("FACE", 3);
        weights.put("COMMAND", 10);
        weights.put("INVERT", 10);
        weights.put("EMERGENCY", 10);
        weights.put("TNT", 5);
        weights.put("IRONTRAP", 10);
        weights.put("LAVA", 10);
        weights.put("BROWSER", 3);
        weights.put("KICK", 5);
        weights.put("SIGN", 10);
        weights.put("SCALE", 10);
        weights.put("FREEDOM", 5);
        weights.put("MINE", 10);
        weights.put("DOOR", 10);
        weights.put("SHRINK", 10);
        weights.put("PAUSE", 10);
        weights.put("ITEM", 10);
        weights.put("FRAME", 15);
        weights.put("NAME", 10);
        return weights;
    }

    private static Map<String, Integer> createDefaultEventStages() {
        Map<String, Integer> stages = new HashMap<>();
        stages.put("SPAWNTHEOTHER", 0);
        stages.put("POEMSCREEN", 1);
        stages.put("DOYOUSEEME", 2);
        stages.put("UNDERGROUNDMINING", 0);
        stages.put("REDSKY", 1);
        stages.put("NOTEPAD", 1);
        stages.put("SCREENOVERLAY", 1);
        stages.put("WHITESCREENOVERLAY", 0);
        stages.put("INVENTORYOVERLAY", 0);
        stages.put("THEOTHERSCREENSHOT", 1);
        stages.put("DESTROYCHUNK", 1);
        stages.put("FROZENSCREEN", 2);
        stages.put("HOUSE", 1);
        stages.put("BEDROCKPILLAR", 0);
        stages.put("BILLY", 0);
        stages.put("FACE", 1);
        stages.put("COMMAND", 1);
        stages.put("INVERT", 0);
        stages.put("EMERGENCY", 2);
        stages.put("TNT", 0);
        stages.put("IRONTRAP", 0);
        stages.put("LAVA", 1);
        stages.put("BROWSER", 1);
        stages.put("KICK", 1);
        stages.put("SIGN", 0);
        stages.put("SCALE", 1);
        stages.put("FREEDOM", 2);
        stages.put("MINE", 0);
        stages.put("DOOR", 0);
        stages.put("SHRINK", 2);
        stages.put("PAUSE", 1);
        stages.put("ITEM", 0);
        stages.put("FRAME", 1);
        stages.put("NAME", 2);
        return stages;
    }

    private static Map<String, Boolean> createDefaultOneTimeEvents() {
        Map<String, Boolean> oneTimers = new HashMap<>();
        oneTimers.put("SPAWNTHEOTHER", false);
        oneTimers.put("POEMSCREEN", false);
        oneTimers.put("DOYOUSEEME", false);
        oneTimers.put("UNDERGROUNDMINING", false);
        oneTimers.put("REDSKY", false);
        oneTimers.put("NOTEPAD", true);
        oneTimers.put("SCREENOVERLAY", false);
        oneTimers.put("WHITESCREENOVERLAY", false);
        oneTimers.put("INVENTORYOVERLAY", false);
        oneTimers.put("THEOTHERSCREENSHOT", false);
        oneTimers.put("DESTROYCHUNK", false);
        oneTimers.put("FROZENSCREEN", false);
        oneTimers.put("HOUSE", false);
        oneTimers.put("BEDROCKPILLAR", false);
        oneTimers.put("BILLY", true);
        oneTimers.put("FACE", false);
        oneTimers.put("COMMAND", false);
        oneTimers.put("INVERT", false);
        oneTimers.put("EMERGENCY", true);
        oneTimers.put("TNT", false);
        oneTimers.put("IRONTRAP", false);
        oneTimers.put("LAVA", false);
        oneTimers.put("BROWSER", true);
        oneTimers.put("KICK", false);
        oneTimers.put("SIGN", false);
        oneTimers.put("SCALE", false);
        oneTimers.put("FREEDOM", false);
        oneTimers.put("MINE", false);
        oneTimers.put("DOOR", false);
        oneTimers.put("SHRINK", false);
        oneTimers.put("PAUSE", false);
        oneTimers.put("ITEM", false);
        oneTimers.put("FRAME", false);
        oneTimers.put("NAME", false);
        return oneTimers;
    }

    public static Map<String, Integer> getDefaultEventWeights() {return new HashMap<>(eventWeights);}
    public static int getDefaultEventWeight(String eventName) {return eventWeights.getOrDefault(eventName, 10);}

    public static Map<String, Integer> getDefaultEventStages() {return new HashMap<>(eventStages);}
    public static int getDefaultEventStage(String eventName) {return eventStages.getOrDefault(eventName, 0);}

    public static Map<String, Boolean> getDefaultOneTimeEvents() {return new HashMap<>(oneTimeEvents);}
    public static Boolean getDefaultOneTimeEvents(String eventName) {return oneTimeEvents.getOrDefault(eventName, false);}

}
