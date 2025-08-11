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

public class SplitSelfYACLConfig {
    public static final ConfigClassHandler<SplitSelfYACLConfig> HANDLER = ConfigClassHandler.<SplitSelfYACLConfig>createBuilder(SplitSelfYACLConfig.class)
            .id(Identifier.of(SplitSelf.MOD_ID))
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(FabricLoader.getInstance().getConfigDir().resolve("splitself.json5"))
                    .setJson5(true)
                    .build())
            .build();

    @SerialEntry
    public boolean eventsEnabled = true;

    @SerialEntry
    public int eventTickInterval = 20;

    @SerialEntry
    public double eventChance = 0.003;

    @SerialEntry
    public int eventCooldown = 600;

    @SerialEntry
    public int startEventsAfter = 3000;

    @SerialEntry
    public int guaranteedEvent = 15600;

    public static Screen createScreen(Screen parent) {
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
                                        .binding(70, () -> HANDLER.instance().eventTickInterval, newVal -> HANDLER.instance().eventTickInterval = newVal)
                                        .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(1, 1000).step(1))
                                        .build())
                                .option(Option.<Double>createBuilder()
                                        .name(Text.translatable("config.splitself.event_chance"))
                                        .description(OptionDescription.of(Text.translatable("config.splitself.event_chance.description")))
                                        .binding(0.03, () -> HANDLER.instance().eventChance, newVal -> HANDLER.instance().eventChance = newVal)
                                        .controller(opt -> DoubleSliderControllerBuilder.create(opt).range(0.01, 1.00).step(0.01))
                                        .build())
                                .option(Option.<Integer>createBuilder()
                                        .name(Text.translatable("config.splitself.event_cooldown"))
                                        .description(OptionDescription.of(Text.translatable("config.splitself.event_cooldown.description")))
                                        .binding(600, () -> HANDLER.instance().eventCooldown, newVal -> HANDLER.instance().eventCooldown = newVal)
                                        .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(0, 10000).step(50))
                                        .build())
                                .option(Option.<Integer>createBuilder()
                                        .name(Text.translatable("config.splitself.guaranteed_event"))
                                        .description(OptionDescription.of(Text.translatable("config.splitself.guaranteed_event.description")))
                                        .binding(15600, () -> HANDLER.instance().guaranteedEvent, newVal -> HANDLER.instance().guaranteedEvent = newVal)
                                        .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(0, 24000).step(100))
                                        .build())
                                .option(Option.<Integer>createBuilder()
                                        .name(Text.translatable("config.splitself.start_events_after"))
                                        .description(OptionDescription.of(Text.translatable("config.splitself.start_events_after.description")))
                                        .binding(3000, () -> HANDLER.instance().startEventsAfter, newVal -> HANDLER.instance().startEventsAfter = newVal)
                                        .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(50, 20000).step(50))
                                        .build())
                                .build())
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

    public void setEventsEnabled(boolean eventsEnabled) { this.eventsEnabled = eventsEnabled; }
    public void setEventTickInterval(int eventTickInterval) { this.eventTickInterval = eventTickInterval; }
    public void setEventChance(double eventChance) { this.eventChance = eventChance; }
    public void setEventCooldown(int eventCooldown) { this.eventCooldown = eventCooldown; }
    public void setGuaranteedEvent(int guaranteedEvent) { this.guaranteedEvent = guaranteedEvent; }
    public void setStartEventsAfter(int startEventsAfter) { this.startEventsAfter = startEventsAfter; }
}