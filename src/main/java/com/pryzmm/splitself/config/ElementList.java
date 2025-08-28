package com.pryzmm.splitself.config;

import com.pryzmm.splitself.file.JsonReader;
import com.pryzmm.splitself.config.CustomConfigScreen.InputType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.text.Text;

import java.util.List;

public class ElementList extends ElementListWidget<ElementList.Entry> {

    public ElementList(MinecraftClient client, int x, int y, int width, int height) {
        super(client, width, height, y, 25);
    }

    @Override
    public int addEntry(Entry entry) {
        return super.addEntry(entry);
    }

    @Override
    public int getRowWidth() {
        return this.width / 2;
    }

    public abstract static class Entry extends ElementListWidget.Entry<Entry> {
    }

    public static class DoubleButtonEntry extends Entry {

        private final DoubleTextButtonWidget button;
        private final String configKey;
        private final String arrayID;
        private final InputType inputType;

        public DoubleButtonEntry(String configKey, String arrayID, InputType inputType, ButtonWidget.PressAction onPress) {
            this.configKey = configKey;
            this.arrayID = arrayID;
            this.inputType = inputType;

            this.button = new DoubleTextButtonWidget(
                    0, 0, 200, 20,
                    Text.literal(configKey),
                    this::getDisplayValue,
                    this::getDisplayColor,
                    null,
                    onPress
            );
        }

        private String getDisplayValue() {
            if (inputType == InputType.BOOLEAN) {
                // Use the correct method for reading boolean from array
                boolean value = JsonReader.getBooleanFromArray(arrayID, configKey);
                return value ? "True" : "False";
            } else {
                return String.valueOf(JsonReader.getValueFromArray(arrayID, configKey));
            }
        }

        private int getDisplayColor() {
            if (inputType == InputType.BOOLEAN) {
                // Use the correct method for reading boolean from array
                boolean value = JsonReader.getBooleanFromArray(arrayID, configKey);
                return value ? 0x00FF00 : 0xFF0000; // Green for true, red for false
            } else {
                return 0xFFFF00; // Yellow for other types
            }
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight,
                           int mouseX, int mouseY, boolean hovered, float tickDelta) {
            this.button.setPosition(x + (entryWidth - this.button.getWidth()) / 2, y + 2);
            this.button.render(context, mouseX, mouseY, tickDelta);
        }

        @Override
        public List<? extends Element> children() {
            return List.of(this.button);
        }

        @Override
        public List<? extends Selectable> selectableChildren() {
            return List.of(this.button);
        }
    }
}