package com.pryzmm.splitself.config;

import com.pryzmm.splitself.SplitSelf;
import com.pryzmm.splitself.config.CustomConfigScreen.InputType;
import com.pryzmm.splitself.file.JsonReader;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.*;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import static com.pryzmm.splitself.config.CustomConfigScreen.*;

public class ScrollingConfigScreen extends Screen {

    public static final Identifier CONFIG_IMAGE = Identifier.of(SplitSelf.MOD_ID, "textures/gui/title/config_title.png");
    private static JsonReader jsonReader;
    private final Screen parent;
    private ElementList elementList;

    public ScrollingConfigScreen(Screen parent) {
        super(Text.literal(""));

        this.parent = parent;

        if (jsonReader == null) {
            jsonReader = new JsonReader("splitself.json5");
        }
    }

    @Override
    protected void init() {
        createDoneButton();
        createElementListWidget();
        populateConfigOptions(arrayID, CustomConfigScreen.ScrollInputType);
    }

    public void createElementListWidget() {
        this.elementList = this.addDrawableChild(new ElementList(
                client, 0, 60, this.width, this.height - 110
        ));
        this.elementList.getRowWidth();
    }

    private void populateConfigOptions(String arrayID, InputType inputType) {
        for (String configKey : JsonReader.getKeysFromObject(arrayID)) {
            elementList.addEntry(new ElementList.DoubleButtonEntry(
                    configKey,
                    arrayID,
                    inputType,
                    button -> {
                        if (inputType == InputType.INT || inputType == InputType.DOUBLE) {
                            createNumericValueWidget(5, this.height - 25, ScrollMinimum, ScrollMaximum, configKey, inputType);
                        } else if (inputType == InputType.BOOLEAN) {
                            boolean newValue = !JsonReader.getBooleanFromArray(arrayID, configKey);
                            jsonReader.setBooleanInObject(arrayID, configKey, newValue);
                            jsonReader.saveConfig();
                        }
                    }
            ));
        }
    }

    public void createDoneButton() {
        this.addDrawableChild(ButtonWidget.builder(
                        Text.translatable("gui.done"), button -> {
                            assert this.client != null;
                            CustomConfigScreen.applyConfig();
                            this.client.setScreen(parent);
                        }
                ).position(this.width - 155, this.height - 25)
                .size(150, 20)
                .build());
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
        CustomConfigScreen.applyConfig();
        this.client.setScreen(new CustomConfigScreen(CustomConfigScreen.Parent));
        return false;
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
                button -> submitPrompt(textFieldWidget, minimum, maximum, inputType, configKey)
        ));
    }

    private void submitPrompt(TextFieldWidget textFieldWidget, double minimum, double maximum, InputType inputType, String configValue) {
        try {
            if (textFieldWidget.getText().isEmpty()) {
                throw new NumberFormatException("Input value between " + minimum + " and " + maximum + "!");
            } else {
                if (inputType == InputType.INT) {
                    int newValue = Integer.parseInt(textFieldWidget.getText());
                    if (newValue >= minimum && newValue <= maximum) {
                        textFieldHeaderWidget.setTextColor(0xFFFFFF);
                        jsonReader.setIntInObject(arrayID, configValue, newValue);
                        jsonReader.saveConfig();
                    } else {
                        throw new NumberFormatException("Invalid value! (Not Within Bounds!)");
                    }
                } else if (inputType == InputType.DOUBLE) {
                    double newValue = Double.parseDouble(textFieldWidget.getText());
                    if (newValue >= minimum && newValue <= maximum) {
                        textFieldHeaderWidget.setTextColor(0xFFFFFF);
                        jsonReader.setDoubleInObject(arrayID, configValue, newValue);
                        jsonReader.saveConfig();
                    } else {
                        throw new NumberFormatException("Invalid value! (Not Within Bounds!)");
                    }
                }
            }
        } catch (NumberFormatException e) {
            textFieldHeaderWidget.setTextColor(0xFF0000);
        }
    }
}