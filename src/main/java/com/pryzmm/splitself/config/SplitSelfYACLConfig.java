package com.pryzmm.splitself.config;

import com.pryzmm.splitself.SplitSelf;
import com.pryzmm.splitself.events.EventManager;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.DoubleSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
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

    @SerialEntry
    public Map<String, Integer> eventStages = ConfigDefaults.getDefaultEventStages();

    @SerialEntry
    public Map<String, Boolean> oneTimeEvents = ConfigDefaults.getDefaultOneTimeEvents();

    public static Screen createScreen(Screen parent) {
        // Create event weights group
        OptionGroup.Builder eventWeightsGroup = OptionGroup.createBuilder()
                .name(SplitSelf.translate("config.splitself.group.event_weights"))
                .description(OptionDescription.of(SplitSelf.translate("config.splitself.group.event_weights.description")));

        // Create event stages group
        OptionGroup.Builder eventStagesGroup = OptionGroup.createBuilder()
                .name(SplitSelf.translate("config.splitself.group.event_stages"))
                .description(OptionDescription.of(SplitSelf.translate("config.splitself.group.event_stages.description")));

        // Create one time events group
        OptionGroup.Builder oneTimeEventsGroup = OptionGroup.createBuilder()
                .name(SplitSelf.translate("config.splitself.group.one_time_events"))
                .description(OptionDescription.of(SplitSelf.translate("config.splitself.group.one_time_events.description")));

        // Add options for each event weight and stage
        for (EventManager.Events event : EventManager.Events.values()) {
            String eventName = event.name();
            String formattedName = formatEventName(eventName);
            int defaultWeight = ConfigDefaults.getDefaultEventWeight(eventName);
            int defaultStage = ConfigDefaults.getDefaultEventStage(eventName);
            boolean defaultSOneTimeEvent = ConfigDefaults.getDefaultOneTimeEvents(eventName);

            // Add weight option
            eventWeightsGroup.option(Option.<Integer>createBuilder()
                    .name(Text.literal(formattedName))
                    .description(OptionDescription.of(SplitSelf.translate("config.splitself.weight_value_title", formattedName)))
                    .binding(defaultWeight,
                            () -> HANDLER.instance().eventWeights.getOrDefault(eventName, defaultWeight),
                            newVal -> HANDLER.instance().eventWeights.put(eventName, newVal))
                    .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(0, 200).step(1))
                    .build());

            // Add stage option
            eventStagesGroup.option(Option.<Integer>createBuilder()
                    .name(Text.literal(formattedName))
                    .description(OptionDescription.of(SplitSelf.translate("config.splitself.stage_value_title", formattedName)))
                    .binding(defaultStage,
                            () -> HANDLER.instance().eventStages.getOrDefault(eventName, defaultStage),
                            newVal -> HANDLER.instance().eventStages.put(eventName, newVal))
                    .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(0, 3).step(1))
                    .build());

            // Add one time event option
            oneTimeEventsGroup.option(Option.<Boolean>createBuilder()
                    .name(Text.literal(formattedName))
                    .description(OptionDescription.of(SplitSelf.translate("config.splitself.one_time_event_value_title", formattedName)))
                    .binding(defaultSOneTimeEvent,
                            () -> HANDLER.instance().oneTimeEvents.getOrDefault(eventName, defaultSOneTimeEvent),
                            newVal -> HANDLER.instance().oneTimeEvents.put(eventName, newVal))
                    .controller(TickBoxControllerBuilder::create)
                    .build());
        }

        return YetAnotherConfigLib.createBuilder()
                .title(SplitSelf.translate("config.splitself.title"))
                .category(ConfigCategory.createBuilder()
                        .name(SplitSelf.translate("config.splitself.category.events"))
                        .tooltip(SplitSelf.translate("config.splitself.category.events.tooltip"))
                        .group(OptionGroup.createBuilder()
                                .name(Text.translatable("config.splitself.group.general"))
                                .description(OptionDescription.of(SplitSelf.translate("config.splitself.group.general.description")))
                                .option(Option.<Boolean>createBuilder()
                                        .name(SplitSelf.translate("config.splitself.events_enabled"))
                                        .description(OptionDescription.of(SplitSelf.translate("config.splitself.events_enabled.description")))
                                        .binding(true, () -> HANDLER.instance().eventsEnabled, newVal -> HANDLER.instance().eventsEnabled = newVal)
                                        .controller(opt -> BooleanControllerBuilder.create(opt).yesNoFormatter())
                                        .build())
                                .option(Option.<Integer>createBuilder()
                                        .name(SplitSelf.translate("config.splitself.event_tick_interval"))
                                        .description(OptionDescription.of(SplitSelf.translate("config.splitself.event_tick_interval.description")))
                                        .binding(ConfigDefaults.DEFAULT_EVENT_TICK_INTERVAL, () -> HANDLER.instance().eventTickInterval, newVal -> HANDLER.instance().eventTickInterval = newVal)
                                        .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(1, 1000).step(1))
                                        .build())
                                .option(Option.<Double>createBuilder()
                                        .name(SplitSelf.translate("config.splitself.event_chance"))
                                        .description(OptionDescription.of(SplitSelf.translate("config.splitself.event_chance.description")))
                                        .binding(ConfigDefaults.DEFAULT_EVENT_CHANCE, () -> HANDLER.instance().eventChance, newVal -> HANDLER.instance().eventChance = newVal)
                                        .controller(opt -> DoubleSliderControllerBuilder.create(opt).range(0.01, 1.00).step(0.01))
                                        .build())
                                .option(Option.<Integer>createBuilder()
                                        .name(SplitSelf.translate("config.splitself.event_cooldown"))
                                        .description(OptionDescription.of(SplitSelf.translate("config.splitself.event_cooldown.description")))
                                        .binding(ConfigDefaults.DEFAULT_EVENT_COOLDOWN, () -> HANDLER.instance().eventCooldown, newVal -> HANDLER.instance().eventCooldown = newVal)
                                        .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(100, 10000).step(50))
                                        .build())
                                .option(Option.<Integer>createBuilder()
                                        .name(SplitSelf.translate("config.splitself.guaranteed_event"))
                                        .description(OptionDescription.of(SplitSelf.translate("config.splitself.guaranteed_event.description")))
                                        .binding(ConfigDefaults.DEFAULT_GUARANTEED_EVENT, () -> HANDLER.instance().guaranteedEvent, newVal -> HANDLER.instance().guaranteedEvent = newVal)
                                        .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(0, 24000).step(100))
                                        .build())
                                .option(Option.<Integer>createBuilder()
                                        .name(SplitSelf.translate("config.splitself.start_events_after"))
                                        .description(OptionDescription.of(SplitSelf.translate("config.splitself.start_events_after.description")))
                                        .binding(ConfigDefaults.DEFAULT_START_EVENTS_AFTER, () -> HANDLER.instance().startEventsAfter, newVal -> HANDLER.instance().startEventsAfter = newVal)
                                        .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(50, 20000).step(50))
                                        .build())
                                .build())
                        .group(eventWeightsGroup.build())
                        .group(eventStagesGroup.build())
                        .group(oneTimeEventsGroup.build())
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
        StringBuilder formatted = new StringBuilder();
        String[] words = eventName.toLowerCase().split("_");
        for (String word : words) {
            if (!formatted.isEmpty()) {
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
}