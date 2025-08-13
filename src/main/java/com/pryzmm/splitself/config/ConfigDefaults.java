package com.pryzmm.splitself.config;

import com.pryzmm.splitself.events.EventManager;
import java.util.HashMap;
import java.util.Map;

public class ConfigDefaults {

    public static final boolean DEFAULT_EVENTS_ENABLED = true;
    public static final int DEFAULT_EVENT_TICK_INTERVAL = 20;
    public static final double DEFAULT_EVENT_CHANCE = 0.003;
    public static final int DEFAULT_EVENT_COOLDOWN = 600;
    public static final int DEFAULT_START_EVENTS_AFTER = 3000;
    public static final int DEFAULT_GUARANTEED_EVENT = 15600;

    private static final Map<String, Integer> DEFAULT_EVENT_WEIGHTS = createDefaultEventWeights();

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
        weights.put("CAMERA", 10);
        weights.put("FREEDOM", 5);
        weights.put("MINE", 10);
        weights.put("DOOR", 10);
        weights.put("SHRINK", 10);
        return weights;
    }

    public static Map<String, Integer> getDefaultEventWeights() {
        return new HashMap<>(DEFAULT_EVENT_WEIGHTS);
    }

    public static int getDefaultEventWeight(String eventName) {
        return DEFAULT_EVENT_WEIGHTS.getOrDefault(eventName, 10);
    }

    public static int getDefaultEventWeight(EventManager.Events event) {
        return getDefaultEventWeight(event.name());
    }
}