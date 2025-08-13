package com.pryzmm.splitself.config;

import com.pryzmm.splitself.SplitSelf;
import com.pryzmm.splitself.events.EventManager;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.DoubleSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Map;

public class SplitSelfYACLConfig {
    public static final ConfigClassHandler<SplitSelfYACLConfig> HANDLER = ConfigClassHandler.<SplitSelfYACLConfig>createBuilder(SplitSelfYACLConfig.class)
            .id(Identifier.of(SplitSelf.MOD_ID))
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(FabricLoader.getInstance().getConfigDir().resolve("splitself.json5"))
                    .setJson5(true)
                    .build())
            .build();

    @SerialEntry
    public boolean eventsEnabled = ConfigDefaults.DEFAULT_EVENTS_ENABLED;

    @SerialEntry
    public int eventTickInterval = ConfigDefaults.DEFAULT_EVENT_TICK_INTERVAL;

    @SerialEntry
    public double eventChance = ConfigDefaults.DEFAULT_EVENT_CHANCE;

    @SerialEntry
    public int eventCooldown = ConfigDefaults.DEFAULT_EVENT_COOLDOWN;

    @SerialEntry
    public int startEventsAfter = ConfigDefaults.DEFAULT_START_EVENTS_AFTER;

    @SerialEntry
    public int guaranteedEvent = ConfigDefaults.DEFAULT_GUARANTEED_EVENT;

    @SerialEntry
    public Map<String, Integer> eventWeights = ConfigDefaults.getDefaultEventWeights();

    public static Screen createScreen(Screen parent) {
        // Create event weights group
        OptionGroup.Builder eventWeightsGroup = OptionGroup.createBuilder()
                .name(Text.translatable("config.splitself.group.event_weights"))
                .description(OptionDescription.of(Text.translatable("config.splitself.group.event_weights.description")));

        // Add options for each event weight
        for (EventManager.Events event : EventManager.Events.values()) {
            String eventName = event.name();
            int defaultWeight = ConfigDefaults.getDefaultEventWeight(eventName);

            eventWeightsGroup.option(Option.<Integer>createBuilder()
                    .name(Text.literal(formatEventName(eventName)))
                    .description(OptionDescription.of(Text.literal("Weight for " + formatEventName(eventName) + " event")))
                    .binding(defaultWeight,
                            () -> HANDLER.instance().eventWeights.getOrDefault(eventName, defaultWeight),
                            newVal -> HANDLER.instance().eventWeights.put(eventName, newVal))
                    .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(0, 200).step(1))
                    .build());
        }

        return YetAnotherConfigLib.createBuilder()
                .title(Text.translatable("config.splitself.title"))
                .category(ConfigCategory.createBuilder()
                        .name(Text.translatable("config.splitself.category.events"))
                        .tooltip(Text.translatable("config.splitself.category.events.tooltip"))
                        .group(OptionGroup.createBuilder()
                                .name(Text.translatable("config.splitself.group.general"))
                                .description(OptionDescription.of(Text.translatable("config.splitself.group.general.description")))
                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.translatable("config.splitself.events_enabled"))
                                        .description(OptionDescription.of(Text.translatable("config.splitself.events_enabled.description")))
                                        .binding(true, () -> HANDLER.instance().eventsEnabled, newVal -> HANDLER.instance().eventsEnabled = newVal)
                                        .controller(opt -> BooleanControllerBuilder.create(opt).yesNoFormatter())
                                        .build())
                                .option(Option.<Integer>createBuilder()
                                        .name(Text.translatable("config.splitself.event_tick_interval"))
                                        .description(OptionDescription.of(Text.translatable("config.splitself.event_tick_interval.description")))
                                        .binding(ConfigDefaults.DEFAULT_EVENT_TICK_INTERVAL, () -> HANDLER.instance().eventTickInterval, newVal -> HANDLER.instance().eventTickInterval = newVal)
                                        .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(1, 1000).step(1))
                                        .build())
                                .option(Option.<Double>createBuilder()
                                        .name(Text.translatable("config.splitself.event_chance"))
                                        .description(OptionDescription.of(Text.translatable("config.splitself.event_chance.description")))
                                        .binding(ConfigDefaults.DEFAULT_EVENT_CHANCE, () -> HANDLER.instance().eventChance, newVal -> HANDLER.instance().eventChance = newVal)
                                        .controller(opt -> DoubleSliderControllerBuilder.create(opt).range(0.01, 1.00).step(0.01))
                                        .build())
                                .option(Option.<Integer>createBuilder()
                                        .name(Text.translatable("config.splitself.event_cooldown"))
                                        .description(OptionDescription.of(Text.translatable("config.splitself.event_cooldown.description")))
                                        .binding(ConfigDefaults.DEFAULT_EVENT_COOLDOWN, () -> HANDLER.instance().eventCooldown, newVal -> HANDLER.instance().eventCooldown = newVal)
                                        .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(0, 10000).step(50))
                                        .build())
                                .option(Option.<Integer>createBuilder()
                                        .name(Text.translatable("config.splitself.guaranteed_event"))
                                        .description(OptionDescription.of(Text.translatable("config.splitself.guaranteed_event.description")))
                                        .binding(ConfigDefaults.DEFAULT_GUARANTEED_EVENT, () -> HANDLER.instance().guaranteedEvent, newVal -> HANDLER.instance().guaranteedEvent = newVal)
                                        .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(0, 24000).step(100))
                                        .build())
                                .option(Option.<Integer>createBuilder()
                                        .name(Text.translatable("config.splitself.start_events_after"))
                                        .description(OptionDescription.of(Text.translatable("config.splitself.start_events_after.description")))
                                        .binding(ConfigDefaults.DEFAULT_START_EVENTS_AFTER, () -> HANDLER.instance().startEventsAfter, newVal -> HANDLER.instance().startEventsAfter = newVal)
                                        .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(50, 20000).step(50))
                                        .build())
                                .build())
                        .group(eventWeightsGroup.build())
                        .build())
                .save(() -> {
                    HANDLER.save();
                    try {
                        Class<?> mainConfigClass = Class.forName("com.pryzmm.splitself.config.SplitSelfConfig");
                        mainConfigClass.getMethod("onYACLSave").invoke(null);
                    } catch (Exception ignored) {}
                })
                .build()
                .generateScreen(parent);
    }

    private static String formatEventName(String eventName) {
        // Convert ENUM_CASE to Title Case
        StringBuilder formatted = new StringBuilder();
        String[] words = eventName.toLowerCase().split("_");
        for (String word : words) {
            if (formatted.length() > 0) {
                formatted.append(" ");
            }
            formatted.append(word.substring(0, 1).toUpperCase()).append(word.substring(1));
        }
        return formatted.toString();
    }

    public static void load() {
        HANDLER.load();
    }

    public void save() {
        HANDLER.save();
    }

    public boolean getEventsEnabled() { return eventsEnabled; }
    public int getEventTickInterval() { return eventTickInterval; }
    public double getEventChance() { return eventChance; }
    public int getEventCooldown() { return eventCooldown; }
    public int getGuaranteedEvent() { return guaranteedEvent; }
    public int getStartEventsAfter() { return startEventsAfter; }
    public Map<String, Integer> getEventWeights() { return eventWeights; }

    public void setEventsEnabled(boolean eventsEnabled) { this.eventsEnabled = eventsEnabled; }
    public void setEventTickInterval(int eventTickInterval) { this.eventTickInterval = eventTickInterval; }
    public void setEventChance(double eventChance) { this.eventChance = eventChance; }
    public void setEventCooldown(int eventCooldown) { this.eventCooldown = eventCooldown; }
    public void setGuaranteedEvent(int guaranteedEvent) { this.guaranteedEvent = guaranteedEvent; }
    public void setStartEventsAfter(int startEventsAfter) { this.startEventsAfter = startEventsAfter; }
    public void setEventWeights(Map<String, Integer> eventWeights) { this.eventWeights = eventWeights; }
}