package com.pryzmm.splitself.forge;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Data extracted from the pinned Fabric DefaultConfig, without event side effects. */
final class EventCatalog {
    record Option(int weight, int stage, boolean oneTime, boolean client) {}

    private static final String RESOURCE = "/data/splitself/event_defaults.json";
    private static final JsonObject ROOT = load();
    private static final Map<String, Option> OPTIONS = readOptions(ROOT.getAsJsonObject("events"));

    private EventCatalog() {}

    static int startAfter() { return SplitSelfConfig.startAfter(); }
    static int tickInterval() { return SplitSelfConfig.tickInterval(); }
    static double chance() { return SplitSelfConfig.eventChance(); }
    static int cooldown() { return SplitSelfConfig.eventCooldown(); }
    static int guaranteedAfter() { return SplitSelfConfig.guaranteedEvent(); }
    static int repeatAfter() { return SplitSelfConfig.repeatAfter(); }
    static int baseSafeRadius() { return SplitSelfConfig.baseSafeRadius(); }
    static boolean enabled() { return SplitSelfConfig.eventsEnabled(); }
    static Map<String, Option> options() { return OPTIONS; }

    static List<String> eligible(int sleepStage, Set<String> consumedOneTime,
            Map<String, Integer> lastTriggered, int totalTriggered) {
        List<String> candidates = new ArrayList<>();
        for (var entry : OPTIONS.entrySet()) {
            String name = entry.getKey();
            // Only events with Forge handlers may enter the automatic pool.
            if (!PortedEvents.supported().contains(name)) continue;
            Option option = entry.getValue();
            Integer last = lastTriggered.get(name);
            if (SplitSelfConfig.weight(name) > 0 && SplitSelfConfig.stage(name) <= sleepStage
                    && !consumedOneTime.contains(name)
                    && (last == null || totalTriggered - last >= repeatAfter())) {
                candidates.add(name);
            }
        }
        return candidates;
    }

    private static JsonObject load() {
        try (InputStream stream = EventCatalog.class.getResourceAsStream(RESOURCE)) {
            if (stream == null) throw new IllegalStateException("Missing " + RESOURCE);
            return JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (Exception e) {
            throw new IllegalStateException("Cannot load Split Self event defaults", e);
        }
    }

    private static Map<String, Option> readOptions(JsonObject events) {
        Map<String, Option> result = new LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> entry : events.entrySet()) {
            JsonObject value = entry.getValue().getAsJsonObject();
            result.put(entry.getKey(), new Option(value.get("weight").getAsInt(),
                    value.get("stage").getAsInt(), value.get("oneTime").getAsBoolean(),
                    value.get("type").getAsString().equals("CLIENT")));
        }
        if (result.size() != 67) throw new IllegalStateException("Expected 67 source event options");
        return Collections.unmodifiableMap(result);
    }
}
