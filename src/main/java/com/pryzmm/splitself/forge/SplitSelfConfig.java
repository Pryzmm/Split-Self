package com.pryzmm.splitself.forge;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraftforge.common.ForgeConfigSpec;

/** Forge config equivalent of the source mod's event defaults and per-event weights. */
final class SplitSelfConfig {
    static final ForgeConfigSpec SPEC;
    private static final ForgeConfigSpec.BooleanValue EVENTS_ENABLED;
    private static final ForgeConfigSpec.IntValue TICK_INTERVAL;
    private static final ForgeConfigSpec.DoubleValue EVENT_CHANCE;
    private static final ForgeConfigSpec.IntValue EVENT_COOLDOWN;
    private static final ForgeConfigSpec.IntValue START_AFTER;
    private static final ForgeConfigSpec.IntValue GUARANTEED_EVENT;
    private static final ForgeConfigSpec.IntValue REPEAT_AFTER;
    private static final ForgeConfigSpec.IntValue BASE_SAFE_RADIUS;
    private static final Map<String, ForgeConfigSpec.IntValue> WEIGHTS = new LinkedHashMap<>();
    private static final Map<String, ForgeConfigSpec.BooleanValue> ONCE = new LinkedHashMap<>();
    private static final Map<String, ForgeConfigSpec.IntValue> STAGES = new LinkedHashMap<>();

    static {
        JsonObject root = defaults();
        JsonObject globals = root.getAsJsonObject("globals");
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("events");
        EVENTS_ENABLED = builder.define("eventsEnabled", globals.get("eventsEnabled").getAsBoolean());
        TICK_INTERVAL = builder.defineInRange("eventTickInterval", globals.get("eventTickInterval").getAsInt(), 1, 72000);
        EVENT_CHANCE = builder.defineInRange("eventChance", globals.get("eventChance").getAsDouble(), 0.0, 1.0);
        EVENT_COOLDOWN = builder.defineInRange("eventCooldown", globals.get("eventCooldown").getAsInt(), 0, 720000);
        START_AFTER = builder.defineInRange("startEventsAfter", globals.get("startEventsAfter").getAsInt(), 0, 7200000);
        GUARANTEED_EVENT = builder.defineInRange("guaranteedEvent", globals.get("guaranteedEvent").getAsInt(), 0, 7200000);
        REPEAT_AFTER = builder.defineInRange("repeatEventsAfter", globals.get("repeatEventsAfter").getAsInt(), 0, 10000);
        BASE_SAFE_RADIUS = builder.defineInRange("baseSafeRadius", globals.get("baseSafeRadius").getAsInt(), 0, 1024);
        builder.pop();

        builder.push("eventWeights");
        JsonObject events = root.getAsJsonObject("events");
        for (var entry : events.entrySet()) {
            String id = entry.getKey();
            JsonObject value = entry.getValue().getAsJsonObject();
            WEIGHTS.put(id, builder.defineInRange(id, value.get("weight").getAsInt(), 0, 100000));
        }
        builder.pop();

        builder.push("eventStages");
        for (var entry : events.entrySet()) {
            String id = entry.getKey();
            STAGES.put(id, builder.defineInRange(id,
                    entry.getValue().getAsJsonObject().get("stage").getAsInt(), 0, 100));
        }
        builder.pop();
        builder.push("oneTimeEvents");
        for(var entry:events.entrySet())ONCE.put(entry.getKey(),builder.define(entry.getKey(),entry.getValue().getAsJsonObject().get("oneTime").getAsBoolean()));
        builder.pop();
        SPEC = builder.build();
    }

    private SplitSelfConfig() {}

    static boolean eventsEnabled() { return EVENTS_ENABLED.get(); }
    static int tickInterval() { return TICK_INTERVAL.get(); }
    static double eventChance() { return EVENT_CHANCE.get(); }
    static int eventCooldown() { return EVENT_COOLDOWN.get(); }
    static int startAfter() { return START_AFTER.get(); }
    static int guaranteedEvent() { return GUARANTEED_EVENT.get(); }
    static int repeatAfter() { return REPEAT_AFTER.get(); }
    static int baseSafeRadius() { return BASE_SAFE_RADIUS.get(); }
    static int weight(String id) { return WEIGHTS.getOrDefault(id, null) == null ? 0 : WEIGHTS.get(id).get(); }
    static int stage(String id) { return STAGES.getOrDefault(id, null) == null ? Integer.MAX_VALUE : STAGES.get(id).get(); }

    private static JsonObject defaults() {
        try (InputStream stream = SplitSelfConfig.class.getResourceAsStream("/data/splitself/event_defaults.json")) {
            if (stream == null) throw new IllegalStateException("Missing event_defaults.json");
            return JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }
}
