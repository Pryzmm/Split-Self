package com.pryzmm.splitself.config;

import com.pryzmm.splitself.SplitSelf;
import com.pryzmm.splitself.events.EventManager;
import net.fabricmc.loader.api.FabricLoader;

public class SplitSelfConfig {
    private static SplitSelfConfig INSTANCE;
    private static boolean initializing = false;

    // Default values
    public boolean eventsEnabled = true;
    public int eventTickInterval = 20;
    public double eventChance = 0.003;
    public int eventCooldown = 600;
    public int startEventsAfter = 3000;

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

        // Get the HANDLER field
        yaclHandler = yaclConfigClass.getField("HANDLER").get(null);

        // Load the config from file
        yaclHandler.getClass().getMethod("load").invoke(yaclHandler);

        // Get the config instance
        yaclInstance = yaclHandler.getClass().getMethod("instance").invoke(yaclHandler);

        SplitSelf.LOGGER.info("YACL config initialized successfully");
    }

    private void loadFromYACL() throws Exception {
        if (yaclInstance == null) return;

        Class<?> configClass = yaclInstance.getClass();

        // Read values directly from the config instance fields
        eventsEnabled = configClass.getField("eventsEnabled").getBoolean(yaclInstance);
        eventTickInterval = configClass.getField("eventTickInterval").getInt(yaclInstance);
        eventChance = configClass.getField("eventChance").getDouble(yaclInstance);
        eventCooldown = configClass.getField("eventCooldown").getInt(yaclInstance);
        startEventsAfter = configClass.getField("startEventsAfter").getInt(yaclInstance);

        SplitSelf.LOGGER.info("Loaded values from YACL config");
    }

    private void saveToYACL() {
        if (!yaclAvailable || yaclInstance == null || yaclHandler == null) return;

        try {
            Class<?> configClass = yaclInstance.getClass();

            // Write values directly to the config instance fields
            configClass.getField("eventsEnabled").setBoolean(yaclInstance, eventsEnabled);
            configClass.getField("eventTickInterval").setInt(yaclInstance, eventTickInterval);
            configClass.getField("eventChance").setDouble(yaclInstance, eventChance);
            configClass.getField("eventCooldown").setInt(yaclInstance, eventCooldown);
            configClass.getField("startEventsAfter").setInt(yaclInstance, startEventsAfter);

            // Save to file
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
    public int getStartEventsAfter() { return startEventsAfter; }

    // Setters (will update YACL config if available)
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

    public boolean isYACLAvailable() {
        return yaclAvailable;
    }
}