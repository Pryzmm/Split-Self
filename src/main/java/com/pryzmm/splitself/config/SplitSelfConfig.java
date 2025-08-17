package com.pryzmm.splitself.config;

import com.pryzmm.splitself.SplitSelf;
import com.pryzmm.splitself.events.EventManager;
import net.fabricmc.loader.api.FabricLoader;

import java.util.HashMap;
import java.util.Map;

public class SplitSelfConfig {
    private static SplitSelfConfig INSTANCE;
    private static boolean initializing = false;

    // Default values from ConfigDefaults
    public boolean eventsEnabled = ConfigDefaults.DEFAULT_EVENTS_ENABLED;
    public int eventTickInterval = ConfigDefaults.DEFAULT_EVENT_TICK_INTERVAL;
    public double eventChance = ConfigDefaults.DEFAULT_EVENT_CHANCE;
    public int eventCooldown = ConfigDefaults.DEFAULT_EVENT_COOLDOWN;
    public int startEventsAfter = ConfigDefaults.DEFAULT_START_EVENTS_AFTER;
    public int guaranteedEvent = ConfigDefaults.DEFAULT_GUARANTEED_EVENT;

    // Event weights and stages from ConfigDefaults
    public Map<String, Integer> eventWeights = ConfigDefaults.getDefaultEventWeights();
    public Map<String, Integer> eventStages = ConfigDefaults.getDefaultEventStages();

    // YACL integration (optional)
    private boolean yaclAvailable;
    private Object yaclHandler;
    private Object yaclInstance;

    private SplitSelfConfig() {
        yaclAvailable = FabricLoader.getInstance().isModLoaded("yet_another_config_lib_v3");

        if (!yaclAvailable) {
            try {
                Class.forName("dev.isxander.yacl3.api.YetAnotherConfigLib");
                yaclAvailable = true;
            } catch (ClassNotFoundException ignored) {}
        }

        if (yaclAvailable) {
            try {
                initializeYACL();
                loadFromYACL();
            } catch (Exception e) {
                SplitSelf.LOGGER.error("Error initializing YACL config: {}", e.getMessage(), e);
                yaclAvailable = false;
            }
        }

        SplitSelf.LOGGER.info("Config initialized - YACL Available: {}", yaclAvailable);
        SplitSelf.LOGGER.info("Config values: eventsEnabled={}, eventTickInterval={}, eventChance={}",
                eventsEnabled, eventTickInterval, eventChance);
    }

    public static SplitSelfConfig getInstance() {
        if (INSTANCE == null) {
            initializing = true;
            INSTANCE = new SplitSelfConfig();
            initializing = false;
        }
        return INSTANCE;
    }

    public static void onYACLSave() {
        if (INSTANCE != null && INSTANCE.yaclAvailable) {
            try {
                INSTANCE.loadFromYACL();
                SplitSelf.LOGGER.info("Config reloaded after YACL save");
                SplitSelf.LOGGER.info("Updated values: eventsEnabled={}, eventTickInterval={}, eventChance={}",
                        INSTANCE.eventsEnabled, INSTANCE.eventTickInterval, INSTANCE.eventChance);
            } catch (Exception e) {
                SplitSelf.LOGGER.error("Error reloading config after YACL save: {}", e.getMessage(), e);
            }
        }
    }

    public static void reload() {
        INSTANCE = null;
        getInstance();
    }

    private void initializeYACL() throws Exception {
        Class<?> yaclConfigClass = Class.forName("com.pryzmm.splitself.config.SplitSelfYACLConfig");

        yaclHandler = yaclConfigClass.getField("HANDLER").get(null);
        yaclHandler.getClass().getMethod("load").invoke(yaclHandler);
        yaclInstance = yaclHandler.getClass().getMethod("instance").invoke(yaclHandler);

        SplitSelf.LOGGER.info("YACL config initialized successfully");
    }

    private void loadFromYACL() throws Exception {
        if (yaclInstance == null) return;

        Class<?> configClass = yaclInstance.getClass();

        eventsEnabled = configClass.getField("eventsEnabled").getBoolean(yaclInstance);
        eventTickInterval = configClass.getField("eventTickInterval").getInt(yaclInstance);
        eventChance = configClass.getField("eventChance").getDouble(yaclInstance);
        eventCooldown = configClass.getField("eventCooldown").getInt(yaclInstance);
        guaranteedEvent = configClass.getField("guaranteedEvent").getInt(yaclInstance);
        startEventsAfter = configClass.getField("startEventsAfter").getInt(yaclInstance);

        // Load event weights
        @SuppressWarnings("unchecked")
        Map<String, Integer> yaclEventWeights = (Map<String, Integer>) configClass.getField("eventWeights").get(yaclInstance);
        if (yaclEventWeights != null && !yaclEventWeights.isEmpty()) {
            this.eventWeights = new HashMap<>(yaclEventWeights);
        }

        // Load event stages
        @SuppressWarnings("unchecked")
        Map<String, Integer> yaclEventStages = (Map<String, Integer>) configClass.getField("eventStages").get(yaclInstance);
        if (yaclEventStages != null && !yaclEventStages.isEmpty()) {
            this.eventStages = new HashMap<>(yaclEventStages);
        }

        SplitSelf.LOGGER.info("Loaded values from YACL config");
    }

    private void saveToYACL() {
        if (!yaclAvailable || yaclInstance == null || yaclHandler == null) return;

        try {
            Class<?> configClass = yaclInstance.getClass();

            configClass.getField("eventsEnabled").setBoolean(yaclInstance, eventsEnabled);
            configClass.getField("eventTickInterval").setInt(yaclInstance, eventTickInterval);
            configClass.getField("eventChance").setDouble(yaclInstance, eventChance);
            configClass.getField("eventCooldown").setInt(yaclInstance, eventCooldown);
            configClass.getField("startEventsAfter").setInt(yaclInstance, startEventsAfter);
            configClass.getField("eventWeights").set(yaclInstance, eventWeights);
            configClass.getField("eventStages").set(yaclInstance, eventStages);

            yaclHandler.getClass().getMethod("save").invoke(yaclHandler);

            SplitSelf.LOGGER.info("Saved values to YACL config");
        } catch (Exception e) {
            SplitSelf.LOGGER.error("Error saving YACL config: {}", e.getMessage(), e);
        }
    }

    // Getters
    public boolean isEventsEnabled() { return eventsEnabled; }
    public int getEventTickInterval() { return eventTickInterval; }
    public double getEventChance() { return eventChance; }
    public int getEventCooldown() { return eventCooldown; }
    public int getGuaranteedEvent() { return guaranteedEvent; }
    public int getStartEventsAfter() { return startEventsAfter; }
    public Map<String, Integer> getEventWeights() { return new HashMap<>(eventWeights); }
    public Map<String, Integer> getEventStages() { return new HashMap<>(eventStages); }

    // Get weight for specific event
    public int getEventWeight(EventManager.Events event) {
        return eventWeights.getOrDefault(event.name(), ConfigDefaults.getDefaultEventWeight(event));
    }

    // Get stage for specific event
    public int getEventStage(EventManager.Events event) {
        return eventStages.getOrDefault(event.name(), ConfigDefaults.getDefaultEventStage(event));
    }

    public void setEventsEnabled(boolean eventsEnabled) {
        this.eventsEnabled = eventsEnabled;
        saveToYACL();
    }

    public void setEventTickInterval(int eventTickInterval) {
        this.eventTickInterval = eventTickInterval;
        saveToYACL();
    }

    public void setEventChance(double eventChance) {
        this.eventChance = eventChance;
        saveToYACL();
    }

    public void setEventCooldown(int eventCooldown) {
        this.eventCooldown = eventCooldown;
        saveToYACL();
    }

    public void setStartEventsAfter(int startEventsAfter) {
        this.startEventsAfter = startEventsAfter;
        saveToYACL();
    }

    public void setGuaranteedEvent(int guaranteedEvent) {
        this.guaranteedEvent = guaranteedEvent;
        saveToYACL();
    }

    public void setEventWeight(String eventName, int weight) {
        eventWeights.put(eventName, weight);
        saveToYACL();
    }

    public void setEventWeights(Map<String, Integer> eventWeights) {
        this.eventWeights = new HashMap<>(eventWeights);
        saveToYACL();
    }

    public void setEventStage(String eventName, int stage) {
        eventStages.put(eventName, stage);
        saveToYACL();
    }

    public void setEventStages(Map<String, Integer> eventStages) {
        this.eventStages = new HashMap<>(eventStages);
        saveToYACL();
    }

    public boolean isYACLAvailable() {
        return yaclAvailable;
    }
}