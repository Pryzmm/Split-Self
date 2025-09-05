package com.pryzmm.splitself.config;

import com.pryzmm.splitself.SplitSelf;
import com.pryzmm.splitself.events.EventManager;
import com.pryzmm.splitself.events.MicrophoneReader;
import com.pryzmm.splitself.file.JsonReader;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.awt.*;

public class CustomConfigScreen extends Screen {

    public static final Identifier CONFIG_IMAGE = Identifier.of(SplitSelf.MOD_ID, "textures/gui/title/config_title.png");
    private static JsonReader configReader;
    public static Screen Parent;

    // for scrolling menus
    public static String arrayID;
    public static InputType ScrollInputType;
    public static double ScrollMinimum;
    public static double ScrollMaximum;

    public enum InputType {
        INT, DOUBLE, BOOLEAN
    }

    public CustomConfigScreen(Screen parent) {
        super(Text.literal(""));

        Parent = parent;

        if (configReader == null) {
            configReader = new JsonReader("splitself.json5");
        }

    }

    @Override
    protected void init() {
        createDoneButton();
        createConfigButtons();
    }

    public void createDoneButton() {
        this.addDrawableChild(ButtonWidget.builder(
            Text.translatable("gui.done"), button -> {
                assert this.client != null;
                applyConfig();
                this.client.setScreen(Parent);
            }
        ).position(this.width - 155, this.height - 25)
        .size(150, 20)
        .build());
    }

    public static void applyConfig() {
        configReader.saveConfig();
        EventManager.EVENTS_ENABLED = JsonReader.getBoolean("eventsEnabled", DefaultConfig.eventsEnabled);
        EventManager.TICK_INTERVAL = JsonReader.getInt("eventTickInterval", DefaultConfig.eventTickInterval);
        EventManager.EVENT_CHANCE = JsonReader.getDouble("eventChance", DefaultConfig.eventChance);
        EventManager.START_AFTER = JsonReader.getDouble("startEventsAfter", DefaultConfig.startEventsAfter);
        EventManager.GUARANTEED_EVENT = JsonReader.getDouble("guaranteedEvent", DefaultConfig.guaranteedEvent);
    }

    public void createConfigButtons() {
        if (MicrophoneReader.ShriekInstalled) {
            createBooleanConfigButton(this.width / 2 - 153, 65, "eventsEnabled", DefaultConfig.eventsEnabled, "config.splitself.events_enabled");
            createVoskConfigButton(this.width / 2 + 3, 65, "https://alphacephei.com/vosk/models", "config.splitself.vosk_model");
        } else {
            createBooleanConfigButton(this.width / 2 - 75, 65, "eventsEnabled", DefaultConfig.eventsEnabled, "config.splitself.events_enabled");
        }
        createIntConfigButton(this.width / 2 - 230, 105, "eventTickInterval", DefaultConfig.eventTickInterval, 1, 1000, "config.splitself.event_tick_interval");
        createDoubleConfigButton(this.width / 2 - 75, 105, "eventChance", DefaultConfig.eventChance, 0.01, 1.00, "config.splitself.event_chance");
        createIntConfigButton(this.width / 2 + 80, 105, "eventCooldown", DefaultConfig.eventCooldown, 100, 10000, "config.splitself.event_cooldown");
        createIntConfigButton(this.width / 2 - 230, 130, "guaranteedEvent", DefaultConfig.guaranteedEvent, 0, 24000, "config.splitself.guaranteed_event");
        createIntConfigButton(this.width / 2 - 75, 130, "startEventsAfter", DefaultConfig.startEventsAfter, 50, 20000, "config.splitself.start_events_after");
        createIntConfigButton(this.width / 2 + 80, 130, "repeatEventsAfter", DefaultConfig.repeatEventsAfter, 0, 10, "config.splitself.event_until_repeat");
        createMenuConfigButton(this.width / 2 - 230, 180, "config.splitself.group.event_weights", 0, 200, "eventWeights", InputType.INT);
        createMenuConfigButton(this.width / 2 - 75, 180, "config.splitself.group.event_stages", 0, 3, "eventStages", InputType.INT);
        createMenuConfigButton(this.width / 2 + 80, 180, "config.splitself.group.one_time_events", 0, 0, "oneTimeEvents", InputType.BOOLEAN);
    }

    public void createIntConfigButton(int x, int y, String configKey, int defaultValue, int minimum, int maximum, String translationKey) {
        this.addDrawableChild(new DoubleTextButtonWidget(
                x, y, 150, 20,
                SplitSelf.translate(translationKey),
                () -> String.valueOf(JsonReader.getInt(configKey, defaultValue)),
                () -> 0xFFFF00,
                translationKey + ".description",
                button -> {
                    createNumericValueWidget(5, this.height - 25, minimum, maximum, configKey, InputType.INT);
                }
        ));
    }

    public void createDoubleConfigButton(int x, int y, String configKey, double defaultValue, double minimum, double maximum, String translationKey) {
        this.addDrawableChild(new DoubleTextButtonWidget(
                x, y, 150, 20,
                SplitSelf.translate(translationKey),
                () -> String.valueOf(JsonReader.getDouble(configKey, defaultValue)),
                () -> 0xFFFF00,
                translationKey + ".description",
                button -> {
                    createNumericValueWidget(5, this.height - 25, minimum, maximum, configKey, InputType.DOUBLE);
                }
        ));
    }

    public void createBooleanConfigButton(int x, int y, String configKey, boolean defaultValue, String translationKey) {
        this.addDrawableChild(new DoubleTextButtonWidget(
                x, y, 150, 20,
                SplitSelf.translate(translationKey),
                () -> JsonReader.getBoolean(configKey, defaultValue) ? "True" : "False",
                () -> JsonReader.getBoolean(configKey,defaultValue) ? 0x00FF00 : 0xFF0000,
                translationKey + ".description",
                button -> {
                    boolean newValue = !JsonReader.getBoolean(configKey, defaultValue);
                    configReader.setBoolean(configKey, newValue);
                    configReader.saveConfig();
                }
        ));
    }

    public void createVoskConfigButton(int x, int y, String link, String translationKey) {
        this.addDrawableChild(new SingleTextButtonWidget(
                x, y, 150, 20,
                SplitSelf.translate(translationKey),
                SplitSelf.translate(translationKey + ".description", JsonReader.getString("voskModel")).getString(),
                button -> {
                    Util.getOperatingSystem().open(link);
                    createVoskValueWidget(5, this.height - 25);
                }
        ));
    }

    public void createMenuConfigButton(int x, int y, String translationKey, double minimum, double maximum, String menuID, InputType inputType) {
        this.addDrawableChild(new SingleTextButtonWidget(
                x, y, 150, 20,
                SplitSelf.translate(translationKey),
                SplitSelf.translate(translationKey + ".description").getString(),
                button -> {
                    assert client != null;
                    arrayID = menuID;
                    ScrollInputType = inputType;
                    ScrollMinimum = minimum;
                    ScrollMaximum = maximum;
                    client.setScreen(new ScrollingConfigScreen(this));
                }
        ));
    }

    TextFieldWidget textFieldWidget;
    TextWidget textFieldHeaderWidget;
    ButtonWidget submitButtonWidget;
    public void createNumericValueWidget(int x, int y, double minimum, double maximum, String configKey, InputType inputType) {
        if (textFieldWidget != null) {
            this.remove(textFieldWidget);
            this.remove(textFieldHeaderWidget);
            this.remove(submitButtonWidget);
        }
        textFieldWidget = this.addDrawableChild(new TextFieldWidget(
                this.textRenderer,
                x, y, 100, 20,
                Text.empty() // nothing renders here for some reason :(
        ));
        if (inputType == InputType.INT) {
            int textWidth = textRenderer.getWidth(SplitSelf.translate("config.splitself.numeric_value", (int) minimum, (int) maximum));
            textFieldHeaderWidget = this.addDrawableChild(new TextWidget(
                    x, y - 15, textWidth, 20,
                    SplitSelf.translate("config.splitself.numeric_value", (int) minimum, (int) maximum),
                    this.textRenderer
            ));
        } else if (inputType == InputType.DOUBLE) {
            int textWidth = textRenderer.getWidth(SplitSelf.translate("config.splitself.numeric_value", minimum, maximum));
            textFieldHeaderWidget = this.addDrawableChild(new TextWidget(
                    x, y - 15, textWidth, 20,
                    SplitSelf.translate("config.splitself.numeric_value", minimum, maximum),
                    this.textRenderer
            ));
        }
        submitButtonWidget = this.addDrawableChild(new SingleTextButtonWidget(
                x + 100, y, 50, 20,
                Text.literal("Submit"),
                null,
                button -> {
                    submitNumericPrompt(textFieldWidget, minimum, maximum, inputType, configKey);
                }
        ));
    }

    public void createVoskValueWidget(int x, int y) {
        if (textFieldWidget != null) {
            this.remove(textFieldWidget);
            this.remove(textFieldHeaderWidget);
            this.remove(submitButtonWidget);
        }
        textFieldWidget = this.addDrawableChild(new TextFieldWidget(
                this.textRenderer,
                x, y, 100, 20,
                Text.empty() // nothing renders here for some reason :(
        ));
        int textWidth = textRenderer.getWidth(SplitSelf.translate("config.splitself.string_value"));
        textFieldHeaderWidget = this.addDrawableChild(new TextWidget(
                x, y - 15, textWidth, 20,
                SplitSelf.translate("config.splitself.string_value"),
                this.textRenderer
        ));
        submitButtonWidget = this.addDrawableChild(new SingleTextButtonWidget(
                x + 100, y, 50, 20,
                Text.literal("Submit"),
                null,
                button -> {
                    submitVoskPrompt(textFieldWidget);
                }
        ));
    }

    private void submitNumericPrompt(TextFieldWidget textFieldWidget, double minimum, double maximum, InputType inputType, String configValue) {
        try {
            if (textFieldWidget.getText().isEmpty()) {
                throw new NumberFormatException("Input value between " + minimum + " and " + maximum + "!");
            } else {
                if (inputType == InputType.INT) {
                    int newValue = Integer.parseInt(textFieldWidget.getText());
                    if (newValue >= minimum && newValue <= maximum) {
                        textFieldHeaderWidget.setTextColor(0xFFFFFF);
                        configReader.setInt(configValue, newValue);
                        configReader.saveConfig();
                    } else {
                        throw new NumberFormatException("Invalid value! (Not Within Bounds!)");
                    }
                } else if (inputType == InputType.DOUBLE) {
                    double newValue = Double.parseDouble(textFieldWidget.getText());
                    if (newValue >= minimum && newValue <= maximum) {
                        textFieldHeaderWidget.setTextColor(0xFFFFFF);
                        configReader.setDouble(configValue, newValue);
                        configReader.saveConfig();
                    } else {
                        throw new NumberFormatException("Invalid value! (Not Within Bounds!)");
                    }
                }
            }
        } catch (NumberFormatException e) {
            textFieldHeaderWidget.setTextColor(0xFF0000);
        }
    }

    private void submitVoskPrompt(TextFieldWidget textFieldWidget) {
        try {
            if (textFieldWidget.getText().isEmpty()) {
                throw new NumberFormatException("Input a value!");
            } else {
                textFieldHeaderWidget.setTextColor(0xFFFFFF);
                configReader.setString("voskModel", textFieldWidget.getText());
                configReader.saveConfig();
            }
        } catch (NumberFormatException e) {
            textFieldHeaderWidget.setTextColor(0xFF0000);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        assert client != null;
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        context.drawTexture(CONFIG_IMAGE, this.width / 2 - 64, -35, 0, 0, 128, 128, 128, 128);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        assert this.client != null;
        applyConfig();
        this.client.setScreen(Parent);
        return false;
    }
}