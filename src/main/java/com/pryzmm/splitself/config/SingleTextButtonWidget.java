package com.pryzmm.splitself.config;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class SingleTextButtonWidget extends ButtonWidget {

    public SingleTextButtonWidget(int x, int y, int width, int height, Text text, String translation, PressAction onPress) {
        super(x, y, width, height, text, onPress, DEFAULT_NARRATION_SUPPLIER);
        if (translation != null) {
            this.setTooltip(Tooltip.of(Text.of(text.getString() + "\n" + translation)));
        }
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        context.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer, this.getMessage(), this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, 0xFFFFFF);
        if (this.isHovered()) {
            context.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, 0x80FFFFFF);
        } else {
            context.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, 0x40000000);
        }
        context.drawBorder(this.getX(), this.getY(), this.width, this.height, this.isFocused() ? 0xFFFFFFFF : 0xFF808080);
    }
}
